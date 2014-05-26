package servlets.admin.moduleManagement;

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
import utils.ModuleBlock;
import dbProcs.Getter;

/**
 * Class to manage interaction of an optional blocking module. This is used to prevent users from getting ahead of themselfs. By deafult this is not enabled
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
public class StopHere extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(StopHere.class);
	/**
	 * Used to set a limit to the progress a player can make in a CTF environment
	 * @param moduleId The identifier of the module that is locked, preventing the user from continuing in the game
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& servlets.module.StopHere &&&");
		Encoder encoder = ESAPI.encoder();
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				boolean notNull = false;
				String storedResult = null;
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String moduleId = (String)request.getParameter("moduleId");;
					log.debug("moduleId = " + moduleId.toString());
					
					String message = new String();
					if(request.getParameter("message") != null)
						message = (String)request.getParameter("message");
					else
						message = "Ask your administrator when these modules will be made available";
					log.debug("message = " + message);
					
					//Validation
					notNull = (moduleId != null);
					if(notNull)
					{
						storedResult = Getter.getModuleResult(ApplicationRoot, moduleId);
					}
					if(notNull && storedResult != null)
					{
						//Data is good, Module exists! Store it as the Blocking module. Storing the message as well to show 
						ModuleBlock.blockerId = moduleId;
						ModuleBlock.blockerMessage = "<h2 class=\"title\">Well Done!</h2><p>You've done very well, but you can go no further at this time!" +
								" The administrator has temporary marked the following modules as closed.</p>" +
								"<p>" + message + "</p>";
						ModuleBlock.blockerEnabled = true;
						log.debug("Blocker Enabled on module ");
						out.write("<h2 class='title'>Block Enabled</h2><p>The block has been enabled successfully</p>");
					}
					else
					{
						//Validation Error Responses
						if(!notNull)
						{
							log.error("Null values detected");
						}
						else if(storedResult == null)
						{
							log.error("Module not found");
						}
						out.write("<h2 class='title'>Error</h2><p>Invalid data recieved</p>");
					}
				}
				catch (Exception e)
				{
					log.error("StopHere Error: " + e.toString());
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
			}
		}
		else
		{
			log.error("Invalid Session Detected");
			out.write(encoder.encodeForHTMLAttribute(ExposedServer.getSecureUrl()) + "css/images/loggedOutSheep.jpg");
		}
		log.debug("&&& END StopHere &&&");
	}
}

