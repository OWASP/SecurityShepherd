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

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import dbProcs.Getter;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Failure to Restrict URL Access 3
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
public class UrlAccess3 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(UrlAccess3.class);
	private static String levelName = "Failure to Restrict URL Access 3";
	private static String levelHash = "e40333fc2c40b8e0169e433366350f55c77b82878329570efa894838980de5b4";
	/**
	 * Users must take advance of the broken session management in this application by 
	 * modifying the tracking cookie "currentPerson" which is encoded in Base64. 
	 * They must modify this cookie to be equal a super admin to access the result key.
	 * @param userId Red herring that is pre set to d3d9446802a44259755d38e6d163e820
	 * @param secure Red herring that is pre set to true 
	 * @param adminDetected Red herring 
	 * @param currentPerson Cookie encoded base64 that manages who is signed in to the sub schema
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		String redherringOne = new String("userId");
		String redherringTwo = new String("secure");
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.urlAccess.urlAccess3", locale);
		
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
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
					if(userCookies[i].getName().compareTo("currentPerson") == 0)
					{
						theCookie = userCookies[i];
						break; //End Loop, because we found the token
					}
				}
				String htmlOutput = null;
				if(theCookie != null)
				{
					log.debug("Cookie value: " + theCookie.getValue());
					byte[] decodedCookieBytes = Base64.decodeBase64(theCookie.getValue());
					String decodedCookie = new String(decodedCookieBytes, "UTF-8");
					log.debug("Decoded Cookie: " + decodedCookie);

					if(decodedCookie.equals("MrJohnReillyTheSecond"))
					{
						log.debug("Super Admin Cookie detected");
						// Get key and add it to the output
						String userKey = Hash.generateUserSolution(Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash), (String)ses.getAttribute("userName"));
						htmlOutput = "<h2 class='title'>" + bundle.getString("admin.superAdminClub") + "</h2>" +
								"<p>" +
								bundle.getString("admin.superAdminClub.keyMessage") + " " +
								"<a>" + userKey + "</a>" +
								"</p>";
					}
					else if (!decodedCookie.equals("aGuest"))
					{
						log.debug("Tampered role cookie detected: " + decodedCookie);
						htmlOutput = "<!-- " + bundle.getString("response.invalidUser") + " -->";
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
				if(htmlOutput == null)
				{
					log.debug("Challenge Not Complete");
					boolean hackDetected = false;
					boolean badUserId = false;
					hackDetected = !(request.getParameter(redherringOne) != null && request.getParameter(redherringTwo) != null);
					if(!hackDetected)
					{
						String paramOne = request.getParameter(redherringOne).toString();
						String paramTwo = request.getParameter(redherringTwo).toString();
						log.debug("Param value of " + redherringOne + ":" + paramOne);
						log.debug("Param value of " + redherringTwo + ":" + paramTwo);
						badUserId = paramOne.equalsIgnoreCase("d3d9446802a44259755d38e6d163e820");
						hackDetected = !badUserId && !paramTwo.equalsIgnoreCase("true");
					}
					if(!hackDetected)
					{
						htmlOutput = "<h2 class='title'>" + bundle.getString("response.notSuperAdmin") + "</h2>" +
								"<p>" +
								bundle.getString("response.notSuperAdmin.message") +
								"</p>";
					}
					else
					{
						if(badUserId)
						{
							htmlOutput = "<h2 class='title'>" + bundle.getString("response.whoAreYou") + "</h2>" +
								"<p>" +
								bundle.getString("response.whoAreYou.message") + 
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
