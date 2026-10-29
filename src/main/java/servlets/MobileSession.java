package servlets;

import jakarta.servlet.http.HttpSession;
import utils.Validate;

/**
 * Session check shared by the mobile endpoints. It goes through {@link
 * Validate#validateSession(HttpSession)} so suspended users (via {@code UserKicker}), logged-out
 * sessions and tampered roles are rejected exactly as they are on the web platform.
 */
final class MobileSession {

  private MobileSession() {}

  /**
   * Returns {@code true} only for a live, valid player/admin session that carries the {@code
   * userName} and {@code userStamp} attributes the mobile endpoints rely on.
   *
   * <p>The attribute checks stay here because {@code validateSession} does not reject a session
   * whose {@code userName} is missing. Order matters: {@code validateSession} may invalidate the
   * session, after which its attributes must not be read.
   */
  static boolean isAuthenticated(HttpSession ses) {
    return Validate.validateSession(ses)
        && ses.getAttribute("userName") != null
        && ses.getAttribute("userStamp") != null;
  }
}
