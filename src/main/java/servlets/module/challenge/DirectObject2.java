package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;

/**
 * Insecure Direct Object Reference Challenge Two
 * Does not use user specific key because key is currently hard coded into database schema
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
public class DirectObject2 extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DirectObject2.class);
	private static String levelName = "Insecure Direct Object Reference Challenge Two";
	public static String levelHash = "vc9b78627df2c032ceaf7375df1d847e47ed7abac2a4ce4cb6086646e0f313a4";
	/**
	 * The user must abuse this functionality to reveal a hidden user. The result key is hidden in this users profile.
	 * @param userId To be used in generating the HTML output
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.directObject.directObjectRef2", locale);
		
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();
			out.print(getServletInfo());
			try
			{
				String userId = request.getParameter("userId[]");
				log.debug("User Submitted - " + userId);
				String ApplicationRoot = getServletContext().getRealPath("");
				log.debug("Servlet root = " + ApplicationRoot );
				String htmlOutput = new String();
				
				Connection conn = Database.getChallengeConnection(ApplicationRoot, "directObjectRefChalTwo");
				PreparedStatement prepstmt = conn.prepareStatement("SELECT userName, privateMessage FROM users WHERE userId = ?");
				prepstmt.setString(1, userId);
				ResultSet resultSet = prepstmt.executeQuery();
				if(resultSet.next())
				{
					log.debug("Found user: " + resultSet.getString(1));
					String userName = resultSet.getString(1);
					String privateMessage = resultSet.getString(2);
					htmlOutput = "<h2 class='title'>" + userName + "'s " + bundle.getString("response.message") + "</h2>" +
							"<p>" + privateMessage + "</p>";
				}
				else
				{
					log.debug("No Profile Found");
					
					htmlOutput = "<h2 class='title'>" + bundle.getString("response.notFound") + "</h2><p>" + bundle.getString("response.notFoundMessage.1") + " '" + Encode.forHtml(userId) + "' " + bundle.getString("response.notFoundMessage.2") + "</p>";
				}
				log.debug("Outputting HTML");
				out.write(htmlOutput);
				Database.closeConnection(conn);
			}
			catch(Exception e)
			{
				out.write(errors.getString("error.funky"));
				log.fatal(levelName + " - " + e.toString());
			}
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}
