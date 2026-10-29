package utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SamlSsoTest {

  private static SamlTestSupport saml;

  @BeforeAll
  public static void generateKeys() throws Exception {
    saml = new SamlTestSupport();
  }

  @BeforeEach
  public void installClient() throws Exception {
    saml.installClient();
  }

  @AfterEach
  public void uninstallClient() {
    SamlTestSupport.uninstallClient();
  }

  private static Map<String, List<String>> studentAttributes() {
    Map<String, List<String>> attributes = new LinkedHashMap<>();
    attributes.put("uid", Collections.singletonList("jdoe"));
    attributes.put("displayName", Collections.singletonList("Jane Doe"));
    attributes.put("groups", Arrays.asList("students", "chess-club"));
    return attributes;
  }

  private static MockHttpServletRequest acsPost(String samlResponse) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/saml/acs");
    request.setScheme("https");
    request.setSecure(true);
    request.setServerName("shepherd.test");
    request.setServerPort(443);
    request.addParameter("SAMLResponse", samlResponse);
    // Delivered by the browser that started the login, with the cookies it sends to the ACS
    Cookie[] cookies = SamlTestSupport.cookiesFor("/saml/acs");
    if (cookies.length > 0) {
      request.setCookies(cookies);
    }
    return request;
  }

  @Test
  public void parsePrivateKey_AcceptsPkcs8AndPkcs1() throws Exception {
    KeyPair keys = SamlTestSupport.newKeyPair();

    PrivateKey fromPkcs8 = SamlSso.parsePrivateKey(SamlTestSupport.pkcs8Pem(keys));
    PrivateKey fromPkcs1 = SamlSso.parsePrivateKey(SamlTestSupport.pkcs1Pem(keys));

    assertEquals(keys.getPrivate(), fromPkcs8);
    assertEquals(keys.getPrivate(), fromPkcs1);
  }

  @Test
  public void parseCertificate_AcceptsBareBase64() throws Exception {
    String bare = Base64.getEncoder().encodeToString(saml.idpCert.getEncoded());

    X509Certificate cert = SamlSso.parseCertificate(bare);

    assertEquals(saml.idpCert, cert);
  }

  @Test
  public void buildClient_MissingIdpCertificate_NamesTheSetting() throws Exception {
    Properties props = saml.properties();
    props.remove(SamlSso.IDP_CERT);
    props.setProperty(SamlSso.IDP_CERT_FINGERPRINT, "AB:CD");

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> SamlSso.build(props));

    assertThat(e.getMessage(), containsString(SamlSso.IDP_CERT));
    assertThat(e.getMessage(), containsString("not supported"));
  }

  @Test
  public void buildClient_MissingAcsUrl_NamesTheSetting() throws Exception {
    Properties props = saml.properties();
    props.setProperty(SamlSso.SP_ACS_URL, " ");

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> SamlSso.build(props));

    assertThat(e.getMessage(), containsString(SamlSso.SP_ACS_URL));
  }

  @Test
  public void build_AcsBindingOtherThanPost_IsRejected() throws Exception {
    Properties props = saml.properties();
    props.setProperty(SamlSso.SP_ACS_BINDING, "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> SamlSso.build(props));

    assertThat(e.getMessage(), containsString(SamlSso.SP_ACS_BINDING));
  }

  @Test
  public void build_AcsBindingPost_IsAccepted() throws Exception {
    Properties props = saml.properties();
    props.setProperty(SamlSso.SP_ACS_BINDING, "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");

    SamlSso.build(props);
  }

  @Test
  public void login_RedirectsToIdpWithAuthnRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/saml/dologin");
    MockHttpServletResponse response = new MockHttpServletResponse();

    SamlSso.login(request, response);

    assertEquals(302, response.getStatus());
    String location = response.getRedirectedUrl();
    assertThat(location, startsWith(SamlTestSupport.IDP_SSO_URL + "?"));
    assertThat(location, containsString("Signature="));
    String authnRequest = SamlTestSupport.decodeRedirectRequest(location);
    assertThat(authnRequest, containsString("AuthnRequest"));
    assertThat(authnRequest, containsString(SamlTestSupport.ACS_URL));
    assertThat(authnRequest, containsString(SamlTestSupport.SP_ENTITY_ID));
  }

  @Test
  public void getServiceProviderMetadata_DescribesConfiguredEndpoints() throws Exception {
    String metadata = SamlSso.getServiceProviderMetadata();

    assertThat(metadata, containsString("entityID=\"" + SamlTestSupport.SP_ENTITY_ID + "\""));
    assertThat(metadata, containsString("Location=\"" + SamlTestSupport.ACS_URL + "\""));
    assertThat(metadata, containsString("Location=\"" + SamlTestSupport.SLS_URL + "\""));
    // The ACS URL must be exactly what the IdP has registered
    assertFalse(metadata.contains("client_name="));
    String spCertBase64 = Base64.getEncoder().encodeToString(saml.spCert.getEncoded());
    assertThat(metadata.replaceAll("\\s", ""), containsString(spCertBase64));
  }

  @Test
  public void authenticate_ValidSignedResponse_ReturnsProfile() throws Exception {
    String samlResponse =
        saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);

    Optional<SAML2Profile> profile =
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse());

    assertTrue(profile.isPresent());
    assertEquals("user-123", profile.get().getId());
    assertEquals("_session-user-123", profile.get().getSessionIndex());
    SsoUserMapper.Result user = SsoUserMapper.map(profile.get().getAttributes(), ssoProperties());
    assertTrue(user.isValid());
    assertEquals("jdoe", user.ssoName);
    assertEquals("Jane Doe", user.userName);
    assertEquals("player", user.userRole);
  }

  @Test
  public void authenticate_TamperedResponse_IsRejected() throws Exception {
    String signed =
        saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);
    String xml = new String(Base64.getDecoder().decode(signed), "UTF-8");
    String tampered =
        Base64.getEncoder().encodeToString(xml.replace("students", "teachers").getBytes("UTF-8"));

    Optional<SAML2Profile> profile =
        SamlSso.authenticate(acsPost(tampered), new MockHttpServletResponse());

    assertFalse(profile.isPresent());
  }

  @Test
  public void authenticate_SignedByUnknownKey_IsRejected() throws Exception {
    KeyPair attackerKeys = SamlTestSupport.newKeyPair();
    X509Certificate attackerCert = SamlTestSupport.selfSigned(attackerKeys, "idp.test");
    String samlResponse =
        saml.signedResponse("user-123", studentAttributes(), attackerKeys, attackerCert);

    Optional<SAML2Profile> profile =
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse());

    assertFalse(profile.isPresent());
  }

  @Test
  public void authenticate_ReplayedResponse_IsRejected() throws Exception {
    String samlResponse =
        saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);

    assertTrue(
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse()).isPresent());
    assertFalse(
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse()).isPresent());
  }

  @Test
  public void authenticate_NoSamlResponse_ReturnsEmpty() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/saml/acs");

    assertFalse(SamlSso.authenticate(request, new MockHttpServletResponse()).isPresent());
  }

  @Test
  public void logout_SendsLogoutRequestWithNameIdAndSessionIndex() throws Exception {
    String samlResponse =
        saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);
    SAML2Profile profile =
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse()).get();
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean started =
        SamlSso.logout(new MockHttpServletRequest("GET", "/logout"), response, profile);

    assertTrue(started);
    String location = response.getRedirectedUrl();
    assertThat(location, startsWith(SamlTestSupport.IDP_SLO_URL + "?"));
    String logoutRequest = SamlTestSupport.decodeRedirectRequest(location);
    assertThat(logoutRequest, containsString("LogoutRequest"));
    assertThat(logoutRequest, containsString(">user-123<"));
    assertThat(logoutRequest, containsString("_session-user-123"));
  }

  @Test
  public void logout_KeepsNameIdQualifiers() throws Exception {
    String samlResponse =
        saml.signedResponse(
            "user-123",
            SamlTestSupport.IDP_ENTITY_ID,
            SamlTestSupport.SP_ENTITY_ID,
            studentAttributes(),
            saml.idpKeys,
            saml.idpCert);
    SAML2Profile profile =
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse()).get();
    MockHttpServletResponse response = new MockHttpServletResponse();

    SamlSso.logout(new MockHttpServletRequest("GET", "/logout"), response, profile);

    String logoutRequest = SamlTestSupport.decodeRedirectRequest(response.getRedirectedUrl());
    assertThat(
        logoutRequest, containsString("NameQualifier=\"" + SamlTestSupport.IDP_ENTITY_ID + "\""));
    assertThat(
        logoutRequest, containsString("SPNameQualifier=\"" + SamlTestSupport.SP_ENTITY_ID + "\""));
  }

  @Test
  public void login_RequestedAuthnContextWithoutComparison_DefaultsToExact() throws Exception {
    Properties props = saml.properties();
    props.setProperty(
        SamlSso.REQUESTED_AUTHN_CONTEXT,
        "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
    SamlTestSupport.install(props);
    MockHttpServletResponse response = new MockHttpServletResponse();

    SamlSso.login(new MockHttpServletRequest("GET", "/saml/dologin"), response);

    String authnRequest = SamlTestSupport.decodeRedirectRequest(response.getRedirectedUrl());
    assertThat(authnRequest, containsString("Comparison=\"exact\""));
    assertThat(authnRequest, containsString("PasswordProtectedTransport"));
  }

  @Test
  public void login_LegacyComparisonKey_IsRead() throws Exception {
    Properties props = saml.properties();
    props.setProperty(
        SamlSso.REQUESTED_AUTHN_CONTEXT,
        "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
    props.setProperty(SamlSso.LEGACY_REQUESTED_AUTHN_CONTEXT_COMPARISON, "minimum");
    SamlTestSupport.install(props);
    MockHttpServletResponse response = new MockHttpServletResponse();

    SamlSso.login(new MockHttpServletRequest("GET", "/saml/dologin"), response);

    String authnRequest = SamlTestSupport.decodeRedirectRequest(response.getRedirectedUrl());
    assertThat(authnRequest, containsString("Comparison=\"minimum\""));
  }

  @Test
  public void getServiceProviderMetadata_AdvertisesOnlyVerifiableLogoutBindings() {
    String metadata = SamlSso.getServiceProviderMetadata();

    assertThat(metadata, containsString("bindings:HTTP-POST\""));
    assertThat(metadata, containsString("bindings:HTTP-Redirect\""));
    assertFalse(metadata.contains("HTTP-POST-SimpleSign"));
    assertFalse(metadata.contains("bindings:SOAP"));
  }

  @Test
  public void build_WithoutSpKeyPair_AuthenticatesSignedAssertions() throws Exception {
    Properties props = saml.properties();
    props.remove(SamlSso.SP_CERT);
    props.remove(SamlSso.SP_KEY);
    props.setProperty(SamlSso.AUTHN_REQUEST_SIGNED, "false");
    props.setProperty(SamlSso.LOGOUT_REQUEST_SIGNED, "false");
    SamlTestSupport.install(props);
    String samlResponse =
        saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);

    Optional<SAML2Profile> profile =
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse());

    assertTrue(profile.isPresent());
    assertEquals("user-123", profile.get().getId());
  }

  @Test
  public void getServiceProviderMetadata_WithoutSpKeyPair_PublishesNoKey() throws Exception {
    Properties props = saml.properties();
    props.remove(SamlSso.SP_CERT);
    props.remove(SamlSso.SP_KEY);
    props.setProperty(SamlSso.AUTHN_REQUEST_SIGNED, "false");
    props.setProperty(SamlSso.LOGOUT_REQUEST_SIGNED, "false");
    SamlTestSupport.install(props);

    String metadata = SamlSso.getServiceProviderMetadata();

    assertFalse(metadata.contains("KeyDescriptor"));
    assertFalse(metadata.contains("X509Certificate"));
  }

  @Test
  public void getServiceProviderMetadata_WithSpKeyPair_PublishesIt() {
    assertThat(SamlSso.getServiceProviderMetadata(), containsString("KeyDescriptor"));
  }

  @Test
  public void build_WithoutSpKeyPair_WhenSigningRequired_NamesTheSettings() throws Exception {
    Properties props = saml.properties();
    props.remove(SamlSso.SP_CERT);
    props.remove(SamlSso.SP_KEY);

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> SamlSso.build(props));

    assertThat(e.getMessage(), containsString(SamlSso.SP_CERT));
    assertThat(e.getMessage(), containsString(SamlSso.AUTHN_REQUEST_SIGNED));
  }

  @Test
  public void build_OnlyOneOfSpCertAndKey_IsRejected() throws Exception {
    Properties props = saml.properties();
    props.remove(SamlSso.SP_KEY);

    IllegalStateException e = assertThrows(IllegalStateException.class, () -> SamlSso.build(props));

    assertThat(e.getMessage(), containsString("or neither"));
  }

  private Optional<SAML2Profile> authenticateWithRequestedContexts(
      String requested, String comparison, String returned) throws Exception {
    Properties props = saml.properties();
    props.setProperty(SamlSso.REQUESTED_AUTHN_CONTEXT, requested);
    if (comparison != null) {
      props.setProperty(SamlSso.REQUESTED_AUTHN_CONTEXT_COMPARISON, comparison);
    }
    SamlTestSupport.install(props);
    saml.authnContextClassRef = returned;
    try {
      String samlResponse =
          saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);
      return SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse());
    } finally {
      saml.authnContextClassRef = AuthnContext.PPT_AUTHN_CTX;
    }
  }

  @Test
  public void authenticate_OneOfSeveralRequestedContexts_IsAccepted() throws Exception {
    assertTrue(
        authenticateWithRequestedContexts(
                AuthnContext.PPT_AUTHN_CTX + ", " + AuthnContext.X509_AUTHN_CTX,
                null,
                AuthnContext.X509_AUTHN_CTX)
            .isPresent());
  }

  @Test
  public void authenticate_ExactComparison_UnrequestedContext_IsRejected() throws Exception {
    assertFalse(
        authenticateWithRequestedContexts(
                AuthnContext.PPT_AUTHN_CTX + ", " + AuthnContext.X509_AUTHN_CTX,
                "exact",
                AuthnContext.KERBEROS_AUTHN_CTX)
            .isPresent());
  }

  @Test
  public void authenticate_MinimumComparison_StrongerUnlistedContext_IsAccepted() throws Exception {
    assertTrue(
        authenticateWithRequestedContexts(
                AuthnContext.PPT_AUTHN_CTX, "minimum", AuthnContext.X509_AUTHN_CTX)
            .isPresent());
  }

  @Test
  public void authenticate_NoRequestedContext_AcceptsAnyContext() throws Exception {
    String samlResponse =
        saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);

    assertTrue(
        SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse()).isPresent());
  }

  @Test
  public void getServiceProviderMetadata_WithoutSlsUrl_AdvertisesNoLogoutEndpoint()
      throws Exception {
    Properties props = saml.properties();
    props.remove(SamlSso.SP_SLS_URL);
    SamlTestSupport.install(props);

    String metadata = SamlSso.getServiceProviderMetadata();

    assertFalse(metadata.contains("SingleLogoutService"));
    assertThat(metadata, containsString("Location=\"" + SamlTestSupport.ACS_URL + "\""));
  }

  /** Two tabs start a login before either finishes: both callbacks must succeed */
  @Test
  public void overlappingLogins_InOneBrowser_BothSucceed() throws Exception {
    saml.startLogin();
    String first = SamlTestSupport.lastRequestId();
    saml.startLogin();
    String second = SamlTestSupport.lastRequestId();

    assertTrue(
        SamlSso.authenticate(
                acsPost(saml.signedResponseTo(first, "user-123", studentAttributes())),
                new MockHttpServletResponse())
            .isPresent());
    assertTrue(
        SamlSso.authenticate(
                acsPost(saml.signedResponseTo(second, "user-123", studentAttributes())),
                new MockHttpServletResponse())
            .isPresent());
  }

  /**
   * A second tab starts a login just before the first tab's cookie would expire: the cookie must
   * last as long as the newest login, or that login's callback arrives without it
   */
  @Test
  public void overlappingLogins_LaterLoginKeepsTheCookieAlive() throws Exception {
    saml.startLogin();
    SamlTestSupport.advanceBrowserClock(Duration.ofMinutes(9));
    saml.startLogin();
    String second = SamlTestSupport.lastRequestId();
    SamlTestSupport.advanceBrowserClock(Duration.ofMinutes(2));

    assertTrue(
        SamlSso.authenticate(
                acsPost(saml.signedResponseTo(second, "user-123", studentAttributes())),
                new MockHttpServletResponse())
            .isPresent());
  }

  /** Two tabs open the login page before the browser holds any binding cookie: both must work */
  @Test
  public void concurrentFirstLogins_InOneBrowser_BothSucceed() throws Exception {
    List<String> requests = SamlTestSupport.startConcurrentLogins(2);

    assertTrue(
        SamlSso.authenticate(
                acsPost(saml.signedResponseTo(requests.get(0), "user-123", studentAttributes())),
                new MockHttpServletResponse())
            .isPresent());
    assertTrue(
        SamlSso.authenticate(
                acsPost(saml.signedResponseTo(requests.get(1), "user-123", studentAttributes())),
                new MockHttpServletResponse())
            .isPresent());
  }

  /** A completed login deletes its binding cookie, so cookies don't pile up in the browser */
  @Test
  public void authenticate_Success_DeletesThatLoginsCookie() throws Exception {
    Cookie cookie = saml.startLogin();
    String samlResponse =
        saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(SamlSso.authenticate(acsPost(samlResponse), response).isPresent());

    Cookie deleted = response.getCookie(cookie.getName());
    assertNotNull(deleted);
    assertEquals(0, deleted.getMaxAge());
    assertEquals(cookie.getPath(), deleted.getPath());
  }

  @Test
  public void bindingCookie_IsScopedToTheSamlDirectory() throws Exception {
    Cookie cookie = saml.startLogin();

    assertEquals("/saml/", cookie.getPath());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.getSecure());
    assertEquals("None", cookie.getAttribute("SameSite"));
  }

  @Test
  public void authenticate_UnsolicitedResponse_IsRejected() throws Exception {
    saml.unsolicited = true;
    try {
      String samlResponse =
          saml.signedResponse("user-123", studentAttributes(), saml.idpKeys, saml.idpCert);

      assertFalse(
          SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse()).isPresent());
    } finally {
      saml.unsolicited = false;
    }
  }

  /**
   * Login CSRF: the attacker's own valid assertion, with only the unsigned envelope's InResponseTo
   * changed to the victim browser's pending request, must not log the victim's browser in
   */
  @Test
  public void authenticate_EnvelopeRetargetedToAnotherBrowsersRequest_IsRejected()
      throws Exception {
    saml.startLogin();
    String attackersRequest = SamlTestSupport.lastRequestId();
    SamlTestSupport.newBrowser();
    saml.startLogin();
    String victimsRequest = SamlTestSupport.lastRequestId();

    saml.envelopeInResponseTo = victimsRequest;
    try {
      String forged = saml.signedResponseTo(attackersRequest, "attacker", studentAttributes());

      assertFalse(SamlSso.authenticate(acsPost(forged), new MockHttpServletResponse()).isPresent());
    } finally {
      saml.envelopeInResponseTo = null;
    }
  }

  /**
   * An assertion whose bearer confirmation names no request (as an IdP-initiated one) can't be
   * bound to a login, even inside an envelope that names this browser's pending request
   */
  @Test
  public void authenticate_ConfirmationWithoutInResponseTo_IsRejected() throws Exception {
    saml.startLogin();
    String request = SamlTestSupport.lastRequestId();

    saml.omitConfirmationInResponseTo = true;
    try {
      String samlResponse = saml.signedResponseTo(request, "user-123", studentAttributes());

      assertFalse(
          SamlSso.authenticate(acsPost(samlResponse), new MockHttpServletResponse()).isPresent());
    } finally {
      saml.omitConfirmationInResponseTo = false;
    }
  }

  /** Login CSRF: a valid response must not log in a browser other than the one that asked */
  @Test
  public void authenticate_ResponseDeliveredThroughAnotherBrowser_IsRejected() throws Exception {
    saml.startLogin();
    String request = SamlTestSupport.lastRequestId();
    String samlResponse =
        saml.signedResponse("attacker", studentAttributes(), saml.idpKeys, saml.idpCert);
    MockHttpServletRequest victimWithoutCookie = acsPost(samlResponse);
    victimWithoutCookie.setCookies();
    MockHttpServletRequest victimWithOwnCookie = acsPost(samlResponse);
    victimWithOwnCookie.setCookies(
        new jakarta.servlet.http.Cookie(SamlRequestStore.cookieName(request), "victims-own-token"));

    assertFalse(
        SamlSso.authenticate(victimWithoutCookie, new MockHttpServletResponse()).isPresent());
    assertFalse(
        SamlSso.authenticate(victimWithOwnCookie, new MockHttpServletResponse()).isPresent());
  }

  @Test
  public void authenticate_ResponseToAnotherBrowsersLogin_IsRejected() throws Exception {
    saml.startLogin();
    String samlResponse =
        saml.signedResponse("attacker", studentAttributes(), saml.idpKeys, saml.idpCert);
    // The victim's (different) browser has started a login of its own, with its own cookie
    SamlTestSupport.newBrowser();
    jakarta.servlet.http.Cookie victimCookie = saml.startLogin();
    MockHttpServletRequest victim = acsPost(samlResponse);
    victim.setCookies(victimCookie);

    assertFalse(SamlSso.authenticate(victim, new MockHttpServletResponse()).isPresent());
  }

  @Test
  public void processLogoutMessage_NoMessage_IsInvalid() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/saml/sls");

    assertEquals(
        SamlSso.LogoutMessage.INVALID,
        SamlSso.processLogoutMessage(request, new MockHttpServletResponse()));
  }

  private static Properties ssoProperties() {
    Properties props = new Properties();
    props.setProperty("sso.saml.ssoName", "uid");
    props.setProperty("sso.saml.userName", "displayName");
    props.setProperty("sso.saml.affiliation", "groups");
    props.setProperty("sso.saml.adminAffiliation", "staff");
    props.setProperty("sso.saml.playerAffiliation", "students");
    return props;
  }
}
