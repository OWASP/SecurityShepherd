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

import utils.ScoreboardStatus;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This is the control class for disabling the the user accessible scoreboard
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
public class DisableScoreboard extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DisableScoreboard.class);

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
		log.debug("*** servlets.Admin.DisableScoreboard ***");
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
				try
				{			
					log.debug("Scoreboard Disabled by: " + ses.getAttribute("userName"));
					ScoreboardStatus.disableScoreboard();
					htmlOutput = "Scoreboard is now disabled and cannot be accessed.";
					log.debug(htmlOutput);
					htmlOutput = "<h3 class='title'>Scoreboard Settings Updated</h3>"
							+ "<p>" + htmlOutput +"</p>";
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
		log.debug("*** DisableScoreboard END ***");
	}
}
