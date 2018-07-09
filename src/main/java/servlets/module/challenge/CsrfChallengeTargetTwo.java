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

import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Getter;
import dbProcs.Setter;

/**
 * Cross Site Request Forgery Challenge Target Two - Does not return Result key
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
public class CsrfChallengeTargetTwo extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfChallengeTargetTwo.class);
	private static String levelName = "CSRF 2 Target";
	/**
	 * CSRF vulnerable function that can be used by users to force other users to mark their CSRF challenge Two as complete.
	 * @param userId User identifier to be incremented
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("Cross-SiteForegery Challenge Two Target Servlet");
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle csrfGenerics = ResourceBundle.getBundle("i18n.servlets.challenges.csrf.csrfGenerics", locale);
		
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			boolean result = false;
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				String plusId = request.getParameter("userId");
				log.debug("User Submitted - " + plusId);
				String userId = (String)ses.getAttribute("userStamp");
				if(!userId.equals(plusId))
				{
					String ApplicationRoot = getServletContext().getRealPath("");
					String userName = (String)ses.getAttribute("userName");
					String attackerName = Getter.getUserName(ApplicationRoot, plusId);
					if(attackerName != null)
					{
						log.debug(userName + " is been CSRF'd by " + attackerName);
						
						log.debug("Attempting to Increment ");
						String moduleHash = CsrfChallengeTwo.getLevelHash();
						String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, moduleHash);
						result = Setter.updateCsrfCounter(ApplicationRoot, moduleId, plusId);
					}
					else
					{
						log.error("UserId '" + plusId + "' could not be found.");
					}
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
			log.fatal("Cross Site Request Forgery Target Challenge 2 - " + e.toString());
		}
	}
}
