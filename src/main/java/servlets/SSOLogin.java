package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import utils.ShepherdLogManager;

/**
 * Does SSO login. <br/>
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
public class SSOLogin extends HttpServlet {

	private static final long serialVersionUID = 1488140446224058032L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SSOLogin.class);

	/**
	 * Redirects user to index.jsp
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.sendRedirect("index.jsp");

	}


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("**** servlets.SSOLogin ***");
		request.setCharacterEncoding("UTF-8");
		Auth auth;
		try {
			auth = new Auth(request, response);
			auth.login();
		} catch (SettingsException | Error e) {
			log.error("Caught exception when initializing SSO: " + e.toString());
			throw new RuntimeException(e);
		}

		log.debug("**** End servlets.SSOLogin ***");	}
}
