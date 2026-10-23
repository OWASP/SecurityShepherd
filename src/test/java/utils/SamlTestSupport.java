package utils;

import jakarta.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.xml.namespace.QName;
import net.shibboleth.shared.xml.SerializeSupport;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test fixtures for SAML SSO: generated SP/IdP key pairs, onelogin.saml.properties contents for
 * them, and signed SAML responses as an IdP would send them.
 */
public class SamlTestSupport {

  public static final String SP_ENTITY_ID = "https://shepherd.test/saml/metadata.xml";
  public static final String ACS_URL = "https://shepherd.test/saml/acs";
  public static final String SLS_URL = "https://shepherd.test/saml/sls";
  public static final String IDP_ENTITY_ID = "https://idp.test/metadata";
  public static final String IDP_SSO_URL = "https://idp.test/sso";
  public static final String IDP_SLO_URL = "https://idp.test/slo";

  /** The AuthnContextClassRef that {@link #signedResponse} reports */
  public String authnContextClassRef = AuthnContext.PPT_AUTHN_CTX;

  public final KeyPair spKeys;
  public final X509Certificate spCert;
  public final KeyPair idpKeys;
  public final X509Certificate idpCert;

  public SamlTestSupport() throws Exception {
    spKeys = newKeyPair();
    spCert = selfSigned(spKeys, "shepherd.test");
    idpKeys = newKeyPair();
    idpCert = selfSigned(idpKeys, "idp.test");
  }

  /** onelogin.saml.properties as an operator would write it, with PEM armour */
  public Properties properties() throws Exception {
    Properties props = new Properties();
    props.setProperty(SamlSso.STRICT, "true");
    props.setProperty(SamlSso.SP_ENTITY_ID, SP_ENTITY_ID);
    props.setProperty(SamlSso.SP_ACS_URL, ACS_URL);
    props.setProperty(SamlSso.SP_ACS_BINDING, "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
    props.setProperty(SamlSso.SP_SLS_URL, SLS_URL);
    props.setProperty(
        SamlSso.SP_NAMEID_FORMAT, "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
    props.setProperty(SamlSso.SP_CERT, certificatePem(spCert));
    props.setProperty(SamlSso.SP_KEY, pkcs8Pem(spKeys));
    props.setProperty(SamlSso.IDP_ENTITY_ID, IDP_ENTITY_ID);
    props.setProperty(SamlSso.IDP_SSO_URL, IDP_SSO_URL);
    props.setProperty(
        SamlSso.IDP_SSO_BINDING, "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
    props.setProperty(SamlSso.IDP_SLO_URL, IDP_SLO_URL);
    props.setProperty(
        SamlSso.IDP_SLO_BINDING, "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
    props.setProperty(SamlSso.IDP_CERT, certificatePem(idpCert));
    props.setProperty(SamlSso.AUTHN_REQUEST_SIGNED, "true");
    props.setProperty(SamlSso.LOGOUT_REQUEST_SIGNED, "true");
    props.setProperty(SamlSso.WANT_ASSERTIONS_SIGNED, "true");
    props.setProperty(
        SamlSso.SIGNATURE_ALGORITHM, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
    return props;
  }

  /** Builds a client from {@link #properties()} and makes it the one SamlSso uses */
  public SAML2Client installClient() throws Exception {
    return install(properties());
  }

  /** Builds a client from the given properties and makes it the one SamlSso uses */
  public static SAML2Client install(Properties props) {
    SamlSso.Sso sso = SamlSso.build(props);
    SamlSso.set(sso);
    LoginMethod.setSaml(true);
    return sso.client;
  }

  /** Clears the client installed by {@link #installClient()} */
  public static void uninstallClient() {
    SamlSso.set(null);
    LoginMethod.setSaml(null);
  }

  /** Switches SSO (sso.enabled) on or off without touching the installed client */
  public static void setSsoEnabled(boolean enabled) {
    LoginMethod.setSaml(enabled);
  }

  /** When true, {@link #signedResponse} builds an unsolicited response (no InResponseTo) */
  public boolean unsolicited;

  private String pendingRequestId;
  private Cookie browserCookie;

  /**
   * Starts an SP-initiated login as a browser would. The next {@link #signedResponse} answers it.
   *
   * @return the browser's request-binding cookie, which the ACS request must carry
   */
  public Cookie startLogin() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/saml/dologin");
    request.setScheme("https");
    request.setSecure(true);
    request.setServerName("shepherd.test");
    request.setServerPort(443);
    MockHttpServletResponse response = new MockHttpServletResponse();
    SamlSso.login(request, response);
    Matcher id =
        Pattern.compile(" ID=\"([^\"]+)\"")
            .matcher(decodeRedirectRequest(response.getRedirectedUrl()));
    if (!id.find()) {
      throw new IllegalStateException("No ID in AuthnRequest");
    }
    pendingRequestId = id.group(1);
    browserCookie = response.getCookie(SamlRequestStore.COOKIE);
    return browserCookie;
  }

  /** The binding cookie of the browser that started the most recent login, if any */
  public Cookie browserCookie() {
    return browserCookie;
  }

  /**
   * Builds a base64 SAMLResponse for the HTTP-POST binding, with the assertion signed by the given
   * key pair.
   */
  public String signedResponse(
      String nameId,
      Map<String, List<String>> attributes,
      KeyPair signingKeys,
      X509Certificate signingCert)
      throws Exception {
    return signedResponse(nameId, null, null, attributes, signingKeys, signingCert);
  }

  /** As {@link #signedResponse}, with NameQualifier / SPNameQualifier on the NameID */
  public String signedResponse(
      String nameId,
      String nameQualifier,
      String spNameQualifier,
      Map<String, List<String>> attributes,
      KeyPair signingKeys,
      X509Certificate signingCert)
      throws Exception {
    Instant now = Instant.now();

    Response response = build(Response.DEFAULT_ELEMENT_NAME);
    response.setID("_" + UUID.randomUUID());
    response.setIssueInstant(now);
    response.setVersion(SAMLVersion.VERSION_20);
    response.setDestination(ACS_URL);
    String inResponseTo = null;
    if (!unsolicited) {
      if (pendingRequestId == null) {
        startLogin();
      }
      inResponseTo = pendingRequestId;
      pendingRequestId = null;
      response.setInResponseTo(inResponseTo);
    }
    response.setIssuer(issuer());
    Status status = build(Status.DEFAULT_ELEMENT_NAME);
    StatusCode statusCode = build(StatusCode.DEFAULT_ELEMENT_NAME);
    statusCode.setValue(StatusCode.SUCCESS);
    status.setStatusCode(statusCode);
    response.setStatus(status);

    Assertion assertion = build(Assertion.DEFAULT_ELEMENT_NAME);
    assertion.setID("_" + UUID.randomUUID());
    assertion.setIssueInstant(now);
    assertion.setVersion(SAMLVersion.VERSION_20);
    assertion.setIssuer(issuer());

    Subject subject = build(Subject.DEFAULT_ELEMENT_NAME);
    NameID name = build(NameID.DEFAULT_ELEMENT_NAME);
    name.setValue(nameId);
    name.setFormat(NameIDType.PERSISTENT);
    name.setNameQualifier(nameQualifier);
    name.setSPNameQualifier(spNameQualifier);
    subject.setNameID(name);
    SubjectConfirmation confirmation = build(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
    confirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
    SubjectConfirmationData confirmationData = build(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
    confirmationData.setRecipient(ACS_URL);
    confirmationData.setInResponseTo(inResponseTo);
    confirmationData.setNotOnOrAfter(now.plus(Duration.ofMinutes(5)));
    confirmation.setSubjectConfirmationData(confirmationData);
    subject.getSubjectConfirmations().add(confirmation);
    assertion.setSubject(subject);

    Conditions conditions = build(Conditions.DEFAULT_ELEMENT_NAME);
    conditions.setNotBefore(now.minus(Duration.ofMinutes(1)));
    conditions.setNotOnOrAfter(now.plus(Duration.ofMinutes(5)));
    AudienceRestriction audienceRestriction = build(AudienceRestriction.DEFAULT_ELEMENT_NAME);
    Audience audience = build(Audience.DEFAULT_ELEMENT_NAME);
    audience.setURI(SP_ENTITY_ID);
    audienceRestriction.getAudiences().add(audience);
    conditions.getAudienceRestrictions().add(audienceRestriction);
    assertion.setConditions(conditions);

    AuthnStatement authn = build(AuthnStatement.DEFAULT_ELEMENT_NAME);
    authn.setAuthnInstant(now);
    authn.setSessionIndex("_session-" + nameId);
    AuthnContext authnContext = build(AuthnContext.DEFAULT_ELEMENT_NAME);
    AuthnContextClassRef classRef = build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
    classRef.setURI(authnContextClassRef);
    authnContext.setAuthnContextClassRef(classRef);
    authn.setAuthnContext(authnContext);
    assertion.getAuthnStatements().add(authn);

    AttributeStatement attributeStatement = build(AttributeStatement.DEFAULT_ELEMENT_NAME);
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      Attribute attribute = build(Attribute.DEFAULT_ELEMENT_NAME);
      attribute.setName(entry.getKey());
      for (String value : entry.getValue()) {
        XSString xsValue =
            (XSString)
                XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(XSString.TYPE_NAME)
                    .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        xsValue.setValue(value);
        attribute.getAttributeValues().add(xsValue);
      }
      attributeStatement.getAttributes().add(attribute);
    }
    assertion.getAttributeStatements().add(attributeStatement);

    Signature signature = build(Signature.DEFAULT_ELEMENT_NAME);
    signature.setSigningCredential(new BasicX509Credential(signingCert, signingKeys.getPrivate()));
    signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
    signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
    assertion.setSignature(signature);
    response.getAssertions().add(assertion);

    XMLObjectProviderRegistrySupport.getMarshallerFactory()
        .getMarshaller(response)
        .marshall(response);
    Signer.signObject(signature);

    String xml = SerializeSupport.nodeToString(response.getDOM());
    return Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));
  }

  /** An IdP LogoutResponse to an SP-initiated logout */
  public LogoutResponse logoutResponse() {
    LogoutResponse response = build(LogoutResponse.DEFAULT_ELEMENT_NAME);
    response.setID("_" + UUID.randomUUID());
    response.setIssueInstant(Instant.now());
    response.setVersion(SAMLVersion.VERSION_20);
    response.setDestination(SLS_URL);
    response.setIssuer(issuer());
    Status status = build(Status.DEFAULT_ELEMENT_NAME);
    StatusCode statusCode = build(StatusCode.DEFAULT_ELEMENT_NAME);
    statusCode.setValue(StatusCode.SUCCESS);
    status.setStatusCode(statusCode);
    response.setStatus(status);
    return response;
  }

  /** An IdP-initiated LogoutRequest naming several logins of one user */
  public LogoutRequest logoutRequest(String nameId, List<String> sessionIndexes) {
    LogoutRequest request = logoutRequest(nameId, (String) null);
    for (String value : sessionIndexes) {
      SessionIndex index = build(SessionIndex.DEFAULT_ELEMENT_NAME);
      index.setValue(value);
      request.getSessionIndexes().add(index);
    }
    return request;
  }

  /** An IdP-initiated LogoutRequest for a login */
  public LogoutRequest logoutRequest(String nameId, String sessionIndex) {
    LogoutRequest request = build(LogoutRequest.DEFAULT_ELEMENT_NAME);
    request.setID("_" + UUID.randomUUID());
    request.setIssueInstant(Instant.now());
    request.setVersion(SAMLVersion.VERSION_20);
    request.setDestination(SLS_URL);
    request.setIssuer(issuer());
    NameID name = build(NameID.DEFAULT_ELEMENT_NAME);
    name.setValue(nameId);
    name.setFormat(NameIDType.PERSISTENT);
    request.setNameID(name);
    if (sessionIndex != null) {
      SessionIndex index = build(SessionIndex.DEFAULT_ELEMENT_NAME);
      index.setValue(sessionIndex);
      request.getSessionIndexes().add(index);
    }
    return request;
  }

  /** Serialises a SAML message, with an enveloped XML signature when signing keys are given */
  public static String toXml(
      SignableSAMLObject message, KeyPair signingKeys, X509Certificate signingCert)
      throws Exception {
    Signature signature = null;
    if (signingKeys != null) {
      signature = build(Signature.DEFAULT_ELEMENT_NAME);
      signature.setSigningCredential(
          new BasicX509Credential(signingCert, signingKeys.getPrivate()));
      signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
      signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
      message.setSignature(signature);
    }
    XMLObjectProviderRegistrySupport.getMarshallerFactory()
        .getMarshaller(message)
        .marshall(message);
    if (signature != null) {
      Signer.signObject(signature);
    }
    return SerializeSupport.nodeToString(message.getDOM());
  }

  /**
   * Builds an HTTP-Redirect binding query string carrying a SAML message, with a query-string
   * signature when signing keys are given.
   */
  public static String redirectQuery(String param, String xml, KeyPair signingKeys)
      throws Exception {
    return redirectQuery(
        param, xml, signingKeys, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, "SHA256withRSA");
  }

  /** As {@link #redirectQuery(String, String, KeyPair)}, with a chosen signature algorithm */
  public static String redirectQuery(
      String param, String xml, KeyPair signingKeys, String sigAlgUri, String jcaAlgorithm)
      throws Exception {
    Deflater deflater = new Deflater(Deflater.DEFLATED, true);
    deflater.setInput(xml.getBytes(StandardCharsets.UTF_8));
    deflater.finish();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    while (!deflater.finished()) {
      out.write(buffer, 0, deflater.deflate(buffer));
    }
    String query =
        param
            + "="
            + URLEncoder.encode(
                Base64.getEncoder().encodeToString(out.toByteArray()), StandardCharsets.UTF_8);
    if (signingKeys == null) {
      return query;
    }
    query += "&SigAlg=" + URLEncoder.encode(sigAlgUri, StandardCharsets.UTF_8);
    java.security.Signature signer = java.security.Signature.getInstance(jcaAlgorithm);
    signer.initSign(signingKeys.getPrivate());
    signer.update(query.getBytes(StandardCharsets.UTF_8));
    return query
        + "&Signature="
        + URLEncoder.encode(
            Base64.getEncoder().encodeToString(signer.sign()), StandardCharsets.UTF_8);
  }

  /**
   * A request to the single logout service carrying the given HTTP-Redirect query string. Like a
   * servlet container, parameter names and values are both URL-decoded, in order.
   */
  public static MockHttpServletRequest slsGet(String query) {
    MockHttpServletRequest request = slsRequest("GET");
    request.setQueryString(query);
    for (String pair : query.split("&")) {
      int eq = pair.indexOf('=');
      request.addParameter(
          URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
          URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
    }
    return request;
  }

  /** A request to the single logout service carrying an HTTP-POST binding message */
  public static MockHttpServletRequest slsPost(String param, String xml) {
    MockHttpServletRequest request = slsRequest("POST");
    request.addParameter(
        param, Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)));
    return request;
  }

  private static MockHttpServletRequest slsRequest(String method) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, "/saml/sls");
    request.setScheme("https");
    request.setSecure(true);
    request.setServerName("shepherd.test");
    request.setServerPort(443);
    return request;
  }

  /** Decodes the SAMLRequest parameter of an HTTP-Redirect binding URL */
  public static String decodeRedirectRequest(String location) throws Exception {
    String query = location.substring(location.indexOf('?') + 1);
    for (String param : query.split("&")) {
      if (param.startsWith("SAMLRequest=")) {
        byte[] deflated =
            Base64.getDecoder()
                .decode(URLDecoder.decode(param.substring(12), StandardCharsets.UTF_8));
        Inflater inflater = new Inflater(true);
        inflater.setInput(deflated);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (!inflater.finished()) {
          out.write(buffer, 0, inflater.inflate(buffer));
        }
        return out.toString(StandardCharsets.UTF_8);
      }
    }
    throw new IllegalArgumentException("No SAMLRequest in " + location);
  }

  /** An EC P-256 key pair, as used by IdPs that sign with ecdsa-sha256 */
  public static KeyPair newEcKeyPair() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
    generator.initialize(256);
    return generator.generateKeyPair();
  }

  public static KeyPair newKeyPair() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    return generator.generateKeyPair();
  }

  public static X509Certificate selfSigned(KeyPair keys, String commonName) throws Exception {
    X500Name name = new X500Name("CN=" + commonName);
    Instant now = Instant.now();
    JcaX509v3CertificateBuilder builder =
        new JcaX509v3CertificateBuilder(
            name,
            BigInteger.valueOf(now.toEpochMilli()),
            Date.from(now.minus(Duration.ofDays(1))),
            Date.from(now.plus(Duration.ofDays(365))),
            name,
            keys.getPublic());
    return new JcaX509CertificateConverter()
        .getCertificate(
            builder.build(
                new JcaContentSignerBuilder(
                        "EC".equals(keys.getPrivate().getAlgorithm())
                            ? "SHA256withECDSA"
                            : "SHA256withRSA")
                    .build(keys.getPrivate())));
  }

  public static String certificatePem(X509Certificate cert) throws Exception {
    return pem("CERTIFICATE", cert.getEncoded());
  }

  public static String pkcs8Pem(KeyPair keys) {
    return pem("PRIVATE KEY", keys.getPrivate().getEncoded());
  }

  public static String pkcs1Pem(KeyPair keys) throws Exception {
    byte[] pkcs1 =
        PrivateKeyInfo.getInstance(keys.getPrivate().getEncoded())
            .parsePrivateKey()
            .toASN1Primitive()
            .getEncoded();
    return pem("RSA PRIVATE KEY", pkcs1);
  }

  private static String pem(String type, byte[] der) {
    return "-----BEGIN "
        + type
        + "-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der)
        + "\n-----END "
        + type
        + "-----";
  }

  private static Issuer issuer() {
    Issuer issuer = build(Issuer.DEFAULT_ELEMENT_NAME);
    issuer.setValue(IDP_ENTITY_ID);
    return issuer;
  }

  @SuppressWarnings("unchecked")
  private static <T extends XMLObject> T build(QName name) {
    return (T)
        XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(name).buildObject(name);
  }
}
