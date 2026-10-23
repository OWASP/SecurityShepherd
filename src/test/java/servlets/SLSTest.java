package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import utils.SamlTestSupport;

public class SLSTest {

  private static SamlTestSupport saml;

  @BeforeAll
  public static void installClient() throws Exception {
    saml = new SamlTestSupport();
    saml.installClient();
  }

  @AfterAll
  public static void uninstallClient() {
    SamlTestSupport.uninstallClient();
  }

  private static MockHttpSession loggedInSession() {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("userName", "jdoe");
    session.setAttribute("userRole", "player");
    return session;
  }

  private static MockHttpServletRequest logoutResponse() throws Exception {
    return SamlTestSupport.slsGet(
        SamlTestSupport.redirectQuery(
            "SAMLResponse", SamlTestSupport.toXml(saml.logoutResponse(), null, null), null));
  }

  /** The SLS used to only define handleRequest, so the IdP's redirect got a 405 */
  @Test
  public void doGet_WithoutSamlMessage_IsBadRequest() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    new SLS().doGet(new MockHttpServletRequest("GET", "/saml/sls"), response);

    assertEquals(400, response.getStatus());
    assertNull(response.getRedirectedUrl());
  }

  @Test
  public void doPost_WithoutSamlMessage_IsBadRequest() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    new SLS().doPost(new MockHttpServletRequest("POST", "/saml/sls"), response);

    assertEquals(400, response.getStatus());
    assertNull(response.getRedirectedUrl());
  }

  /**
   * A bogus message must not end the session, directly or by redirecting to login.jsp, which
   * invalidates the session it is loaded in
   */
  @Test
  public void invalidLogoutMessage_KeepsSessionAndDoesNotRedirect() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/saml/sls");
    MockHttpSession session = loggedInSession();
    request.setSession(session);
    MockHttpServletResponse response = new MockHttpServletResponse();

    new SLS().doGet(request, response);

    assertFalse(session.isInvalid());
    assertEquals(400, response.getStatus());
    assertNull(response.getRedirectedUrl());
  }

  @Test
  public void logoutResponse_WithoutSession_GoesToLogin() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    new SLS().doGet(logoutResponse(), response);

    assertEquals("../login.jsp", response.getRedirectedUrl());
  }

  /** A LogoutResponse delivered to a still-logged-in browser must not end that session */
  @Test
  public void logoutResponse_InLoggedInBrowser_KeepsSession() throws Exception {
    MockHttpServletRequest request = logoutResponse();
    MockHttpSession session = loggedInSession();
    request.setSession(session);
    MockHttpServletResponse response = new MockHttpServletResponse();

    new SLS().doGet(request, response);

    assertFalse(session.isInvalid());
    assertEquals("../index.jsp", response.getRedirectedUrl());
  }
}
