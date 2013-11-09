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

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.Hash;

import dbProcs.Database;

/**
 * Session Management Challenge Two
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
public class d779e34a54172cbc245300d3bc22937090ebd3769466a501a5e7ac605b9f34b7 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(d779e34a54172cbc245300d3bc22937090ebd3769466a501a5e7ac605b9f34b7.class);
	/**
	 * The user attempts to use this funciton to sign into a sub schema. If they successfully sign in then they are able to retrieve the result key for the challenge
	 * If they sign in with a correct user name but incorrect password then the email address of the user will be returned in a error message
	 * @param subName Sub schema user name
	 * @param subName Sub schema user password
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		Encoder encoder = ESAPI.encoder();
		String htmlOutput = new String();
		log.debug("Broken Auth and Session Management Challenge Two Servlet");
		try
		{
			log.debug("Getting Challenge Parameters");
			Object nameObj = request.getParameter("subName");
			Object passObj = request.getParameter("subPassword");
			String subName = new String();
			String subPass = new String();
			String userAddress = new String();
			if(nameObj != null)
				subName = (String) nameObj;
			if(passObj != null)
				subPass = (String) passObj;
			log.debug("subName = " + subName);
			log.debug("subPass = " + subPass);
			
			log.debug("Getting ApplicationRoot");
			String ApplicationRoot = getServletContext().getRealPath("");
			log.debug("Servlet root = " + ApplicationRoot );
			
			Connection conn = Database.getChallengeConnection(ApplicationRoot, "BrokenAuthAndSessMangChalTwo");
			log.debug("Checking credentials");
			PreparedStatement callstmt;
			
			log.debug("Commiting changes made to database");
			callstmt = conn.prepareStatement("COMMIT");
			callstmt.execute();
			log.debug("Changes commited.");
			
			callstmt = conn.prepareStatement("SELECT userName, userAddress FROM users WHERE userName = ? AND userPassword = SHA(?)");
			callstmt.setString(1, subName);
			callstmt.setString(2, subPass);
			log.debug("Executing authUser");
			ResultSet resultSet = callstmt.executeQuery();
			if(resultSet.next())
			{
				log.debug("Successful Login");
				// Get key and add it to the output
				String userKey = Hash.generateUserSolution("4ba31e5ffe29de092fe1950422a", request.getCookies());
				htmlOutput = "<h2 class='title'>Welcome " + encoder.encodeForHTML(resultSet.getString(1)) + "</h2>" +
						"<p>" +
						"The result key is <a>" + userKey + "</a>" +
						"</p>";
			}
			else
			{
				log.debug("Incorrect credentials, checking if username correct");
				callstmt = conn.prepareStatement("SELECT userAddress FROM users WHERE userName = ?");
				callstmt.setString(1, subName);
				log.debug("Executing getAddress");
				resultSet = callstmt.executeQuery();
				if(resultSet.next())
				{
					log.debug("User Found");
					userAddress = "Incorrect password for <a>" + encoder.encodeForHTML(resultSet.getString(1)) + "</a><br/>";
				}
				else
				{
					userAddress = "Username not found.<br/>";
				}
				htmlOutput = htmlStart + userAddress + htmlEnd;
			}
			Database.closeConnection(conn);
			log.debug("Outputing HTML");
			out.write(htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occured! You must be getting funky!");
			log.fatal("Session Management Challenge Two - " + e.toString());
		}
	}
	
	private static String htmlStart = "<table>";
	private static String htmlEnd = "<tr><td>Username:</td><td><input type='text' id='subName'/></td></tr>" +
			"<tr><td>Password:</td><td><input type='password' id='subPassword'/></td></tr>" +
			"<tr><td colspan='2'><div id='submitButton'><input type='submit' value='Sign In'/>" +
			"</div></td></tr>" +
			"</table>";
}
