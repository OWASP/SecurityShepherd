package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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
 * Cross Site Request Forgery Challenge Five - Does not return result Key
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
public class CsrfChallengeFive extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfChallengeFive.class);
	private static final String levelHash = "70b96195472adf3bf347cbc37c34489287969d5ba504ac2439915184d6e5dc49";
	private static String levelName = "CSRF Challenge 5";
	/**
	 * Allows users to set their CSRF attack string to complete this module. They should be using this to force users to visit their own pages that
	 * forces the victim to submit a post request to the CSRFChallengeTargetFive
	 * @param myMessage To Be stored as the users message for this module
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
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				Cookie tokenCookie = Validate.getToken(request.getCookies());
				Object tokenParmeter = request.getParameter("csrfToken");
				if(Validate.validateTokens(tokenCookie, tokenParmeter))
				{
					String myMessage = request.getParameter("myMessage");
					log.debug("User Submitted - " + myMessage);
					myMessage = Validate.makeValidUrl(myMessage);
					
					log.debug("Updating User's Stored Message");
					String ApplicationRoot = getServletContext().getRealPath("");
					String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, CsrfChallengeFive.levelHash);
					String userId = (String)ses.getAttribute("userStamp");
					Setter.setStoredMessage(ApplicationRoot, myMessage, userId, moduleId);
					
					log.debug("Retrieving user's class's forum");
					String classId = null;
					if(ses.getAttribute("userClass") != null)
						classId = (String)ses.getAttribute("userClass");
					String htmlOutput = Getter.getCsrfForumWithIframe(ApplicationRoot, classId, moduleId, csrfGenerics);
					
					log.debug("Outputting HTML");
					out.write(htmlOutput);
				}
			}
		}
		catch(Exception e)
		{
			out.write(errors.getString("error.funky"));
			log.fatal(levelName + " - " + e.toString());
		}
	}
}