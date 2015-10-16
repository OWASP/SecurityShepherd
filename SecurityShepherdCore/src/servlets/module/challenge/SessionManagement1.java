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

import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Session Management Challenge One
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
public class SessionManagement1 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SessionManagement1.class);
	private static String levelName = "Session Management Challenge One";
	public static String levelHash = "dfd6bfba1033fa380e378299b6a998c759646bd8aea02511482b8ce5d707f93a";
	private static String levelResult = "db7b1da5d7a43c7100a6f01bb0c";
	/**
	 * Users must take advance of the broken session management in this application by modifying the tracking cookie "checksum" which is encoded in base 64. They must modify this cookie to be equal to administrator to access the result key.
	 * @param upgraeUserToAdmin Red herring 
	 * @param returnPassword Red herring 
	 * @param adminDetected Red herring 
	 * @param checksum Cookie encoded base 64 that manages who is signed in to the sub schema
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.sessionManagement.sessionManagement1", locale);
		
		try
		{
			//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				Cookie userCookies[] = request.getCookies();
				int i = 0;
				Cookie theCookie = null;
				for(i = 0; i < userCookies.length; i++)
				{
					if(userCookies[i].getName().compareTo("checksum") == 0)
					{
						theCookie = userCookies[i];
						break; //End Loop, because we found the token
					}
				}
				String htmlOutput = null;
				if(theCookie != null)
				{
					log.debug("Cookie value: " + theCookie.getValue());
					
					if(theCookie.getValue().equals("dXNlclJvbGU9YWRtaW5pc3RyYXRvcg"))
					{
						log.debug("Challenge Complete");
						// Get key and add it to the output
						String userKey = Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName"));
						htmlOutput = "<h2 class='title'>" + bundle.getString("response.adminClub") + "</h2>" +
								"<p>" +
								bundle.getString("response.welcomeAdmin") + 
								"<a>" + userKey + "</a>" +
								"</p>";
					}
				}
				if(htmlOutput == null)
				{
					log.debug("Challenge Not Complete");
					boolean hackDetected = false;
					hackDetected = !(request.getParameter("adminDetected") != null && request.getParameter("returnPassword") != null && request.getParameter("upgradeUserToAdmin") != null);
					if(!hackDetected)
						hackDetected = !(request.getParameter("adminDetected").toString().equalsIgnoreCase("false") &&
										request.getParameter("adminDetected").toString().equalsIgnoreCase("false") &&
										request.getParameter("adminDetected").toString().equalsIgnoreCase("false"));
					
					
					if(!hackDetected)
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("response.notAdmin") + "</h2>" +
								"<p>" +
								bundle.getString("response.notAdminMessage") +
								"</p>";
					}
					else
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("response.hackDetected") + "</h2>" +
								"<p>" +
								bundle.getString("response.hackDetectedMessage") +
								"</p>";
					}
				}
				log.debug("Outputting HTML");
				out.write(htmlOutput);
			}
			else
			{
				log.error(levelName + " servlet accessed with no session");
			}
		}
		catch(Exception e)
		{
			out.write(errors.getString("error.funky"));
			log.fatal(levelName + " - " + e.toString());
		}
	}
}
