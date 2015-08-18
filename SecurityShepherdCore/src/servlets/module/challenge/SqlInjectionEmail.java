package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;

/**
 * SQL Injection Challenge Two - Does not use user specific keys
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
public class SqlInjectionEmail extends HttpServlet
{
	//SQL Challenge One
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjectionEmail.class);
	private static String levelName = "SQL Injection Challenge Two";
	private static String levelHash = "ffd39cb26727f34cbf9fce3e82b9d703404e99cdef54d2aa745f497abe070b";
	private static String levelResult = ""; // Stored in Vulnerable DB. Not user Specific
	/**
	 * This function is used to make a call to a database and process its results. The call made to the database is secured using an insufficient privilege. 
	 * Players must overcome this filter to complete the module
	 * @param userIdentity Used to filter database results
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			Encoder encoder = ESAPI.encoder();
			try
			{
				String userIdentity = request.getParameter("userIdentity");
				log.debug("User Submitted - " + userIdentity);
				if(Validate.isValidEmailAddress(userIdentity))
				{
					log.debug("Filtered to " + userIdentity);
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Connection to Database");
					Connection conn = Database.getChallengeConnection(ApplicationRoot, "SqlChallengeEmail");
					Statement stmt = conn.createStatement();
					log.debug("Gathering result set");
					ResultSet resultSet = stmt.executeQuery("SELECT * FROM customers WHERE customerAddress = '" + userIdentity + "'");
					
					int i = 0;
					htmlOutput = "<h2 class='title'>Search Results</h2>";
					htmlOutput += "<table><tr><th>Name</th><th>Address</th><th>Comment</th></tr>";
					
					log.debug("Opening Result Set from query");
					while(resultSet.next())
					{
						log.debug("Adding Customer " + resultSet.getString(2));
						htmlOutput += "<tr><td>"
							+ encoder.encodeForHTML(resultSet.getString(2)) + "</td><td>" 
							+ encoder.encodeForHTML(resultSet.getString(3)) + "</td><td>"
							+ encoder.encodeForHTML(resultSet.getString(4)) + "</td></tr>";
						i++;
					}
					conn.close();
					htmlOutput += "</table>";
					if(i == 0)
					{
						htmlOutput = "<p>There were no results found in your search</p>";
					}
				}
				else
				{
					htmlOutput = new String("<h2 class='title'>Search Error</h2><p>Invalid Email Address was submitted");
				}
			}
			catch (SQLException e)
			{
				log.debug("SQL Error caught - " + e.toString());
				htmlOutput += "<p>An error was detected!</p>" +
					"<p>" + encoder.encodeForHTML(e.toString()) + "</p>";
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
