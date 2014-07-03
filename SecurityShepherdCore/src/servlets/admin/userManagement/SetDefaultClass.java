package servlets.admin.userManagement;

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

import dbProcs.Getter;
import servlets.Register;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This is the control class for the Set Default Class functionality
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
public class SetDefaultClass extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SetDefaultClass.class);

	/** Initiated by assignPlayers.jsp. A number of players can be assigned to a new class. Changing the actual class of the player is handed by Setter.changePlayerClass
	 * @param classId The identifier of the class to add the players to
	 * @param players[] An array of player identifiers to add to the specified class	
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.SetDefaultClass ***");
		Encoder encoder = ESAPI.encoder();
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			String htmlOutput = new String();
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				String[] classInfo = new String[2];
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String classId = (String)request.getParameter("classId");
					log.debug("classId = " + classId);
					
					if(classId.isEmpty()) // Null Submitted - Change default class to unassigned players group
					{
						log.debug("Null Class submitted");
						Register.setDefaultClass(""); // Empty string is caught in the Register Servlet and sets the user to the Unassigned Group
						htmlOutput = "Default Class Set To Unassigned Players";
						log.debug(htmlOutput);
					}
					else
					{
						//validate class identifier
						classInfo = Getter.getClassInfo(ApplicationRoot, classId);
						if(classInfo != null && !classInfo[0].isEmpty()) // Class Exists
						{
							log.debug("Valid Class Submitted");
							Register.setDefaultClass(classId);
							htmlOutput = "Default class has been set to " + encoder.encodeForHTML(classInfo[0]);
							log.debug(htmlOutput);
						}
					}
					if(htmlOutput.isEmpty())
					{
						htmlOutput = "<h2 class='title'>Default Class is Unchanged</h2>"
								+ "<p>Invalid data was submitted. Please try again.</p>";
					}
					else //Function must have completed if this isn't empty
					{
						htmlOutput = "<h2 class='title'>Default Class Updated</h2>"
								+ "<p>" + htmlOutput +"</p>";
					}
					out.write(htmlOutput);
				}
				catch (Exception e)
				{
					log.error("SetDefaultClass Error: " + e.toString());
					out.print("<h2 class=\"title\">Set Default Class Failure</h2><br>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("<h2 class=\"title\">Player Assignment Failure</h2><br>" +
						"<p>" +
						"<font color=\"red\">An error Occurred! Please try again.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** SetDefaultClass END ***");
	}
}
