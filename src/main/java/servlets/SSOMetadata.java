package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.settings.Saml2Settings;

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
public class SSOMetadata extends HttpServlet {

	private static final long serialVersionUID = 3679066677850981985L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SSOMetadata.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * Redirects user to index.jsp
		 */

		response.sendRedirect("index.jsp");

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));

		try {
			Auth auth = new Auth();

			Saml2Settings settings = auth.getSettings();
			settings.setSPValidationOnly(true);
			String metadata = settings.getSPMetadata();
			List<String> errors = Saml2Settings.validateMetadata(metadata);

			PrintWriter out = response.getWriter();
			
			if (errors.isEmpty()) {
				response.setContentType("application/xml; charset=UTF-8");

				out.println(metadata);
			} else {
				response.setContentType("text/html; charset=UTF-8");

				for (String error : errors) {
					out.println("<p>" + error + "</p>");
				}
			}
		} catch (Exception e) {
			log.error("Caught exception when initializing SSO: " + e.toString());
			throw new RuntimeException(e);
		}
	}
}
