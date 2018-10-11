package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import org.owasp.encoder.Encode;


import dbProcs.Database;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Failure to Restrict URL Access Challenge 3 (UserList)
 * <br/><br/>
 * This class is the target functionality for the challenge.
 * The information required to find this admin function is 
 * contained in the javascript of the JSP page associated with the level. This level returns
 * a user specific key.
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
public class UrlAccess3UserList extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(UrlAccess3UserList.class);
	private static String levelName = "URL Access 3 (UserList)";
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			
			try
			{
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
				String currentUser = new String("aGuest");
				if(theCookie != null)
				{
					log.debug("Cookie value: " + theCookie.getValue());
					byte[] decodedCookieBytes = Base64.decodeBase64(theCookie.getValue());
					String decodedCookie = new String(decodedCookieBytes, "UTF-8");
					log.debug("Decoded Cookie: " + decodedCookie);
					currentUser = decodedCookie;
				}
				String ApplicationRoot = getServletContext().getRealPath("");
				Connection conn = Database.getChallengeConnection(ApplicationRoot, "UrlAccessThree");
				PreparedStatement callstmt;
				callstmt = conn.prepareStatement("SELECT userName FROM users WHERE userRole = \"admin\" OR userName = \"" + currentUser + "\";");
				log.debug("Getting User List");
				htmlOutput = new String();
				ResultSet rs = callstmt.executeQuery();
				while(rs.next())
				{
					htmlOutput += Encode.forHtml(rs.getString(1)) + "<br>";
					if(rs.getString(1).equalsIgnoreCase("MrJohnReillyTheSecond"))
					{
						log.debug("Super Admin contained in response");
					}
				}
			}
			catch(Exception e)
			{
				htmlOutput = new String(errors.getString("error.funky"));
				log.fatal(levelName + " - " + e.toString());
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}