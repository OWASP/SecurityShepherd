package servlets.module.lesson;

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
import utils.FindXSS;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Cross Site Scripting Lesson
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
public class XssLesson 
extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(XssLesson.class);
	private static String levelName = "XSS Lesson";
	private static String levelHash = "zf8ed52591579339e590e0726c7b24009f3ac54cdff1b81a65db1688d86efb3a";
	/**
	 * Cross Site Request Forgery safe Reflected XSS vulnerability. cannot be remotely deployed, and therefore only is executable against the person initiating the function.
	 * @param searchTerm To be spat back out at the user
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(levelName + " Servlet Accessed");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " accessed by: " + ses.getAttribute("userName").toString());
				Cookie tokenCookie = Validate.getToken(request.getCookies());
				Object tokenParmeter = request.getParameter("csrfToken");
				if(Validate.validateTokens(tokenCookie, tokenParmeter))
				{
					String searchTerm = request.getParameter("searchTerm");
					log.debug("User Submitted - " + searchTerm);
					String htmlOutput = new String();
					if(FindXSS.antiSamySearch(searchTerm, getServletContext().getRealPath("")))
					{
						String theHash = this.getClass().getSimpleName();
						log.debug("Completed Module! Module Hash: " + theHash);
						Encoder encoder = ESAPI.encoder();
						htmlOutput = "<h2 class='title'>Well Done</h2>" +
								"<p>You successfully executed the JavaScript alert command!<br />" +
								"The result key for this lesson is <a>" +
								encoder.encodeForHTML(
										Hash.generateUserSolution(Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash), (String)ses.getAttribute("userName"))
								) +
								"</a>";
					}
					log.debug("Adding searchTerm to Html: " + searchTerm);
					htmlOutput += "<h2 class='title'>Search Results</h2>" +
						"<p>Sorry but there were no results found that related to '" +
						searchTerm +
						"'</p>";
					log.debug("Outputting HTML");
					out.write(htmlOutput);
				}
			}
			else
			{
				log.error(levelName + " accessed with no session");
			}
		}
		catch(Exception e)
		{
			out.write("An Error Occurred! You must be getting funky!");
			log.fatal(levelName + " - " + e.toString());
		}
		log.debug("End of " + levelName + " Servlet");
	}
}
