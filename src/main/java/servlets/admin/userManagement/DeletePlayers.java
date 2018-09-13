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
import utils.UserKicker;

/**
 * The delete players server side functionality is hanled by this control class
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
 * @author Cosmin Craciun
 *
 */
public class DeletePlayers extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DeletePlayers.class);
	
	/** Initiated by deletePlayers.jsp.
	 * Deletion of player from Core Database is handed by Setter.deleteUser
	 * @param player Player ID to be deleted
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
						throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.UserManagement.DeletePlayers ***");
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
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String player = request.getParameter("player");
					log.debug("player = " + player);
					
					//Validation
					notNull = (player != null);
					if(notNull)
					{
						log.debug("Validating player " + player);
						validPlayer = Getter.findPlayerById(ApplicationRoot, player);
					}
					if(notNull && validPlayer)
					{
						//Data is good, delete user
						log.debug("Deleting player");
						String reponseMessage = new String();
						String userName = new String(Getter.getUserName(ApplicationRoot, player));
						if(Setter.userDelete(ApplicationRoot, player))
						{
							reponseMessage += "User deleted successfully.<br>";
							// Adding user to kick list so its current session be invalidated at next request
							UserKicker.addUserToKickList(userName);
						}
						else
						{
							reponseMessage += "<font color='red'>User could not be deleted. Please try again.</font><br/>";
						}
						
						out.print("<h3 class=\"title\">Player Deletion Result</h3>" +
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
						out.print("<h3 class=\"title\">Player Deletion Failure</h3>" +
								"<p><font color=\"red\">" +
								Encode.forHtml(errorMessage) +
								"</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Delete Player Error: " + e.toString());
					out.print("<h3 class=\"title\">Player Deletion Failure</h3>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("<h3 class=\"title\">Player Deletion Failure</h3>" +
						"<p>" +
						"<font color=\"red\">CSRF Tokens Did Not Match. Function Aborted.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h3 class=\"title\">Player Deletion Failure</h3>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** DeletePlayers END ***");
	}
}
