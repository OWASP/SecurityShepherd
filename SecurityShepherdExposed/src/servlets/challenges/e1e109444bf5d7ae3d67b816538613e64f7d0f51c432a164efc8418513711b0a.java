package servlets.challenges;

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

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.ShepherdExposedLogManager;
import utils.SqlFilter;
import dbProcs.Database;

/**
 * SQL Injection Challenge 3 - Does not use User specific keys
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
public class e1e109444bf5d7ae3d67b816538613e64f7d0f51c432a164efc8418513711b0a extends HttpServlet
{
	//SQL Challenge 2
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(e1e109444bf5d7ae3d67b816538613e64f7d0f51c432a164efc8418513711b0a.class);
	
	/**
	 * This function is used to make a call to a database and process its results. The call made to the database is secured using an insufficient privilege. 
	 * Players must overcome this filter to complete the module
	 * @param aUserId Used to filter database results
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdExposedLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		String htmlOutput = new String();
		Encoder encoder = ESAPI.encoder();
		try
		{
			String aUserId = request.getParameter("aUserId");
			log.debug("User Submitted - " + aUserId);
			aUserId = SqlFilter.levelTwo(aUserId);
			log.debug("Filtered to " + aUserId);
			String ApplicationRoot = getServletContext().getRealPath("");
			log.debug("Servlet root = " + ApplicationRoot );
			
			log.debug("Getting Connection to Database");
			Connection conn = Database.getChallengeConnection(ApplicationRoot, "SqlChallengeTwo");
			Statement stmt = conn.createStatement();
			log.debug("Gathering result set");
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM customers WHERE customerId = '" + aUserId + "'");
			
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
			htmlOutput += "</table>";
			if(i == 0)
			{
				htmlOutput = "<p>There were no results found in your search</p>";
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
			out.write("An Error Occured! You must be getting funky!");
			log.fatal("SQL Injection Lesson - " + e.toString());
		}
		log.debug("outputing HTML");
		out.write(htmlOutput);
	}
}
