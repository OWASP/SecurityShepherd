package servlets.admin.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

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
 * This class is responsible for requests to configure the applications core host address. This address is consumed when returning users with modules that are stored on the core sever (XSS and CSRF for example)
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
public class ChangeCoreHostAddress extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangeHostAddress.class);
	
	/**
	 * If this method is called by a valid administrator the new core host address if validated. 
	 * If the host address is valid, it is stored on the application server's utils.VulnerableServer class.
	 * @param hostAddress The new address to store as the core host address
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.config.ChangeCoreHostAddress ***");
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
				boolean hostValidate = false;
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String hostAddress = (String)request.getParameter("hostAddress");
					log.debug("hostAddress = " + hostAddress);
					
					String protocol = new String();
					String host = new String();
					String port = new String();
					
					boolean badHostUrl = !Validate.validHostUrl(hostAddress);
					
					//Validation
					try
					{
						URL newHost = new URL(hostAddress);
						log.debug("Url Protocol: " + newHost.getProtocol());
						log.debug("Url Host: " + newHost.getHost());
						log.debug("Url Port: " + newHost.getPort());
						protocol = newHost.getProtocol().toLowerCase();
						host = newHost.getHost();
						port = new Integer(newHost.getPort()).toString();
						if(!(protocol.equals("https") || protocol.equals("http")))
						{
							log.debug("Incorrect protocol submitted: " + protocol);
							protocol = new String();
						}
					}
					catch(Exception e)
					{
						log.error("Invalid Host Address: " + e.toString());
					}
					
					hostValidate = !badHostUrl && !(protocol.isEmpty() && port.isEmpty() && host.isEmpty());
	
					if( hostValidate)
					{
						ExposedServer.setSecureHost(host);
						ExposedServer.setSecurePort(port);
						ExposedServer.setSecureUrl(hostAddress);
						out.write("<h2 class='title'>Core Host Address Updated</h2>" +
								"<p>The application's core host address has been updated to the following path...<br/><br/><a href='" +
								encoder.encodeForHTMLAttribute(ExposedServer.getSecureUrl()) +
								"'>" +
								encoder.encodeForHTML(ExposedServer.getSecureUrl()) +
								"</a></p>");
						
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occured: ";
						if(!hostValidate)
						{
							log.error("Invalid Application Address");
							errorMessage += "Invalid Host Address. Please try again";
						}
						else if (!badHostUrl)
						{
							errorMessage += "URL didn't end with a slash! Likely very wrong or you want to customise the platform!";
						}
						out.print("<h2 class=\"title\">Host Address Update Failure</h2><br>" +
								"<p><font color=\"red\">" +
								encoder.encodeForHTML(errorMessage) +
								"</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Core Host Address Update Error: " + e.toString());
					out.print("<h2 class=\"title\">Host Address Update Failure</h2><br>" +
							"<p>" +
							"<font color=\"red\">An error occured! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h2 class=\"title\">Core Host Address Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! CSRF Tokens did not match.</font>" +
					"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Core Host Address Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! Please log in or try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** servlets.Admin.config.ChangeCoreHostAddress END ***");
	}
}
