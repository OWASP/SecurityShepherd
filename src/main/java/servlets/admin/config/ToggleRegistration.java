package servlets.admin.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.OpenRegistration;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This control class is responsible for toggling the availability of the Registration functionality
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * @author Mark Denihan
 *
 */
public class ToggleRegistration extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ToggleRegistration.class);
	/**
	 * This method when called by an administrator toggles the availability of the registraion functionality,
	 * This is actually performed by the OpenRegistration.toggle() method.
	 * @param csrfToken 
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.config.ToggleRegistration ***");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		Cookie tokenCookie = Validate.getToken(request.getCookies());
		Object tokenParmeter = request.getParameter("csrfToken");
		if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					OpenRegistration.toggle();
					String htmlOutput = new String();
					if(OpenRegistration.isDisabled())
						htmlOutput = "<h3 class='title'>Registration Closed</h3><p>The application Registration has been disabled. Users will not be able to register new accounts.</p>";
					else
						htmlOutput = "<h3 class='title'>Registration Opened</h3><p>The application Registration has been enabled. Users will now be able to register new accounts.</p>";
					out.write(htmlOutput);
				}
				catch (Exception e)
				{
					log.error("Registration Availability Update Error: " + e.toString());
					out.print("<h3 class=\"title\">Registration Availability Update Failure</h3><br>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h3 class=\"title\">Registration Availability Update Failure</h3><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! CSRF Tokens did not match.</font>" +
					"<p>");
			}
		}
		else
		{
			out.print("<h3 class=\"title\">Registration Availability Update Failure</h3><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please log in or try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** servlets.Admin.ToggleRegistration END ***");
	}
}
