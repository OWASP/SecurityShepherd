package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.Hash;
import utils.ShepherdExposedLogManager;

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
public class fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100.class);
	/**
	 * System users are insecurely directed by their user name in a post request parameter. Users can abuse this to retrieve an administrator's information.
	 * @param username User name of profile to retrieve
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdExposedLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("Direct Object Refernce Lesson");
		PrintWriter out = response.getWriter();
		out.print(getServletInfo());
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
				htmlOutput = htmlGuest;
			}
			else if(userName.equalsIgnoreCase("admin"))
			{
				// Get key and add it to the output
				String userKey = Hash.generateUserSolution("59e571b1e59441e76e0c85e5b49", request.getCookies());
				log.debug("Admin Profile Found");
				htmlOutput = htmlAdmin + userKey + "<a></a></td></tr></table>";
			}
			else
			{
				log.debug("No Profile Found");
				Encoder encoder = ESAPI.encoder();
				htmlOutput = "<h2 class='title'>User: 404 - User Not Found</h2><p>User '" + encoder.encodeForHTML(userName) + "' could not be found or does not exist.</p>";
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occured! You must be getting funky!");
			log.fatal("Insecure Direct Object Lesson Lesson - " + e.toString());
		}
	}
	private static String htmlGuest = "<h2 class='title'>User: Guest</h2><table><tr><th>Age:</th><td>22</td></tr>" +
			"<tr><th>Address:</th><td>54 Kevin Street, Dublin</td></tr>" +
			"<tr><th>Email:</th><td>guestAccount@securityShepherd.com</td></tr>" +
			"<tr><th>Private Message:</th><td>No Private Message Set</td></tr></table>";
	private static String htmlAdmin = "<h2 class='title'>User: Admin</h2><table><tr><th>Age:</th><td>43</td></tr>" +
			"<tr><th>Address:</th><td>12 Bolton Street, Dublin</td></tr>" +
			"<tr><th>Email:</th><td>administratorAccount@securityShepherd.com</td></tr>" +
			"<tr><th>Private Message:</th>" +
			"<td>Result Key: ";
}