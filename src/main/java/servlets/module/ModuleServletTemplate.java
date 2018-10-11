package servlets.module;

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


import utils.Hash;
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
 * @author name
 *
 */
public class ModuleServletTemplate 
extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ModuleServletTemplate.class);
	private static String levelName = "Level Name Here";
	public static String levelHash = "Level Hash Here";
	private static String levelResult = ""; // Put the Level Result Key here only if the level is not hardcoded in the database or mobile application
	/**
	 * Describe level here, and how a user is supposed to beat it
	 * @param aUserName Expected Parameters
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Dont Change any of this. This is logging player activity
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.folder.fileNameWithoutExtention", locale);
		try
		{
			//Get the session from the request
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses)) //Is this an active session?
			{
				//Valid Session, time to log who it is
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				
				boolean returnKey = false;
				
				//Template Users: Edit from here
				String aUserName = request.getParameter("aUserName");
				log.debug("User submitted aUserName: " + aUserName); //Log what the player submitted for your expected parameters
				
				//If you want to call a database function, this section if for you. All the way up until if(returnKey)
				//Get Running Context of Application to make Database Call with
				String applicationRoot = getServletContext().getRealPath("");
				String output = doLevelSqlStuff(applicationRoot, aUserName, bundle);
				log.debug("Logging in English. Going to Output " + output);
				String htmlOutput = "<h2 class='title'>" + bundle.getString("module.example.header") + "</h2>";
				if (output == null)
				{
					htmlOutput += "<p>" + bundle.getString("module.example.outputWasNull") + "/p>";
				}
				else if(output.startsWith("123"))
				{
					log.debug("Setting Error Message");
					htmlOutput += "<p>" + bundle.getString("example.error.123") + "</p>" +
							"<p>" + output + "</p>";
				}
				else
				{
					//Do some Database Stuff. Example assumes key is hardcoded and returned in the query from the database
					//If you want to return a user specific key if the user has used SQLi to bypass authentication or somthing, use the following bit of code for that
					returnKey = true;
				}
				if(returnKey)
				{
					//Something happened and now you want the user to be given a user specific key. then do this
					String userKey = Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName"));
					log.debug("User has compelted level");
					//Otherwise just set userKey to "resultKey" and use the rest of this snip (If key is hardcoded, make sure you set it that way in your database level entry)
					htmlOutput = "<h2 class='title'>" + bundle.getString("module.example.completedHeader") + "</h2>" +
							"<p>" +
							bundle.getString("module.example.theKeyIs") + " " +
							"<a>" + userKey + "</a>" +
							"</p>";
					
				}
				log.debug("Outputting HTML");
				out.write(htmlOutput);
			}
			else
			{
				//Dont change this error
				log.error(levelName + " accessed with no session");
			}
		}
		catch(Exception e)
		{
			//Dont change this error
			out.write(errors.getString("error.funky"));
			log.fatal(levelName + " - " + e.toString());
		}
	}
	
	public static String doLevelSqlStuff (String applicationRoot, String username, ResourceBundle bundle)
	{
		
		String result = new String();
		try 
		{
			//You will need to make a schema in the database/moduleSchemas.sql file, and define a user which can access it.
			//The details of this user need to be entered in a properties file in WEB-INF/challenges
			//The Name of that user need to be entered in the following funciton;
			Connection conn = Database.getChallengeConnection(applicationRoot, "nameOfPropertiesFile.properties");
			Statement stmt;
			stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM tb_users WHERE username = '" + username + "'");
			log.debug("Opening Result Set from query");
			for(int i = 0; resultSet.next(); i++)
			{
				log.debug("Row " + i + ": User ID = " + resultSet.getString(1));
				result = Encode.forHtml(resultSet.getString(1));
			}
			log.debug("That's All");
		} 
		catch (SQLException e)
		{
			log.debug("SQL Error caught - " + e.toString());
			result = bundle.getString("example.error") + ": " + Encode.forHtml(e.toString()); //Html Encode Error to prevent XSS
		}
		catch (Exception e)
		{
			log.fatal(bundle.getString("example.error") + ": " + Encode.forHtml(e.toString())); //Html Encode Error to prevent XSS
		}
		return result;
	}
}
