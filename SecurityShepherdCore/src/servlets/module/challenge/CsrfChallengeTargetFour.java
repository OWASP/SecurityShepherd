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

import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;
import dbProcs.Getter;
import dbProcs.Setter;

/**
 * Cross Site Request Forgery Challenge Target Four - Does not return Result key
 * <br/><br/>
 * Weak Nonce Variety can be broken
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
public class CsrfChallengeTargetFour extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static String moduleHash = "84118752e6cd78fecc3563ba2873d944aacb7b72f28693a23f9949ac310648b5";
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfChallengeTargetFour.class);
	private static String levelName = "CSRF Target 4";
	/**
	 * CSRF vulnerable function that can be used by users to force other users to mark their CSRF challenge Two as complete.
	 * @param userId User identifier to be incremented
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(levelName + " Servlet");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle csrfGenerics = ResourceBundle.getBundle("i18n.servlets.challenges.csrf.csrfGenerics", locale);
		
		String storedToken = new String();
		try
		{
			String ApplicationRoot = getServletContext().getRealPath("");
			String csrfTokenName = "csrfChallengeFourNonce";
			boolean result = false;
			HttpSession ses = request.getSession(true);
			String userId = (String)ses.getAttribute("userStamp");
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				//Get CSRF Token From session
				if(ses.getAttribute(csrfTokenName) == null || ses.getAttribute(csrfTokenName).toString().isEmpty())
				{
					log.debug("No CSRF Token found in session");
					storedToken = Setter.setCsrfChallengeFourCsrfToken(userId, Hash.randomString(), ApplicationRoot);
					out.write(csrfGenerics.getString("target.noTokenNewToken") + " " + storedToken + "<br><br>");
					ses.setAttribute(csrfTokenName, storedToken);
				}
				else
				{
					storedToken = "" + ses.getAttribute(csrfTokenName);
				}
				log.debug("Victom is - " + userId);
				String plusId = request.getParameter("userId").trim();
				log.debug("User Submitted - " + plusId);
				String csrfToken = request.getParameter("csrfToken").trim();
				log.debug("csrfToken Submitted - '" + csrfToken + "'");
				log.debug("storedCsrf Token is - '" + storedToken + "'");
				
				if(!userId.equals(plusId))
				{
					if(validCsrfToken(ApplicationRoot, csrfToken)) // Poor CSRF Validation Method
					{
						log.debug("'Valid' Nonce Value Submitted");
						String userName = (String)ses.getAttribute("userName");
						String attackerName = Getter.getUserName(ApplicationRoot, plusId);
						if(attackerName != null)
						{
							log.debug(userName + " is been CSRF'd by " + attackerName);
							
							log.debug("Attempting to Increment ");
							String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, moduleHash);
							result = Setter.updateCsrfCounter(ApplicationRoot, moduleId, plusId);
						}
						else
						{
							log.error("UserId '" + plusId + "' could not be found in system.");
						}
					}
					else
					{
						log.debug("User " + plusId + " CSRF attack failed due to invalid nonce");
					}
				}
				else
				{
					log.debug("User " + userId + " is attacking themselves");
				}
				
				if(result)
				{
					out.write(csrfGenerics.getString("target.incrementSuccess"));
				}
				else
				{
					out.write(csrfGenerics.getString("target.incrementFailed"));
				}
			}
			else
			{
				out.write(csrfGenerics.getString("target.noSession"));
			}
		}
		catch(Exception e)
		{
				out.write(errors.getString("error.funky"));
				log.fatal(levelName + " - " + e.toString());
		}
	}
	
	/**
	 * CSRF Validator that checks if user submitted CSRF token is in the DB. This function does not filter the CSRF table for CSRF tokens belonging to the user submitting the request. It will return true as long as the token exists in the database, regardless of who owns the token
	 * @param ApplicationRoot Running context of the application
	 * @param csrfToken CSRF Token value to search DB for
	 * @return Returns true if the CSRF Token is Deemed valid
	 */
	private static boolean validCsrfToken (String ApplicationRoot, String csrfToken)
	{
		log.debug("*** CSRF4.validCsrfToken ***");
		boolean result = false;
		Connection conn = Database.getChallengeConnection(ApplicationRoot, "csrfChallengeFour");
		try
		{
			PreparedStatement prepstmt = conn.prepareStatement("SELECT count(csrfTokenscol) FROM csrfTokens WHERE csrfTokenscol = ?");
			prepstmt.setString(1, csrfToken);
			ResultSet rs = prepstmt.executeQuery();
			result = rs.next(); //If there is a row then the CSRF token was in the DB. Therefore CSRF Validated
		}
		catch(Exception e)
		{
			log.error("CSRF4 Token Check Failure: " + e.toString());
			result = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END CSRF4.validCsrfToken ***");
		return result;
	}
}
