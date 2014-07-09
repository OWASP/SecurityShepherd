package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.Hash;
import utils.ShepherdExposedLogManager;

/**
 * Session Management Lesson
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
public class SessionManagementLesson extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SessionManagementLesson.class);
	private static String levelName = "Session Management Lesson";
	private static String levelHash = "b8c19efd1a7cc64301f239f9b9a7a32410a0808138bbefc98986030f9ea83806";
	private static String levelResult = "6594dec9ff7c4e60d9f8945ca0d4";
	/**
	 * Controller is tracking the user completion through the "lessonComplete" cookie. If this cookie is changed the user can complete the level
	 * @param lessonComplete Tracking cookie
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdExposedLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			//Attempting to recover user name of session that made request
			try
			{
				if (request.getSession() != null)
				{
					HttpSession ses = request.getSession();
					String userName = (String) ses.getAttribute("decyrptedUserName");
					log.debug(userName + " accessed " + levelName + " Servlet");
				}
			}
			catch (Exception e)
			{
				log.debug(levelName + " Servlet Accessed");
				log.error("Could not retrieve user name from session");
			}
			Cookie userCookies[] = request.getCookies();
			int i = 0;
			Cookie theCookie = null;
			for(i = 0; i < userCookies.length; i++)
			{
				if(userCookies[i].getName().compareTo("lessonComplete") == 0)
				{
					theCookie = userCookies[i];
					break; //End Loop, because we found the token
				}
			}
			String htmlOutput = null;
			if(theCookie != null)
			{
				log.debug("Cookie value: " + theCookie.getValue());
				
				if(theCookie.getValue().equals("lessonComplete"))
				{
					log.debug("Lesson Complete");
					
					// Get key and add it to the output
					String userKey = Hash.generateUserSolution(levelResult, request.getCookies());
					
					htmlOutput = "<h2 class='title'>Lesson Complete</h2>" +
							"<p>" +
							"Congratulations, you have bypassed this lessons <strong><a>VERY WEAK</a></strong> session management. The result key for this lesson is " +
							"<a>"+ userKey + "</a>" +
							"</p>";
				}
			}
			if(htmlOutput == null)
			{
				log.debug("Lesson Not Complete");
				htmlOutput = "<h2 class='title'>Lesson Not Complete</h2>" +
						"<p>" +
						"You have not completed this lesson yet." +
						"</p>";
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occurred! You must be getting funky!");
			log.fatal(levelName + " - " + e.toString());
		}
	}
}
