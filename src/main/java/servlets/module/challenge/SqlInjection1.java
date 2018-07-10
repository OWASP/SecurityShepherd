package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;

/**
 * SQL Injection Challenge 1 - Does not use User specific keys
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
public class SqlInjection1 extends HttpServlet
{
	//SQL Challenge 1
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjection1.class);
	private static String levelName = "SQL Injection Challenge 1";
	public static String levelHash = "e1e109444bf5d7ae3d67b816538613e64f7d0f51c432a164efc8418513711b0a";
	// private static String levelResult = ""; //Stored in vulnerable DB. Not user Specific
	/**
	 * This function is used to make a call to a database and process its results. The call made to the database is secured using an insufficient privilege. 
	 * Players must overcome this filter to complete the module
	 * @param aUserId Used to filter database results
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.sqli.sqli1", locale);
		
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			
			String htmlOutput = new String();
			
			try
			{
				String aUserId = request.getParameter("aUserId");
				log.debug("User Submitted - " + aUserId);
				String ApplicationRoot = getServletContext().getRealPath("");
				log.debug("Servlet root = " + ApplicationRoot );
				
				log.debug("Getting Connection to Database");
				Connection conn = Database.getChallengeConnection(ApplicationRoot, "SqlChallengeOne");
				Statement stmt = conn.createStatement();
				log.debug("Gathering result set");
				ResultSet resultSet = stmt.executeQuery("SELECT * FROM customers WHERE customerId = \"" + aUserId + "\"");
				
				int i = 0;
				htmlOutput = "<h2 class='title'>" + bundle.getString("response.searchResults")+ "</h2>";
				htmlOutput += "<table><tr><th>"+ bundle.getString("response.table.name") +"</th><th>"+ bundle.getString("response.table.address") +"</th><th>"+ bundle.getString("response.table.comment") +"</th></tr>";
				
				log.debug("Opening Result Set from query");
				while(resultSet.next())
				{
					log.debug("Adding Customer " + resultSet.getString(2));
					htmlOutput += "<tr><td>"
						+ Encode.forHtml(resultSet.getString(2)) + "</td><td>" 
						+ Encode.forHtml(resultSet.getString(3)) + "</td><td>"
						+ Encode.forHtml(resultSet.getString(4)) + "</td></tr>";
					i++;
				}
				htmlOutput += "</table>";
				if(i == 0)
				{
					htmlOutput = "<p>"+bundle.getString("response.noResults")+"</p>";
				}
			}
			catch (SQLException e)
			{
				log.debug("SQL Error caught - " + e.toString());
				htmlOutput += "<p>"+errors.getString("error.detected")+"</p>" +
					"<p>" + Encode.forHtml(e.toString()) + "</p>";
			}
			catch(Exception e)
			{
				out.write(errors.getString("error.funky"));
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
