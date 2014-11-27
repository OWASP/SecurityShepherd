package servlets.lessons;

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

import utils.ShepherdExposedLogManager;
import dbProcs.Database;

/**
 * SQL Injection Lesson - Does not use User Specific Key
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
public class SqlInjectionLesson 
extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjectionLesson.class);
	private static String levelName = "SQL Injection Lesson";
	private static String levelHash = "e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594";
	private static String levelResult = ""; // Stored in Vulnerable DB. Not User Specific
	/**
	 * Uses user input in an insecure fashion when executing queries in database. Vulnerable to SQL injection.
	 * @param aUserName User submitted filter for database results
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
			ShepherdExposedLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
			//Attempting to recover username of session that made request
			try
			{
				if (request.getSession() != null)
				{
					HttpSession ses = request.getSession();
					String userName = (String) ses.getAttribute("decyrptedUserName");
					log.debug(userName + " accessed " + levelName + " Servlet");
				}
			}
			catch (Exception e)
			{
				log.debug(levelName + " Servlet Accessed");
				log.error("Could not retrieve user name from session");
			}
			
			String aUserName = request.getParameter("aUserName");
			log.debug("User Submitted - " + aUserName);
			String ApplicationRoot = getServletContext().getRealPath("");
			log.debug("Servlet root = " + ApplicationRoot );
			String[][] output = getSqlInjectionResult(ApplicationRoot, aUserName);
			log.debug("output returned. [0][0] is " + output[0][0]);
			String htmlOutput = "<h2 class='title'>Search Results</h2>";
			if (output[0][0] == null)
			{
				htmlOutput += "<p>No rows returned from that query! Make sure your <a>escaping</a> the string and changing the <a>boolean result</a> of the <a>WHERE</a> to be always true";
			}
			else if(output[0][0].equalsIgnoreCase("error"))
			{
				log.debug("Setting Error Message");
				htmlOutput += "<p>An error was detected!</p>" +
						"<p>" + output[0][1] + "</p>";
			}
			else
			{
				log.debug("Adding table");
				int i = 0;
				log.debug("outputLength = " + output.length);
				htmlOutput += "<table><tr><th>User Id</th><th>User Name</th><th>Comment</th></tr>";
				do
				{
					log.debug("Adding User " + output[i][1]);
					htmlOutput += "<tr><td>" + output[i][0] + "</td><td>" + output[i][1] + "</td><td>"
						+ output[i][2] + "</td></tr>";
					i++;
					
				}
				while(i < output.length && output[i][0] != null);
				htmlOutput += "</table>";			
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occurred! You must be getting funky!");
			log.fatal(levelName + " - " + e.toString());
		}
	}
	
	public static String[][] getSqlInjectionResult (String ApplicationRoot, String username)
	{
		Encoder encoder = ESAPI.encoder();
		String[][] result = new String[10][3];
		try 
		{
			Connection conn = Database.getSqlInjLessonConnection(ApplicationRoot);
			Statement stmt;
			stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM tb_users WHERE username = '" + username + "'");
			log.debug("Opening Result Set from query");
			for(int i = 0; resultSet.next(); i++)
			{
				log.debug("Row " + i + ": User ID = " + resultSet.getString(1));
				result[i][0] = encoder.encodeForHTML(resultSet.getString(1));
				result[i][1] = encoder.encodeForHTML(resultSet.getString(2));
				result[i][2] = encoder.encodeForHTML(resultSet.getString(3));
			}
			log.debug("That's All");
		} 
		catch (SQLException e)
		{
			log.debug("SQL Error caught - " + e.toString());
			result[0][0] = "error";
			result[0][1] = encoder.encodeForHTML(e.toString());
		}
		catch (Exception e)
		{
			log.fatal("Error: " + e.toString());
		}
		return result;
	}
}
