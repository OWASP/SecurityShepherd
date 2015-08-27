package servlets.module.lesson;

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

/**
 * Unvalidated and redirect lesson targer. Does not return result key
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
public class RedirectLessonTarget extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(RedirectLessonTarget.class);
	
	public void doGet (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("Redirect Lesson Target Lesson Target Servlet");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.unvalidatedRedirect", locale);
		
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateAdminSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug("Current User: " + ses.getAttribute("userName").toString());
				log.debug("RedirectLessonTarget Lesson Target Hit By Admin");
				out.write("<p>" + bundle.getString("target.completed") + "</p>");
			}
			else
			{
				log.debug("RedirectLessonTarget Lesson Target Hit");
				out.write("<p>" + bundle.getString("target.completed") + "</p>");
			}
		}
		catch(Exception e)
		{
			log.error("RedirectLessonTarget Error: " + e.toString());
			out.write(errors.getString("error.shouldNotBeHere"));
		}
	}
}
