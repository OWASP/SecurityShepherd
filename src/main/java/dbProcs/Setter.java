package dbProcs;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * Used to add information to the Database
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
public class Setter 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(Setter.class);	
	/**
	 * Database procedure just adds this. So this method just prepares the statement
	 * @param ApplicationRoot
	 * @param className Class name
	 * @param classYear Year of the class in YY/YY. eg 11/12 
	 * @return
	 */
	public static boolean classCreate (String ApplicationRoot, String className, String classYear)
	{
		log.debug("*** Setter.classCreate ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing classCreate call");
			CallableStatement callstmnt = conn.prepareCall("call classCreate(?, ?)");
			callstmnt.setString(1, className);
			callstmnt.setString(2, classYear);
			log.debug("Executing classCreate");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("classCreate Failure: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END classCreate ***");
		return result;
	}
	
	/**
	 * This method sets every module status to Closed.
	 * @param ApplicationRoot Current running director of the application
	 * @param moduleId The identifier of the module that is been set to open status
	 * @return Boolean result depicting success of statement
	 */
	public static boolean closeAllModules (String ApplicationRoot)
	{
		log.debug("*** Setter.closeAllModules ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement callstmt = conn.prepareStatement("UPDATE modules SET moduleStatus = 'closed'");
			callstmt.execute();
			log.debug("All modules Set to closed");
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not close all modules: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END closeAllModules ***");
		return result;
	}
	
	/**
	 * Used to increment bad submission counter in DB. DB will handle point deductions once the counter hits 40
	 * @param ApplicationRoot application running context
	 * @param userId user identifier to increment 
	 * @return False if the statement fails to execute
	 */
	public static boolean incrementBadSubmission(String ApplicationRoot, String userId)
	{
		log.debug("*** Setter.incrementBadSubmission ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Prepairing bad Submission call");
			PreparedStatement callstmnt = conn.prepareCall("CALL userBadSubmission(?)");
			callstmnt.setString(1, userId);
			log.debug("Executing userBadSubmission statement on id '" + userId + "'");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("userBadSubmission Failure: " + e.toString());
			result = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END userBadSubmisison ***");
		return result;
	}
	
	/**
	 * This method sets every module status to Open.
	 * @param ApplicationRoot Current running director of the application
	 * @param moduleId The identifier of the module that is been set to open status
	 * @return Boolean result depicting success of statement
	 */
	public static boolean openAllModules (String ApplicationRoot)
	{
		log.debug("*** Setter.openAllModules ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement callstmt = conn.prepareStatement("UPDATE modules SET moduleStatus = 'open'");
			callstmt.execute();
			log.debug("All modules Set to open");
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not open all modules: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END setModuleStatusOpen ***");
		return result;
	}
	
	//TODO - Replace this with a new mobile/web/etc attribute in the modules table
	final public static String webModuleCategoryHardcodedWhereClause = new String(""
			+ "moduleCategory = 'CSRF'"
			+ " OR moduleCategory = 'Failure to Restrict URL Access'"
			+ " OR moduleCategory = 'Injection'"
			+ " OR moduleCategory = 'Insecure Cryptographic Storage'"
			+ " OR moduleCategory = 'Insecure Direct Object References'"
			+ " OR moduleCategory = 'Poor Data Validation'"
			+ " OR moduleCategory = 'Security Misconfigurations'"
			+ " OR moduleCategory = 'Session Management'"
			+ " OR moduleCategory = 'Unvalidated Redirects and Forwards'"
			+ " OR moduleCategory = 'XSS'");
	final public static String mobileModuleCategoryHardcodedWhereClause = new String(""
			+ "moduleCategory = 'Mobile Broken Crypto'"
			+ " OR moduleCategory = 'Mobile Content Provider'"
			+ " OR moduleCategory = 'Mobile Data Leakage'"
			+ " OR moduleCategory = 'Mobile Injection'"
			+ " OR moduleCategory = 'Mobile Insecure Data Storage'"
			+ " OR moduleCategory = 'Mobile Poor Authentication'"
			+ " OR moduleCategory = 'Mobile Reverse Engineering'"
			+ " OR moduleCategory = 'Mobile Security Decisions via Untrusted Input'");
	
	/**
	 * This is used to only open Mobile category levels
	 * @param ApplicationRoot Used to locate database properties file
	 * @return
	 */
	public static boolean openOnlyMobileCategories (String ApplicationRoot)
	{
		log.debug("*** Setter.openOnlyMobileCategories ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement prepstmt = conn.prepareStatement("UPDATE modules SET moduleStatus = 'closed' WHERE " + webModuleCategoryHardcodedWhereClause);
			prepstmt.execute();
			log.debug("Web Levels have been closed");
			prepstmt = conn.prepareStatement("UPDATE modules SET moduleStatus = 'open' WHERE " + mobileModuleCategoryHardcodedWhereClause);
			prepstmt.execute();
			log.debug("Mobile Levels have been opened");
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not only open Mobile Levels: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END openOnlyMobileCategories ***");
		return result;
	}
	
	/**
	 * This is used to only open Mobile category levels
	 * @param ApplicationRoot Used to locate database properties file
	 * @return
	 */
	public static boolean openOnlyWebCategories (String ApplicationRoot)
	{
		log.debug("*** Setter.openOnlyWebCategories ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement prepstmt = conn.prepareStatement("UPDATE modules SET moduleStatus = 'open' WHERE " + webModuleCategoryHardcodedWhereClause);
			prepstmt.execute();
			log.debug("Web Levels have been opened");
			prepstmt = conn.prepareStatement("UPDATE modules SET moduleStatus = 'closed' WHERE " + mobileModuleCategoryHardcodedWhereClause);
			prepstmt.execute();
			log.debug("Mobile Levels have been closed");
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not only open Web levels: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END openOnlyWebCategories ***");
		return result;
	}
	
	/**
	 * Resets user bad submission counter to 0
	 * @param ApplicationRoot Application's running context
	 * @param userId User Identifier to reset
	 * @return
	 */
	public static boolean resetBadSubmission(String ApplicationRoot, String userId)
	{
		log.debug("*** Setter.resetBadSubmission ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Prepairing resetUserBadSubmission call");
			PreparedStatement callstmnt = conn.prepareCall("CALL resetUserBadSubmission(?)");
			callstmnt.setString(1, userId);
			log.debug("Executing resetUserBadSubmission statement on id '" + userId + "'");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("resetUserBadSubmission Failure: " + e.toString());
			result = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END resetBadSubmission ***");
		return result;
	}
	
	/**
	 * This method converts the default database properties file at applicationRoot/WEB-INF/site.properties
	 * @param applicationRoot The directory that the server is actually in
	 * @param url The Url of the core Database
	 * @param userName The user name of the database user
	 * @param password The password of the database user
	 * @return Boolean value depicting the success of the method
	 */
	public static boolean setCoreDatabaseInfo(String applicationRoot, String url, String userName, String password)
	{
		try 
		{
			//Update Database Settings
			File siteProperties = new File(applicationRoot + "/WEB-INF/database.properties");
			DataOutputStream writer = new DataOutputStream(new FileOutputStream(siteProperties,false));
			String theProperties = new String("databaseConnectionURL=" + url +
										"\nDriverType=org.gjt.mm.mysql.Driver");
			writer.write(theProperties.getBytes());
			writer.close();
			//Update Core Schema Settings
			siteProperties = new File(applicationRoot + "/WEB-INF/coreDatabase.properties");
			writer = new DataOutputStream(new FileOutputStream(siteProperties,false));
			theProperties = new String("databaseConnectionURL=core"+					
					"\ndatabaseUsername=" + userName +
					"\ndatabasePassword=" + password);
			writer.write(theProperties.getBytes());
			writer.close();
			return true;
		} 
		catch (IOException e) 
		{
			log.error("Could not update Core Database Info: " + e.toString());
			return false;
		}
	}
	
	/**
	 * This method is used to store a CSRF Token for a specific user in the csrfChallengeSeven DB Schema. May not necessarily be a new CSRF token after running
	 * @param userId User Identifier
	 * @param csrfToken CSRF Token to add to the csrfChallengeFour DB Schema
	 * @param ApplicationRoot Running context of the application
	 * @return Returns current CSRF token for user for CSRF Ch4 
	 */
	public static String setCsrfChallengeFourCsrfToken (String userId, String csrfToken, String ApplicationRoot)
	{
		log.debug("*** setCsrfChallengeFourToken ***");
		Connection conn = Database.getChallengeConnection(ApplicationRoot, "csrfChallengeFour");
		try
		{
			boolean tokenExists = false;
			log.debug("Preparing setSsrfChallengeFourToken call");
			PreparedStatement callstmnt = conn.prepareStatement("SELECT csrfTokenscol FROM csrfTokens WHERE userId = ?");
			callstmnt.setString(1, userId);
			log.debug("Executing setCsrfChallengeFourToken");
			ResultSet rs = callstmnt.executeQuery();
			if(rs.next())
			{
				//Need to Update CSRF token rather than Insert
				log.debug("CSRF for Challenge 4 already is set");
				csrfToken = rs.getString(1); //overwrite token with DB Stored Entry
				tokenExists = true;
			}
			else
			{
				log.debug("No CSRF token Found for Challenge 4... Creating");
			}
			rs.close();
			
			String whatToDo = new String();
			if(!tokenExists)
				whatToDo = "INSERT INTO `csrfChallengeFour`.`csrfTokens` (`csrfTokenscol`, `userId`) VALUES (?, ?)";
			callstmnt = conn.prepareStatement(whatToDo);
			callstmnt.setString(1, csrfToken);
			callstmnt.setString(2, userId);
			callstmnt.execute();
			callstmnt.close();
		}
		catch(SQLException e)
		{
			log.error("CsrfChallenge4 TokenUpdate Failure: " + e.toString());
		}
		Database.closeConnection(conn);		
		return csrfToken;
	}
	
	/**
	 * This method is used to store a CSRF Token for a specific user in the csrfChallengeSix DB Schema
	 * @param userId User Identifier
	 * @param csrfToken CSRF Token to add to the csrfChallengeSix DB Schema
	 * @param ApplicationRoot Running context of the application
	 * @return
	 */
	public static boolean setCsrfChallengeSevenCsrfToken (String userId, String csrfToken, String ApplicationRoot)
	{
		log.debug("*** setCsrfChallengeSevenToken ***");
		boolean result = false;
		Connection conn = Database.getChallengeConnection(ApplicationRoot, "csrfChallengeEnumerateTokens");
		try
		{
			boolean updateToken = false;
			log.debug("Preparing setCsrfChallengeSevenToken call");
			PreparedStatement callstmnt = conn.prepareStatement("SELECT csrfTokenscol FROM csrfTokens WHERE userId = ?");
			callstmnt.setString(1, userId);
			log.debug("Executing setCsrfChallengeSevenToken");
			ResultSet rs = callstmnt.executeQuery();
			if(rs.next())
			{
				//Need to Update CSRF token rather than Insert
				log.debug("CSRF token Found for Challenge 7... Updating");
				updateToken = true;
			}
			else
			{
				log.debug("No CSRF token Found for Challenge 7... Creating");
			}
			rs.close();
			
			String whatToDo = new String();
			if(updateToken)
				whatToDo = "UPDATE `csrfChallengeEnumTokens`.`csrfTokens` SET csrfTokenscol = ? WHERE userId = ?";
			else
				whatToDo = "INSERT INTO `csrfChallengeEnumTokens`.`csrfTokens` (`csrfTokenscol`, `userId`) VALUES (?, ?)";
			callstmnt = conn.prepareStatement(whatToDo);
			callstmnt.setString(1, csrfToken);
			callstmnt.setString(2, userId);
			callstmnt.execute();
			result = true;
			callstmnt.close();
		}
		catch(SQLException e)
		{
			log.error("csrfChallenge7EnumTokens TokenUpdate Failure: " + e.toString());
		}
		Database.closeConnection(conn);		
		return result;
	}
	
	/**
	 * This method is used to set the status of all modules in a category to open or closed.
	 * @param ApplicationRoot Used to locate database properties file
	 * @param moduleCategory The module category to open or closed
	 * @param openOrClosed What to set the module status to. Can only be "open" or "closed"
	 * @return True if method executes without failure
	 */
	public static boolean setModuleCategoryStatusOpen (String ApplicationRoot, String moduleCategory, String openOrClosed)
	{
		log.debug("*** Setter.setModuleCategoryStatusOpen ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			PreparedStatement prepstmt = conn.prepareStatement("UPDATE modules SET moduleStatus = ? WHERE moduleCategory = ?");
			prepstmt.setString(1, openOrClosed);
			prepstmt.setString(2, moduleCategory);
			prepstmt.execute();
			log.debug("Set " + moduleCategory + " to " + openOrClosed);
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not open/close category: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END setModuleCategoryStatusOpen ***");
		return result;
	}
	
	/**
	 * This method sets the module status to Closed. This information is absorbed by the Tournament Floor Plan
	 * @param ApplicationRoot Current running director of the application
	 * @param moduleId The identifier of the module that is been set to closed status
	 * @return Boolean result depicting success of statement
	 */
	public static boolean setModuleStatusClosed (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Setter.setModuleStatusClosed ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleSetStatus(?, ?)");
			log.debug("Preparing moduleSetStatus procedure");
			callstmt.setString(1, moduleId);
			callstmt.setString(2, "closed");
			callstmt.execute();
			log.debug("Executed moduleSetStatus");
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not execute moduleSetStatus: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END setModuleStatusClosed ***");
		return result;
	}
	
	/**
	 * This method sets the module status to Open. This information is absorbed by the Tournament Floor Plan
	 * @param ApplicationRoot Current running director of the application
	 * @param moduleId The identifier of the module that is been set to open status
	 * @return Boolean result depicting success of statement
	 */
	public static boolean setModuleStatusOpen (String ApplicationRoot, String moduleId)
	{
		log.debug("*** Setter.setModuleStatusOpen ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call moduleSetStatus(?, ?)");
			log.debug("Preparing moduleSetStatus procedure");
			callstmt.setString(1, moduleId);
			callstmt.setString(2, "open");
			callstmt.execute();
			log.debug("Executed moduleSetStatus");
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not execute moduleSetStatus: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END setModuleStatusOpen ***");
		return result;
	}
	
	/**
	 * Used by CSRF levels to store their CSRF attack string, that will be displayed in a CSRF forum for the class the user is in
	 * @param ApplicationRoot The current running context of the application
	 * @param message The String they want to store
	 * @param userId The identifier of the user in which to store the attack under
	 * @param moduleId The module identifier of which to store the message under
	 * @return A boolean value reflecting the success of the function
	 */
	public static boolean setStoredMessage (String ApplicationRoot, String message, String userId, String moduleId)
	{
		log.debug("*** Setter.setStoredMessage ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call resultMessageSet(?, ?, ?)");
			log.debug("Preparing resultMessageSet procedure");
			callstmt.setString(1, message);
			callstmt.setString(2, userId);
			callstmt.setString(3, moduleId);
			callstmt.execute();
			log.debug("Executed resultMessageSet");
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not execute resultMessageSet: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END setStoredMessage ***");
		return result;
	}
	
	/**
	 * Sets user to suspended in the database for a specific amount of time. This prevents them from signing into the application
	 * @param ApplicationRoot Running context of application
	 * @param userId User Identifier of the to be suspended user
	 * @param numberOfMinutes Amount of minutes to suspend user
	 * @return Returns true if statement succeeds without fatal error
	 */
	public static boolean suspendUser(String ApplicationRoot, String userId, int numberOfMinutes)
	{
		log.debug("*** Setter.suspendUser ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Prepairing suspendUser call");
			PreparedStatement callstmnt = conn.prepareCall("CALL suspendUser(?, ?)");
			callstmnt.setString(1, userId);
			callstmnt.setInt(2, numberOfMinutes);
			log.debug("Executing suspendUser statement on id '" + userId + "'");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("suspendUser Failure: " + e.toString());
			result = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END suspendUser ***");
		return result;
	}
	
	/**
	 * Revokes a suspension that may have been applied to a user
	 * @param ApplicationRoot Running context of application
	 * @param userId The Identifier of the user that will be released from suspension
	 * @return
	 */
	public static boolean unSuspendUser(String ApplicationRoot, String userId)
	{
		log.debug("*** Setter.unSuspendUser ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Prepairing suspendUser call");
			PreparedStatement callstmnt = conn.prepareCall("CALL unSuspendUser(?)");
			callstmnt.setString(1, userId);
			log.debug("Executing unSuspendUser statement on id '" + userId + "'");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("unSuspendUser Failure: " + e.toString());
			result = false;
		}
		Database.closeConnection(conn);
		log.debug("*** END unSuspendUser ***");
		return result;
	}
	
	/**
	 * Used to increment a users CSRF counter for CSRF levels.
	 * @param ApplicationRoot The current running context of the application.
	 * @param moduleId The identifier of the module to increment the counter of
	 * @param userId The user to be incremented
	 * @return Boolean reflecting the success of the operation
	 */
	public static boolean updateCsrfCounter (String ApplicationRoot, String moduleId, String userId)
	{
		log.debug("*** Getter.updateCsrfCounter ***");
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			CallableStatement callstmt = conn.prepareCall("call resultMessagePlus(?, ?)");
			log.debug("Preparing resultMessagePlus procedure");
			callstmt.setString(1, moduleId);
			callstmt.setString(2, userId);
			callstmt.execute();
			result = true;
		}
		catch (SQLException e)
		{
			log.error("Could not execute resultMessagePlus: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END updateCsrfCounter ***");
		return result;
	}
	
	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param userName User name of the user
	 * @param currentPassword User's current password
	 * @param newPassword New password to use in update
	 * @return ResultSet that contains error details if not successful
	 */
	public static boolean updatePassword (String ApplicationRoot, String userName, String currentPassword, String newPassword)
	{
		log.debug("*** Setter.updatePassword ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing userPasswordChange call");
			CallableStatement callstmnt = conn.prepareCall("call userPasswordChange(?, ?, ?)");
			callstmnt.setString(1, userName);
			callstmnt.setString(2, currentPassword);
			callstmnt.setString(3, newPassword);
			log.debug("Executing userPasswordChange");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("updatePassword Failure: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END updatePassword ***");
		return result;
	}
	
	/**
	 * Updates a player's password without needing the current password
	 * @param ApplicationRoot Running context of the applicaiton
	 * @param userId The user id of the user to update
	 * @param newPassword The new password to assign to the user
	 * @return
	 */
	public static boolean updatePasswordAdmin (String ApplicationRoot, String userId, String newPassword)
	{
		log.debug("*** Setter.updatePasswordAdmin ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing userPasswordChangeAdmin call");
			CallableStatement callstmnt = conn.prepareCall("call userPasswordChangeAdmin(?, ?)");
			callstmnt.setString(1, userId);
			callstmnt.setString(2, newPassword);
			log.debug("Executing userPasswordChangeAdmin");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("updatePasswordAdmin Failure: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END updatePasswordAdmin ***");
		return result;
	}
	
	/**
	 * Updates a PLAYER's class identifier
	 * @param ApplicationRoot The current running context of the application
	 * @param classId New class to be assigned to
	 * @param playerId Player to be assigned to new class
	 * @return The userName that was updated
	 */
	public static String updatePlayerClass (String ApplicationRoot, String classId, String playerId)
	{
		log.debug("*** Setter.updatePlayerClass ***");
		
		String result = null;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing playerUpdateClass call");
			CallableStatement callstmnt = conn.prepareCall("call playerUpdateClass(?, ?)");
			callstmnt.setString(1, playerId);
			callstmnt.setString(2, classId);
			log.debug("Executing playerUpdateClass");
			ResultSet resultSet = callstmnt.executeQuery();
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch(SQLException e)
		{
			log.error("playerUpdateClass Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END updatePlayerClass ***");
		return result;
	}
	
	/**
	 * Updates a PLAYER's class identifier to null
	 * @param ApplicationRoot The current running context of the application
	 * @param playerId The identifier of the player to be assigned to class NULL
	 * @return The userName that was updated
	 */
	public static String updatePlayerClassToNull (String ApplicationRoot, String playerId)
	{
		log.debug("*** Setter.updatePlayerClassToNull ***");
		
		String result = null;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing playerUpdateClassToNull call");
			CallableStatement callstmnt = conn.prepareCall("call playerUpdateClassToNull(?)");
			callstmnt.setString(1, playerId);
			log.debug("Executing playerUpdateClassToNull");
			ResultSet resultSet = callstmnt.executeQuery();
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch(SQLException e)
		{ 	
			log.error("updatePlayerClassToNull Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END updatePlayerClassToNull ***");
		return result;
	}
	
	/**
	 * Updates a users result of a specific module
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId Identifier of the module the user is completing
	 * @param userId Identifier of the user completing the module
	 * @param extra The additional comments submitted in feedback by the user, or if CSRF, the attack string they used
	 * @param before The knowledge the user felt before they completed the level
	 * @param after The knowledge the user felt after they completed the level
	 * @param difficulty The difficulty the user felt they encountered
	 * @return The module name of the module completed by the user
	 */
	public static String updatePlayerResult(String ApplicationRoot, String moduleId, String userId, String extra, int before, int after, int difficulty)
	{
		log.debug("*** Setter.updatePlayerResult ***");
		
		String result = null;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing userUpdateResult call");
			CallableStatement callstmnt = conn.prepareCall("call userUpdateResult(?, ?, ?, ?, ?, ?)");
			callstmnt.setString(1, moduleId);
			callstmnt.setString(2, userId);
			callstmnt.setInt(3, before);
			callstmnt.setInt(4, after);
			callstmnt.setInt(5, difficulty);
			callstmnt.setString(6, extra);
			log.debug("Executing userUpdateResult");
			callstmnt.execute();
			//User Executed. Now Get the Level Name Langauge Key
			result = Getter.getModuleNameLocaleKey(ApplicationRoot, moduleId);
		}
		catch(SQLException e)
		{
			log.error("userUpdateResult Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END updatePlayerResult ***");
		return result;
	}
	
	/**
	 * Adds or Subtracts points from a user. 
	 * @param ApplicationRoot Running context of application
	 * @param userId Identifier of user to update
	 * @param points Positive or Negative number of points to update by
	 * @return Returns true if statement executes without fatal error
	 */
	public static boolean updateUserPoints (String ApplicationRoot, String userId, int points)
	{
		log.debug("*** Setter.updateUserPoints ***");
		
		boolean result = false;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing updateUserPoints call");
			CallableStatement callstmnt = conn.prepareCall("UPDATE users SET userScore = userScore + ? WHERE userId = ?");
			callstmnt.setInt(1, points);
			callstmnt.setString(2, userId);
			log.debug("Executing updateUserPoints");
			callstmnt.execute();
			result = true;
		}
		catch(SQLException e)
		{
			log.error("updateUserPoints Failure: " + e.toString());
		}
		Database.closeConnection(conn);
		log.debug("*** END updateUserPoints ***");
		return result;
	}
	
	/**
	 * Updates a USER's role
	 * @param ApplicationRoot The current running context of the application
	 * @param playerId The identifier of the player to update
	 * @param newRole Must be "player" or "admin"
	 * @return The user name of the user updated
	 */
	public static String updateUserRole(String ApplicationRoot, String playerId, String newRole)
	{
		log.debug("*** Setter.updateUserRole ***");
		
		String result = null;
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Preparing userUpdateRole call");
			CallableStatement callstmnt = conn.prepareCall("call userUpdateRole(?, ?)");
			callstmnt.setString(1, playerId);
			callstmnt.setString(2, newRole);
			log.debug("Executing userUpdateRole");
			ResultSet resultSet = callstmnt.executeQuery();
			resultSet.next();
			result = resultSet.getString(1);
		}
		catch(SQLException e)
		{
			log.error("userUpdateRole Failure: " + e.toString());
			result = null;
		}
		Database.closeConnection(conn);
		log.debug("*** END updateUserRole ***");
		return result;
	}
	
	/**
	 * Used by many functions to create players or admins
	 * @param ApplicationRoot
	 * @param classId Cannot be null, relationship depending
	 * @param userName Cannot be null
	 * @param userPass Cannot be null
	 * @param userRole Cannot be null, must be "player" or "admin"
	 * @param userAddress Must be unique
	 * @param tempPass Whether or not to set the user with a temporary pass flag
	 * @return A boolean value determining the result of the creation
	 * @throws SQLException If the creation fails, a Exception is thrown
	 */
	public static boolean userCreate (String ApplicationRoot, String classId, String userName, String userPass, String userRole, String userAddress, boolean tempPass)
	throws SQLException
	{
		boolean result = false;
		log.debug("*** Setter.userCreate ***");
		log.debug("classId = " + classId);
		log.debug("userName" + userName);
		log.debug("userRole" + userRole);
		log.debug("userAddress" + userAddress);
		Connection conn = Database.getCoreConnection(ApplicationRoot);
		try
		{
			log.debug("Executing userCreate procedure on Database");
			CallableStatement callstmt = conn.prepareCall("call userCreate(?, ?, ?, ?, ?, ?)");
			callstmt.setString(1, classId);
			callstmt.setString(2, userName);
			callstmt.setString(3, userPass);
			callstmt.setString(4, userRole);
			callstmt.setString(5, userAddress);
			callstmt.setBoolean(6, tempPass);
			ResultSet registerAttempt = callstmt.executeQuery();
			log.debug("Opening result set");
			boolean goOn = false;
			try
			{
				registerAttempt.next(); //Procedure Ran correctly
				goOn = true;
			}
			catch(Exception e)
			{
				log.fatal("Could not open result set for register...");
				result = false;
			}
			if(goOn)
			{
				if(registerAttempt.getString(1) == null) //Registration success
				{
					log.debug("Register Success");
					result = true;
				}
				else //Registration failure
				{
					result = false;
					log.debug("ResultSet contained -> " + registerAttempt.getString(1));
					throw new SQLException(registerAttempt.getString(1));
				}
			}
		}
		catch(SQLException e)
		{
			log.fatal("userCreate Failure: " + e.toString());
			throw new SQLException(e);
		}
		Database.closeConnection(conn);
		log.debug("*** END userCreate ***");	
		return result;
	}
	
	public static boolean userDelete (String ApplicationRoot, String userId) throws SQLException
	 {
         boolean result = false;
         log.debug("*** Setter.userDelete ***");
         log.debug("userId = " + userId);

         Connection conn = Database.getCoreConnection(ApplicationRoot);
         try {
        	 log.debug("Deleting User's Results");
             PreparedStatement callDelResults = conn.prepareStatement("DELETE FROM results WHERE userId = ?");
             callDelResults.setString(1, userId);
             callDelResults.executeUpdate();
        	 
             log.debug("Executing delete from users on Database");
             PreparedStatement callUserDel = conn.prepareStatement("DELETE FROM users WHERE userId = ?");
             callUserDel.setString(1, userId);
             int deleteAttemptResult = callUserDel.executeUpdate();

             if (deleteAttemptResult == 1) {
                     result = true;
             }
         }
         catch(SQLException sqlEx) {
             log.fatal("userDelete Failure: " + sqlEx.toString());
             throw new SQLException(sqlEx);
         }
         Database.closeConnection(conn);
         log.debug("*** END userDelete ***");
         return result;
	 }
	
}
