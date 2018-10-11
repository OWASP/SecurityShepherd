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

import dbProcs.Getter;
import utils.FindXSS;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;
import utils.XssFilter;
/**
 * Cross Site Scripting Challenge Five control class.
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
public class XssChallengeFive extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(XssChallengeFive.class);
	private static final String levelHash = "f37d45f597832cdc6e91358dca3f53039d4489c94df2ee280d6203b389dd5671";
	private static String levelName = "XSS Challenge 5";
	/**
	 * Cross Site Request Forgery safe Reflected XSS vulnerability. cannot be remotely exploited, and there fore only is executable against the person initiating the function.
	 * @param searchTerm To be spat back out at the user after been encoded for wrong HTML Context
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("Cross-Site Scripting Challenge Five Servlet");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.xss.xss5", locale);
		
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
					String htmlOutput = new String();
					String userPost = new String();
					String searchTerm = request.getParameter("searchTerm");
					log.debug("User Submitted - " + searchTerm);
					searchTerm = XssFilter.badUrlValidate(searchTerm);
					userPost = "<a href=\"" + searchTerm + "\">Your HTTP Link!</a>";
					log.debug("After WhiteListing - " + searchTerm);
					
					boolean xssDetected = FindXSS.search(userPost);
					if(xssDetected)
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("result.wellDone") + "</h2>" +
								"<p>" + bundle.getString("result.youDidIt") + "<br />" +
								bundle.getString("result.resultKey") + " <a>" +
									Hash.generateUserSolution(
											Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash),
										(String)ses.getAttribute("userName")
									)
								+ "</a>";
					}
					log.debug("Adding searchTerm to Html: " + searchTerm);
					htmlOutput += "<h2 class='title'>" + bundle.getString("response.yourPost") + "</h2>" +
							"<p>" + bundle.getString("response.linkPosted") + "</p> " +
							userPost +
							"</p>";
					out.write(htmlOutput);
				}
			}
		}
		catch(Exception e)
		{
			out.write(errors.getString("error.funky"));
			log.fatal("Cross Site Scripting Challenge 5 - " + e.toString());
		}
	}
}
