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
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Session Management Challenge Eight
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
public class SessionManagement8 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SessionManagement8.class);
	private static String levelName = "Session Management Challenge Eight";
	private static String levelHash = "714d8601c303bbef8b5cabab60b1060ac41f0d96f53b6ea54705bb1ea4316334";
	/**
	 * Users must take advance of the broken session management in this application by modifying the tracking cookie "challengeRole" which is encoded in ATOM-128. They must modify this cookie to be equal to superuser to access the result key.
	 * @param returnUserRole Red herring 
	 * @param returnPassword Red herring 
	 * @param adminDetected Red herring 
	 * @param challengeRole Cookie encoded ATOM-128 that manages who is signed in to the sub schema
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		String redherringOne = new String("returnUserRole");
		String redherringTwo = new String("returnPassword");
		String redherringThr = new String("adminDetected");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.sessionManagement.sessionManagement8", locale);
		
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
					if(userCookies[i].getName().compareTo("challengeRole") == 0)
					{
						theCookie = userCookies[i];
						break; //End Loop, because we found the token
					}
				}
				String htmlOutput = new String();
				if(theCookie != null)
				{
					log.debug("Cookie value: " + theCookie.getValue());
					
					if(theCookie.getValue().equals("nmHqLjQknlHs"))
					{
						log.debug("Super User Cookie detected");
						// Get key and add it to the output
						String userKey = Hash.generateUserSolution(Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash), (String)ses.getAttribute("userName"));
						htmlOutput = "<h2 class='title'>" + bundle.getString("response.superUserClub") + "</h2>" +
								"<p>" +
								bundle.getString("response.welcomeSuperUser") + " " +
								"<a>" + userKey + "</a>" +
								"</p>";
					}
					else if (!theCookie.getValue().equals("LmH6nmbC"))
					{
						log.debug("Tampered role cookie detected: " + theCookie.getValue());
						htmlOutput += "<!-- " + bundle.getString("response.invalidRole") + " -->";
					}
					else
					{
							log.debug("No change to role cookie submitted");
					}
				}
				else
				{
					log.debug("No Role Cookie Submitted");
				}
				if(htmlOutput.isEmpty())
				{
					log.debug("Challenge Not Complete");
					boolean hackDetected = false;
					hackDetected = !(request.getParameter(redherringOne) != null && request.getParameter(redherringTwo) != null && request.getParameter(redherringThr) != null);
					if(!hackDetected)
					{
						String paramOne = request.getParameter(redherringOne).toString();
						String paramTwo = request.getParameter(redherringTwo).toString();
						String paramThr = request.getParameter(redherringThr).toString();
						log.debug("Param value of " + redherringOne + ":" + paramOne);
						log.debug("Param value of " + redherringTwo + ":" + paramTwo);
						log.debug("Param value of " + redherringThr + ":" + paramThr);
						hackDetected = !(paramOne.equalsIgnoreCase("false") && paramTwo.equalsIgnoreCase("false") && paramThr.equalsIgnoreCase("false"));
					}
					if(!hackDetected)
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("response.notPrivileged") + "</h2>" +
								"<p>" +
								bundle.getString("response.notPrivileged.message") +
								"</p>";
					}
					else
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("response.hackDetected") + "</h2>" +
								"<p>" +
								bundle.getString("response.hackDetected.message") + 
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
