package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import utils.Hash;

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
public class b8c19efd1a7cc64301f239f9b9a7a32410a0808138bbefc98986030f9ea83806 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(b8c19efd1a7cc64301f239f9b9a7a32410a0808138bbefc98986030f9ea83806.class);
	/**
	 * Controller is tracking the user completion through the "lessonComplete" cookie. If this cookie is changed the user can complete the level
	 * @param lessonComplete Tracking cookie
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			log.debug("Broken Auth and Session Management Lesson Servlet");
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
					String userKey = Hash.generateUserSolution("6594dec9ff7c4e60d9f8945ca0d4", request.getCookies());
					
					htmlOutput = "<h2 class='title'>Lesson Complete</h2>" +
							"<p>" +
							"Congradulations, you have bypassed this lessons <strong><a>VERY WEAK</a></strong> session managment. The result key for this lesson is " +
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
			log.debug("Outputing HTML");
			out.write(htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occured! You must be getting funky!");
			log.fatal("Session management lesson - " + e.toString());
		}
	}
}
