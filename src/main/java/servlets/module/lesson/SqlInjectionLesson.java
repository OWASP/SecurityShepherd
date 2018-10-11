package servlets.module.lesson;

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
	public static String levelHash = "e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594";
	// private static String levelResult = ""; // Stored in Vulnerable DB. Not User Specific
	/**
	 * Uses user input in an insecure fashion when executing queries in database. Vulnerable to SQL injection.
	 * @param aUserName User submitted filter for database results
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.sqlInjection", locale);
		
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				String aUserName = request.getParameter("aUserName");
				log.debug("User Submitted - " + aUserName);
				String ApplicationRoot = getServletContext().getRealPath("");
				log.debug("Servlet root = " + ApplicationRoot );
				String[][] output = getSqlInjectionResult(ApplicationRoot, aUserName);
				log.debug("output returned. [0][0] is " + output[0][0]);
				String htmlOutput = "<h2 class='title'>" + bundle.getString("response.searchResults") + "</h2>";
				if (output[0][0] == null)
				{
					htmlOutput += "<p>" + bundle.getString("response.noResults") + "</p>";
				}
				else if(output[0][0].equalsIgnoreCase("error"))
				{
					log.debug("Setting Error Message");
					htmlOutput += "<p>" + errors.getString("error.detected") + "</p>" +
							"<p>" + output[0][1] + "</p>";
				}
				else
				{
					log.debug("Adding table");
					int i = 0;
					log.debug("outputLength = " + output.length);
					htmlOutput += "<table><tr><th>" + bundle.getString("response.userId") + "</th><th>" + bundle.getString("response.userName") + "</th><th>" + bundle.getString("response.comment") + "</th></tr>";
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
			else
			{
				log.error(levelName + " accessed with no session");
				out.write(errors.getString("error.noSession"));
			}
		}
		catch(Exception e)
		{
			out.write(errors.getString("error.funky"));
			log.fatal(levelName + " - " + e.toString());
		}
	}
	
	public static String[][] getSqlInjectionResult (String ApplicationRoot, String username)
	{
		
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
				result[i][0] = Encode.forHtml(resultSet.getString(1));
				result[i][1] = Encode.forHtml(resultSet.getString(2));
				result[i][2] = Encode.forHtml(resultSet.getString(3));
			}
			log.debug("That's All");
		} 
		catch (SQLException e)
		{
			log.debug("SQL Error caught - " + e.toString());
			result[0][0] = "error";
			result[0][1] = Encode.forHtml(e.toString());
		}
		catch (Exception e)
		{
			log.fatal("Error: " + e.toString());
		}
		return result;
	}
}
