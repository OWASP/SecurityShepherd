package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
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
import dbProcs.Getter;
import dbProcs.Setter;

/**
 * Cross Site Request Forgery Challenge Target Seven - Does not return Result key
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
public class CsrfChallengeTargetSeven extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static String moduleHash = "7d79ea2b2a82543d480a63e55ebb8fef3209c5d648b54d1276813cd072815df3";
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfChallengeTargetSeven.class);
	private static String levelName = "CSRF Seven Target";
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
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle csrfGenerics = ResourceBundle.getBundle("i18n.servlets.challenges.csrf.csrfGenerics", locale);
		
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		String storedToken = new String();
		try
		{
			String ApplicationRoot = getServletContext().getRealPath("");
			String csrfTokenName = "csrfChallengeSevenNonce";
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
					log.debug("No CSRF Token associated with user");
					storedToken = Hash.randomString();
					out.write(csrfGenerics.getString("target.noTokenNewToken") + " " + storedToken + "<br><br>");
					ses.setAttribute(csrfTokenName, storedToken);
					Setter.setCsrfChallengeSevenCsrfToken(userId, storedToken, ApplicationRoot);
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
					if(csrfToken.equalsIgnoreCase(storedToken))
					{
						log.debug("Valid Nonce Value Submitted");
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
							log.error("UserId '" + plusId + "' could not be found.");
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
}
