package servlets.module.lesson;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Security Misconfiguration Lesson
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
 * 
 * @author Mark Denihan
 */
public class SecurityMisconfigLesson extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SecurityMisconfigLesson.class);
	private static String levelName = "Security Misconfig Lesson";
	private static String levelhash = "fe04648f43cdf2d523ecf1675f1ade2cde04a7a2e9a7f1a80dbb6dc9f717c833";
	private static String levelResult = "55b34717d014a5a355f6eced4386878fab0b2793e1d1dbfd23e6262cd510ea96";
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		//Attempting to recover username of session that made request
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();
			out.print(getServletInfo());
			try
			{
				String userName = request.getParameter("userName");
				log.debug("User Name - " + userName);
				String userPass = request.getParameter("userPass");
				log.debug("User Pass - " + userName);
				boolean loggedIn = userName.contentEquals("admin") && userPass.contentEquals("password");
				String htmlOutput = new String();
				if(!loggedIn)
				{
					if(userName.contentEquals("admin"))
						htmlOutput = "Incorrect Passsword Submitted";
					else
					{
						Encoder encoder = ESAPI.encoder();
						htmlOutput = "No user records found for \"" + encoder.encodeForHTML(userName) + "\"";
					}
					htmlOutput = "<h2 class='title'>Authentication Error</h2><p>" + htmlOutput + "</p>";
				}
				else
				{
					// Default username and password were used
					log.debug("User has signed in as admin");
					htmlOutput = "<h2 class='title'>Authentication Successful</h2><p>"
							+ "You have successfully signed in with the default sign in details for this applicaiton. You should always change default passwords and avoid default administration usernames.<br><br>"
							+ "Result Key: <a>" + Hash.generateUserSolution(levelResult, ses.getAttribute("userName").toString()) + "</a>";
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
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}