package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Getter;
import dbProcs.Setter;

/**
 * Cross Site Request Forgery Challenge Target Five - Does not return Result key
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
public class CsrfChallengeTargetFive extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String levelHash = "70b96195472adf3bf347cbc37c34489287969d5ba504ac2439915184d6e5dc49";
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfChallengeTargetFive.class);
	private static String levelName = "CSRF 5 Target";
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
			boolean result = false;
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				//Get CSRF Token From session
				if(ses.getAttribute("csrfChallengeFiveNonce") == null || ses.getAttribute("csrfChallengeFiveNonce").toString().isEmpty())
				{
					log.debug("No CSRF Token associated with user");
					Random random = new Random();
					int newToken = random.nextInt(3);
					out.write(csrfGenerics.getString("target.noTokenNewToken") + " " + newToken + "<br><br>");
					storedToken = "" + newToken;
					ses.setAttribute("csrfChallengeFiveNonce", newToken);
				}
				else
				{
					storedToken = "" + ses.getAttribute("csrfChallengeFiveNonce");
				}
				String userId = (String)ses.getAttribute("userStamp");
				
				String plusId = (String)request.getParameter("userId").trim();
				log.debug("User Submitted - " + plusId);
				String csrfToken = (String)request.getParameter("csrfToken").trim();;
				log.debug("csrfToken Submitted - " + csrfToken);
				
				if(!userId.equals(plusId))
				{
					if(csrfToken.equalsIgnoreCase(storedToken))
					{
						log.debug("Valid Nonce Value Submitted");
						String ApplicationRoot = getServletContext().getRealPath("");
						String userName = (String)ses.getAttribute("userName");
						String attackerName = Getter.getUserName(ApplicationRoot, plusId);
						if(attackerName != null)
						{
							log.debug(userName + " is been CSRF'd by " + attackerName);
							
							log.debug("Attempting to Increment ");
							String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, levelHash);
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
