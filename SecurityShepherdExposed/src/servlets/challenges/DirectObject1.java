package servlets.challenges;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.ShepherdExposedLogManager;
import dbProcs.Database;

/**
 * Insecure Direct Object Challenge Challenge One
 * Does not use user specific key because key is currently hardcoded into database schema
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
public class DirectObject1 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DirectObject1.class);
	private static String levelName = "Insecure Direct Object Challenge Challenge One";
	private static String levelHash = "o9a450a64cc2a196f55878e2bd9a27a72daea0f17017253f87e7ebd98c71c98c";
	private static String levelResult = ""; //Stored in DB. Not user Specific
	/**
	 * The user must abuse this functionality to revial a hidden user. The result key is hidden in this users profile.
	 * @param userId To be used in generating the HTML output
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdExposedLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		
		//Attempting to recover username of session that made request
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
			log.error("Could not retrieve username from session");
		}
		
		PrintWriter out = response.getWriter();
		out.print(getServletInfo());
		try
		{
			String userId = request.getParameter("userId[]");
			log.debug("User Submitted - " + userId);
			String ApplicationRoot = getServletContext().getRealPath("");
			log.debug("Servlet root = " + ApplicationRoot );
			String htmlOutput = new String();
			
			Connection conn = Database.getChallengeConnection(ApplicationRoot, "directObjectRefChalOne");
			PreparedStatement prepstmt = conn.prepareStatement("SELECT userName, privateMessage FROM users WHERE userId = ?");
			prepstmt.setString(1, userId);
			ResultSet resultSet = prepstmt.executeQuery();
			if(resultSet.next())
			{
				log.debug("Found user: " + resultSet.getString(1));
				String userName = resultSet.getString(1);
				String privateMessage = resultSet.getString(2);
				htmlOutput = "<h2 class='title'>" + userName + "'s Message</h2>" +
						"<p>" + privateMessage + "</p>";
			}
			else
			{
				log.debug("No Profile Found");
				Encoder encoder = ESAPI.encoder();
				htmlOutput = "<h2 class='title'>User: 404 - User Not Found</h2><p>User '" + encoder.encodeForHTML(userId) + "' could not be found or does not exist.</p>";
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
			Database.closeConnection(conn);
		}
		catch(Exception e)
		{
			out.write("An Error Occured! You must be getting funky!");
			log.fatal(levelName + " - " + e.toString());
		}
	}
}
