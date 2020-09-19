package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;

import dbProcs.Setter;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class for the Change Username function <br/>
 * <br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mark Denihan
 *
 */
public class ChangeUsername extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangeUsername.class);

	/**
	 * Initiated by index.jsp, getStarted.jsp. This changes a users username.
	 * 
	 * @param csrfToken
	 * @param newUsername Submitted new username
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Setting IpAddress To Log and taking header for original IP if forwarded from
		// proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.ChangeUsername ***");

		request.setCharacterEncoding("UTF-8");

		HttpSession ses = request.getSession(true);
		if (Validate.validateSession(ses)) {
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
					ses.getAttribute("userName").toString());
			log.debug("Current username: " + ses.getAttribute("userName").toString());
			Cookie tokenCookie = Validate.getToken(request.getCookies());

			Object tokenParmeter = request.getParameter("csrfToken");
			if (Validate.validateTokens(tokenCookie, tokenParmeter)) {
				log.debug("Getting Parameters");
				String userName = (String) ses.getAttribute("userName");
				String newUsername = Encode.forHtml((String) request.getParameter("newUsername"));
				String ApplicationRoot = getServletContext().getRealPath("");
				log.debug("New username: " + newUsername);

				boolean validUsername = false;
				validUsername = newUsername.length() > 3 && newUsername.length() <= 32;
				if (validUsername) {

					log.debug("New username passed validation! Username Change gets the go ahead");
					Setter.updateUsername(ApplicationRoot, userName, newUsername);
					ses.setAttribute("ChangeUsername", "false");
					ses.setAttribute("userName", newUsername);
					log.debug("Username changed");

					response.sendRedirect("index.jsp");

				} else {

					log.error("Invalid Username Submitted (Too Short/Long)");
					ses.setAttribute("errorMessage", "Invalid Username! Please try again.");
					response.sendRedirect("index.jsp");

				}
			} else {
				log.error("CSRF Attack Detected");
			}
		} else {
			log.error("Change Username Function Called with no valid session");
			response.sendRedirect("index.jsp");
		}

		log.debug("*** END ChangeUsername ***");
	}
}
