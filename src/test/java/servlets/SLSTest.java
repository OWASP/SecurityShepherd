package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import utils.SamlTestSupport;

public class SLSTest {

  @BeforeAll
  public static void installClient() throws Exception {
    new SamlTestSupport().installClient();
  }

  @AfterAll
  public static void uninstallClient() {
    SamlTestSupport.uninstallClient();
  }

  /** The SLS used to only define handleRequest, so the IdP's redirect got a 405 */
  @Test
  public void doGet_IsHandled() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    new SLS().doGet(new MockHttpServletRequest("GET", "/saml/sls"), response);

    assertEquals(302, response.getStatus());
    assertEquals("../login.jsp", response.getRedirectedUrl());
  }

  @Test
  public void doPost_IsHandled() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    new SLS().doPost(new MockHttpServletRequest("POST", "/saml/sls"), response);

    assertEquals(302, response.getStatus());
    assertEquals("../login.jsp", response.getRedirectedUrl());
  }

  @Test
  public void invalidLogoutMessage_KeepsSession() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/saml/sls");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("userName", "jdoe");
    request.setSession(session);

    new SLS().doGet(request, new MockHttpServletResponse());

    assertNotNull(session.getAttribute("userName"));
  }
}
