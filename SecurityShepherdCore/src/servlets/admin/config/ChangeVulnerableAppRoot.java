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

import utils.ExposedServer;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This class is used to set the vulnerable application servers running context so that the Challnge builder can correctly deploy generated challenges.
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
public class ChangeVulnerableAppRoot extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangeVulnerableAppRoot.class);
	/**
	 * This method stores the newly specified vulnerable application root in the application server, in the utils.VulnerableServer class.
	 * @param vulnerableApplicationRoot The new vulnerable application root
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.config.ChangeVulnerableAppRoot ***");
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
				boolean notNull = false;
				boolean notEmpty = false;
				boolean appValidate = false;
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String newAppRoot = (String)request.getParameter("vulnerableApplicationRoot");
					log.debug("newAppRoot = " + newAppRoot);
					
					
					//Validation
					log.debug("Checking for nulls");
					notNull = (newAppRoot != null);
					log.debug("Ensuring strings are not empty");
					notEmpty = (!newAppRoot.isEmpty());
					log.debug("Validating Name");
					appValidate = newAppRoot.length() > 8 && newAppRoot.length() <= 256;
	
					if(notNull && notEmpty && appValidate)
					{
						ExposedServer.setApplicationRoot(newAppRoot);
						out.write("<h2 class='title'>Vulnerable Application Root Updated</h2>" +
								"<p>The application root has been updated to the following path...<br/><br/><a>" +
								encoder.encodeForHTML(newAppRoot) +
								"</a></p>");
						
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occured: ";
						if(!notNull || !notEmpty)
						{
							log.error("Null values detected");
							errorMessage += "Invalid Request. Please try again";
						}
						else if(!appValidate)
						{
							log.error("Invalid Application root (Too long/too short)");
							errorMessage += "Invalid Class Name. Please try again";
						}
						out.print("<h2 class=\"title\">Vulnerable Application Root Update Failure</h2><br>" +
								"<p><font color=\"red\">" +
								encoder.encodeForHTML(errorMessage) +
								"</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Vulnerable Application Root Update Error: " + e.toString());
					out.print("<h2 class=\"title\">Vulnerable Application Root Update Failure</h2><br>" +
							"<p>" +
							"<font color=\"red\">An error occured! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h2 class=\"title\">Vulnerable Application Root Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! CSRF Tokens did not match.</font>" +
					"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Vulnerable Application Root Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! Please log in or try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** servlets.Admin.config.ChangeVulnerableAppRoot END ***");
	}

}
