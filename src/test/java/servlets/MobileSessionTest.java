package servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import utils.UserKicker;

class MobileSessionTest {

  private static final String USER = "mobileSessionTestUser";

  @AfterEach
  void clearKickList() {
    UserKicker.removeFromKicklist(USER);
  }

  private static HttpSession session(String role, String userName, String userStamp) {
    HttpSession ses = mock(HttpSession.class);
    when(ses.getAttribute("userRole")).thenReturn(role);
    when(ses.getAttribute("userName")).thenReturn(userName);
    when(ses.getAttribute("userStamp")).thenReturn(userStamp);
    return ses;
  }

  @Test
  void validPlayerSession_isAuthenticated() {
    assertTrue(MobileSession.isAuthenticated(session("player", USER, "stamp")));
  }

  @Test
  void validAdminSession_isAuthenticated() {
    assertTrue(MobileSession.isAuthenticated(session("admin", USER, "stamp")));
  }

  @Test
  void nullSession_isRejected() {
    assertFalse(MobileSession.isAuthenticated(null));
  }

  @Test
  void missingUserName_isRejected() {
    assertFalse(MobileSession.isAuthenticated(session("player", null, "stamp")));
  }

  @Test
  void missingUserStamp_isRejected() {
    assertFalse(MobileSession.isAuthenticated(session("player", USER, null)));
  }

  @Test
  void missingRole_isRejected() {
    assertFalse(MobileSession.isAuthenticated(session(null, USER, "stamp")));
  }

  @Test
  void tamperedRole_isRejected() {
    assertFalse(MobileSession.isAuthenticated(session("superuser", USER, "stamp")));
  }

  @Test
  void loggedOutSession_isRejectedAndInvalidated() {
    HttpSession ses = session("player", USER, "stamp");
    when(ses.getAttribute("logout")).thenReturn("true");
    assertFalse(MobileSession.isAuthenticated(ses));
    verify(ses).invalidate();
  }

  @Test
  void suspendedUser_isRejectedAndSessionInvalidated() {
    HttpSession ses = session("player", USER, "stamp");
    UserKicker.addUserToKickList(USER);
    assertFalse(MobileSession.isAuthenticated(ses));
    verify(ses).invalidate();
  }
}
