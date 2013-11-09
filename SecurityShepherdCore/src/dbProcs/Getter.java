package dbProcs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.ExposedServer;


/** 
 * Used to retrieve information from the Database
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
 *  @author Mark					
 */
public class Getter 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(Getter.class);
	
	/**
	 * This method hashes the user submitted password and sends it to the database.
	 * The database does the rest of the work, including Brute Force prevention.
	 * @param userName The submitted user name to be used in authentication process
	 * @param password The submitted password in plain text to be used in authentication
	 * @return A string array made up of nothing or information to be consumed by the initiating authentication process.
	 */
	public static String[] authUser (String ApplicationRoot, String userName, String password)
	{
		String[] result = null;
		log.debug("$$$ Getter.authUser $$$");
		log.debug("userName = "  + userName);
		
		boolean userFound = false;
		boolean goOn = false;
		Connection conn = Database.getConnection(ApplicationRoot);
		try 
		{
			//See if user Exists
			CallableStatement callstmt = conn.prepareCall("call userFind(?)");
			log.debug("Gathering userFind ResultSet");
			callstmt.setString(1, userName);
			ResultSet userFind = callstmt.executeQuery();
			log.debug("Opening Result Set from userFind");
			try
			{
				userFind.next();
				log.debug("User Found"); //User found if a row is in the database, this line will not work if the result set is empty
				userFound = true;
			}
			catch(Exception e)
			{
				log.debug("User did not exist");
				userFound = false;
			}
			if(userFound)
			{
				//Authenticate User
				callstmt = conn.prepareCall("call authUser(?, ?)");
				log.debug("Gathering authUser ResultSet");
				callstmt.setString(1, userName);
				callstmt.setString(2, password);
				ResultSet loginAttempt = callstmt.executeQuery();
				log.debug("Opening Result Set from authUser");
				try
				{
					loginAttempt.next();
					goOn = true; //Valid password for user submitted
				}
				catch (SQLException e)
				{
					//... Outer Catch has preference to this one for some reason... This code is never reached!
					// But I'll leave it here just incase. That includes the else block if goOn is false
					log.debug("Incorrect Credentials");
					goOn = false;
				}
				if(goOn)
				{
					//ResultSet Not Empty => Credentials Correct
					result = new String[5];
					result[0] = loginAttempt.getString(1); //Id
					result[1] = loginAttempt.getString(2); //userName
					result[2] = loginAttempt.getString(3); //role
					result[4] = loginAttempt.getString(6); //classId
					if (loginAttempt.getBoolean(5)) //Checking for temp password flag, if true, index View will prompt to change
						result[3] = "true";
					else
						result[3] = "false";
					if (!result[1].equals(userName)) //If somehow this functionality has been compromised to sign in as other users, this will limit the expoitability. But the method is sql injection safe, so it should be ok
					{
						log.fatal("User Name used ("+ userName +") and User Name retrieved ("+ result[1] +") were not the Same. Nulling Result");
						result = null;
					}
					else
					{
						log.debug("User '" + userName + "' has logged in");
						//Before finishing, check if user had a badlogin history, if so, Clear it
						if(loginAttempt.getInt(4) > 0)
						{
							log.debug("Clearing Bad Login History");
							callstmt = conn.prepareCall("call userBadLoginReset(?)");
							callstmt.setString(1, result[0]);
							callstmt.execute();
							log.debug("userBadLoginReset executed!");
						}
					}
					//User has logged in, or a Authentication Bypass was detected... You never know! Better safe than sorry	
					return result;
				}
				else
				{
					// Empty Result Set, But the user exists => Incorrect Password
					// Run increment bad login count / temp lock user's account
					//Setting userFound to False so that an exception isnt triggered here that sets the function off again
					userFound = false;
					callstmt = conn.prepareCall("call userLock(?)");
					log.debug("Running account lock function on user '" + userName + "'");
					callstmt.setString(1, userName);
					callstmt.execute();
					log.debug("userLock Executed");
				}
			}
		} 
		catch (SQLException e) 
		{
			log.error("Login Failure: " + e.toString());
			if(userFound)
			{
				try
				{
					CallableStatement callstmt = conn.prepareCall("call userLock(?)");
					log.debug("Running account lock function on user '" + userName + "'");
					callstmt.setString(1, userName);
					callstmt.execute();
					log.debug("userLock Executed");
				}
				catch (SQLException e1)
				{
					log.fatal("Could not run userLock on user: " + e1.toString());
				}
			}
		}
		Database.closeConnection(conn);
		log.debug("$$$ End authUser $$$");
		return result;
	}
	
	/**
	 * Used to determine if a user has completed a module already
	 * @param ApplicationRoot The current running context of an application
	 * @param moduleId The module identifier 
	 * @param userId The user identifier
	 * @return The result key of the module if the user has not completed the level
	 */
	public static String checkPlayerResult(String ApplicationRoot, String moduleId, String userId)
	{
		log.debug("*** Setter.checkPlayerResult ***");
		
		String result = null;
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			log.debug("Prepairing userUpdateResult call");
			CallableStatement callstmnt = conn.prepareCall("call userCheckResult(?, ?)");
			callstmnt.setString(1, moduleId);
			callstmnt.setString(2, userId);
			log.debug("Executing userCheckResult");
			ResultSet resultSet = callstmnt.executeQuery();
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch(SQLException e)
		{
			log.error("userCheckResult Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END checkPlayerResult ***");
		return result;
	}
	
	/**
	 * Used to decipher whether or not a user exists as a player
	 * @param userId The user identifier of the player to be found
	 * @return A boolean reflecting the state of existence of the player
	 */
	public static boolean findPlayerById (String ApplicationRoot, String userId)
	{
		log.debug("*** Getter.findPlayerById ***");
		boolean userFound = false;
		//Get connection
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call playerFindById(?)");
			log.debug("Gathering playerFindById ResultSet");
			callstmt.setString(1, userId);
			ResultSet userFind = callstmt.executeQuery();
			log.debug("Opening Result Set from playerFindById");
			userFind.next(); //This will throw an exception if player not found
			log.debug("Player Found"); //This line will not execute if player not found
			userFound = true;
		}
		catch(Exception e)
		{
			log.error("Player did not exist: " + e.toString());
			userFound = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END findPlayerById ***");
		return userFound;
	}
	
	/**
	 * Used to gather all module information for internal functionality. This method is used in creating View's or in control class operations
	 * @param ApplicationRoot The current runing context of the application
	 * @return An ArrayList of String arrays that contain the module identifier, module name, module type and module category of each module in the core database.
	 */
	public static ArrayList<String[]> getAllModuleInfo (String ApplicationRoot)
	{
		log.debug("*** Getter.getAllModuleInfo ***");
		ArrayList<String[]> modules = new ArrayList<String[]>();
		
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleGetAll()");
			log.debug("Gathering moduleGetAll ResultSet");
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetAll");
			int i = 0;
			while(resultSet.next())
			{
				String[] result = new String[4];
				i++;
				result[0] = resultSet.getString(1); //moduleId
				result[1] = resultSet.getString(2); //moduleName
				result[2] = resultSet.getString(3); //moduleType
				result[3] = resultSet.getString(4); //mdouleCategory
				modules.add(result);
			}
			log.debug("Returning Array list with " + i + " enteries.");
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getAllModuleInfo ***");
		return modules;
	}
	
	/**
	 * Returns HTML menu for challenges. Challenges are only referenced by their id, 
	 * The user will have to go through another servlet to get the module's View address
	 * @param ApplicationRoot The current running context of the application
	 * @return HTML menu for challenges	
	 */
	public static String getChallenges (String ApplicationRoot, String userId)
	{
		log.debug("*** Getter.getChallenges ***");
		String output = new String();
		//Encoder to prevent XSS
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleAllInfo(?, ?)");
			callstmt.setString(1, "challenge");
			callstmt.setString(2, userId);
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet challenges = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			String challengeCategory = new String();
			int rowNumber = 0; // Identifies the first row, ie the start of the list. This is sliglty different output to every other row
			while(challenges.next())
			{
				if(!challengeCategory.equalsIgnoreCase(challenges.getString(2)))
				{
					challengeCategory = challenges.getString(2);
					log.debug("New Category Detected: " + challengeCategory);
					if(rowNumber > 0) //output prepaired for Every row after row 1
						output += "</ul></li><li><a href='javascript:;' class='challengeHeader' >" + encoder.encodeForHTML(challengeCategory)+ "</a><ul class='challengeList' style='display: none;'>";
					else //output prepaired for First row in entire challenge
						output += "<li><a href='javascript:;' class='challengeHeader'>" + encoder.encodeForHTML(challengeCategory)+ "</a><ul class='challengeList' style='display: none;'>";
					log.debug("Compiling Challenge Category - " + challengeCategory);
				}
				output += "<li>"; //Starts next LI element
				if(challenges.getString(4) != null)
				{
					output += "<img src='css/images/completed.gif'/>"; //Completed marker
				}
				else
				{
					output+= "<img src='css/images/uncompleted.gif'/>"; //Incompleted marker
				}
				//Final out put compilation
				output +="<a class='challenge' id='" 
					+ encoder.encodeForHTMLAttribute(challenges.getString(3))
					+ "' href='javascript:;'>" 
					+ encoder.encodeForHTML(challenges.getString(1)) 
					+ "</a>";
				output += "</li>";
				rowNumber++;
			}
			//Check if output is empty
			if(output.isEmpty())
			{
				output = "<li><a href='javascript:;'>No challenges found</a></li>";
			}
			else
			{
				log.debug("Appending End tags");
				output += "</ul></li>";
			}
		}
		catch(Exception e)
		{
			log.error("Challenge Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getChallenges() ***");
		return output;
	}
	
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @return The amount of classes currently existing in the database
	 */
	public static int getClassCount(String ApplicationRoot)
	{
		int result = 0;
		ResultSet resultSet = null;
		log.debug("*** Getter.getClassCount ***");
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call classCount()");
			log.debug("Gathering classCount ResultSet");
			resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from classCount");
			resultSet.next();
			result = resultSet.getInt(1);
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = 0;
		}
		Database.closeConnection(conn);
		log.debug("*** END getClassCount");
		return result;
	}
	
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @return Result set containing class information
	 */
	public static ResultSet getClassInfo(String ApplicationRoot)
	{
		ResultSet result = null;
		log.debug("*** Getter.getClassInfo (All Classes) ***");
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call classesGetData()");
			log.debug("Gathering classesGetData ResultSet");
			result = callstmt.executeQuery();
			log.debug("Returning Result Set from classesGetData");
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getClassInfo");
		return result;
	}
	
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param classId The indentifier of the class
	 * @return Class information based on the classId parameter
	 */
	public static String[] getClassInfo(String ApplicationRoot, String classId)
	{
		String[] result = new String[2];
		log.debug("*** Getter.getClassInfo (Single Class) ***");
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call classFind(?)");
			callstmt.setString(1, classId);
			log.debug("Gathering classFind ResultSet");
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from classFind");
			resultSet.next();
			result[0] = resultSet.getString(1);//Name
			result[1] = resultSet.getString(2);//Year
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getClassInfo");
		return result;
	}
	
	/**
	 * The CSRF forum is used in CSRF levels for users to deliver CSRF attacks against each other.
	 * @param ApplicationRoot The current running context of the application
	 * @param classId Identifier of the class to populate the forum with
	 * @param moduleId The module in which to return the forum for
	 * @return A HTML table of a Class's CSRF Submissions for a specific module
	 */
	public static String getCsrfForum (String ApplicationRoot, String classId, String moduleId)
	{
		log.debug("*** Getter.getCsrfForum ***");
		log.debug("Getting stored messages from class: " + classId);
		Encoder encoder = ESAPI.encoder();
		String htmlOutput = new String();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			if(classId != null)
			{
				CallableStatement callstmt = conn.prepareCall("call resultMessageByClass(?, ?)");
				log.debug("Gathering resultMessageByClass ResultSet");
				callstmt.setString(1, classId);
				callstmt.setString(2, moduleId);
				ResultSet resultSet = callstmt.executeQuery();
				log.debug("resultMessageByClass executed");
				
				//Table Header
				htmlOutput = "<table><tr><th>User Name</th><th>Message</th></tr>";
				
				log.debug("Opening Result Set from resultMessageByClass");
				int counter = 0;
				while(resultSet.next())
				{
					counter++;
					//Table content
					htmlOutput += "<tr><td>" + encoder.encodeForHTML(resultSet.getString(1)) + "</td><td>" + resultSet.getString(2) + "</td></tr>";
				}
				if(counter > 0)
					log.debug("Added a " + counter + " row table");
				else
					log.debug("No results from query");
				//Table end
				htmlOutput += "</table>";
			}
			else
			{
				log.error("User with Null Class detected");
				htmlOutput = "<p><font color='red'>You must be assigned to a class to use this function. Please contact your administrator.</font></p>";
			}
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			htmlOutput = "<p>Sorry! An Error Occured. Please contact administrator</p>";
		}
		Database.closeConnection(conn);
		log.debug("*** END getCsrfForum ***");
		return htmlOutput;
	}
	
	/**
	 * Used to present a modules feedback, including averages and raw results.
	 * @param applicationRoot The current running context of the application.
	 * @param moduleId The module identifier
	 * @return A HTML table of the feedback for a specific module
	 */
	public static String getFeedback(String applicationRoot, String moduleId)
	{
		log.debug("*** Getter.getFeedback ***");
		
		String result = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(applicationRoot);
		try
		{
			log.debug("Prepairing userUpdateResult call");
			CallableStatement callstmnt = conn.prepareCall("call moduleFeedback(?)");
			callstmnt.setString(1, moduleId);
			log.debug("Executing moduleFeedback");
			ResultSet resultSet = callstmnt.executeQuery();
			int resultAmount = 0;
			int before = 0;
			int after = 0;
			int difficulty = 0;
			boolean color = true;
			while(resultSet.next())
			{
				if(resultSet.getString(1) != null)
				{
					resultAmount++;
					difficulty += resultSet.getInt(3);
					before += resultSet.getInt(4);
					after += resultSet.getInt(5);
					result += "<tr ";
					if(color) //Alternate row color
					{
						color = !color;
						result += "BGCOLOR='A878EF'";
					}
					else
					{
						color = !color;
						result += "BGCOLOR='D4BCF7'";
					}
					//A row off intormation
					result += "><td>" + encoder.encodeForHTML(resultSet.getString(1)) + "</td><td>" + encoder.encodeForHTML(resultSet.getString(2)) + "</td><td>" +
							resultSet.getInt(3) + "</td><td>" + resultSet.getInt(4) + "</td><td>" +
							resultSet.getInt(5) + "</td><td>" + encoder.encodeForHTML(resultSet.getString(6)) + "</td></tr>";
				}
			}
			if(resultAmount > 0)//Table header
				result = "<table><tr><th>Player</th><th>Time</th><th>Difficulty</th><th>Before</th><th>After</th><th>Comments</th></tr>" +
						"<tr><td>Average</td><td></td><td>" + difficulty/resultAmount + "</td><td>" + 
						before/resultAmount + "</td><td>" + after/resultAmount + "</td><td></td></tr>" + result + "<table>";
			else // If empty, Blank output
				result = new String();
		}
		catch(SQLException e)
		{
			log.error("moduleFeedback Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getFeedback ***");
		return result;
	}
	
	/**
	 * This method prepares the incremental module menu. This is when Security Shepherd is in "Game Mode".
	 * Users are presented with one uncompleted module at a time. This method also returns a script to be executed every time the menu is chanegd.
	 * This is script defines the animation and operations to be carried out when the menu is interacted with
	 * @param ApplicationRoot The running context of the applicaiton.
	 * @param userId The user identifier of the user.
	 * @param csrfToken The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for interaction with this menu
	 */
	public static String getIncrementalModules (String ApplicationRoot, String userId, String csrfToken)
	{
		log.debug("*** Getter.getIncrementalChallenges ***");
		String output = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleIncrementalInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering moduleIncrementalInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleIncrementalInfo");
			boolean lastRow = false;
			boolean completedModules = false;
			
			//Preparing first Category header; "Completed"
			output = "<li><h2 id='completedList'><a href='javascript:;'>Completed</a></h2>\n" +
				"<ul id='theCompletedList' style='display: none;'>";
			
			while(modules.next() && !lastRow)
			{
				//For each row, prepair the modules the users can select
				if(modules.getString(4) != null) //If not Last Row
				{
					completedModules = true;
					output += "<li>";
					output += "<a class='lesson' id='" 
						+ encoder.encodeForHTMLAttribute(modules.getString(3))
						+ "' href='javascript:;'>" 
						+ encoder.encodeForHTML(modules.getString(1)) 
						+ "</a>";
					output += "</li>";
				}
				else
				{
					lastRow = true;
					//Last Row - Highlighed Next Challenge
					if(completedModules)
					{
						output += "</ul></li><li>";
					}
					else
					{
						//NO completed modules, so dont show any...
						output = new String();
					}
					
					//Second category - Uncompleted
					output += "<h2 id='uncompletedList'><a href='javascript:;'>Next Challenge</a></h2>\n" +
					"<ul id='theUncompletedList' style='display: none;'>";
					output += "<li>";
					output += "<a class='lesson' id='" 
						+ encoder.encodeForHTMLAttribute(modules.getString(3))
						+ "' href='javascript:;'>" 
						+ encoder.encodeForHTML(modules.getString(1)) 
						+ "</a>";
					output += "</li>";
					output += "</ul></li>";
					
				}
			}
			
			if(!lastRow) //If true, then the user has completed all challenges
			{
				output += "<h2 id='uncompletedList'><a href='javascript:;'>Next Challenge</a></h2>\n" +
				"<ul id='theUncompletedList' style='display: none;'>";
				output += "<li>";
				output += "<a>You've Finished It All! Well Done</a>";
				output += "</li>";
				output += "</ul></li>";
			}
			if(output.isEmpty()) //If this method has gone so far without any output, create a error message
			{
				output = "<li><a href='javascript:;'>No Modules Fonud found</a></li>";
			}
			else //final tags to ensure valid HTML
			{
				log.debug("Appending End tags");
				output += "</ul></li>";
			}
			
			//This is the script for menu interaction
			output += "<script>" +
		"$(\"#completedList\").click(function () {" +
			"$(\"#theCompletedList\").toggle(\"slow\");" +
			"$(\"#theUncompletedList\").hide(\"fast\");" +
			"$(\"#theAdminList\").hide(\"fast\");" +
		"});" +
		"" +
		"$(\"#uncompletedList\").click(function () {" +
			"$(\"#theUncompletedList\").toggle(\"slow\");" +
			"$(\"#theCompletedList\").hide(\"fast\");" +
			"$(\"#theAdminList\").hide(\"fast\");" +
		"});" +
		"$(\".lesson\").click(function(){" +
		"	var whatFile = $(this).attr('id');" +
		"	$(\"#currentModule\").val(whatFile);" +
		"	var theActualFile = \"\";" +
		"	$(\"#solutionDiv\").hide(\"fast\");" +
		"	$(\"#contentDiv\").slideUp(\"slow\", function(){" +
		"		var ajaxCall = $.ajax({" +
		"			type: \"POST\"," +
		"			url: \"getModule\"," +
		"			data: {" +
		"				moduleId: whatFile," +
		"				csrfToken: \""
		+ encoder.encodeForHTML(csrfToken) +
		"\"" +
		"			}," +
		"			async: false" +
		"		});" +
		"		if(ajaxCall.status == 200)" +
		"		{" +
		"			theActualFile = ajaxCall.responseText;" +
		"			$('#contentDiv').html(\"<iframe frameborder='no' style='width: 685px; height: 2056px;' id='theLesson' src='\" + theActualFile + \"'></iframe>\");" +
		"			$(\"#theLesson\").load(function(){" +
		"				$(\"#submitResult\").slideDown(\"fast\", function(){" +
		"					$(\"#contentDiv\").slideDown(\"slow\");" +
		"				});" +
		"			}).appendTo('#contentDiv');" +
		"		}" +
		"		else" +
		"		{" +
		"			$('#contentDiv').html(\"<p> Sorry but there was an error: \" + ajaxCall.status + \" \" + ajaxCall.statusText + \"</p>\");" +
		"			$(\"#contentDiv\").slideDown(\"slow\");" +
		"		}" +
		"	});" +
		"});" +
		"</script>";
		}
		catch(Exception e)
		{
			log.error("Challenge Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getIncrementalChallenges() ***");
		return output;
	}
	
	/**
	 * This method prepares the Tournament module menu. This is when Security Shepherd is in "Tournament Mode".
	 * Users are presented with a list of that are specified as open. 
	 * @param ApplicationRoot The running context of the applicaiton.
	 * @param userId The user identifier of the user.
	 * @param csrfToken The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for interaction with this menu
	 */
	public static String getTournamentModules (String ApplicationRoot, String userId)
	{
		log.debug("*** Getter.getTournamentModules ***");
		String output = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			//Get the modules
			CallableStatement callstmt = conn.prepareCall("call moduleOpenInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering moduleOpenInfo ResultSet for user " + userId);
			ResultSet lessons = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleOpenInfo");
			int rowNumber = 0; // Used to indetify the first row, as it is slightly different to all other rows for output
			while(lessons.next())
			{
				//log.debug("Adding " + lessons.getString(1));
				output += "<li>";
				//Markers for completion
				if(lessons.getString(4) != null)
				{
					output += "<img src='css/images/completed.gif'/>";
				}
				else
				{
					output+= "<img src='css/images/uncompleted.gif'/>";
				}
				//Prepare lesson output
				output += "<a class='lesson' id='" 
					+ encoder.encodeForHTMLAttribute(lessons.getString(3))
					+ "' href='javascript:;'>" 
					+ encoder.encodeForHTML(lessons.getString(1)) 
					+ "</a>";
				output += "</li>";
				rowNumber++;
			}
			//If no output has been found, return an error message
			if(output.isEmpty())
			{
				output = "<li><a href='javascript:;'>No modules found</a></li>";
			}
			else
			{
				log.debug("Tournaments List returned");
			}
		}
		catch(Exception e)
		{
			log.error("Tournament List Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		return output;
	}
	
	/**
	 * This method returns the modules with open and closed in different &lt;select&gt; elements for administration manipulation
	 * @param ApplicationRoot
	 * @param userId
	 * @param csrfToken
	 * @return
	 */
	public static String getModuleStatusMenu (String ApplicationRoot)
	{
		log.debug("*** Getter.getModuleStatusMenu ***");
		String openModules = new String();
		String closedModules = new String();
		String output = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			//Get the modules
			CallableStatement callstmt = conn.prepareCall("call moduleAllStatus()");
			log.debug("Gathering moduleAllStatus ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllStatus");
			while(modules.next())
			{
				String theModule = "<option value='" + encoder.encodeForHTMLAttribute(modules.getString(1)) + 
						"'>" + encoder.encodeForHTML(modules.getString(2)) + "</option>\n";
				if(modules.getString(3).equalsIgnoreCase("open"))
				{
					//Module is Open currently, so add it to the open side of the list
					openModules += theModule;
				}
				else 
				{
					//If it is not open: It must be closed (NULL or not)
					closedModules += theModule;
				}
			}
			//This is the actual output: It assumes a <table> environment
			output = "<tr><th>To Open</th><th>To Close</th></tr><tr>\n" +
					"<td><select style='width: 300px; height: 200px;' multiple id='toOpen'>" + closedModules + "</select></td>\n" +
					"<td><select style='width: 300px; height: 200px;' multiple id='toClose'>" + openModules + "</select></td>\n" +
					"</tr>\n";
			log.debug("Module Status Menu returned");
		}
		catch(Exception e)
		{
			log.error("Module Status Menu: " + e.toString());
		}
		Database.closeConnection(conn);
		return output;
	}
	
	/**
	 * Used to gather a menu of lessons for a user, including markers for each lesson they have completed or not completed
	 * @param ApplicationRoot The current running context of the application
	 * @param userId Identifier of the user
	 * @return HTML lesson menu.
	 */
	public static String getLessons (String ApplicationRoot, String userId)
	{
		log.debug("*** Getter.getLesson ***");
		String output = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			//Get the lesson modules
			CallableStatement callstmt = conn.prepareCall("call moduleAllInfo(?, ?)");
			callstmt.setString(1, "lesson");
			callstmt.setString(2, userId);
			log.debug("Gathering moduleAllInfo ResultSet for user " + userId);
			ResultSet lessons = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			int rowNumber = 0; // Used to indetify the first row, as it is slightly different to all other rows for output
			while(lessons.next())
			{
				//log.debug("Adding " + lessons.getString(1));
				output += "<li>";
				//Markers for completion
				if(lessons.getString(4) != null)
				{
					output += "<img src='css/images/completed.gif'/>";
				}
				else
				{
					output+= "<img src='css/images/uncompleted.gif'/>";
				}
				//Prepare lesson output
				output += "<a class='lesson' id='" 
					+ encoder.encodeForHTMLAttribute(lessons.getString(3))
					+ "' href='javascript:;'>" 
					+ encoder.encodeForHTML(lessons.getString(1)) 
					+ "</a>";
				output += "</li>";
				rowNumber++;
			}
			//If no output has been found, return an error message
			if(output.isEmpty())
			{
				output = "<li><a href='javascript:;'>No lessons found</a></li>";
			}
			else
			{
				log.debug("Lesson List returned");
			}
		}
		catch(Exception e)
		{
			log.error("lesson Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getLesson() ***");
		return output;
	}
	
	/**
	 * This method returns the address of a module based on the module identifier submitted. 
	 * If user has not accessed this level before, they are put down as starting the level at this time.
	 * If the level is a client side attack, or other issues that cannot be abused to return a result key (like XSS, CSRF or network sniffing)
	 * the address is of the core server. Otherwise the modules sit on the vulnerable application server
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId Identifier of the module the to return
	 * @param userId The identifier of the user that wants to get the module
	 * @return The module address
	 */
	public static String getModuleAddress (String ApplicationRoot, String moduleId, String userId)
	{
		log.debug("*** Getter.getModuleAddress ***");
		String output = new String();
		String type = new String();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleGetHash(?, ?)");
			callstmt.setString(1, moduleId);
			callstmt.setString(2, userId);
			log.debug("Gathering moduleGetHash ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetHash");
			modules.next(); //Exception thrown if no hash was found
			//Set Type. Used to ensure the URL points at the correct directory
			if(modules.getString(3).equalsIgnoreCase("challenge"))
			{
				type = "challenges";
			}
			else
			{
				type = "lessons";
			}
			//Identify which server to point to
			if(modules.getString(2).equalsIgnoreCase("XSS") || modules.getString(2).equalsIgnoreCase("CSRF") || modules.getString(2).equalsIgnoreCase("Unvalidated Redirects and Forwards") || modules.getString(2).equalsIgnoreCase("Insufficient Transport Layer Protection"))
			{
				output = ExposedServer.getSecureUrl() + type + "/" + modules.getString(1) + ".jsp";
			}
			else
			{
				output = ExposedServer.getUrl() + type + "/" + modules.getString(1) + ".jsp";
			}
		}
		catch(Exception e)
		{
			log.error("Module Hash Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleAddress() ***");
		return output;
	}
	
	/**
	 * @param applicationRoot The current running context of the application.
	 * @param moduleId The identifier of a module
	 * @return The hash of the module specified
	 */
	public static String getModuleHash(String applicationRoot, String moduleId) 
	{
		log.debug("*** Getter.getModuleHash ***");
		String result = new String();
		Connection conn = Database.getConnection(applicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleGetHashById(?)");
			log.debug("Gathering moduleGetHash ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetHash");
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch (SQLException e)
		{
			log.error("Could not execute moduleGetHash: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleHash ***");
		return result;
	}
	
	/**
	 * Convert module hash to ID
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleHash The module hash to use for look up
	 * @return The identifier of the module with the module hash of the moduleHash parameter
	 */
	public static String getModuleIdFromHash (String ApplicationRoot, String moduleHash)
	{
		log.debug("*** Getter.getModuleIdFromHash ***");
		log.debug("Getting ID from Hash: " + moduleHash);
		String result = new String();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleGetIdFromHash(?)");
			log.debug("Gathering moduleGetIdFromHash ResultSet");
			callstmt.setString(1, moduleHash);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetIdFromHash");
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleIdFromHash ***");
		return result;
	}
	
	/**
	 * Retrieves the module category based on the moduleId submitted
	 * @param ApplicationRoot The current runing context of the application
	 * @param moduleId The id of the module that 
	 * @return
	 */
	public static String getModuleCategory (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Getter.getModuleResult ***");
		String theCategory = null;
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			PreparedStatement prepstmt = conn.prepareStatement("SELECT moduleCategory FROM modules WHERE moduleId = ?");
			prepstmt.setString(1, moduleId);
			ResultSet moduleFind = prepstmt.executeQuery();
			moduleFind.next();
			theCategory = moduleFind.getString(1);
		}
		catch(Exception e)
		{
			log.error("Module did not exist: " + e.toString());
			theCategory = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleCategory ***");
		return theCategory;
	}
	
	/**
	 * @param ApplicationRoot The current runing context of the application
	 * @param moduleId Identifier of module
	 * @return The solution key for a module
	 */
	public static String getModuleResult (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Getter.getModuleResult ***");
		String moduleFound = null;
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleGetResult(?)");
			log.debug("Gathering moduleGetResult ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet moduleFind = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetResult");
			moduleFind.next();
			log.debug("Module " + moduleFind.getString(1) + " Found");
			moduleFound =  moduleFind.getString(2);
		}
		catch(Exception e)
		{
			log.error("Module did not exist: " + e.toString());
			moduleFound = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleResult ***");
		return moduleFound;
	}
	
	/**
	 * Returns the result key for a module using the module's hash for the lookup procedure.
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleHash The hash to use for module look up
	 * @return
	 */
	public static String getModuleResultFromHash (String ApplicationRoot, String moduleHash)
	{
		log.debug("*** Getter.getModuleResultFromHash ***");
		String result = new String();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			log.debug("hash '" + moduleHash + "'");
			CallableStatement callstmt = conn.prepareCall("call moduleGetResultFromHash(?)");
			log.debug("Gathering moduleGetResultFromHash ResultSet");
			callstmt.setString(1, moduleHash);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetResultFromHash");
			resultSet.next();
			result = resultSet.getString(1);
			
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleResultFromHash ***");
		return result;
	}
	
	/**
	 * Used in creating functionality that requires a user to select a module. 
	 * This method only prepares the option tags for this type of input. It must still be wrapped in select tags.
	 * @param ApplicationRoot The current running context of the applicaiton
	 * @return All modules in HTML option tags
	 */
	public static String getModulesInOptionTags (String ApplicationRoot)
	{
		log.debug("*** Getter.getModulesInOptionTags ***");
		String output = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			PreparedStatement callstmt = conn.prepareStatement("SELECT moduleId, moduleName FROM modules ORDER BY moduleCategory, moduleName;");
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while(modules.next())
			{
				//Each module name is embed in option tags, with a value of their module identifier
				output += "<option value='" + encoder.encodeForHTMLAttribute(modules.getString(1)) + "'>" +
						encoder.encodeForHTML(modules.getString(2)) + "</option>\n";
			}
		}
		catch(Exception e)
		{
			log.error("Challenge Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getModulesInOptionTags() ***");
		return output;
	}
	
	/**
	 * Used in creating functionality that requires a user to select a module. 
	 * This method only prepares the option tags for this type of input. It must still be wrapped in select tags.
	 * @param ApplicationRoot The current running context of the applicaiton
	 * @return All modules in HTML option tags
	 */
	public static String getModulesInOptionTagsCTF (String ApplicationRoot)
	{
		log.debug("*** Getter.getModulesInOptionTags ***");
		String output = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			PreparedStatement callstmt = conn.prepareStatement("SELECT moduleId, moduleName FROM modules ORDER BY incrementalRank;");
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while(modules.next())
			{
				//Each module name is embed in option tags, with a value of their module identifier
				output += "<option value='" + encoder.encodeForHTMLAttribute(modules.getString(1)) + "'>" +
						encoder.encodeForHTML(modules.getString(2)) + "</option>\n";
			}
		}
		catch(Exception e)
		{
			log.error("Challenge Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getModulesInOptionTags() ***");
		return output;
	}
	
	/**
	 * Used to return a module cheat sheet
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId The identifier of the module to return the cheat sheet for
	 * @return Module cheat sheet
	 */
	public static String[] getModuleSolution (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Getter.getModuleSolution ***");
		String[] result = new String[2];
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call cheatSheetGetSolution(?)");
			log.debug("Gathering cheatSheetGetSolution ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from cheatSheetGetSolution");
			resultSet.next();
			result[0] = resultSet.getString(1);
			result[1] = resultSet.getString(2);
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleSolution ***");
		return result;
	}
	
	/**
	 * This method is used to gather users according by class. Thanks to MySQL syntax, where class = null will return nothing, is null must be used.
	 *  <br/>is 'validClass' will Error, = 'validclass' must be used.<br/>
	 * So there are two proecureds this method calls. One that handles null classes, one that does not
	 * @param ClassId Identifier of class
	 * @param ApplicationRoot The current running context of the application
	 * @return ResultSet that contains users for the selected class
	 */
	public static ResultSet getPlayersByClass(String ApplicationRoot, String classId)
	{
		ResultSet result = null;
		log.debug("*** Getter.getPlayersByClass (Single Class) ***");
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = null;
			if(classId != null)
			{
				log.debug("Gathering playersByClass ResultSet");
				callstmt = conn.prepareCall("call playersByClass(?)");
				callstmt.setString(1, classId);
				log.debug("Returning Result Set from playersByClass");
			}
			else
			{
				log.debug("Gathering playersWithoutClass ResultSet");
				callstmt = conn.prepareCall("call playersWithoutClass()");
				log.debug("Returning Result Set from playersByClass");
			}
			ResultSet resultSet = callstmt.executeQuery();
			result = resultSet;
			resultSet.next();
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getPlayersByClass");
		return result;
	}
	
	/**
	 * Used to present the progress of a class in a series of loading bars
	 * @param applicationRoot The current running context of the application
	 * @param classId The identifier of the class to use in lookup
	 * @return A HTML representation of a class's progress in the application
	 */
	public static String getProgress(String applicationRoot, String classId) 
	{
		log.debug("*** Getter.getProgress ***");
		
		String result = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(applicationRoot);
		try
		{
			log.debug("Prepairing userProgress call");
			CallableStatement callstmnt = conn.prepareCall("call userProgress(?)");
			callstmnt.setString(1, classId);
			log.debug("Executing userProgress");
			ResultSet resultSet = callstmnt.executeQuery();
			int resultAmount = 0;
			while(resultSet.next()) //For each user in a class
			{
				resultAmount++;
				if(resultSet.getString(1) != null)
				{
					result += "<tr><td>" + encoder.encodeForHTML(resultSet.getString(1)) + //Output their progress
						"</td><td><div style='background-color: #A878EF; heigth: 25px; width: " + 25*resultSet.getInt(2) + "px;'>" +
								"<font color='white'><strong>" +
								resultSet.getInt(2);
					if(resultSet.getInt(2) > 3)
						result += " Modules";
					result += "</strong></font></div></td></tr>";
				}
			}
			if(resultAmount > 0)
				result = "<table><tr><th>Player</th><th>Progress</th></tr>" +
						 result + "</table>";
			else
				result = new String();
		}
		catch(SQLException e)
		{
			log.error("getProgress Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getProgress ***");
		return result;
	}
	
	/**
	 * Use to return the current progress of a class in JSON format with information like user name, score and completed modules
	 * @param applicationRoot The current running context of the application
	 * @param classId The identifier of the class to use in lookup
	 * @return A JSON representation of a class's progress in the application
	 */
	@SuppressWarnings("unchecked")
	public static String getProgressJSON(String applicationRoot, String classId) 
	{
		log.debug("*** Getter.getProgressJSON ***");
		
		String result = new String();
		Encoder encoder = ESAPI.encoder();
		Connection conn = Database.getConnection(applicationRoot);
		try
		{
			log.debug("Prepairing userProgress call");
			//Returns User's: Name, # of Completed modules and Score
			CallableStatement callstmnt = conn.prepareCall("call userProgress(?)");
			callstmnt.setString(1, classId);
			log.debug("Executing userProgress");
			ResultSet resultSet = callstmnt.executeQuery();
			JSONArray json = new JSONArray();
			JSONObject jsonInner = new JSONObject();
			int resultAmount = 0;
			while(resultSet.next()) //For each user in a class
			{
				resultAmount++;
				jsonInner = new JSONObject();
				if(resultSet.getString(1) != null)
				{
					jsonInner.put("userName", new String(encoder.encodeForHTML(resultSet.getString(1)))); //User Name
					jsonInner.put("progressBar", new Integer(resultSet.getInt(2)*25)); //Progress Bar Width
					jsonInner.put("score", new Integer(resultSet.getInt(3))); //Score
					log.debug("Adding: " + jsonInner.toString());
					json.add(jsonInner);
				}
			}
			if(resultAmount > 0)
				result = json.toString();
			else
				result = new String();
		}
		catch(SQLException e)
		{
			log.error("getProgressJSON Failure: " + e.toString());
			result = null;
		}
		catch(Exception e)
		{
			log.error("getProgressJSON Unexpected Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getProgressJSON ***");
		return result;
	}
	
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param userId The identifier of a user
	 * @return The user name of the submitted user identifier
	 */
	public static String getUserName (String ApplicationRoot, String userId)
	{
		log.debug("*** Getter.getUserName ***");
		String result = new String();
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call userGetNameById(?)");
			log.debug("Gathering userGetNameById ResultSet");
			callstmt.setString(1, userId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from userGetNameById");
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getUserName ***");
		return result;
	}
	
	/**
	 * Used by authentication to check if account is locked before continuing with authentication process.
	 * @param ApplicationRoot The current running context of the application
	 * @param userName The userName to use for check
	 * @return A boolean value of if the user account is locked
	 */
	public static boolean isUserLocked (String ApplicationRoot, String userName)
	{
		log.debug("*** Getter.isUserLocked ***");
		boolean result = true;
		Connection conn = Database.getConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call userLocked(?)");
			log.debug("Gathering userLocked ResultSet");
			callstmt.setString(1, userName);
			ResultSet userLocked = callstmt.executeQuery();
			log.debug("Opening Result Set from userLocked");
			userLocked.next();
			result = !userLocked.getString(1).equalsIgnoreCase(userName);
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END isUserLocked ***");
		return result;
	}
}
