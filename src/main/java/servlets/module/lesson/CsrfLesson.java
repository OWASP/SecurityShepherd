package servlets.module.lesson;

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
import org.owasp.encoder.Encode;


import dbProcs.Getter;
import utils.FindXSS;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * CSRF Lesson
 * Currently does not use user specific result key because of current CSRF blanket rule
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
public class CsrfLesson extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfLesson.class);
	private static String levelName = "CSRF Lesson";
	private static String levelHash = "ed4182af119d97728b2afca6da7cdbe270a9e9dd714065f0f775cd40dc296bc7";
	/**
	 * User submission is parsed for a valid HTML IMG tag. The SRC attribute of this tag is then used to construct a URL object. This URL object is then checked to ensure a valid attack
	 * @param falseId User's session stored tempId
	 * @param messageForAdmin CSRF Submission
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(levelName + " Servlet Accessed");
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.csrfLesson", locale);
		
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug("Current User: " + ses.getAttribute("userName").toString());
				Cookie tokenCookie = Validate.getToken(request.getCookies());
				Object tokenParmeter = request.getParameter("csrfToken");
				if(Validate.validateTokens(tokenCookie, tokenParmeter))
				{
					String falseId = (String) ses.getAttribute("falseId");
					log.debug("falseId = " + falseId);
					String messageForAdmin = request.getParameter("messageForAdmin").toLowerCase();
					log.debug("User Submitted - " + messageForAdmin);
					
					String htmlOutput = new String();
					boolean validLessonAttack = FindXSS.findCsrfAttackUrl(messageForAdmin, "/root/grantComplete/csrflesson", "userId", falseId);
					
					if(validLessonAttack)
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("result.wellDone") + "</h2>" +
								bundle.getString("result.youDidIt") + "<br />" +
								bundle.getString("result.theKeyIs") + " <a>" + 
									Hash.generateUserSolution(
											Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash
											), (String)ses.getAttribute("userName")
									)
								+
								"</a>";
					}
					log.debug("Adding searchTerm to Html: " + messageForAdmin);
					htmlOutput += "<h2 class='title'>" + bundle.getString("challenge.messageSent") + "</h2>" +
						"<p><table><tr><td>" + bundle.getString("challenge.sentTo") + ": </td><td>administrator@SecurityShepherd.com</td></tr>" +
						"<tr><td>" + bundle.getString("challenge.message") + ": </td><td> " +
						"<img src=\"" + Encode.forHtmlAttribute(messageForAdmin) + "\"/>" +
						"</td></tr></table></p>";
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
		log.debug("End of " + levelName + " Servlet");
	}
}
