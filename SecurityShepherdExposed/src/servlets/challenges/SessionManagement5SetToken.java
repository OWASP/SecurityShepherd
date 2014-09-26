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
 * Session Management Challenge Five SessionManagement5SetToken
 * (Does not Return Result Key)
 * 
 * This function is a shell to give the appearance that a token has been set for a user. 
 * A DB call is made to check if a user exists. If the user does exist the server returns an ok message 
 * claiming that the user has been emailed a URL with a token embedded for resetting their password. 
 * This in fact does not happen. User must find another way to sign in as an admin.
 * 
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
public class SessionManagement5SetToken extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SessionManagement5SetToken.class);
	private static String levelName = "SessionManagement5SetToken";
	public static String levelHash = SessionManagement5.levelHash;
	private static String levelResult = ""; //This class does not return a result key
	/**
	 * Used to apparently send a message to a user with a token to reset their password.
	 * 
	 * @param userName Sub schema user name
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdExposedLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
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
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		Encoder encoder = ESAPI.encoder();
		String htmlOutput = new String();
		log.debug(levelName + " Servlet Accessed");
		try
		{
			log.debug("Getting Parameters");
			Object nameObj = request.getParameter("subUserName");
			String userName = new String();
			if(nameObj != null)
				userName = (String) nameObj;
			log.debug("subName = " + userName);
			
			log.debug("Getting ApplicationRoot");
			String ApplicationRoot = getServletContext().getRealPath("");
			log.debug("Servlet root = " + ApplicationRoot );
			
			Connection conn = Database.getChallengeConnection(ApplicationRoot, "BrokenAuthAndSessMangChalFive");
			log.debug("Checking name");
			PreparedStatement callstmt;
			
			log.debug("Committing changes made to database");
			callstmt = conn.prepareStatement("COMMIT");
			callstmt.execute();
			log.debug("Changes committed.");
			
			callstmt = conn.prepareStatement("SELECT userName FROM users WHERE userName = ?");
			callstmt.setString(1, userName);
			log.debug("Executing findUser");
			ResultSet resultSet = callstmt.executeQuery();
			//Is the username valid?
			if(resultSet.next())
			{
				log.debug("User found");
				htmlOutput = "URL with embedded password reset token has been sent to '" + encoder.encodeForHTML(userName) + "' via email.";
			}
			else
			{
				log.debug("User not Found");
				htmlOutput = "Could not find user" + encoder.encodeForHTML(userName);
			}
			Database.closeConnection(conn);
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