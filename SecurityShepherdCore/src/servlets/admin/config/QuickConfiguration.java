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

import utils.ConfigurationHelper;
import utils.ExposedServer;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This class is used to set the exposed and core host addresses when prompted by the first time configuration slash
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
public class QuickConfiguration extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(QuickConfiguration.class);
	
	/**
	 * If this method is called by a valid administrator the new host addresses is validated. If the host addresses are both valid, it is stored on the application server's utils.ExposedServer class.
	 * @param hostAddress The new address to store as the host address
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.config.QuickConfig ***");
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
				boolean coreHostValid = false;
				boolean exposedHostValid = false;
				try
				{					
					log.debug("Getting Parameters");
					String coreHostAddress = (String)request.getParameter("coreHostAddress");
					log.debug("coreHostAddress = " + coreHostAddress);
					String exposedHostAdress = (String)request.getParameter("exposedHostAddress");
					log.debug("exposedHoseAdress = " + exposedHostAdress);
					
					String coreProtocol = new String();
					String coreHost = new String();
					String corePort = new String();
					String exposedProtocol = new String();
					String exposedHost = new String();
					String exposedPort = new String(); 
					boolean badCoreHostUrl = !Validate.validHostUrl(coreHostAddress);
					boolean badExposedHostUrl = !Validate.validHostUrl(exposedHostAdress);
					//Validation For Core Host
					try
					{
						URL newHost = new URL(coreHostAddress);
						log.debug("Core Url Protocol: " + newHost.getProtocol().toLowerCase());
						log.debug("Core Url Host: " + newHost.getHost());
						log.debug("Core Url Port: " + newHost.getPort());
						coreProtocol = newHost.getProtocol();
						coreHost = newHost.getHost();
						corePort = new Integer(newHost.getPort()).toString();
						if(!(coreProtocol.equals("https") || coreProtocol.equals("http")))
						{
							log.debug("Incorrect Core protocol submitted: " + coreProtocol);
							coreProtocol = new String();
						}
					}
					catch(Exception e)
					{
						log.error("Invalid Core Host Address: " + e.toString());
					}
					//Validation For Exposed Host
					try
					{
						URL newHost = new URL(exposedHostAdress);
						log.debug("Exposed Url Protocol: " + newHost.getProtocol().toLowerCase());
						log.debug("Exposed Url Host: " + newHost.getHost());
						log.debug("Exposed Url Port: " + newHost.getPort());
						exposedProtocol = newHost.getProtocol();
						exposedHost = newHost.getHost();
						exposedPort = new Integer(newHost.getPort()).toString();
						if(!(exposedProtocol.equals("https") || exposedProtocol.equals("http")))
						{
							log.debug("Incorrect exposed protocol submitted: " + exposedProtocol);
							exposedProtocol = new String();
						}
					}
					catch(Exception e)
					{
						log.error("Invalid Core Host Address: " + e.toString());
					}
					
					coreHostValid = !badCoreHostUrl && !(coreProtocol.isEmpty() && corePort.isEmpty() && coreHost.isEmpty());
					exposedHostValid = !badExposedHostUrl && !(exposedProtocol.isEmpty() && exposedPort.isEmpty() && exposedHost.isEmpty());
					
					
					if(coreHostValid && exposedHostValid)
					{
						ExposedServer.setUrl(exposedHostAdress);
						ExposedServer.setSecureHost(coreHost);
						ExposedServer.setSecurePort(corePort);
						ExposedServer.setSecureUrl(coreHostAddress);
						out.write("<h2 class='title'>Host Addresses Updated</h2>" +
								"<p>The server has been configured! Distribute this link: <a href='" +
								encoder.encodeForHTMLAttribute(coreHostAddress) + "'>" + 
								encoder.encodeForHTML(coreHostAddress) + "</a><br><br>"
								+ "<input id='finishedSetup' onclick='$(\"#configurationWizardDiv\").hide(\"slow\");$(\"html, body\").animate({ scrollTop: 0 }, \"fast\");' "
								+ "type='button' value='Click Here To Hide Config Menu'></p>");
						ConfigurationHelper.setConfiguredFlag(true);
						
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occured: ";
						if(!coreHostValid)
						{
							log.error("Invalid Core Host Address");
							errorMessage += "Invalid Core Host Address. Please try again";
						}
						else if (!badCoreHostUrl)
						{
							errorMessage += "Core Host URL didn't end with a slash! Likely very wrong or you want to customise the platform!  Please try again";
						}
						else if(!exposedHostValid)
						{
							log.error("Invalid Exposed Host Address");
							errorMessage += "Invalid Exposed Host Address. Please try again";
						}
						else if (!badExposedHostUrl)
						{
							errorMessage += "Exposed Host URL didn't end with a slash! Likely very wrong or you want to customise the platform!  Please try again";
						}
						out.print("<p><font color=\"red\">" + encoder.encodeForHTML(errorMessage) + "</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Host Address Update Error: " + e.toString());
					out.print("<h2 class=\"title\">Host Address Update Failure</h2><br>" +
							"<p>" +
							"<font color=\"red\">An error occured! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h2 class=\"title\">Host Address Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! CSRF Tokens did not match.</font>" +
					"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Host Address Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! Please log in or try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** servlets.Admin.config.QuickConfig END ***");
	}
}
