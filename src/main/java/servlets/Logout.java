package servlets;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.LoginMethod;
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

        if (LoginMethod.isSaml()) {

          Auth auth;
          try {
            auth = new Auth(request, response);
          } catch (SettingsException e) {
            throw new RuntimeException("SAML not configured: " + e.toString());
          } catch (Error e) {
            throw new RuntimeException("SAML error : " + e.toString());
          }

          String nameId = null;
          if (ses.getAttribute("nameId") != null) {
            nameId = ses.getAttribute("nameId").toString();
          }
          String nameIdFormat = null;
          if (ses.getAttribute("nameIdFormat") != null) {
            nameIdFormat = ses.getAttribute("nameIdFormat").toString();
          }
          String nameidNameQualifier = null;
          if (ses.getAttribute("nameidNameQualifier") != null) {
            nameIdFormat = ses.getAttribute("nameidNameQualifier").toString();
          }
          String nameidSPNameQualifier = null;
          if (ses.getAttribute("nameidSPNameQualifier") != null) {
            nameidSPNameQualifier = ses.getAttribute("nameidSPNameQualifier").toString();
          }
          String sessionIndex = null;
          if (ses.getAttribute("sessionIndex") != null) {
            sessionIndex = ses.getAttribute("sessionIndex").toString();
          }
          try {
            auth.logout(
                null,
                nameId,
                sessionIndex,
                nameIdFormat,
                nameidNameQualifier,
                nameidSPNameQualifier);
          } catch (SettingsException e) {
            throw new RuntimeException("SAML settings error : " + e.toString());
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
