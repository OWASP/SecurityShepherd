package dbProcs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.owasp.encoder.Encode;

import utils.ScoreboardStatus;

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
	 * Used for scoreboards / progress bars
	 */
	private static int widthOfUnitBar = 11; //px
	private static int fieldTrainingCap = 45;
	
	private static int privateCap = 80;
	
	private static int corporalCap = 105;
	
	private static int sergeantCap = 130;
	
	private static int lieutenantCap = 145;
	
	private static int majorCap = 175;
	
	private static int admiralCap = 999; //everything above Major is Admiral
	
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
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
					// But I'll leave it here just in case. That includes the else block if goOn is false
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
			}
		} 
		catch (SQLException e) 
		{
			log.error("Login Failure: " + e.toString());
			result = null;
			//Lagging Response
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
	 * @return The module name of the module IF the user has not completed AND the user has previously opened the challenge. 
	 */
	public static String checkPlayerResult(String ApplicationRoot, String moduleId, String userId)
	{
		log.debug("*** Getter.checkPlayerResult ***");
		
		String result = null;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing userCheckResult call");
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
			log.debug("userCheckResult Failure: " + e.toString());
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
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call playerFindById(?)");
			log.debug("Gathering playerFindById ResultSet");
			callstmt.setString(1, userId);
			ResultSet userFind = callstmt.executeQuery();
			log.debug("Opening Result Set from playerFindById");
			userFind.next(); //This will throw an exception if player not found
			log.debug("Player Found: " + userFind.getString(1)); //This line will not execute if player not found
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
		
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
			log.debug("Returning Array list with " + i + " entries.");
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
	public static String getChallenges (String ApplicationRoot, String userId, Locale lang)
	{
		log.debug("*** Getter.getChallenges ***");
		String output = new String();
		//Getting Translated Level Names
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", lang);
		//Encoder to prevent XSS
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleAllInfo(?, ?)");
			callstmt.setString(1, "challenge");
			callstmt.setString(2, userId);
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet challenges = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			String challengeCategory = new String();
			int rowNumber = 0; // Identifies the first row, ie the start of the list. This is slightly different output to every other row
			while(challenges.next())
			{
				if(!challengeCategory.equalsIgnoreCase(challenges.getString(2)))
				{
					challengeCategory = challenges.getString(2);
					//log.debug("New Category Detected: " + challengeCategory);
					if(rowNumber > 0) //output prepared for Every row after row 1
						output += "</ul></li><li><a href='javascript:;' class='challengeHeader' >" + Encode.forHtml(bundle.getString("category." + challengeCategory))+ "</a><ul class='challengeList' style='display: none;'>";
					else //output prepared for First row in entire challenge
						output += "<li><a href='javascript:;' class='challengeHeader'>" + Encode.forHtml(bundle.getString("category." + challengeCategory))+ "</a><ul class='challengeList' style='display: none;'>";
					//log.debug("Compiling Challenge Category - " + challengeCategory);
				}
				output += "<li>"; //Starts next LI element
				if(challenges.getString(4) != null)
				{
					output += "<img src='css/images/completed.png'/>"; //Completed marker
				}
				else
				{
					output+= "<img src='css/images/uncompleted.png'/>"; //Incomplete marker
				}
				//Final out put compilation
				output +="<a class='lesson' id='" 
					+ Encode.forHtmlAttribute(challenges.getString(3))
					+ "' href='javascript:;'>" 
					+ Encode.forHtml(bundle.getString(challenges.getString(1))) 
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
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * @return Result set containing class info in the order classId, className and then classYear
	 */
	public static ResultSet getClassInfo(String ApplicationRoot)
	{
		ResultSet result = null;
		log.debug("*** Getter.getClassInfo (All Classes) ***");
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * @param classId The identifier of the class
	 * @return String Array with Class information with the format of {name, year}
	 */
	public static String[] getClassInfo(String ApplicationRoot, String classId)
	{
		String[] result = new String[2];
		log.debug("*** Getter.getClassInfo (Single Class) ***");
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * The CSRF forum is used in CSRF levels for users to deliver CSRF attacks against each other. URLs are contained in IFRAME tags
	 * @param ApplicationRoot The current running context of the application
	 * @param classId Identifier of the class to populate the forum with
	 * @param moduleId The module in which to return the forum for
	 * @param bundle Strings Package for the Language Local of the user making the request
	 * @return A HTML table of a Class's CSRF Submissions for a specific module
	 */
	public static String getCsrfForumWithIframe (String ApplicationRoot, String classId, String moduleId, ResourceBundle bundle)
	{
		log.debug("*** Getter.getCsrfForum ***");
		log.debug("Getting stored messages from class: " + classId);
		String htmlOutput = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
				htmlOutput = "<table><tr><th>" + bundle.getString("forum.userName") + "</th><th>" + bundle.getString("forum.message") + "</th></tr>";
				
				log.debug("Opening Result Set from resultMessageByClass");
				int counter = 0;
				while(resultSet.next())
				{
					counter++;
					//Table content
					htmlOutput += "<tr><td>" + Encode.forHtml(resultSet.getString(1)) + "</td><td><iframe sandbox=\"allow-scripts allow-forms\" src=\"" + Encode.forHtmlAttribute(resultSet.getString(2)) + "\"></iframe></td></tr>";
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
				htmlOutput = "<p><font color='red'>" + bundle.getString("error.noClass") + "</font></p>";
			}
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			htmlOutput = "<p>" + bundle.getString("error.occurred ") + "</p>";
		}
		catch (Exception e)
		{
			log.fatal("Could not return CSRF Forum: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getCsrfForum ***");
		return htmlOutput;
	}
	
	/**
	 * The CSRF forum is used in CSRF levels for users to deliver CSRF attacks against each other. URLs are contained in IMG tags
	 * @param ApplicationRoot The current running context of the application
	 * @param classId Identifier of the class to populate the forum with
	 * @param moduleId The module in which to return the forum for
	 * @param bundle The strings package for the language of the user
	 * @return A HTML table of a Class's CSRF Submissions for a specific module
	 */
	public static String getCsrfForumWithImg (String ApplicationRoot, String classId, String moduleId, ResourceBundle bundle)
	{
		log.debug("*** Getter.getCsrfForum ***");
		log.debug("Getting stored messages from class: " + classId);
		String htmlOutput = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
				htmlOutput = "<table><tr><th>" + bundle.getString("forum.userName") + "</th><th>" + bundle.getString("forum.image") + "</th></tr>";
				
				log.debug("Opening Result Set from resultMessageByClass");
				int counter = 0;
				while(resultSet.next())
				{
					counter++;
					//Table content
					htmlOutput += "<tr><td>" + Encode.forHtml(resultSet.getString(1)) + "</td><td><img src=\"" + Encode.forHtmlAttribute(resultSet.getString(2)) + "\"/></td></tr>";
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
				htmlOutput = "<p><font color='red'>" + bundle.getString("error.noClass") + "</font></p>";
			}
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			htmlOutput = "<p>" + bundle.getString("error.occurred") + "</p>";
		}
		catch (Exception e)
		{
			log.fatal("Could not return CSRF Forum: " + e.toString());
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
		Connection conn = Database.getCoreConnection(applicationRoot);
		try
		{
			log.debug("Preparing userUpdateResult call");
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
					//A row off information
					result += "><td>" + Encode.forHtml(resultSet.getString(1)) + "</td><td>" + Encode.forHtml(resultSet.getString(2)) + "</td><td>" +
							resultSet.getInt(3) + "</td><td>" + resultSet.getInt(4) + "</td><td>" +
							resultSet.getInt(5) + "</td><td>" + Encode.forHtml(resultSet.getString(6)) + "</td></tr>";
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
	 * @param ApplicationRoot The running context of the application.
	 * @param userId The user identifier of the user.
	 * @param csrfToken The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for interaction with this menu
	 */
	public static String getIncrementalModules (String ApplicationRoot, String userId, String lang, String csrfToken)
	{
		log.debug("*** Getter.getIncrementalChallenges ***");
		String output = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		
		Locale.setDefault(new Locale("en"));
		Locale locale = new Locale(lang);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", locale);
		ResourceBundle levelNames = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", locale);
		
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
			output = "<li><a id='completedList' href='javascript:;'><div class='menuButton'>" + bundle.getString("getter.button.completed") + "</div></a>\n" +
				"<ul id='theCompletedList' style='display: none;' class='levelList'>";
			
			while(modules.next() && !lastRow)
			{
				//For each row, prepair the modules the users can select
				if(modules.getString(4) != null) //If not Last Row
				{
					completedModules = true;
					output += "<li>";
					output += "<a class='lesson' id='" 
						+ Encode.forHtmlAttribute(modules.getString(3))
						+ "' href='javascript:;'>" 
						+ Encode.forHtml(levelNames.getString(modules.getString(1))) 
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
					output += "<a class='lesson' id='" 
						+ Encode.forHtmlAttribute(modules.getString(3))
						+ "' href='javascript:;'>" 
						+ "<div class='menuButton'>" + bundle.getString("getter.button.nextChallenge")+ "</div>" 
						+ "</a>";
					output += "</li>";					
				}
			}
			
			if(!lastRow) //If true, then the user has completed all challenges
			{
				output += "<h2 id='uncompletedList'><a href='javascript:;'>" + bundle.getString("getter.button.finished") + "</a></h2>\n" +
				"</li>";
			}
			if(output.isEmpty()) //If this method has gone so far without any output, create a error message
			{
				output = "<li><a href='javascript:;'>" + bundle.getString("getter.button.noModulesFound") + "</a></li>";
			}
			else //final tags to ensure valid HTML
			{
				log.debug("Appending End tags");
				//output += "</ul></li>"; //Commented Out to prevent Search Box being pushed into Footer
			}
			
			//This is the script for menu interaction
			output += "<script>applyMenuButtonActionsCtfMode('" + Encode.forHtml(csrfToken) + "', \"" + Encode.forHtml(bundle.getString("generic.text.sorryError")) + "\");</script>";
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
	 * This method prepares the incremental module menu. This is when Security Shepherd is in "Game Mode".
	 * Users are presented with one uncompleted module at a time. This method does not return the JS script describing how the menu used should work
	 * @param ApplicationRoot The running context of the application.
	 * @param userId The user identifier of the user.
	 * @param csrfToken The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for interaction with this menu
	 */
	public static String getIncrementalModulesWithoutScript (String ApplicationRoot, String userId, String lang, String csrfToken)
	{
		log.debug("*** Getter.getIncrementalChallengesWithoutScript ***");
		String output = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		
		Locale.setDefault(new Locale("en"));
		Locale locale = new Locale(lang);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", locale);
		ResourceBundle levelNames = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", locale);
		
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
			output = "<li><a id='completedList' href='javascript:;'><div class='menuButton'>" + bundle.getString("getter.button.completed") + "</div></a>\n" +
				"<ul id='theCompletedList' style='display: none;' class='levelList'>";
			
			while(modules.next() && !lastRow)
			{
				//For each row, prepair the modules the users can select
				if(modules.getString(4) != null) //If not Last Row
				{
					completedModules = true;
					output += "<li>";
					output += "<a class='lesson' id='" 
						+ Encode.forHtmlAttribute(modules.getString(3))
						+ "' href='javascript:;'>" 
						+ Encode.forHtml(levelNames.getString(modules.getString(1))) 
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
					output += "<a class='lesson' id='" 
						+ Encode.forHtmlAttribute(modules.getString(3))
						+ "' href='javascript:;'>" 
						+ "<div class='menuButton'>" + bundle.getString("getter.button.nextChallenge")+ "</div>" 
						+ "</a>";
					output += "</li>";					
				}
			}
			
			if(!lastRow) //If true, then the user has completed all challenges
			{
				output += "<h2 id='uncompletedList'><a href='javascript:;'>" + bundle.getString("getter.button.finished") + "</a></h2>\n" +
				"</li>";
			}
			if(output.isEmpty()) //If this method has gone so far without any output, create a error message
			{
				output = "<li><a href='javascript:;'>" + bundle.getString("getter.button.noModulesFound") + "</a></li>";
			}
			else //final tags to ensure valid HTML
			{
				log.debug("Appending End tags");
				//output += "</ul></li>"; //Commented Out to prevent Search Box being pushed into Footer
			}
		}
		catch(Exception e)
		{
			log.error("Challenge Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END getIncrementalChallengesWithoutScript() ***");
		return output;
	}
	
	/**
	 * Use to return the current progress of a class in JSON format with information like userid, user name and score
	 * @param applicationRoot The current running context of the application
	 * @param classId The identifier of the class to use in lookup
	 * @return A JSON representation of a class's score in the order {id, username, userTitle, score, scale, place, order, 
	 * goldmedalcount, goldDisplay, silverMedalCount, silverDisplay, bronzeDisplay, bronzeMedalCount}
	 */
	@SuppressWarnings("unchecked")
	public static String getJsonScore(String applicationRoot, String classId) 
	{
		log.debug("classId: " + classId);
		String result = new String();
		Connection conn = Database.getCoreConnection(applicationRoot);
		try
		{
			//Returns User's: Name, # of Completed modules and Score
			CallableStatement callstmnt = null;
			if(ScoreboardStatus.getScoreboardClass().isEmpty() && !ScoreboardStatus.isClassSpecificScoreboard())
				callstmnt = conn.prepareCall("call totalScoreboard()"); //Open Scoreboard not based on a class
			else
			{
				callstmnt = conn.prepareCall("call classScoreboard(?)"); //Class Scoreboard based on classId
				callstmnt.setString(1, classId);
			}
			//log.debug("Executing classScoreboard");
			ResultSet resultSet = callstmnt.executeQuery();
			JSONArray json = new JSONArray();
			JSONObject jsonInner = new JSONObject();
			int resultAmount = 0;
			int prevPlace = 0;
			int prevScore = 0;
			int prevGold = 0;
			int prevSilver = 0;
			int prevBronze = 0;
			float baseBarScale = 0; //
			float tieBreaker = 0;
			while(resultSet.next()) //For each user in a class
			{
				resultAmount++;
				jsonInner = new JSONObject();
				if(resultSet.getString(1) != null)
				{
					int place = resultAmount;
					int score = resultSet.getInt(3);
					int goldMedals = resultSet.getInt(4);
					int silverMedals = resultSet.getInt(5);
					int bronzeMedals = resultSet.getInt(6);
					if(resultAmount == 1) //First Place is Returned First, so this will be the biggest bar on the scoreboard
					{
						int highscore = score;
						//log.debug("Current Highscore Listing is " + highscore);
						//Use the high score to scale the width of the bars for the whole scoreboard
						float maxBarScale = 1.02f; //High Score bar will have a scale of 1 //This will get used when a scale is added to the scoreboard
						baseBarScale = highscore * maxBarScale;
						//setting up variables for Tie Scenario Placings
						prevPlace = 1;
						prevScore = score;
					}
					else
					{
						//Does this score line match the one before (Score and Medals)? if so the place shouldnt change
						if (score == prevScore && goldMedals == prevGold && silverMedals == prevSilver && bronzeMedals == prevBronze)
						{
							place = prevPlace;
							tieBreaker = tieBreaker + 0.01f;
						}
						else
						{
							prevScore = score;
							prevPlace = place;
							prevGold = goldMedals;
							prevSilver = silverMedals;
							prevBronze = bronzeMedals;
							tieBreaker = 0;
						}
					}
					String displayMedal = new String("display: inline;");
					String goldDisplayStyle = new String("display: none;");
					String silverDisplayStyle = new String("display: none;");
					String bronzeDisplayStyle = new String("display: none;");
					if (goldMedals > 0)
						goldDisplayStyle = displayMedal;
					if (silverMedals > 0)
						silverDisplayStyle = displayMedal;
					if (bronzeMedals > 0)
						bronzeDisplayStyle = displayMedal;
					
					int barScale = (int)((score*100)/baseBarScale); //bar scale is the percentage the bar should be of the row's context (Highest Possible is depends on scale set in maxBarScale. eg: maxBarScale = 1.1 would mean the max scale would be 91% for a single row)
					
					String userMedalString = new String();
					if(goldMedals > 0 || silverMedals > 0 || bronzeMedals > 0)
					{
						userMedalString += " holding ";
						if (goldMedals > 0)
							userMedalString += goldMedals + " gold";
						if (silverMedals > 0)
						{
							if (goldMedals > 0) //Medals Before, puncuate
							{
								if(bronzeMedals > 0) //more medals after silver? Comma
								{
									userMedalString += ", ";
								}
								else //Say And
								{
									userMedalString += " and ";
								}
							}
							userMedalString += silverMedals + " silver";
						}
						if (bronzeMedals > 0)
						{
							if (goldMedals > 0 || silverMedals > 0) //Medals Before?
							{
								userMedalString += " and ";
							}
							userMedalString += bronzeMedals + " bronze";
						}
						//Say Medal(s) at the end of the string
						userMedalString += " medal";
						if(goldMedals + silverMedals + bronzeMedals > 1)
							userMedalString += "s";
					}
						
					jsonInner.put("id", new String(Encode.forHtml(resultSet.getString(1)))); //User Id
					jsonInner.put("username", new String(Encode.forHtml(resultSet.getString(2)))); //User Name
					jsonInner.put("userTitle", new String(Encode.forHtml(resultSet.getString(2)) + " with " + score + " points" + userMedalString)); //User name encoded for title attribute
					jsonInner.put("score", new Integer(score)); //Score
					jsonInner.put("scale", barScale); //Scale of score bar
					jsonInner.put("place", place); //Place on board
					jsonInner.put("order", (place+tieBreaker)); //Order on board
					jsonInner.put("goldMedalCount", new Integer(goldMedals));
					jsonInner.put("goldDisplay", goldDisplayStyle);
					jsonInner.put("silverMedalCount", new Integer(silverMedals));
					jsonInner.put("silverDisplay", silverDisplayStyle);
					jsonInner.put("bronzeMedalCount", new Integer(bronzeMedals));
					jsonInner.put("bronzeDisplay", bronzeDisplayStyle);
					//log.debug("Adding: " + jsonInner.toString());
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
			log.error("getJsonScore Failure: " + e.toString());
			result = null;
		}
		catch(Exception e)
		{
			log.error("getJsonScore Unexpected Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		//log.debug("*** END getJsonScore ***");
		return result;
	}
	
	/**
	 * Used to gather a menu of lessons for a user, including markers for each lesson they have completed or not completed
	 * @param ApplicationRoot The current running context of the application
	 * @param userId Identifier of the user
	 * @return HTML lesson menu for Open Floor Plan.
	 */
	public static String getLessons (String ApplicationRoot, String userId, Locale lang)
	{
		log.debug("*** Getter.getLesson ***");
		//Getting Translated Level Names
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", lang);
		String output = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			//Get the lesson modules
			CallableStatement callstmt = conn.prepareCall("call lessonInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering lessonInfo ResultSet for user " + userId);
			ResultSet lessons = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while(lessons.next())
			{
				//log.debug("Adding " + lessons.getString(1));
				output += "<li>";
				//Markers for completion
				if(lessons.getString(4) != null)
				{
					output += "<img src='css/images/completed.png'/>";
				}
				else
				{
					output+= "<img src='css/images/uncompleted.png'/>";
				}
				//Prepare lesson output
				output += "<a class='lesson' id='" 
					+ Encode.forHtmlAttribute(lessons.getString(3))
					+ "' href='javascript:;'>" 
					+ Encode.forHtml(bundle.getString(lessons.getString(1))) 
					+ "</a>";
				output += "</li>";
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
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
			output = type + "/" + modules.getString(1) + ".jsp";
		}
		catch(Exception e)
		{
			log.error("Module Hash Retrieval: " + e.toString());
			log.error("moduleID = " + moduleId);
			log.error("userID = " + userId);
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleAddress() ***");
		return output;
	}
	
	/**
	 * Retrieves the module category based on the moduleId submitted
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId The id of the module that 
	 * @return
	 */
	public static String getModuleCategory (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Getter.getModuleResult ***");
		String theCategory = null;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * @param applicationRoot The current running context of the application.
	 * @param moduleId The identifier of a module
	 * @return The hash of the module specified
	 */
	public static String getModuleHash(String applicationRoot, String moduleId) 
	{
		log.debug("*** Getter.getModuleHash ***");
		String result = new String();
		Connection conn = Database.getCoreConnection(applicationRoot);
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
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * Returns true if a module has a hard coded key, false if server encrypts it
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId The id of the module 
	 * @return Returns true if a module has a hard coded key, false if server encrypts it
	 */
	public static boolean getModuleKeyType (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Getter.getModuleKeyType ***");
		boolean theKeyType = true;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement prepstmt = conn.prepareStatement("SELECT hardcodedKey FROM modules WHERE moduleId = ?");
			prepstmt.setString(1, moduleId);
			ResultSet moduleFind = prepstmt.executeQuery();
			moduleFind.next();
			theKeyType = moduleFind.getBoolean(1);
			if(theKeyType)
				log.debug("Module has hard coded Key");
			else
				log.debug("Module has user specific Key");
		}
		catch(Exception e)
		{
			log.error("Module did not exist: " + e.toString());
			theKeyType = true;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleKeyType ***");
		return theKeyType;
	}
	
	/**
	 * This method retrieves the i18n local key for a module's name.
	 * @param applicationRoot Application Running Context
	 * @param moduleId ID of the module to lookup
	 * @return Locale key for the Module's Name.
	 */
	public static String getModuleNameLocaleKey(String applicationRoot, String moduleId) 
	{
		log.debug("*** Getter.getModuleNameLocaleKey ***");
		String result = new String();
		Connection conn = Database.getCoreConnection(applicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleGetNameLocale(?)");
			log.debug("Gathering moduleGetNameLocale ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetNameLocale");
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch (SQLException e)
		{
			log.error("Could not execute moduleGetNameLocale: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getModuleNameLocaleKey ***");
		return result;
	}
	
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId Identifier of module
	 * @return The db stored solution key value for the moduleId submitted
	 */
	public static String getModuleResult (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Getter.getModuleResult ***");
		String moduleFound = null;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * @return The db stored solution key value for the moduleHash submited
	 */
	public static String getModuleResultFromHash (String ApplicationRoot, String moduleHash)
	{
		log.debug("*** Getter.getModuleResultFromHash ***");
		String result = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * @param ApplicationRoot The current running context of the application
	 * @return All modules in HTML option tags
	 */
	public static String getModulesInOptionTags (String ApplicationRoot)
	{
		log.debug("*** Getter.getModulesInOptionTags ***");
		String output = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement callstmt = conn.prepareStatement("SELECT moduleId, moduleName FROM modules ORDER BY moduleCategory, moduleName;");
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while(modules.next())
			{
				//Each module name is embed in option tags, with a value of their module identifier
				output += "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + "'>" +
						Encode.forHtml(modules.getString(2)) + "</option>\n";
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
	 * @param ApplicationRoot The current running context of the application
	 * @return All modules in HTML option tags ordered by incrementalRank
	 */
	public static String getModulesInOptionTagsCTF (String ApplicationRoot)
	{
		log.debug("*** Getter.getModulesInOptionTags ***");
		String output = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement callstmt = conn.prepareStatement("SELECT moduleId, moduleName FROM modules ORDER BY incrementalRank;");
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while(modules.next())
			{
				//Each module name is embed in option tags, with a value of their module identifier
				output += "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + "'>" +
						Encode.forHtml(modules.getString(2)) + "</option>\n";
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
	 * @param lang The Locale the user has enabled
	 * @return String[] containing {ModuleName, CheatSheetSolution}
	 */
	public static String[] getModuleSolution (String ApplicationRoot, String moduleId, Locale lang)
	{
		log.debug("*** Getter.getModuleSolution ***");
		String[] result = new String[2];
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		//Getting Translations
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.cheatsheets.solutions", lang);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call cheatSheetGetSolution(?)");
			log.debug("Gathering cheatSheetGetSolution ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from cheatSheetGetSolution");
			resultSet.next();
			result[0] = resultSet.getString(1);
			result[1] = bundle.getString(resultSet.getString(2));
			
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
	 * This method returns modules in option tags in different &lt;select&gt; elements depending on their current open/closed status. 
	 * The output assumes it is contained in a table context
	 * @param ApplicationRoot The Running Context of the Application
	 * @return Tr/td elements containing a moduleStatusMenu that has lists of the current open and closed modules
	 */
	public static String getModuleStatusMenu (String ApplicationRoot)
	{
		log.debug("*** Getter.getModuleStatusMenu ***");
		String openModules = new String();
		String closedModules = new String();
		String output = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			//Get the modules
			CallableStatement callstmt = conn.prepareCall("call moduleAllStatus()");
			log.debug("Gathering moduleAllStatus ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllStatus");
			while(modules.next())
			{
				String theModule = "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + 
						"'>" + Encode.forHtml(modules.getString(2)) + "</option>\n";
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
	 * This method returns the module categories in option tags that are to be open or closed in a &lt;select&gt; element for administration manipulation
	 * @param ApplicationRoot
	 * @return Module Category List for Html (&lt;SELECT&gt; element)
	 */
	public static String getOpenCloseCategoryMenu (String ApplicationRoot)
	{
		log.debug("*** Getter.getOpenCloseCategoryMenu ***");
		String theModules = new String();
		String output = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			//Get the modules
			CallableStatement callstmt = conn.prepareCall("SELECT DISTINCT moduleCategory FROM modules ORDER BY moduleCategory");
			ResultSet modules = callstmt.executeQuery();
			while(modules.next())
			{
				String theModule = "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + 
						"'>" + Encode.forHtml(modules.getString(1)) + "</option>\n";
				theModules += theModule;
			}
			//This is the actual output: It assumes a <table> environment
			output = "<select style='width: 300px; height: 200px;' multiple id='toDo'>" + theModules + "</select>\n";
			log.debug("Module Category Menu returned");
		}
		catch(Exception e)
		{
			log.error("Module Status Menu: " + e.toString());
		}
		Database.closeConnection(conn);
		return output;
	}
	/**
	 * This method is used to gather users according by class. Thanks to MySQL syntax, where class = null will return nothing, is null must be used.
	 *  <br/>is 'validClass' will Error, = 'validclass' must be used.<br/>
	 * So there are two procedures this method calls. One that handles null classes, one that does not
	 * @param ClassId Identifier of class
	 * @param ApplicationRoot The current running context of the application
	 * @return ResultSet that contains users for the selected class in the formate {userId, userName, userAddress}
	 */
	public static ResultSet getPlayersByClass(String ApplicationRoot, String classId)
	{
		ResultSet result = null;
		log.debug("*** Getter.getPlayersByClass (Single Class) ***");
		log.debug("classId: '" + classId + "'");
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
		Connection conn = Database.getCoreConnection(applicationRoot);
		try
		{
			log.debug("Preparing userProgress call");
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
					result += "<tr><td>" + Encode.forHtml(resultSet.getString(1)) + //Output their progress
						"</td><td><div style='background-color: #A878EF; heigth: 25px; width: " + widthOfUnitBar*resultSet.getInt(2) + "px;'>" +
								"<font color='white'><strong>" +
								resultSet.getInt(2);
					if(resultSet.getInt(2) > 6)
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
		Connection conn = Database.getCoreConnection(applicationRoot);
		try
		{
			log.debug("Preparing userProgress call");
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
					jsonInner.put("userName", new String(Encode.forHtml(resultSet.getString(1)))); //User Name
					jsonInner.put("progressBar", new Integer(resultSet.getInt(2)*widthOfUnitBar)); //Progress Bar Width
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
	private static int getTounnamentSectionFromRankNumber (int rankNumber)
	{
		if(rankNumber < fieldTrainingCap)
			return 1;
		else if (rankNumber < privateCap)
			return 2;
		else if (rankNumber < corporalCap)
			return 3;
		else if (rankNumber < sergeantCap)
			return 4;
		else if (rankNumber < lieutenantCap)
			return 5;
		else if (rankNumber < majorCap)
			return 6;
		else if (rankNumber < admiralCap)
			return 7;
		else
			return 7; //Max level is 7.
	}
	/**
	 * This method prepares the Tournament module menu. This is when Security Shepherd is in "Tournament Mode".
	 * Users are presented with a list of that are specified as open. 
	 * @param ApplicationRoot The running context of the application.
	 * @param userId The user identifier of the user.
	 * @param csrfToken The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for interaction with this menu
	 */
	public static String getTournamentModules (String ApplicationRoot, String userId, Locale lang)
	{
		log.debug("*** Getter.getTournamentModules ***");
		String levelMasterList = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		//Getting Translations
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", lang);
		ResourceBundle levelNames = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", lang);
		try
		{
			
			String listEntry = new String();
			//Get the modules
			CallableStatement callstmt = conn.prepareCall("call moduleTournamentOpenInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering moduleTournamentOpenInfo ResultSet for user " + userId);
			ResultSet levels = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleTournamentOpenInfo");
			int currentSection = 0; // Used to identify the first row, as it is slightly different to all other rows for output
			while(levels.next())
			{
				//Create Row Entry First
				//log.debug("Adding " + lessons.getString(1));
				listEntry = "<li>";
				//Markers for completion
				if(levels.getString(4) != null)
				{
					listEntry += "<img src='css/images/completed.png'/>";
				}
				else
				{
					listEntry += "<img src='css/images/uncompleted.png'/>";
				}
				//Prepare entry output
				listEntry += "<a class='lesson' id='" 
					+ Encode.forHtmlAttribute(levels.getString(3))
					+ "' href='javascript:;'>" 
					+ Encode.forHtml(levelNames.getString(levels.getString(1))) 
					+ "</a>\n";
				listEntry += "</li>";
				//What section does this belong in? Current or Next?
				if (getTounnamentSectionFromRankNumber(levels.getInt(5)) > currentSection)
				{
					//This level is not in the same level band as the previous level. So a new Level Band Header is required on the master list before we add the entry.
					//Do we need to close a previous list?
					if(currentSection != 0) //If a Section Select hasn't been made before, we don't need to close any previous sections
					{
						//We've had a section before, so need to close the previous one before we make this new one
						levelMasterList += "</ul>\n";
					}
					//Update the current section to the one we have just added to the list
					currentSection = getTounnamentSectionFromRankNumber(levels.getInt(5));
					//Which to Add?
					switch(currentSection)
					{
						case 1: //fieldTraining
							//log.debug("Starting Field Training List");
							levelMasterList += "<a id=\"fieldTrainingList\" href=\"javascript:;\"><div class=\"menuButton\">" + bundle.getString("getter.tournamentRank.1") + "</div></a>"
								+ "<ul id=\"theFieldTrainingList\" style=\"display: none;\" class='levelList'>\n";
							break;
						case 2: //private
							//log.debug("Starting Private List");
							levelMasterList += "<a id=\"privateList\" href=\"javascript:;\"><div class=\"menuButton\">" + bundle.getString("getter.tournamentRank.2") + "</div></a>"
								+ "<ul id=\"thePrivateList\" style=\"display: none;\" class='levelList'>\n";
							break;
						case 3: //corporal
							//log.debug("Starting Corporal List");
							levelMasterList += "<a id=\"corporalList\" href=\"javascript:;\"><div class=\"menuButton\">" + bundle.getString("getter.tournamentRank.3") + "</div></a>"
								+ "<ul id=\"theCorporalList\" style=\"display: none;\" class='levelList'>\n";
							break;
						case 4: //sergeant
							//log.debug("Starting Sergeant List");
							levelMasterList += "<a id=\"sergeantList\" href=\"javascript:;\"><div class=\"menuButton\">" + bundle.getString("getter.tournamentRank.4") + "</div></a>"
								+ "<ul id=\"theSergeantList\" style=\"display: none;\" class='levelList'>\n";
							break;
						case 5: //Lieutenant
							//log.debug("Starting Lieutenant List");
							levelMasterList += "<a id=\"lieutenantList\" href=\"javascript:;\"><div class=\"menuButton\">" + bundle.getString("getter.tournamentRank.5") + "</div></a>"
								+ "<ul id=\"theLieutenantList\" style=\"display: none;\" class='levelList'>\n";
							break;
						case 6: //major
							//log.debug("Starting Major List");
							levelMasterList += "<a id=\"majorList\" href=\"javascript:;\"><div class=\"menuButton\">" + bundle.getString("getter.tournamentRank.6") + "</div></a>"
								+ "<ul id=\"theMajorList\" style=\"display: none;\" class='levelList'>\n";
							break;
						case 7: //admiral
							//log.debug("Starting Admiral List");
							levelMasterList += "<a id=\"admiralList\" href=\"javascript:;\"><div class=\"menuButton\">" + bundle.getString("getter.tournamentRank.7") + "</div></a>"
								+ "<ul id=\"theAdmiralList\" style=\"display: none;\" class='levelList'>\n";
							break;
					}
				}
				//Now we can add the entry to the level master List and start again
				levelMasterList += listEntry;
				//log.debug("Put level in category: " + currentSection);
			}
			//If no output has been found, return an error message
			if(levelMasterList.isEmpty())
			{
				levelMasterList = "<ul><li><a href='javascript:;'>" + bundle.getString("getter.button.noModulesFound") + "</a></li></ul>";
			}
			else
			{
				//List is complete, but we need to close the last list we made, which deinfetly exists as the levelmasterList is not empty
				levelMasterList += "</ul>";
				log.debug("Tournament List returned");
			}
		}
		catch(Exception e)
		{
			log.error("Tournament List Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		return levelMasterList;
	}
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param userName The username of the user
	 * @return The class id of the submitted user name
	 */
	public static String getUserClassFromName (String ApplicationRoot, String userName)
	{
		log.debug("*** Getter.getUserClass ***");
		String result = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call userClassId(?)");
			log.debug("Gathering userClassId ResultSet");
			callstmt.setString(1, userName);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from userClassId");
			resultSet.next();
			result = resultSet.getString(1);
			log.debug("Found " + result);
		}
		catch (SQLException e)
		{
			log.error("Could not execute userClassId: " + e.toString());
			result = new String();
		}
		Database.closeConnection(conn);
		log.debug("*** END getUserClass ***");
		return result;
	}
	
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param userName The username of the user
	 * @return The user id of the submitted user name
	 */
	public static String getUserIdFromName (String ApplicationRoot, String userName)
	{
		log.debug("*** Getter.getUserIdFromName ***");
		String result = new String();
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call userGetIdByName(?)");
			log.debug("Gathering userGetIdByName ResultSet");
			callstmt.setString(1, userName);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from userGetIdByName");
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END getUserIdFromName ***");
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
		Connection conn = Database.getCoreConnection(ApplicationRoot);
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
	 * This method is used to determine if a CSRF level has been completed. 
	 * A call is made to the DB that returns the CSRF counter for a level. 
	 * If this counter is greater than 0, the level has been completed
	 * @param applicationRoot Running context of the application
	 * @param moduleHash Hash ID of the CSRF module you wish to check if a user has completed
	 * @param userId the ID of the user to check
	 * @return True or False value depicting if the user has completed the module
	 */
	public static boolean isCsrfLevelComplete (String applicationRoot, String moduleId, String userId)
	{
		log.debug("*** Setter.isCsrfLevelComplete ***");
		
		boolean result = false;
		
		Connection conn = Database.getCoreConnection(applicationRoot);
		try
		{
			log.debug("Preparing csrfLevelComplete call");
			CallableStatement callstmnt = conn.prepareCall("call csrfLevelComplete(?, ?)");
			callstmnt.setString(1, moduleId);
			callstmnt.setString(2, userId);
			log.debug("moduleId: " + moduleId);
			log.debug("userId: " + userId);
			log.debug("Executing csrfLevelComplete");
			ResultSet resultSet = callstmnt.executeQuery();
			resultSet.next();
			result = resultSet.getInt(1) > 0; // If Result is > 0, then the CSRF level is complete
			if(result)
				log.debug("CSRF Level is complete");
		}
		catch(SQLException e)
		{
			log.error("csrfLevelComplete Failure: " + e.toString());
			result = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END isCsrfLevelComplete ***");
		return result;
	}
	
	public static boolean isModuleOpen (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Getter.isModuleOpen ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			//Get the modules
			PreparedStatement prepStmt = conn.prepareCall("SELECT moduleStatus FROM modules WHERE moduleId = ?");
			prepStmt.setString(1, moduleId);
			ResultSet rs = prepStmt.executeQuery();
			if(rs.next())
			{
				if(rs.getString(1).equalsIgnoreCase("open"))
				{
					result = true;
				}
			}
			rs.close();
		}
		catch(Exception e)
		{
			log.error("isModuleOpen Error: " + e.toString());
		}
		Database.closeConnection(conn);
		return result;
	}
	
	/**
	* @param ApplicationRoot The current running context of the application
	* @return Result set containing admin info in the order userId, userName and userAddress
	*/
	public static ResultSet getAdmins(String ApplicationRoot)
	{
		ResultSet result = null;
		log.debug("*** Getter.adminGetAll () ***");
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call adminGetAll()");
			log.debug("Gathering adminGetAll ResultSet");
			result = callstmt.executeQuery();
			log.debug("Returning Result Set from adminGetAll");
		}
		catch (SQLException e)
		{
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END adminGetAll ***");
		return result;
	}
		
	/**
	 * Used to decipher whether or not a user exists as an admin
	 * @param userId The user identifier of the admin to be found
	 * @return A boolean reflecting the state of existence of the admin
	 */
	public static boolean findAdminById (String ApplicationRoot, String userId)
	{
		log.debug("*** Getter.findAdminById ***");
		boolean userFound = false;
		//Get connection
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call adminFindById(?)");
			log.debug("Gathering adminFindById ResultSet");
			callstmt.setString(1, userId);
			ResultSet userFind = callstmt.executeQuery();
			log.debug("Opening Result Set from adminFindById");
			userFind.next(); //This will throw an exception if player not found
			log.debug("Admin Found: " + userFind.getString(1)); //This line will not execute if admin not found
			userFound = true;
		}
		catch(Exception e)
		{
			log.error("Admin does not exist: " + e.toString());
			userFound = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END findAdminById ***");
		return userFound;
	}	
}
