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
import org.owasp.encoder.Encode;

import dbProcs.Getter;
import utils.ScoreboardStatus;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This is the control class for enabling the the user accessible scoreboard
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
public class EnableScoreboard extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(EnableScoreboard.class);

	/** 
	 * @param classId The identifier of the class to add the players to
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.EnableScoreboard ***");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		Cookie tokenCookie = Validate.getToken(request.getCookies());
		Object tokenParmeter = request.getParameter("csrfToken");
		if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			String htmlOutput = new String();
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				log.debug("Scoreboard being enabled by: " + ses.getAttribute("userName"));
				String[] classInfo = new String[2];
				try
				{
					String applicationRoot = getServletContext().getRealPath("");
					
					log.debug("Getting Parameters");
					String classId = (String)request.getParameter("classId");
					log.debug("classId = " + classId);
					String scoreboardMessage = new String();
					if(classId.isEmpty()) // Null Submitted - configure scoreboard to list all players regardless of class
					{
						log.debug("Null Class submitted");
						ScoreboardStatus.setScoreboeardOpen();
						scoreboardMessage = "Scoreboard is now enabled and lists all users regardless of their class.";
					}
					else if (classId.equalsIgnoreCase("classSpecific"))
					{
						//Set Class Specific Scoreboards
						ScoreboardStatus.setScoreboardClassSpecific();
						scoreboardMessage = "Scoreboard has been enabled and only lists users from the viewer's class. Admin users will still see the scoreboard of the default class.";
					}
					else
					{
						//validate class identifier
						classInfo = Getter.getClassInfo(applicationRoot, classId);
						if(classInfo != null && !classInfo[0].isEmpty()) // Class Exists
						{
							log.debug("Valid Class Submitted");
							ScoreboardStatus.setScoreboardClass(classId);
							scoreboardMessage = "Scoreboard has been enabled and only lists users from " + Encode.forHtml(classInfo[0]);
						}
					}
					if(scoreboardMessage.isEmpty())
					{
						htmlOutput = "<h3 class='title'>Scoreboard Settings are Unchanged</h3>"
								+ "<p>Invalid data was submitted. Please try again.</p>";
					}
					else //Function must have completed if this isn't empty 
					{
						log.debug(scoreboardMessage);
						String restrictedScoreboard = Validate.validateParameter(request.getParameter("restricted"), 5);
						if(restrictedScoreboard.isEmpty() && classId.equalsIgnoreCase("classSpecific")) //Total Public Scoreboard
						{
							log.debug("User Accessible Scoreboard Enabled");
							htmlOutput = "<h3 class='title'>Scoreboard Settings Updated</h3>"
								+ "<p>" + scoreboardMessage +"</p>";
						}
						else
						{
							if(!classId.equalsIgnoreCase("classSpecific") && !restrictedScoreboard.isEmpty())
							{
								ScoreboardStatus.setScoreboardAdminOnly();
								log.debug("Admin Only Scoreboard Enabled");
								htmlOutput = "<h3 class='title'>Scoreboard Settings Updated</h3>"
									+ "<p>" + scoreboardMessage +" The scoreboard is only accessible by administrators</p>";
							}
							else //Not an Admin Only Board. Give response
							{
								htmlOutput = "<h3 class='title'>Scoreboard Settings Updated</h3>"
										+ "<p>" + scoreboardMessage + "</p>";
							}
						}
					}
					out.write(htmlOutput);
				}
				catch (Exception e)
				{
					log.error("SetDefaultClass Error: " + e.toString());
					out.print("<h3 class=\"title\">Scoreboard Configuration Failure</h3><br>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("<h3 class=\"title\">Scoreboard Configuration Failure</h3><br>" +
						"<p>" +
						"<font color=\"red\">An error Occurred! Please try again.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h3 class=\"title\">Failure</h3><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** EnableScoreboard END ***");
	}
}
