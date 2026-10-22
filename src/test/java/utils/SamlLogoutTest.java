package utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

/** Single logout service: which logout messages are trusted, and which sessions they end */
public class SamlLogoutTest {

  private static SamlTestSupport saml;
  private static KeyPair attackerKeys;
  private static X509Certificate attackerCert;

  private MockHttpSession loggedIn;
  private MockHttpSession otherUser;

  @BeforeAll
  public static void generateKeys() throws Exception {
    saml = new SamlTestSupport();
    attackerKeys = SamlTestSupport.newKeyPair();
    attackerCert = SamlTestSupport.selfSigned(attackerKeys, "idp.test");
  }

  /** Installs a client that requires signed logout messages, as the sample configuration does */
  @BeforeEach
  public void setUp() throws Exception {
    SamlTestSupport.install(signedLogoutProperties(true));
    loggedIn = new MockHttpSession();
    SamlSessionRegistry.register(loggedIn, "user-123", "_session-1");
    otherUser = new MockHttpSession();
    SamlSessionRegistry.register(otherUser, "user-456", "_session-2");
  }

  @AfterEach
  public void tearDown() {
    for (MockHttpSession session : new MockHttpSession[] {loggedIn, otherUser}) {
      if (!session.isInvalid()) {
        session.invalidate();
      }
    }
    SamlTestSupport.uninstallClient();
  }

  private static Properties signedLogoutProperties(boolean wantMessagesSigned) throws Exception {
    Properties props = saml.properties();
    props.setProperty(SamlSso.WANT_MESSAGES_SIGNED, Boolean.toString(wantMessagesSigned));
    return props;
  }

  private static SamlSso.LogoutMessage process(MockHttpServletRequest request) {
    return SamlSso.processLogoutMessage(request, new MockHttpServletResponse());
  }

  private String logoutResponseXml() throws Exception {
    return SamlTestSupport.toXml(saml.logoutResponse(), null, null);
  }

  // LogoutResponse (end of an SP-initiated logout)

  @Test
  public void redirectLogoutResponse_SignedByIdp_IsAccepted() throws Exception {
    String query = SamlTestSupport.redirectQuery("SAMLResponse", logoutResponseXml(), saml.idpKeys);

    assertEquals(SamlSso.LogoutMessage.LOGOUT_RESPONSE, process(SamlTestSupport.slsGet(query)));
  }

  @Test
  public void redirectLogoutResponse_Unsigned_IsRejected() throws Exception {
    String query = SamlTestSupport.redirectQuery("SAMLResponse", logoutResponseXml(), null);

    assertEquals(SamlSso.LogoutMessage.INVALID, process(SamlTestSupport.slsGet(query)));
  }

  @Test
  public void redirectLogoutResponse_SignedByUnknownKey_IsRejected() throws Exception {
    String query = SamlTestSupport.redirectQuery("SAMLResponse", logoutResponseXml(), attackerKeys);

    assertEquals(SamlSso.LogoutMessage.INVALID, process(SamlTestSupport.slsGet(query)));
  }

  @Test
  public void redirectLogoutResponse_MessageSwappedAfterSigning_IsRejected() throws Exception {
    String signed =
        SamlTestSupport.redirectQuery("SAMLResponse", logoutResponseXml(), saml.idpKeys);
    String otherMessage = SamlTestSupport.redirectQuery("SAMLResponse", logoutResponseXml(), null);
    String tampered = otherMessage + signed.substring(signed.indexOf("&SigAlg="));

    assertEquals(SamlSso.LogoutMessage.INVALID, process(SamlTestSupport.slsGet(tampered)));
  }

  @Test
  public void postLogoutResponse_XmlSignedByIdp_IsAccepted() throws Exception {
    String xml = SamlTestSupport.toXml(saml.logoutResponse(), saml.idpKeys, saml.idpCert);

    assertEquals(
        SamlSso.LogoutMessage.LOGOUT_RESPONSE,
        process(SamlTestSupport.slsPost("SAMLResponse", xml)));
  }

  @Test
  public void postLogoutResponse_Unsigned_IsRejected() throws Exception {
    assertEquals(
        SamlSso.LogoutMessage.INVALID,
        process(SamlTestSupport.slsPost("SAMLResponse", logoutResponseXml())));
  }

  @Test
  public void postLogoutResponse_XmlSignedByUnknownKey_IsRejected() throws Exception {
    String xml = SamlTestSupport.toXml(saml.logoutResponse(), attackerKeys, attackerCert);

    assertEquals(
        SamlSso.LogoutMessage.INVALID, process(SamlTestSupport.slsPost("SAMLResponse", xml)));
  }

  @Test
  public void logoutResponse_EndsNoSession() throws Exception {
    String query = SamlTestSupport.redirectQuery("SAMLResponse", logoutResponseXml(), saml.idpKeys);
    MockHttpServletRequest request = SamlTestSupport.slsGet(query);
    request.setSession(loggedIn);

    process(request);

    assertFalse(loggedIn.isInvalid());
    assertFalse(otherUser.isInvalid());
  }

  @Test
  public void unsignedLogoutResponse_AcceptedWhenSignaturesNotRequired() throws Exception {
    SamlTestSupport.install(signedLogoutProperties(false));
    String query = SamlTestSupport.redirectQuery("SAMLResponse", logoutResponseXml(), null);

    assertEquals(SamlSso.LogoutMessage.LOGOUT_RESPONSE, process(SamlTestSupport.slsGet(query)));
  }

  // IdP-initiated LogoutRequest

  private String logoutRequestQuery(String nameId, String sessionIndex, KeyPair keys)
      throws Exception {
    String xml = SamlTestSupport.toXml(saml.logoutRequest(nameId, sessionIndex), null, null);
    return SamlTestSupport.redirectQuery("SAMLRequest", xml, keys);
  }

  @Test
  public void idpLogoutRequest_EndsOnlyTheNamedSession_RegardlessOfBrowserSession()
      throws Exception {
    MockHttpServletRequest request =
        SamlTestSupport.slsGet(logoutRequestQuery("user-123", "_session-1", saml.idpKeys));
    // Delivered through a different user's browser
    request.setSession(otherUser);
    MockHttpServletResponse response = new MockHttpServletResponse();

    SamlSso.LogoutMessage result = SamlSso.processLogoutMessage(request, response);

    assertEquals(SamlSso.LogoutMessage.IDP_LOGOUT_REQUEST, result);
    assertTrue(loggedIn.isInvalid());
    assertFalse(otherUser.isInvalid());
    assertThat(response.getRedirectedUrl(), startsWith(SamlTestSupport.IDP_SLO_URL + "?"));
    assertThat(response.getRedirectedUrl(), containsString("SAMLResponse="));
  }

  @Test
  public void idpLogoutRequest_WithoutSessionCookie_StillEndsTheNamedSession() throws Exception {
    MockHttpServletRequest request =
        SamlTestSupport.slsGet(logoutRequestQuery("user-123", "_session-1", saml.idpKeys));

    assertEquals(SamlSso.LogoutMessage.IDP_LOGOUT_REQUEST, process(request));
    assertTrue(loggedIn.isInvalid());
  }

  @Test
  public void idpLogoutRequest_DifferentSessionIndex_EndsNothing() throws Exception {
    MockHttpServletRequest request =
        SamlTestSupport.slsGet(logoutRequestQuery("user-123", "_other-login", saml.idpKeys));
    request.setSession(loggedIn);

    process(request);

    assertFalse(loggedIn.isInvalid());
  }

  @Test
  public void idpLogoutRequest_Unsigned_IsRejectedAndEndsNothing() throws Exception {
    MockHttpServletRequest request =
        SamlTestSupport.slsGet(logoutRequestQuery("user-123", "_session-1", null));

    assertEquals(SamlSso.LogoutMessage.INVALID, process(request));
    assertFalse(loggedIn.isInvalid());
  }

  @Test
  public void idpLogoutRequest_SignedByUnknownKey_IsRejectedAndEndsNothing() throws Exception {
    MockHttpServletRequest request =
        SamlTestSupport.slsGet(logoutRequestQuery("user-123", "_session-1", attackerKeys));

    assertEquals(SamlSso.LogoutMessage.INVALID, process(request));
    assertFalse(loggedIn.isInvalid());
  }

  @Test
  public void postIdpLogoutRequest_XmlSignedByIdp_EndsTheNamedSession() throws Exception {
    String xml =
        SamlTestSupport.toXml(
            saml.logoutRequest("user-123", "_session-1"), saml.idpKeys, saml.idpCert);

    assertEquals(
        SamlSso.LogoutMessage.IDP_LOGOUT_REQUEST,
        process(SamlTestSupport.slsPost("SAMLRequest", xml)));
    assertTrue(loggedIn.isInvalid());
    assertFalse(otherUser.isInvalid());
  }

  @Test
  public void unsignedIdpLogoutRequestWithoutSessionIndex_EndsNothing() throws Exception {
    // Even when signatures are not required, a bare NameID is too guessable to end sessions
    SamlTestSupport.install(signedLogoutProperties(false));
    MockHttpServletRequest request =
        SamlTestSupport.slsGet(logoutRequestQuery("user-123", null, null));

    process(request);

    assertFalse(loggedIn.isInvalid());
  }

  @Test
  public void endedSession_IsRemovedFromRegistry() {
    int before = SamlSessionRegistry.size();

    loggedIn.invalidate();

    assertEquals(before - 1, SamlSessionRegistry.size());
  }
}
