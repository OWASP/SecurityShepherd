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
import org.owasp.encoder.Encode;


import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Insecure Direct Object Lesson
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
 * 
 * @author Mark Denihan
 */
public class DirectObjectLesson extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DirectObjectLesson.class);
	private static String levelName = "Insecure Direct Object Lesson";
	public static String levelhash = "fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100";
	private static String levelResult = "59e571b1e59441e76e0c85e5b49";
	/**
	 * System users are insecurely directed by their user name in a post request parameter. Users can abuse this to retrieve an administrator's information.
	 * @param username User name of profile to retrieve
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.directObject", locale);
		
		//Attempting to recover username of session that made request
		HttpSession ses = request.getSession(true);
		PrintWriter out = response.getWriter();
		out.print(getServletInfo());
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			try
			{
				String userName = request.getParameter("username");
				log.debug("User Submitted - " + userName);
				String ApplicationRoot = getServletContext().getRealPath("");
				log.debug("Servlet root = " + ApplicationRoot );
				String htmlOutput = new String();
				if(userName.equalsIgnoreCase("guest"))
				{
					log.debug("Guest Profile Found");
					htmlOutput = htmlGuest(bundle);
				}
				else if(userName.equalsIgnoreCase("admin"))
				{
					// Get key and add it to the output
					String userKey = Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName"));
					log.debug("Admin Profile Found");
					htmlOutput = htmlAdmin(bundle, userKey);
				}
				else
				{
					log.debug("No Profile Found");
					
					htmlOutput = "<h2 class='title'>" + bundle.getString("response.user") + ": " + bundle.getString("response.notFound") + "</h2><p>" + bundle.getString("response.user") + " '" + Encode.forHtml(userName) + "' " + bundle.getString("response.couldNotFind") + ".</p>";
				}
				log.debug("Outputting HTML");
				out.write(htmlOutput);
			}
			catch(Exception e)
			{
				out.write(errors.getString("error.funky"));
				log.fatal("Insecure Direct Object Lesson Lesson - " + e.toString());
			}
		}
		else
		{
			out.write(errors.getString("error.noSession"));
			log.error(levelName + " servlet accessed with no session");
		}
	}
	private static String htmlGuest (ResourceBundle bundle) 
	{
		return "<h2 class='title'>" + bundle.getString("response.user") + ": Guest</h2><table><tr><th>" + bundle.getString("response.age") + ":</th><td>22</td></tr>" +
			"<tr><th>" + bundle.getString("response.address") + ":</th><td>54 Kevin Street, Dublin</td></tr>" +
			"<tr><th>" + bundle.getString("response.email") + ":</th><td>guestAccount@securityShepherd.com</td></tr>" +
			"<tr><th>" + bundle.getString("response.message") + ":</th><td>" + bundle.getString("response.noMessage") + "</td></tr></table>";
	}
	
	private static String htmlAdmin (ResourceBundle bundle, String key)
	{
		return "<h2 class='title'>" + bundle.getString("response.user") + ": Admin</h2><table><tr><th>" + bundle.getString("response.age") + ":</th><td>43</td></tr>" +
			"<tr><th>" + bundle.getString("response.address") + ":</th><td>12 Bolton Street, Dublin</td></tr>" +
			"<tr><th>" + bundle.getString("response.email") + ":</th><td>administratorAccount@securityShepherd.com</td></tr>" +
			"<tr><th>" + bundle.getString("response.message") + ":</th>" +
			"<td>" + bundle.getString("result.resultKey") + ": " + key + "<a></a></td></tr></table>";
	}
}