package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.owasp.encoder.Encode;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver;
import org.pac4j.core.keystore.generation.KeystoreGenerator;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.pac4j.saml.exceptions.SAMLAuthnContextClassRefException;
import org.pac4j.saml.metadata.SAML2FileSystemMetadataGenerator;
import org.pac4j.saml.profile.SAML2Profile;
import org.pac4j.saml.sso.impl.SAML2AuthnResponseValidator;
import org.springframework.core.io.ByteArrayResource;

/**
 * SAML 2.0 single sign-on, backed by pac4j-saml. <br>
 * The service provider and identity provider are configured from onelogin.saml.properties on the
 * classpath, using the same keys the previous java-saml implementation read, so existing
 * deployments keep their configuration. <br>
 * This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 */
public class SamlSso {

  private static final Logger log = LogManager.getLogger(SamlSso.class);

  /** Session attribute holding the authenticated user's SAML profile, used for single logout */
  public static final String PROFILE_SESSION_ATTRIBUTE = "samlProfile";

  static final String PROPERTIES_FILE = "onelogin.saml.properties";

  static final String SP_ENTITY_ID = "onelogin.saml2.sp.entityid";
  static final String SP_ACS_URL = "onelogin.saml2.sp.assertion_consumer_service.url";
  static final String SP_ACS_BINDING = "onelogin.saml2.sp.assertion_consumer_service.binding";
  static final String SP_SLS_URL = "onelogin.saml2.sp.single_logout_service.url";
  static final String SP_NAMEID_FORMAT = "onelogin.saml2.sp.nameidformat";
  static final String SP_CERT = "onelogin.saml2.sp.x509cert";
  static final String SP_KEY = "onelogin.saml2.sp.privatekey";
  static final String IDP_ENTITY_ID = "onelogin.saml2.idp.entityid";
  static final String IDP_SSO_URL = "onelogin.saml2.idp.single_sign_on_service.url";
  static final String IDP_SSO_BINDING = "onelogin.saml2.idp.single_sign_on_service.binding";
  static final String IDP_SLO_URL = "onelogin.saml2.idp.single_logout_service.url";
  static final String IDP_SLO_RESPONSE_URL =
      "onelogin.saml2.idp.single_logout_service.response.url";
  static final String IDP_SLO_BINDING = "onelogin.saml2.idp.single_logout_service.binding";
  static final String IDP_CERT = "onelogin.saml2.idp.x509cert";
  static final String IDP_CERT_FINGERPRINT = "onelogin.saml2.idp.certfingerprint";
  static final String STRICT = "onelogin.saml2.strict";
  static final String AUTHN_REQUEST_SIGNED = "onelogin.saml2.security.authnrequest_signed";
  static final String LOGOUT_REQUEST_SIGNED = "onelogin.saml2.security.logoutrequest_signed";
  static final String WANT_MESSAGES_SIGNED = "onelogin.saml2.security.want_messages_signed";
  static final String WANT_ASSERTIONS_SIGNED = "onelogin.saml2.security.want_assertions_signed";
  static final String SIGN_METADATA = "onelogin.saml2.security.sign_metadata";
  static final String REQUESTED_AUTHN_CONTEXT = "onelogin.saml2.security.requested_authncontext";
  static final String REQUESTED_AUTHN_CONTEXT_COMPARISON =
      "onelogin.saml2.security.requested_authncontextcomparison";

  /** The key as misspelled in the pre-pac4j sample file, still accepted */
  static final String LEGACY_REQUESTED_AUTHN_CONTEXT_COMPARISON =
      "onelogin.saml2.security.onelogin.saml2.security.requested_authncontextcomparison";

  static final String SIGNATURE_ALGORITHM = "onelogin.saml2.security.signature_algorithm";

  private static final String HTTP_REDIRECT_BINDING =
      "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
  private static final String HTTP_POST_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
  private static final String SP_KEY_ALIAS = "shepherd-sp";

  /**
   * HTTP-Redirect binding signature algorithms accepted on logout messages (no SHA-1). XML-DSig
   * ECDSA signatures are the raw r || s concatenation (IEEE P1363), not ASN.1 DER.
   */
  private static final Map<String, String> REDIRECT_SIGNATURE_ALGORITHMS = new HashMap<>();

  static {
    REDIRECT_SIGNATURE_ALGORITHMS.put(
        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "SHA256withRSA");
    REDIRECT_SIGNATURE_ALGORITHMS.put(
        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", "SHA384withRSA");
    REDIRECT_SIGNATURE_ALGORITHMS.put(
        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "SHA512withRSA");
    REDIRECT_SIGNATURE_ALGORITHMS.put(
        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", "SHA256withECDSAinP1363Format");
    REDIRECT_SIGNATURE_ALGORITHMS.put(
        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", "SHA384withECDSAinP1363Format");
    REDIRECT_SIGNATURE_ALGORITHMS.put(
        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512", "SHA512withECDSAinP1363Format");
  }

  private static final Set<String> REDIRECT_SIGNED_PARAMETERS =
      Set.of("SAMLRequest", "SAMLResponse", "RelayState", "SigAlg", "Signature");

  /** How long after its IssueInstant an IdP LogoutRequest is accepted (plus clock skew) */
  static final Duration LOGOUT_REQUEST_LIFETIME = Duration.ofMinutes(5);

  /** Allowed clock difference between the IdP and this server */
  static final Duration CLOCK_SKEW = Duration.ofMinutes(5);

  /** Upper bound on remembered LogoutRequest IDs; beyond it new requests are refused */
  static int maxRememberedLogoutRequests = 10000;

  /**
   * IDs of LogoutRequests already acted on, with when they can be forgotten. An entry outlives the
   * window in which its request would be accepted, so a replay is always caught here or by the age
   * check.
   */
  private static final Map<String, Instant> seenLogoutRequests = new ConcurrentHashMap<>();

  /** The configured SAML client and the settings SamlSso enforces itself */
  static final class Sso {
    final SAML2Client client;
    final X509Certificate idpCertificate;

    /** want_messages_signed: logout messages must carry a valid IdP signature */
    final boolean requireSignedLogout;

    /**
     * This SP's single logout endpoint, which IdP LogoutRequests must be addressed to; null when
     * single logout isn't configured
     */
    final String singleLogoutUrl;

    /** Defaults for omitted NameQualifier / SPNameQualifier (SAML Core 8.3.7) */
    final String idpEntityId;

    final String spEntityId;

    /** Binds each sent AuthnRequest to the browser that started the login */
    final SamlRequestStore requestStore;

    Sso(
        SAML2Client client,
        X509Certificate idpCertificate,
        boolean requireSignedLogout,
        String singleLogoutUrl,
        String idpEntityId,
        String spEntityId,
        SamlRequestStore requestStore) {
      this.client = client;
      this.idpCertificate = idpCertificate;
      this.requireSignedLogout = requireSignedLogout;
      this.singleLogoutUrl = singleLogoutUrl;
      this.idpEntityId = idpEntityId;
      this.spEntityId = spEntityId;
      this.requestStore = requestStore;
    }
  }

  private static volatile Sso sso;

  private static Sso get() {
    Sso result = sso;
    if (result == null) {
      synchronized (SamlSso.class) {
        result = sso;
        if (result == null) {
          result = build(loadProperties());
          sso = result;
        }
      }
    }
    return result;
  }

  /** Replaces the configuration built from onelogin.saml.properties; for tests. */
  static void set(Sso replacement) {
    sso = replacement;
  }

  /**
   * Returns the SAML client, building it from onelogin.saml.properties on first use.
   *
   * @return The initialised SAML client
   */
  public static SAML2Client getClient() {
    return get().client;
  }

  /**
   * Sends the browser to the identity provider with a SAML authentication request.
   *
   * @param request The incoming request
   * @param response The response the redirect (or auto-submitting form) is written to
   */
  public static void login(HttpServletRequest request, HttpServletResponse response) {
    CallContext ctx = callContext(request, response);
    HttpAction action =
        getClient()
            .getRedirectionAction(ctx)
            .orElseThrow(() -> new IllegalStateException("No SAML redirection action"));
    JEEHttpActionAdapter.INSTANCE.adapt(action, ctx.webContext());
  }

  /**
   * Validates the SAML response posted to the assertion consumer service.
   *
   * @param request The ACS request carrying the SAML response
   * @param response The ACS response
   * @return The authenticated user's profile, or empty if the response did not authenticate a user
   */
  public static Optional<SAML2Profile> authenticate(
      HttpServletRequest request, HttpServletResponse response) {
    Sso config = get();
    SAML2Client saml = config.client;
    CallContext ctx = callContext(request, response);
    try {
      Optional<Credentials> extracted = saml.getCredentials(ctx);
      if (extracted.isEmpty()) {
        return Optional.empty();
      }
      // Only answers to a login started in this browser are accepted (login CSRF): an unsolicited
      // (IdP-initiated) Response has no InResponseTo to bind it to a browser, so it is refused;
      // otherwise pac4j checks InResponseTo against this browser's pending requests
      String inResponseTo = inResponseTo(extracted.get());
      if (inResponseTo == null) {
        log.error("SAML response rejected: unsolicited (no InResponseTo)");
        return Optional.empty();
      }
      Optional<Credentials> credentials = saml.validateCredentials(ctx, extracted.get());
      if (credentials.isEmpty()) {
        return Optional.empty();
      }
      config.requestStore.complete(inResponseTo, response);
      Optional<UserProfile> profile = saml.getUserProfile(ctx, credentials.get());
      return profile.filter(SAML2Profile.class::isInstance).map(SAML2Profile.class::cast);
    } catch (RuntimeException e) {
      // Signature, issuer, audience, destination and time checks fail with exceptions
      log.error("SAML response rejected: " + e.getMessage());
      log.debug("SAML response rejected", e);
      return Optional.empty();
    }
  }

  private static String inResponseTo(Credentials credentials) {
    if (!(credentials instanceof SAML2Credentials)) {
      return null;
    }
    Object message = ((SAML2Credentials) credentials).getContext().getMessageContext().getMessage();
    return message instanceof Response ? ((Response) message).getInResponseTo() : null;
  }

  /**
   * Starts SP-initiated single logout for the given profile.
   *
   * @param request The logout request
   * @param response The response the redirect (or auto-submitting form) is written to
   * @param profile The SAML profile captured at login
   * @return true if the browser was sent to the identity provider, false if no logout could be
   *     started (no IdP logout URL configured) and the caller must respond itself
   */
  public static boolean logout(
      HttpServletRequest request, HttpServletResponse response, SAML2Profile profile) {
    CallContext ctx = callContext(request, response);
    Optional<HttpAction> action =
        getClient().getLogoutAction(ctx, profile, null).map(HttpAction.class::cast);
    if (action.isEmpty()) {
      return false;
    }
    JEEHttpActionAdapter.INSTANCE.adapt(action.get(), ctx.webContext());
    return true;
  }

  /**
   * Records a post-login session against the SAML login that created it, so an IdP-initiated logout
   * naming that login can end it.
   *
   * @param session The post-login session
   * @param profile The profile from the validated assertion
   */
  public static void registerLoginSession(HttpSession session, SAML2Profile profile) {
    SamlSessionRegistry.register(
        session,
        nameIdKey(
            get(),
            profile.getId(),
            profile.getSamlNameIdFormat(),
            profile.getSamlNameIdNameQualifier(),
            profile.getSamlNameIdSpNameQualifier()),
        profile.getSessionIndex());
  }

  /** The complete identity of a NameID, as {@link SamlSessionRegistry} matches it */
  static String nameIdKey(
      String value, String format, String nameQualifier, String spNameQualifier) {
    return nameIdKey(get(), value, format, nameQualifier, spNameQualifier);
  }

  /**
   * Two NameIDs identify the same principal only if value, Format, NameQualifier and
   * SPNameQualifier all match. Omitted fields take their SAML defaults: unspecified format, and the
   * IdP / SP entity IDs as qualifiers (SAML Core 8.3.7).
   */
  private static String nameIdKey(
      Sso config, String value, String format, String nameQualifier, String spNameQualifier) {
    if (value == null) {
      return null;
    }
    return String.join(
        "\u0000",
        value,
        format == null || format.isEmpty() ? NameIDType.UNSPECIFIED : format,
        nameQualifier == null || nameQualifier.isEmpty() ? config.idpEntityId : nameQualifier,
        spNameQualifier == null || spNameQualifier.isEmpty() ? config.spEntityId : spNameQualifier);
  }

  /** Outcome of a message received on the single logout service */
  public enum LogoutMessage {
    /**
     * The IdP asked to end a login: the matching sessions were invalidated and a LogoutResponse has
     * been sent back to the IdP
     */
    IDP_LOGOUT_REQUEST,
    /** The IdP confirmed an SP-initiated logout */
    LOGOUT_RESPONSE,
    /** The message was missing, unsigned when a signature is required, or failed validation */
    INVALID
  }

  /**
   * Validates a LogoutRequest or LogoutResponse received on the single logout service.
   *
   * <p>When want_messages_signed is set, the message must be signed by the configured IdP: either
   * an XML signature (HTTP-POST binding) or a query-string signature (HTTP-Redirect binding).
   *
   * <p>For an IdP-initiated LogoutRequest, the sessions created by the login it names (NameID and
   * SessionIndex) are invalidated, whichever browser delivered the request, and the LogoutResponse
   * is written to the response. A LogoutResponse ends nothing itself: the local session was already
   * ended when the SP-initiated logout started.
   *
   * @param request The SLS request
   * @param response The SLS response
   * @return What was received
   */
  public static LogoutMessage processLogoutMessage(
      HttpServletRequest request, HttpServletResponse response) {
    Sso config = get();
    SAML2Client saml = config.client;
    CallContext ctx = callContext(request, response);
    try {
      Optional<Credentials> credentials =
          saml.getCredentials(ctx).flatMap(c -> saml.validateCredentials(ctx, c));
      if (credentials.isEmpty() || !(credentials.get() instanceof SAML2Credentials)) {
        return LogoutMessage.INVALID;
      }
      SAML2MessageContext messageContext = ((SAML2Credentials) credentials.get()).getContext();
      // pac4j validates an XML signature when one is present, but never requires one and does not
      // check HTTP-Redirect query-string signatures
      boolean signed =
          messageContext.getSAMLPeerEntityContext().isAuthenticated()
              || hasValidRedirectSignature(request, config.idpCertificate);
      if (config.requireSignedLogout && !signed) {
        log.error("SAML logout message rejected: no valid IdP signature");
        return LogoutMessage.INVALID;
      }

      Object message = messageContext.getMessageContext().getMessage();
      if (!(message instanceof LogoutRequest)) {
        return LogoutMessage.LOGOUT_RESPONSE;
      }
      LogoutRequest logoutRequest = (LogoutRequest) message;
      // pac4j checks none of these for LogoutRequests
      String rejection = checkLogoutRequest(logoutRequest, signed, config.singleLogoutUrl);
      if (rejection != null) {
        log.error("SAML LogoutRequest rejected: " + rejection);
        return LogoutMessage.INVALID;
      }
      NameID nameId = logoutRequest.getNameID();
      if (nameId == null && logoutRequest.getEncryptedID() != null) {
        nameId = (NameID) saml.getDecrypter().decrypt(logoutRequest.getEncryptedID());
      }
      String nameIdKey =
          nameId == null
              ? null
              : nameIdKey(
                  config,
                  nameId.getValue(),
                  nameId.getFormat(),
                  nameId.getNameQualifier(),
                  nameId.getSPNameQualifier());
      List<String> sessionIndexes = new ArrayList<>();
      for (SessionIndex index : logoutRequest.getSessionIndexes()) {
        if (index != null && index.getValue() != null) {
          sessionIndexes.add(index.getValue());
        }
      }
      // Only a request that will end something takes a slot in the replay cache: a signed request,
      // or an unsigned one naming a login we track. Otherwise unauthenticated no-op requests could
      // fill the cache and lock out genuine signed logouts. (A NameID alone is guessable, so only
      // a signed request may end all of a user's sessions.)
      boolean acts =
          signed
              || sessionIndexes.stream()
                  .anyMatch(index -> SamlSessionRegistry.isRegistered(nameIdKey, index));
      int ended = 0;
      if (acts) {
        String replay = recordLogoutRequest(logoutRequest);
        if (replay != null) {
          log.error("SAML LogoutRequest rejected: " + replay);
          return LogoutMessage.INVALID;
        }
        if (!sessionIndexes.isEmpty()) {
          for (String sessionIndex : sessionIndexes) {
            ended += SamlSessionRegistry.invalidate(nameIdKey, sessionIndex);
          }
        } else {
          ended = SamlSessionRegistry.invalidate(nameIdKey, null);
        }
      } else {
        log.error("Unsigned SAML LogoutRequest names no tracked login; no sessions ended");
      }
      log.debug("IdP-initiated SAML logout ended " + ended + " session(s)");
      HttpAction action = saml.processLogout(ctx, credentials.get());
      JEEHttpActionAdapter.INSTANCE.adapt(action, ctx.webContext());
      return LogoutMessage.IDP_LOGOUT_REQUEST;
    } catch (RuntimeException | DecryptionException e) {
      log.error("SAML logout message rejected: " + e.getMessage());
      log.debug("SAML logout message rejected", e);
      return LogoutMessage.INVALID;
    }
  }

  /**
   * Checks an IdP LogoutRequest's addressing and freshness before it may end sessions. A signed
   * request must name this SP's logout endpoint as its Destination (SAML Bindings 3.4.5.2 /
   * 3.5.5.2); any Destination given must match. Replay is checked by {@link #recordLogoutRequest},
   * only for requests that will act.
   *
   * @return why the request is rejected, or null if it may be acted on
   */
  static String checkLogoutRequest(LogoutRequest request, boolean signed, String singleLogoutUrl) {
    if (singleLogoutUrl == null) {
      return "no single logout endpoint is configured (" + SP_SLS_URL + ")";
    }
    String destination = request.getDestination();
    if (destination == null ? signed : !destination.equals(singleLogoutUrl)) {
      return "Destination " + destination + " is not " + singleLogoutUrl;
    }
    Instant now = Instant.now();
    Instant issued = request.getIssueInstant();
    if (issued == null
        || issued.isAfter(now.plus(CLOCK_SKEW))
        || issued.isBefore(now.minus(LOGOUT_REQUEST_LIFETIME).minus(CLOCK_SKEW))) {
      return "IssueInstant " + issued + " is outside the accepted window";
    }
    Instant notOnOrAfter = request.getNotOnOrAfter();
    if (notOnOrAfter != null && !now.minus(CLOCK_SKEW).isBefore(notOnOrAfter)) {
      return "expired at " + notOnOrAfter;
    }
    if (request.getID() == null) {
      return "no ID";
    }
    return null;
  }

  /**
   * Records a LogoutRequest's ID (per issuer) before it ends sessions, rejecting a replay. Fails
   * closed when the cache is full.
   *
   * @return why the request is rejected, or null if it was recorded
   */
  static String recordLogoutRequest(LogoutRequest request) {
    Instant now = Instant.now();
    String issuer = request.getIssuer() == null ? "" : request.getIssuer().getValue();
    seenLogoutRequests.values().removeIf(forgetAt -> forgetAt.isBefore(now));
    if (seenLogoutRequests.size() >= maxRememberedLogoutRequests) {
      return "too many recent logout requests to check for replay";
    }
    Instant forgetAt = now.plus(LOGOUT_REQUEST_LIFETIME).plus(CLOCK_SKEW.multipliedBy(2));
    if (seenLogoutRequests.putIfAbsent(issuer + "|" + request.getID(), forgetAt) != null) {
      return "replay of " + request.getID();
    }
    return null;
  }

  /** Empties the replay cache; for tests. */
  static void clearRememberedLogoutRequests() {
    seenLogoutRequests.clear();
  }

  /**
   * Verifies an HTTP-Redirect binding signature (SAML Bindings 3.4.4.1): the signature covers
   * SAMLRequest/SAMLResponse, RelayState and SigAlg exactly as they appear, URL-encoded, in the
   * query string.
   *
   * <p>The signed values must be the ones the servlet container (and so pac4j) reads: only a GET is
   * accepted, each signed parameter must appear exactly once under its literal name (no
   * percent-encoded aliases, no duplicates, nothing extra in a request body), and its decoded value
   * must equal the container's.
   */
  static boolean hasValidRedirectSignature(HttpServletRequest request, X509Certificate idpCert) {
    String query = request.getQueryString();
    if (!"GET".equalsIgnoreCase(request.getMethod()) || query == null || idpCert == null) {
      return false;
    }
    Map<String, String> raw = new HashMap<>();
    for (String pair : query.split("&")) {
      if (pair.isEmpty()) {
        continue;
      }
      int eq = pair.indexOf('=');
      String rawName = eq < 0 ? pair : pair.substring(0, eq);
      String name;
      try {
        name = URLDecoder.decode(rawName, StandardCharsets.UTF_8);
      } catch (IllegalArgumentException e) {
        return false;
      }
      if (!REDIRECT_SIGNED_PARAMETERS.contains(name)) {
        continue;
      }
      if (!name.equals(rawName)
          || raw.putIfAbsent(name, eq < 0 ? "" : pair.substring(eq + 1)) != null) {
        log.error("SAML redirect query has an encoded or repeated " + name + " parameter");
        return false;
      }
    }
    if (raw.containsKey("SAMLRequest") == raw.containsKey("SAMLResponse")) {
      return false;
    }
    for (Map.Entry<String, String> param : raw.entrySet()) {
      String[] values = request.getParameterValues(param.getKey());
      String decoded;
      try {
        decoded = URLDecoder.decode(param.getValue(), StandardCharsets.UTF_8);
      } catch (IllegalArgumentException e) {
        return false;
      }
      if (values == null || values.length != 1 || !decoded.equals(values[0])) {
        log.error("SAML redirect parameter " + param.getKey() + " does not match the signed query");
        return false;
      }
    }
    String messageParam = raw.containsKey("SAMLRequest") ? "SAMLRequest" : "SAMLResponse";
    String sigAlg = raw.get("SigAlg");
    String signature = raw.get("Signature");
    if (sigAlg == null || signature == null) {
      return false;
    }
    String algorithm =
        REDIRECT_SIGNATURE_ALGORITHMS.get(URLDecoder.decode(sigAlg, StandardCharsets.UTF_8));
    if (algorithm == null) {
      log.error("Unsupported SAML redirect signature algorithm: " + sigAlg);
      return false;
    }
    StringBuilder signedContent = new StringBuilder();
    signedContent.append(messageParam).append('=').append(raw.get(messageParam));
    if (raw.containsKey("RelayState")) {
      signedContent.append("&RelayState=").append(raw.get("RelayState"));
    }
    signedContent.append("&SigAlg=").append(sigAlg);
    try {
      Signature verifier = Signature.getInstance(algorithm);
      verifier.initVerify(idpCert.getPublicKey());
      verifier.update(signedContent.toString().getBytes(StandardCharsets.UTF_8));
      return verifier.verify(
          Base64.getDecoder().decode(URLDecoder.decode(signature, StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      log.debug("SAML redirect signature verification failed", e);
      return false;
    }
  }

  /**
   * Returns this service provider's SAML metadata.
   *
   * @return The SP metadata XML
   */
  public static String getServiceProviderMetadata() {
    return getClient().getServiceProviderMetadataResolver().getMetadata();
  }

  static CallContext callContext(HttpServletRequest request, HttpServletResponse response) {
    return new CallContext(new JEEContext(request, response), new JEESessionStore());
  }

  static Properties loadProperties() {
    try (InputStream in = SamlSso.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
      if (in == null) {
        throw new IllegalStateException(
            "SAML properties file '" + PROPERTIES_FILE + "' not found in the classpath");
      }
      Properties props = new Properties();
      props.load(in);
      return props;
    } catch (IOException e) {
      throw new IllegalStateException(
          "SAML properties file '" + PROPERTIES_FILE + "' cannot be loaded", e);
    }
  }

  /**
   * Builds and initialises the SAML client from java-saml style properties.
   *
   * @param props onelogin.saml.properties contents
   * @return The initialised client and the settings SamlSso enforces itself
   */
  static Sso build(Properties props) {
    if (!flag(props, STRICT, true)) {
      log.warn(STRICT + "=false is ignored: SAML messages are always validated strictly");
    }

    SAML2Configuration config = new SAML2Configuration();
    config.setServiceProviderEntityId(required(props, SP_ENTITY_ID));
    config.setIdentityProviderEntityId(required(props, IDP_ENTITY_ID));
    config.setSingleSignOutServiceUrl(optional(props, SP_SLS_URL));
    String acsBinding = optional(props, SP_ACS_BINDING, HTTP_POST_BINDING);
    if (!HTTP_POST_BINDING.equals(acsBinding)) {
      // ACS only authenticates on POST, so an IdP told to use another binding would deliver
      // responses that the ACS endpoint throws away, and every login would silently fail
      throw new IllegalStateException(
          SP_ACS_BINDING + " must be " + HTTP_POST_BINDING + ", the only binding ACS accepts");
    }
    config.setResponseBindingType(acsBinding);
    config.setAuthnRequestBindingType(optional(props, IDP_SSO_BINDING, HTTP_REDIRECT_BINDING));
    String sloBinding = optional(props, IDP_SLO_BINDING, HTTP_REDIRECT_BINDING);
    config.setSpLogoutRequestBindingType(sloBinding);
    config.setSpLogoutResponseBindingType(sloBinding);
    config.setNameIdPolicyFormat(optional(props, SP_NAMEID_FORMAT));
    // Send NameQualifier / SPNameQualifier back in LogoutRequests, so IdPs that issue qualified
    // NameIDs can match the logout to the login
    config.setUseNameQualifier(true);

    config.setAuthnRequestSigned(flag(props, AUTHN_REQUEST_SIGNED, false));
    config.setSpLogoutRequestSigned(flag(props, LOGOUT_REQUEST_SIGNED, false));
    config.setWantsResponsesSigned(flag(props, WANT_MESSAGES_SIGNED, false));
    config.setWantsAssertionsSigned(flag(props, WANT_ASSERTIONS_SIGNED, false));
    config.setSignMetadata(flag(props, SIGN_METADATA, false));

    String authnContext = optional(props, REQUESTED_AUTHN_CONTEXT);
    if (authnContext != null) {
      config.setAuthnContextClassRefs(splitList(authnContext));
      // pac4j drops RequestedAuthnContext entirely when no comparison is set
      String comparison =
          optional(
              props,
              REQUESTED_AUTHN_CONTEXT_COMPARISON,
              optional(props, LEGACY_REQUESTED_AUTHN_CONTEXT_COMPARISON, "exact"));
      config.setComparisonType(comparison);
    }
    String signatureAlgorithm = optional(props, SIGNATURE_ALGORITHM);
    if (signatureAlgorithm != null) {
      config.setSignatureAlgorithms(List.of(signatureAlgorithm));
    }

    configureKeystore(config, spCredentials(props));
    config.setMetadataGenerator(
        new SupportedBindingsMetadataGenerator(
            optional(props, SP_SLS_URL) != null, optional(props, SP_CERT) != null));
    config.setIdentityProviderMetadataResource(
        new ByteArrayResource(
            buildIdentityProviderMetadata(props).getBytes(StandardCharsets.UTF_8),
            "identity provider metadata from " + PROPERTIES_FILE));

    // pac4j's default store keeps nothing, so a Response's InResponseTo was never checked
    SamlRequestStore requestStore = new SamlRequestStore(required(props, SP_ACS_URL));
    config.setSamlMessageStoreFactory(requestStore);

    SAML2Client saml = new AlternativeAuthnContextClient(config);
    // The ACS URL is registered with the IdP as-is, so don't append ?client_name=...
    saml.setCallbackUrlResolver(new NoParameterCallbackUrlResolver());
    saml.setCallbackUrl(required(props, SP_ACS_URL));
    saml.init();
    X509Certificate idpCertificate;
    try {
      idpCertificate = parseCertificate(required(props, IDP_CERT));
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new IllegalStateException("Cannot load " + IDP_CERT + ": " + e.getMessage(), e);
    }
    // Null when no SLS URL is configured: no logout endpoint is advertised, and IdP
    // LogoutRequests are refused
    String singleLogoutUrl = optional(props, SP_SLS_URL);
    return new Sso(
        saml,
        idpCertificate,
        flag(props, WANT_MESSAGES_SIGNED, false),
        singleLogoutUrl,
        required(props, IDP_ENTITY_ID),
        required(props, SP_ENTITY_ID),
        requestStore);
  }

  /**
   * pac4j requires every requested AuthnContextClassRef to appear in the response, but SAML treats
   * the requested classes as alternatives (SAML Core 3.3.2.2.1). With "exact" comparison, at least
   * one returned class must be one of those requested. With minimum / better / maximum the IdP
   * judges relative strength, which an SP can't check in general (a stronger class may be one that
   * wasn't listed), so the returned class is accepted, as java-saml did.
   */
  private static final class AlternativeAuthnContextClient extends SAML2Client {
    AlternativeAuthnContextClient(SAML2Configuration configuration) {
      super(configuration);
    }

    @Override
    protected void initSAMLResponseValidator(boolean forceReinit) {
      if (authnResponseValidator == null || forceReinit) {
        String comparison = configuration.getComparisonType();
        authnResponseValidator =
            new SAML2AuthnResponseValidator(
                signatureTrustEngineProvider,
                decrypter,
                replayCache,
                configuration,
                findSessionLogoutHandler()) {
              @Override
              protected void validateAuthnContextClassRefs(
                  SAML2MessageContext context, List<String> provided) {
                List<String> requested = configuration.getAuthnContextClassRefs();
                if (requested.isEmpty()
                    || (comparison != null && !"exact".equalsIgnoreCase(comparison))) {
                  return;
                }
                if (provided.stream().noneMatch(requested::contains)) {
                  throw new SAMLAuthnContextClassRefException(
                      "None of the requested authentication context classes "
                          + requested
                          + " is in the IdP's authentication statements "
                          + provided);
                }
              }

              /**
               * pac4j binds a response to this browser's request only through the Response's
               * InResponseTo, which isn't signed unless the whole message is. The bearer
               * confirmation sits inside the signed assertion and must name the same request (SAML
               * Profiles 4.1.4.3), so an assertion can't be moved into an envelope naming another
               * request. Only SP-initiated login is supported, so it must name one. This runs on
               * decrypted assertions too.
               */
              @Override
              protected boolean isValidBearerSubjectConfirmationData(
                  SubjectConfirmationData data, SAML2MessageContext context) {
                if (!super.isValidBearerSubjectConfirmationData(data, context)) {
                  return false;
                }
                Object message = context.getMessageContext().getMessage();
                String requestId =
                    message instanceof Response ? ((Response) message).getInResponseTo() : null;
                if (requestId == null || !requestId.equals(data.getInResponseTo())) {
                  log.error(
                      "SAML response rejected: the assertion's bearer confirmation doesn't answer"
                          + " the request named by the response");
                  return false;
                }
                return true;
              }
            };
        authnResponseValidator.setAcceptedSkew(configuration.getAcceptedSkew());
      }
    }
  }

  /**
   * pac4j advertises HTTP-POST, HTTP-POST-SimpleSign, HTTP-Redirect and SOAP single logout
   * endpoints. SamlSso can only verify signatures for HTTP-POST (XML signature) and HTTP-Redirect
   * (query-string signature), so advertise just those. With no SLS URL configured pac4j would
   * advertise the ACS URL, which doesn't process logout messages, so advertise none. Without a
   * configured SP certificate the key is temporary, so publish no KeyDescriptor. The descriptor is
   * signed afterwards.
   */
  private static final class SupportedBindingsMetadataGenerator
      extends SAML2FileSystemMetadataGenerator {
    private final boolean advertiseSingleLogout;
    private final boolean publishKeys;

    SupportedBindingsMetadataGenerator(boolean advertiseSingleLogout, boolean publishKeys) {
      super(null);
      this.advertiseSingleLogout = advertiseSingleLogout;
      this.publishKeys = publishKeys;
    }

    @Override
    protected SPSSODescriptor buildSPSSODescriptor() {
      SPSSODescriptor descriptor = super.buildSPSSODescriptor();
      descriptor
          .getSingleLogoutServices()
          .removeIf(
              service ->
                  !advertiseSingleLogout
                      || (!HTTP_POST_BINDING.equals(service.getBinding())
                          && !HTTP_REDIRECT_BINDING.equals(service.getBinding())));
      if (!publishKeys) {
        // A temporary key changes on every restart, so an IdP must not sign-verify or encrypt to it
        descriptor.getKeyDescriptors().clear();
      }
      return descriptor;
    }
  }

  /**
   * The SP's signing/decryption key and certificate. Like java-saml, both settings may be left out
   * when the SP signs nothing (authnrequest_signed, logoutrequest_signed and sign_metadata all
   * false): pac4j still needs a key pair, so a temporary one is generated in memory. Encrypted
   * assertions then can't be used, since the key changes on every restart.
   */
  private static KeyStore.PrivateKeyEntry spCredentials(Properties props) {
    String certPem = optional(props, SP_CERT);
    String keyPem = optional(props, SP_KEY);
    try {
      if (certPem != null && keyPem != null) {
        return new KeyStore.PrivateKeyEntry(
            parsePrivateKey(keyPem), new Certificate[] {parseCertificate(certPem)});
      }
      if (certPem != null || keyPem != null) {
        throw new IllegalStateException("Set both " + SP_CERT + " and " + SP_KEY + ", or neither");
      }
      List<String> signingSettings = new ArrayList<>();
      for (String setting : List.of(AUTHN_REQUEST_SIGNED, LOGOUT_REQUEST_SIGNED, SIGN_METADATA)) {
        if (flag(props, setting, false)) {
          signingSettings.add(setting);
        }
      }
      if (!signingSettings.isEmpty()) {
        throw new IllegalStateException(
            "Missing SAML setting "
                + SP_CERT
                + " / "
                + SP_KEY
                + ", required by "
                + String.join(", ", signingSettings));
      }
      log.info(
          "No SP certificate configured: using a temporary in-memory key pair. SP signing and"
              + " encrypted assertions are unavailable.");
      return temporaryCredentials();
    } catch (GeneralSecurityException | IOException | OperatorCreationException e) {
      throw new IllegalStateException(
          "Cannot load " + SP_CERT + " / " + SP_KEY + ": " + e.getMessage(), e);
    }
  }

  private static KeyStore.PrivateKeyEntry temporaryCredentials()
      throws GeneralSecurityException, OperatorCreationException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keys = generator.generateKeyPair();
    X500Name name = new X500Name("CN=Security Shepherd SAML SP (temporary)");
    Instant now = Instant.now();
    X509Certificate cert =
        new JcaX509CertificateConverter()
            .getCertificate(
                new JcaX509v3CertificateBuilder(
                        name,
                        BigInteger.valueOf(now.toEpochMilli()),
                        Date.from(now.minus(Duration.ofDays(1))),
                        Date.from(now.plus(Duration.ofDays(3650))),
                        name,
                        keys.getPublic())
                    .build(new JcaContentSignerBuilder("SHA256withRSA").build(keys.getPrivate())));
    return new KeyStore.PrivateKeyEntry(keys.getPrivate(), new Certificate[] {cert});
  }

  /**
   * Loads the SP's private key and certificate into an in-memory keystore, so pac4j neither reads
   * nor generates a keystore file.
   */
  private static void configureKeystore(
      SAML2Configuration config, KeyStore.PrivateKeyEntry credentials) {
    String password = UUID.randomUUID().toString();
    byte[] keystore;
    try {
      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(null, null);
      ks.setKeyEntry(
          SP_KEY_ALIAS,
          credentials.getPrivateKey(),
          password.toCharArray(),
          credentials.getCertificateChain());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ks.store(out, password.toCharArray());
      keystore = out.toByteArray();
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Cannot build the SP keystore: " + e.getMessage(), e);
    }

    config.getKeystore().setKeystoreResource(new ByteArrayResource(keystore, "SP keystore"));
    config.getKeystore().setKeyStoreType("PKCS12");
    config.getKeystore().setKeyStoreAlias(SP_KEY_ALIAS);
    config.getKeystore().setKeystorePassword(password);
    config.getKeystore().setPrivateKeyPassword(password);
    config
        .getKeystore()
        .setKeystoreGenerator(
            new KeystoreGenerator() {
              @Override
              public void generate() {}

              @Override
              public boolean shouldGenerate() {
                return false;
              }

              @Override
              public InputStream retrieve() {
                return new ByteArrayInputStream(keystore);
              }
            });
  }

  /**
   * pac4j reads the IdP from SAML metadata, while onelogin.saml.properties describes it with
   * individual keys. Build the equivalent metadata document from those keys.
   */
  static String buildIdentityProviderMetadata(Properties props) {
    String cert = optional(props, IDP_CERT);
    if (cert == null) {
      String reason =
          optional(props, IDP_CERT_FINGERPRINT) != null
              ? " (" + IDP_CERT_FINGERPRINT + " alone is not supported; set the full certificate)"
              : "";
      throw new IllegalStateException("Missing SAML setting " + IDP_CERT + reason);
    }
    StringBuilder xml = new StringBuilder();
    xml.append("<md:EntityDescriptor xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\"")
        .append(" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" entityID=\"")
        .append(Encode.forXmlAttribute(required(props, IDP_ENTITY_ID)))
        .append("\">")
        .append("<md:IDPSSODescriptor")
        .append(" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">")
        .append("<md:KeyDescriptor use=\"signing\"><ds:KeyInfo><ds:X509Data><ds:X509Certificate>")
        .append(pemBody(cert))
        .append("</ds:X509Certificate></ds:X509Data></ds:KeyInfo></md:KeyDescriptor>");
    String sloUrl = optional(props, IDP_SLO_URL);
    if (sloUrl != null) {
      xml.append("<md:SingleLogoutService Binding=\"")
          .append(Encode.forXmlAttribute(optional(props, IDP_SLO_BINDING, HTTP_REDIRECT_BINDING)))
          .append("\" Location=\"")
          .append(Encode.forXmlAttribute(sloUrl))
          .append("\"");
      String sloResponseUrl = optional(props, IDP_SLO_RESPONSE_URL);
      if (sloResponseUrl != null) {
        xml.append(" ResponseLocation=\"")
            .append(Encode.forXmlAttribute(sloResponseUrl))
            .append("\"");
      }
      xml.append("/>");
    }
    xml.append("<md:SingleSignOnService Binding=\"")
        .append(Encode.forXmlAttribute(optional(props, IDP_SSO_BINDING, HTTP_REDIRECT_BINDING)))
        .append("\" Location=\"")
        .append(Encode.forXmlAttribute(required(props, IDP_SSO_URL)))
        .append("\"/>")
        .append("</md:IDPSSODescriptor></md:EntityDescriptor>");
    return xml.toString();
  }

  static X509Certificate parseCertificate(String pem) throws GeneralSecurityException {
    byte[] der = Base64.getDecoder().decode(pemBody(pem));
    return (X509Certificate)
        CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(der));
  }

  /** Accepts PKCS#8 ("BEGIN PRIVATE KEY") or PKCS#1 ("BEGIN RSA PRIVATE KEY") RSA keys. */
  static PrivateKey parsePrivateKey(String pem) throws GeneralSecurityException, IOException {
    byte[] der = Base64.getDecoder().decode(pemBody(pem));
    KeyFactory rsa = KeyFactory.getInstance("RSA");
    try {
      return rsa.generatePrivate(new PKCS8EncodedKeySpec(der));
    } catch (InvalidKeySpecException notPkcs8) {
      PrivateKeyInfo pkcs8 =
          new PrivateKeyInfo(
              new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE),
              RSAPrivateKey.getInstance(der));
      return rsa.generatePrivate(new PKCS8EncodedKeySpec(pkcs8.getEncoded()));
    }
  }

  /** Strips PEM armour and whitespace, leaving the base64 body. */
  static String pemBody(String pem) {
    return pem.replaceAll("-----(BEGIN|END)[^-]*-----", "").replaceAll("\\s", "");
  }

  private static List<String> splitList(String value) {
    List<String> items = new ArrayList<>();
    for (String item : value.split(",")) {
      if (!item.trim().isEmpty()) {
        items.add(item.trim());
      }
    }
    return items;
  }

  private static String required(Properties props, String key) {
    String value = optional(props, key);
    if (value == null) {
      throw new IllegalStateException("Missing SAML setting " + key + " in " + PROPERTIES_FILE);
    }
    return value;
  }

  private static String optional(Properties props, String key) {
    String value = props.getProperty(key);
    return value == null || value.trim().isEmpty() ? null : value.trim();
  }

  private static String optional(Properties props, String key, String defaultValue) {
    String value = optional(props, key);
    return value == null ? defaultValue : value;
  }

  private static boolean flag(Properties props, String key, boolean defaultValue) {
    String value = optional(props, key);
    return value == null ? defaultValue : Boolean.parseBoolean(value);
  }
}
