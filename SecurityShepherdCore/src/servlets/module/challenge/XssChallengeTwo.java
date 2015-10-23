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
 * Cross Site Scripting Challenge Two
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
public class XssChallengeTwo extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(XssChallengeTwo.class);
	private static String levelName = "Cross Site Scripting Challenge Two";
	private static String levelHash = "t227357536888e807ff0f0eff751d6034bafe48954575c3a6563cb47a85b1e888";
	/**
	 * Cross Site Request Forgery safe Reflected XSS vulnerability. As there is no CSRF risk, this XSS flaw cannot be remotely exploited, and therefore is only is executable against the person initiating the function.
	 * @param searchTerm To be spat back out at the user after been filtered
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(levelHash + " Servlet Accessed");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.xss.xss2", locale);
		
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
					String searchTerm = request.getParameter("searchTerm");
					log.debug("User Submitted - " + searchTerm);
					searchTerm = XssFilter.levelTwo(searchTerm);
					log.debug("After Filtering - " + searchTerm);
					String htmlOutput = new String();
					if(FindXSS.search(searchTerm))
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("result.wellDone") + "</h2>" +
								"<p>" + bundle.getString("result.youDidIt") + "<br />" +
								bundle.getString("result.resultKey") + " <a>" +
									Hash.generateUserSolution(
											Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash
											), (String)ses.getAttribute("userName")
									)
								 +
								"</a>";
						log.debug(levelName + " completed");
					}
					log.debug("Adding searchTerm to Html: " + searchTerm);
					htmlOutput += "<h2 class='title'>" + bundle.getString("response.searchResults") + "</h2>" +
						"<p>" + bundle.getString("response.noResults") + " " +
						searchTerm +
						"</p>";
					log.debug("Outputting HTML");
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
			out.write(errors.getString("errors.funky"));
			log.fatal(levelName + " - " + e.toString());
		}
	}
}
