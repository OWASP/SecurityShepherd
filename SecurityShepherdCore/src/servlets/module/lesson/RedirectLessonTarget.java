package servlets.module.lesson;

import java.io.IOException;
import java.io.PrintWriter;

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
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateAdminSession(ses))
			{
				log.debug("Current User: " + ses.getAttribute("userName").toString());
				log.debug("RedirectLessonTarget Lesson Target Hit By Admin");
				out.write("<p>User Marked as completed Redirect Lesson</p>");
			}
			else
			{
				log.debug("RedirectLessonTarget Lesson Target Hit");
				out.write("<p>You must be an administrator to perform this function</p>");
			}
		}
		catch(Exception e)
		{
			log.error("RedirectLessonTarget Error: " + e.toString());
			out.write("You shouldn't be here!");
		}
	}
}
