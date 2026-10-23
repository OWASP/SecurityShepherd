package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Does SSO login. <br>
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
public class SSOMetadata extends HttpServlet {

  private static final long serialVersionUID = 3679066677850981985L;
  private static final Logger log = LogManager.getLogger(SSOMetadata.class);

  /**
   * SAML SSO is unavailable at this step of the Jakarta migration (#861): java-saml has no Jakarta
   * release, and the pac4j-based replacement follows in the next pull request of the stack.
   */
  private void unavailable(HttpServletResponse response) throws IOException {
    log.error("SAML SSO is unavailable during the Jakarta migration (#861)");
    response.sendError(
        HttpServletResponse.SC_SERVICE_UNAVAILABLE, "SAML SSO is unavailable during migration");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    unavailable(response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    unavailable(response);
  }
}
