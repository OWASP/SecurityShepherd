package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Failure to Restrict URL Access Challenge 2 (Admin)
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
public class UrlAccess2Admin extends HttpServlet
{
	//Sql Challenge 4
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(UrlAccess2Admin.class);
	private static String levelResult = "40b675e3d404c52b36abe31d05842b283975ec62e8"; 
	private static String levelHash = "278fa30ee727b74b9a2522a5ca3bf993087de5a0ac72adff216002abf79146fa";
	private static String levelName = "URL Access 2 (Admin)";
	/**
	 * Users have to defeat SQL injection that blocks single quotes.
	 * The input they enter is also been filtered.
	 * @param theUserName User name used in database look up.
	 * @param thePassword User password used in database look up
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			Encoder encoder = ESAPI.encoder();
			try
			{
				String userData = request.getParameter("adminData");
				boolean tamperedRequest = !userData.equalsIgnoreCase("youAreAnAdminOfAwesomenessWoopWoop");
				if(!tamperedRequest)
					log.debug("No request tampering detected");
				else
					log.debug("User Submitted - " + userData);
				
				if(!tamperedRequest)
				{
					String userKey = Hash.generateUserSolution(levelResult, request.getCookies());
					htmlOutput = "<h2 class='title'>Admin Button Clicked</h2>"
						+ "<p>Hey Admin, Here is that key you are looking for: <a>"
						+ userKey
						+ "</a>. Enjoy!</p>";
				}
				else
					htmlOutput = "<h2 class='title'>Server Status Failure</h2>"
							+ "<p>Could not retrieve server status. Invalid userData.</p>"
							+ "<!-- " + encoder.encodeForHTML(userData) + " -->";
			}
			catch(Exception e)
			{
				out.write("An Error Occurred! You must be getting funky!");
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