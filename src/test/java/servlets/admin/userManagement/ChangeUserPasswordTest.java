package servlets.admin.userManagement;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;

public class ChangeUserPasswordTest {

  private static final String CSRF_TOKEN = "9730000000000000000";

  /** A request without a player parameter gets the "Invalid Request" response (#973) */
  @Test
  public void doPost_MissingPlayer_RespondsInvalidRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/changeUserPassword");
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("userName", "admin973");
    session.setAttribute("userRole", "admin");
    request.setSession(session);
    request.setCookies(new Cookie("token", CSRF_TOKEN));
    request.addParameter("csrfToken", CSRF_TOKEN);
    request.addParameter("password", "aValidPassword973");
    MockHttpServletResponse response = new MockHttpServletResponse();
    ChangeUserPassword servlet = new ChangeUserPassword();
    servlet.init(new MockServletConfig("ChangeUserPassword"));

    servlet.doPost(request, response);

    String body = response.getContentAsString();
    assertTrue(body.contains("Invalid Request. Please try again"), body);
  }
}
