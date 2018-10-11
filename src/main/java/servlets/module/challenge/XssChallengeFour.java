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
 * Cross Site Scripting Challenge Four control class.
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
public class XssChallengeFour extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(XssChallengeFour.class);
	private static final String levelHash = "06f81ca93f26236112f8e31f32939bd496ffe8c9f7b564bce32bd5e3a8c2f751";
	private static String levelName = "XSS Challenge 4";
	/**
	 * Cross Site Request Forgery safe Reflected XSS vulnerability. cannot be remotely exploited, and there fore only is executable against the person initiating the function.
	 * @param searchTerm To be spat back out at the user after been encoded for wrong HTML Context
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("Cross-Site Scripting Challenge Four Servlet");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.xss.xss4", locale);
		
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
					if(!searchTerm.startsWith("http"))
					{
						searchTerm = "https://www.owasp.org/index.php/OWASP_Security_Shepherd";
						userPost = "<a href=\"" + searchTerm + "\" alt=\"OWASP Security Shepherd\">" + searchTerm + "</a>";
					}
					else
					{
						
						searchTerm = XssFilter.encodeForHtml(searchTerm);
						userPost = "<a href=\"" + searchTerm + "\" alt=\"" + searchTerm + "\">" + searchTerm + "</a>";
						log.debug("After Encoding - " + searchTerm);
						if(FindXSS.search(userPost))
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
					}
					log.debug("Adding searchTerm to Html: " + searchTerm);
					htmlOutput += "<h2 class='title'>" + bundle.getString("response.yourPost") + "</h2>" +
						"<p>" + bundle.getString("response.linkPosted") + "</p> " +
						userPost +
						"</p>";
					out.write(htmlOutput);
				}
			}
			else
			{
				log.error(levelName + " servlet was accessed without a valid session");
				out.write(errors.getString("error.noSession"));
			}
		}
		catch(Exception e)
		{
			out.write(errors.getString("error.funky"));
			log.fatal("Cross Site Scripting Challenge 4 - " + e.toString());
		}
	}
}
