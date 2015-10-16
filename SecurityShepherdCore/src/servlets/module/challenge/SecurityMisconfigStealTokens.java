package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;

/**
 * Security Misconfiguration Steal Tokens
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
public class SecurityMisconfigStealTokens extends HttpServlet
{
	//Security Misconfiguration Challenge
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SecurityMisconfigStealTokens.class);
	private static String levelName = "Security Misconfig Cookie Flags Servlet";
	public static String levelHash = "c4285bbc6734a10897d672c1ed3dd9417e0530a4e0186c27699f54637c7fb5d4";
	private static String levelResult = "92755de2ebb012e689caf8bfec629b1e237d23438427499b6bf0d7933f1b8215"; // Base Key. User is given user specific key
	/**
	 * This servlet will return the key to complete as long as the cookie submitted is valid and does not belong to the user making the request
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			//Translation Stuff
			Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
			ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
			ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.securityMisconfig.stealTokens", locale);
			
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			try
			{
				String applicationRoot = getServletContext().getRealPath("");
				
				String userId = ses.getAttribute("userStamp").toString();
				String userActualCookie = getUserToken(userId, applicationRoot);
				//Getting Submitted Cookie
				int i = 0;
				Cookie[] userCookies = request.getCookies();
				Cookie theToken = null;
				for(i = 0; i < userCookies.length; i++)
				{
					if(userCookies[i].getName().compareTo("securityMisconfigLesson") == 0)
					{
						theToken = userCookies[i];
						break; //End Loop, because we found the token
					}
				}
				String cookieValue = theToken.getValue();
				
				log.debug("User Submitted Cookie: " + cookieValue);
				log.debug("Stored Cookie Value  : " + userActualCookie);
				
				if(cookieValue.compareTo(userActualCookie) == 0)
				{
					//User is using their own Cookie: Not Complete
					htmlOutput = new String("<h2 class='title'>" + bundle.getString("securityMisconfig.servlet.stealTokens.notComplete") + "</h2>"
							+ "<p>" + bundle.getString("securityMisconfig.servlet.stealTokens.notComplete.message") + "<p>");					
				}
				else
				{
					//User submitted something different from their cookie
					boolean notUsersTokenButValid = validToken(userId, cookieValue, applicationRoot);
					if(notUsersTokenButValid)
					{
						log.debug("Valid Cookie of another User Dectected");
						// Get key and add it to the output
						String userKey = Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName"));
						htmlOutput = "<h2 class='title'>" + bundle.getString("securityMisconfig.servlet.stealTokens.complete") + "</h2>" +
								"<p>" +
								bundle.getString("securityMisconfig.servlet.stealTokens.youDidIt") + " " +
								"<a>" + userKey + "</a>" +
								"</p>";
					}
					else
					{
						htmlOutput = new String("<h2 class='title'>" + bundle.getString("securityMisconfig.servlet.stealTokens.notComplete") + "</h2>"
								+ "<p>" + bundle.getString("securityMisconfig.servlet.stealTokens.notComplete.yourToken") + "<p>");					
					}
				}
			}
			catch(Exception e)
			{
				out.write(errors.getString("securityMisconfig.servlet.stealTokens.notComplete.yourToken"));
				log.fatal(levelName + " - " + e.toString());
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
	
	/**
	 * Method that will return a users token. If the user does not have a token, this will set one.
	 * @param userId User Identifier to search for
	 * @param applicationRoot Running context of application
	 * @return The token associated with the submitted userId
	 * @throws SQLException
	 */
	public static String getUserToken (String userId, String applicationRoot) throws SQLException
	{
		String userToken = new String();
		log.debug("Getting user token with id: " + userId);
		Connection conn = Database.getChallengeConnection(applicationRoot, "SecurityMisconfigStealToken");
		try 
		{
			CallableStatement getTokenCs = conn.prepareCall("call getToken(?)");
			getTokenCs.setString(1, userId);
			log.debug("Executing getToken procedure...");
			ResultSet tokenRs = getTokenCs.executeQuery();
			if(tokenRs.next())
			{
				userToken = tokenRs.getString(1);
			}
			else
			{
				log.error("No Results From Call");
				throw new SQLException("No results from getToken Call. Empty Result Set");
			}
			tokenRs.close();
		} 
		catch (SQLException e) 
		{
			log.error("Could not get user SecurityMisconfigStealToken token: " + e.toString());
			throw e;
		}
		conn.close();
		if (!userToken.isEmpty())
			log.debug("Found token: " + userToken);
		return userToken;
	}
	
	/**
	 * Method to validate if a token exists in the database which does not belong to the user submitting the request
	 * @param userId The ID of the user submitting the request
	 * @param token The token submitted in the request
	 * @param applicationRoot Running context of the application
	 * @return Boolean depicting if the token exists in the database and does not belong to the user submitting the request
	 * @throws SQLException
	 */
	public static boolean validToken (String userId, String token, String applicationRoot) throws SQLException
	{
		boolean validToken = false;
		log.debug("Checking token:" + token);
		Connection conn = Database.getChallengeConnection(applicationRoot, "SecurityMisconfigStealToken");
		try 
		{
			CallableStatement validateTokenCs = conn.prepareCall("call validToken(?, ?)");
			validateTokenCs.setString(1, userId);
			validateTokenCs.setString(2, token);
			log.debug("Executing validToken procedure...");
			ResultSet tokenRs = validateTokenCs.executeQuery();
			if(tokenRs.next())
			{
				if(tokenRs.getInt(1) > 0)
				{
					log.debug("Valid Token Detected");
					validToken = true;
				}
			}
			else
			{
				log.error("No Results From validToken Call");
				throw new SQLException("No results from validToken Call. Empty Result Set");
			}
			tokenRs.close();
		} 
		catch (SQLException e) 
		{
			log.error("Could not verify token: " + e.toString());
			throw e;
		}
		conn.close();
		return validToken;
	}
}
