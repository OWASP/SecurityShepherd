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
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.ConfigurationHelper;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Cancels first time config splash
 * <br/>
 * <br/>
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
public class NoMoreQuickConfig extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(NoMoreQuickConfig.class);
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.config.NoMoreQuickConfig ***");
		Encoder encoder = ESAPI.encoder();
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			log.debug("Current User: " + ses.getAttribute("userName").toString());
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				log.debug("Setting System Configured Flag to True");
				ConfigurationHelper.setConfiguredFlag(true);
				out.write("The first time configuration message won't be bothering you any more.");
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h2 class=\"title\">Host Address Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! CSRF Tokens did not match.</font>" +
					"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Unable to Cancel Config Splash</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please log in or try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** servlets.Admin.config.NoMoreQuickConfig END ***");
	}
}
