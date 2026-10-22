package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.SamlSso;
import utils.ShepherdLogManager;

/**
 * Control class for the SSO logout operation <br>
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
public class SLS extends HttpServlet {

  private static final long serialVersionUID = -5824919455464886874L;
  private static final Logger log = LogManager.getLogger(SLS.class);

  /** IdP logout messages arrive here over the HTTP-Redirect binding */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    handleRequest(request, response);
  }

  /** IdP logout messages arrive here over the HTTP-POST binding */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    handleRequest(request, response);
  }

  /**
   * Single logout service. Handles the IdP's LogoutResponse to an SP-initiated logout (see Logout),
   * and IdP-initiated LogoutRequests, answering them with a LogoutResponse. The SAML message is
   * validated (including its IdP signature when want_messages_signed is set), so no CSRF token is
   * needed: the request originates from the IdP, not from a Security Shepherd page.
   *
   * <p>Sessions are ended by SamlSso for the login an IdP LogoutRequest names, not by whichever
   * session the delivering browser happens to have. A LogoutResponse ends nothing: Logout already
   * ended the local session before sending the user to the IdP.
   */
  public void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Setting IpAddress To Log and taking header for original IP if forwarded from
    // proxy
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.SLS ***");
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");

    SamlSso.LogoutMessage message = SamlSso.processLogoutMessage(request, response);
    log.debug("SAML logout message: " + message);

    // For an IdP LogoutRequest the LogoutResponse back to the IdP has already been written
    if (message != SamlSso.LogoutMessage.IDP_LOGOUT_REQUEST) {
      response.sendRedirect("../login.jsp");
    }
    log.debug("*** END SLS ***");
  }
}
