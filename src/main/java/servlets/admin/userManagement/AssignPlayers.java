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
import org.owasp.encoder.Encode;

import dbProcs.Getter;
import dbProcs.Setter;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This is the control class for the Assign Players to Class functionality
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
public class AssignPlayers extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(AssignPlayers.class);

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
		log.debug("*** servlets.Admin.AssignPlayers ***");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		Cookie tokenCookie = Validate.getToken(request.getCookies());
		Object tokenParmeter = request.getParameter("csrfToken");
		if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				boolean notNull = false;
				boolean validPlayer = false;
				String[] classInfo = new String[2];
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String classId = (String)request.getParameter("classId");
					log.debug("classId = " + classId);
					String[] players = request.getParameterValues("players[]");;
					log.debug("players = " + players.toString());
					
					//Validation
					notNull = (players != null);
					log.debug("Ensuring strings are not empty");
					if(classId.isEmpty())
					{
						log.debug("classId is empty; nulling");
						classId = null;
					}
					if(notNull)
					{
						if(classId != null)
						{
							classInfo = Getter.getClassInfo(ApplicationRoot, classId);
							if(classInfo[0] == null)
								classId = null;
						}
						if(classId == null)
						{
							classInfo[1] = "Unassigned";
							classInfo[0] = "Players";
						}
						for(int i = 0; i < players.length; i++)
						{
							log.debug("Validating player " + players[i]);
							validPlayer = Getter.findPlayerById(ApplicationRoot, players[i]);
						}	
					}
					if(notNull && validPlayer)
					{
						//Data is good, Add user
						log.debug("Updating Player Class");
						String reponseMessage = new String();
						for(int i = 0; i < players.length; i++)
						{
							String userName = new String();
							if(classId != null)
							{
								userName = Setter.updatePlayerClass(ApplicationRoot, classId, players[i]);
							}
							else
							{
								userName = Setter.updatePlayerClassToNull(ApplicationRoot, players[i]);
							}
							if(userName != null)
							{
								reponseMessage += "<a>" + Encode.forHtml(userName) + "</a> assigned successfully to <a>" + Encode.forHtml(classInfo[1] + " " + classInfo[0]) + "</a>.<br>";
							}
							else
							{
								reponseMessage += "<font color='red'>User could not be updated. Please try again.</font><br/>";
							}
						}
						out.print("<h3 class=\"title\">Player Assignment Result</h3>" +
								"<p>" +
								reponseMessage +
								"<p>");
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occurred: ";
						if(!notNull)
						{
							log.error("Null values detected");
							errorMessage += "Invalid Request. Please try again";
						}
						else if(!validPlayer)
						{
							log.error("Player not found");
							errorMessage += "Player(s) Not Found. Please try again";
						}
						out.print("<h3 class=\"title\">Player Assignment Failure</h3>" +
								"<p><font color=\"red\">" +
								Encode.forHtml(errorMessage) +
								"</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Assign Players Error: " + e.toString());
					out.print("<h3 class=\"title\">Player Assignment Failure</h3>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("<h3 class=\"title\">Player Assignment Failure</h3>" +
						"<p>" +
						"<font color=\"red\">An error Occurred! Please try again.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h3 class=\"title\">Player Assignment Failure</h3>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** AssignPlayers END ***");
	}
}
