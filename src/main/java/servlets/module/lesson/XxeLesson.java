package servlets.module.lesson;

import dbProcs.Getter;
import org.apache.log4j.Logger;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Cross Site Scripting Lesson
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
 * @author ismisepaul
 *
 */
public class XxeLesson
extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(XxeLesson.class);
	private static String levelName = "XXE Lesson";
	private static String levelHash = "57dda1bf9a2ca1c34e04f815491ef40836d9b710179cd19754ec5b3c31f27d1a";

	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(levelName + " Servlet Accessed");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.xxe", locale);
		
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
						ses.getAttribute("userName").toString());
				log.debug(levelName + " accessed by: " + ses.getAttribute("userName").toString());
				Cookie tokenCookie = Validate.getToken(request.getCookies());
				Object tokenParmeter = request.getParameter("csrfToken");
				if(Validate.validateTokens(tokenCookie, tokenParmeter))
				{
					String emailAddr = request.getParameter("emailAddr");
					String htmlOutput = new String();
					if(Validate.isValidEmailAddress(emailAddr))
					{
						log.debug("User Submitted - " + emailAddr);

						log.debug("Adding emailAddr to Html: " + emailAddr);
						htmlOutput += "<p>" + bundle.getString("response.noResults") + " '" +
								emailAddr + "'</p>";
						log.debug("Outputting HTML");
						out.write(htmlOutput);
					}
					else
					{
						htmlOutput += "<p>" + bundle.getString("response.invalid.email") + "</p>";
						out.write(htmlOutput);
					}
				}
			}
			else
			{
				log.error(levelName + " accessed with no session");
				out.write(errors.getString("error.noSession"));
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
