package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pac4j.saml.profile.SAML2Profile;
import utils.LoginMethod;
import utils.SamlSso;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class for the logout operation <br>
 * <br>
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
 *
 * @author Mark Denihan
 */
public class Logout extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(Logout.class);

  /**
   * Initiated in index.jsp. Invalidates session and Security Shepherd tokens are removed. The user
   * is logged out.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Setting IpAddress To Log and taking header for original IP if forwarded from
    // proxy
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.Logout ***");
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    HttpSession ses = request.getSession(true);
    if (Validate.validateSession(ses)) {
      ShepherdLogManager.setRequestIp(
          request.getRemoteAddr(),
          request.getHeader("X-Forwarded-For"),
          ses.getAttribute("userName").toString());
      log.debug("Current User: " + ses.getAttribute("userName").toString());
      Cookie tokenCookie = Validate.getToken(request.getCookies());
      Object tokenParmeter = request.getParameter("csrfToken");
      if (Validate.validateTokens(tokenCookie, tokenParmeter)) {

        // Read before the session is invalidated: single logout needs the NameID and
        // session index captured at login
        Object samlProfile = ses.getAttribute(SamlSso.PROFILE_SESSION_ATTRIBUTE);

        // Remove Everything
        ses.removeAttribute("userStamp");
        ses.removeAttribute("userName");
        ses.removeAttribute("userRole");

        // Invalidate Session on server
        ses.invalidate();
        ses = request.getSession(true);

        // Remove cookie
        Cookie emptyCookie = new Cookie("token", "");
        emptyCookie.setPath("/");
        response.addCookie(emptyCookie);
        log.debug("User Logged Out");

        if (LoginMethod.isSaml() && samlProfile instanceof SAML2Profile) {
          if (!SamlSso.logout(request, response, (SAML2Profile) samlProfile)) {
            log.debug("No IdP single logout URL configured, ending local session only");
            response.sendRedirect("login.jsp");
          }
        } else {
          response.sendRedirect("login.jsp");
        }

      } else {
        log.error("CSRF Attack Detected");
        response.sendRedirect("index.jsp");
      }
    } else {
      log.error("Logout Function Called with no valid session");
      response.sendRedirect("login.jsp");
    }
    log.debug("*** END Logout ***");
  }
}
