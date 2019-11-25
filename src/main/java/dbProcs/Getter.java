package dbProcs;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.owasp.encoder.Encode;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import servlets.Register;
import utils.ModulePlan;
import utils.ScoreboardStatus;

/**
 * Used to retrieve information from the Database <br/>
 * <br/>
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 *
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 *
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mark
 */
public class Getter {
	private static org.apache.log4j.Logger log = Logger.getLogger(Getter.class);
	/**
	 * Used for scoreboards / progress bars
	 */
	private static int widthOfUnitBar = 11; // px
	private static int fieldTrainingCap = 45;

	private static int privateCap = 80;

	private static int corporalCap = 105;

	private static int sergeantCap = 130;

	private static int lieutenantCap = 145;

	private static int majorCap = 175;

	private static int admiralCap = 999; // everything above Major is Admiral

	/**
	 * This method hashes the user submitted password and sends it to the database.
	 * The database does the rest of the work, including Brute Force prevention.
	 * 
	 * @param userName The submitted user name to be used in authentication process
	 * @param password The submitted password in plain text to be used in
	 *                 authentication
	 * @return A string array made up of nothing or information to be consumed by
	 *         the initiating authentication process.
	 */

	public static String[] authUser(String ApplicationRoot, String userName, String password) {
		String[] result = null;
		log.debug("$$$ Getter.authUser $$$");

		log.debug("userName = " + userName);

		boolean userFound = false;
		boolean userVerified = false;

		Connection conn;
		try {
			conn = Database.getCoreConnection(ApplicationRoot);
		} catch (SQLException e) {
			log.fatal("Could create get core connection: " + e.toString());
			throw new RuntimeException(e);
		}

		// See if user Exists
		CallableStatement callstmt;
		try {
			callstmt = conn.prepareCall(
					"SELECT userId, userName, userPass, userRole, badLoginCount, tempPassword, classId, suspendedUntil, loginType, tempUsername FROM `users` WHERE userName = ?");
		} catch (SQLException e) {
			log.fatal("Could create call statement: " + e.toString());
			throw new RuntimeException(e);
		}

		log.debug("Gathering results from query");
		ResultSet userResult;
		try {
			callstmt.setString(1, userName);
			userResult = callstmt.executeQuery();
		} catch (SQLException e) {
			log.fatal("Could not execute db query: " + e.toString());
			throw new RuntimeException(e);
		}

		log.debug("Opening Result Set from query");

		try {
			if (userResult.next()) {
				log.debug("User Found"); // User found if a row is in the database, this line will not work if the
											// result
				// set is empty
				userFound = true;
			} else {
				log.debug("User did not exist");
				userFound = false;
			}
		} catch (SQLException e) {
			log.debug("User did not exist");
			userFound = false;
		}

		if (userFound) {
			// Authenticate User
			Argon2 argon2 = Argon2Factory.create();

			log.debug("Getting password hash");
			String dbHash;
			try {
				dbHash = userResult.getString(3);
				log.debug("Verifying hash");

				userVerified = argon2.verify(dbHash, password.toCharArray());

			} catch (SQLException e) {
				log.fatal("Could not retrieve password hash from db: " + e.toString());
				result = null;
				userVerified = false;
				throw new RuntimeException(e);
				// TODO: We should throw a checked exception here instead
			}

			if (userVerified) {
				// Hash matches
				log.debug("Hash matches");

				result = new String[6];

				int badLoginCount;
				String loginType = new String();

				Timestamp suspendedUntil;

				try {
					result[0] = userResult.getString(1);
					result[1] = userResult.getString(2); // userName
					result[2] = userResult.getString(4); // role
					badLoginCount = userResult.getInt(5);
					result[3] = Boolean.toString(userResult.getBoolean(6));
					result[4] = userResult.getString(7); // classId
					suspendedUntil = userResult.getTimestamp(8);
					loginType = userResult.getString(9);
					result[5] = Boolean.toString(userResult.getBoolean(10));
				} catch (SQLException e) {

					log.fatal("Could not retrieve auth data from db: " + e.toString());
					throw new RuntimeException(e);
				}

				if (!loginType.equals("login")) {
					// Login type must be "login" and not "saml" if password login is to be allowed
					log.debug("User is SSO user, can't login with password!");
					result = null;
					return result;
				}

				// Get current system time
				Timestamp currentTime = new Timestamp(System.currentTimeMillis());

				if (suspendedUntil.after(currentTime)) {
					// User is suspended
					result = null;
					return result;
				}

				if (!result[1].equalsIgnoreCase(userName)) // If somehow this functionality has been compromised to sign
															// in as
				// other users, this will limit the expoitability. But the method is
				// sql injection safe, so it should be ok
				{
					log.fatal("User Name used (" + userName + ") and User Name retrieved (" + result[1]
							+ ") were not the Same. Nulling Result");
					result = null;
				} else {
					log.debug("User '" + userName + "' has logged in");
					// Before finishing, check if user had a badlogin history, if so, Clear it
					if (badLoginCount > 0) {
						log.debug("Clearing Bad Login History");
						try {
							callstmt = conn.prepareCall("call userBadLoginReset(?)");
							callstmt.setString(1, result[0]);
							callstmt.execute();
						} catch (SQLException e) {
							log.fatal("Could not reset bad login count: " + e.toString());
							throw new RuntimeException(e);
						}

						log.debug("userBadLoginReset executed!");
					}
				}
				// User has logged in, or a Authentication Bypass was detected... You never
				// know! Better safe than sorry
				// TODO: will this close the db connection if we return here?
				return result;
			} else {
				// Hash did not match
				log.debug("Hash did not match, authentication failed");
			}

		}

		Database.closeConnection(conn);
		log.debug("$$$ End authUser $$$");
		return result;
	}

	/**
	 * This method hashes the user submitted password and sends it to the database.
	 * The database does the rest of the work, including Brute Force prevention.
	 * 
	 * @param userName The submitted user name to be used in authentication process
	 * @param password The submitted password in plain text to be used in
	 *                 authentication
	 * @return A string array made up of nothing or information to be consumed by
	 *         the initiating authentication process.
	 */

	public static String[] authUserSSO(String ApplicationRoot, String classId, String userName, String ssoName,
			String userRole) {

		log.debug("$$$ Getter.authUserSSO $$$");

		log.debug("ssoName = " + ssoName);
		log.debug("userName = " + userName);

		String[] result = new String[6];

		String userID = new String();
		String newUsername=null;

		String defaultClass = Register.getDefaultClass();

		boolean userFound = false;

		boolean isTempUsername = false;

		Connection conn;
		try {
			conn = Database.getCoreConnection(ApplicationRoot);
		} catch (SQLException e) {
			log.fatal("Could create get core connection: " + e.toString());
			throw new RuntimeException(e);
		}
		// See if user Exists
		CallableStatement callstmt;
		try {
			callstmt = conn.prepareCall(
					"SELECT userId, userName, userPass, badLoginCount, tempPassword, classId, suspendedUntil, loginType FROM `users` WHERE ssoName = ? AND loginType='saml'");
		} catch (SQLException e) {
			log.fatal("Could create call statement: " + e.toString());
			throw new RuntimeException(e);
		}

		log.debug("Gathering userFind ResultSet");
		ResultSet userResult;
		try {
			callstmt.setString(1, ssoName);
			log.debug("Executing query");
			userResult = callstmt.executeQuery();
		} catch (SQLException e) {
			log.fatal("Could not execute db query: " + e.toString());
			throw new RuntimeException(e);
		}

		log.debug("Opening Result Set from userResult");

		try {
			if (userResult.next()) {
				// User found if a row is in the database
				userFound = true;
				log.debug("User Found");
			} else {
				userFound = false;
			}

		} catch (SQLException e) {
			log.debug("User did not exist");
			userFound = false;
		}

		if (!userFound) {
			// User wasn't found, enroll them in database

			boolean userCreated = false;

			log.debug("User did not exist, create it from SSO data");

			try {
				
				if (defaultClass.isEmpty()) {
					log.debug("Adding player to database, with null classId");
					newUsername = Setter.userCreateSSO(ApplicationRoot, null, userName, ssoName, userRole);
				} else // defaultClass is not empty, so It must be set to a class!
				{
					log.debug("Adding player to database, to class " + defaultClass);
					newUsername = Setter.userCreateSSO(ApplicationRoot, defaultClass, userName, ssoName, userRole);
				}
				
				if(newUsername== null) {
					userCreated=false;
				} else {
					userCreated=true;
				}
				
				userName=newUsername;
					

			} catch (SQLException e) {
				String message = "Could not create user " + userName + " with ssoName " + ssoName + " via SSO: "
						+ e.toString();
				log.fatal(message);
				throw new RuntimeException(message);
			}

			if (!userCreated) {
				String message = "Could not create user " + userName + " with ssoName " + ssoName + " via SSO";
				log.fatal(message);
				throw new RuntimeException(message);
			}

			log.debug("User created");

		} else {

			Timestamp suspendedUntil;

			log.debug("Getting suspension data");

			try {
				suspendedUntil = userResult.getTimestamp(7);
			} catch (SQLException e) {
				log.fatal("Could not find suspension information from ssoName: " + ssoName + ": " + e.toString());
				throw new RuntimeException(e);
			}

			// Get current system time
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());

			if (suspendedUntil.after(currentTime)) {
				// User is suspended
				log.debug("User is suspended");

				result = null;
				return result;
			}
		}

		// Find the generated userID and username by asking the database
		try {
			callstmt = conn.prepareCall(
					"SELECT userId, userName, classID, tempUsername FROM `users` WHERE ssoName = ? AND loginType='saml'");

		} catch (SQLException e) {
			log.fatal("Could create call statement: " + e.toString());
			throw new RuntimeException(e);
		}

		log.debug("Gathering userResult ResultSet");

		try {
			callstmt.setString(1, ssoName);
			log.debug("Executing query");
			userResult = callstmt.executeQuery();
		} catch (SQLException e) {
			log.fatal("Could not execute db query: " + e.toString());
			throw new RuntimeException(e);
		}

		log.debug("Opening user list result set");

		try {
			if (userResult.next()) {
				userFound = true;
				log.debug("User Found"); // User found if a row is in the database, this line will not work if the
											// result
				// set is empty
			} else {
				userFound = false;
			}

		} catch (SQLException e) {
			log.debug("User did not exist");
			userFound = false;
		}

		if (!userFound) {
			// If user wasn't found at this stage something is quite wrong, so exit
			// forefully
			String message = "User wasn't found after being added!";
			log.fatal(message);
			throw new RuntimeException(message);
		}

		try {
			userID = userResult.getString(1);
			userName = userResult.getString(2);
			classId = userResult.getString(3); // classId
			isTempUsername = userResult.getBoolean(4);
		} catch (SQLException e) {
			String message = "Could find userID for userName " + userName + " with ssoName " + ssoName + " via SSO: "
					+ e.toString();
			log.fatal(message);
			throw new RuntimeException(message);
		}

		log.debug("User '" + userName + "' has logged in via SSO" + " with role " + userRole);

		result[0] = userID;
		result[1] = userName; // userName
		result[2] = userRole; // role
		result[5] = "false"; // sso logins can't change password
		result[4] = classId; // classId
		result[5] = Boolean.toString(isTempUsername);

		Database.closeConnection(conn);
		log.debug("$$$ End authUser $$$");
		return result;
	}

	/**
	 * Used to determine if a user has completed a module already
	 * 
	 * @param ApplicationRoot The current running context of an application
	 * @param moduleId        The module identifier
	 * @param userId          The user identifier
	 * @return The module name of the module IF the user has not completed AND the
	 *         user has previously opened the challenge.
	 */
	public static String checkPlayerResult(String ApplicationRoot, String moduleId, String userId) {
		log.debug("*** Getter.checkPlayerResult ***");

		String result = null;
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			log.debug("Preparing userCheckResult call");
			CallableStatement callstmnt = conn.prepareCall("call userCheckResult(?, ?)");
			callstmnt.setString(1, moduleId);
			callstmnt.setString(2, userId);
			log.debug("Executing userCheckResult");
			ResultSet resultSet = callstmnt.executeQuery();
			resultSet.next();
			result = resultSet.getString(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.debug("userCheckResult Failure: " + e.toString());
			result = null;
		}
		log.debug("*** END checkPlayerResult ***");
		return result;
	}

	/**
	 * Used to decipher whether or not a user exists as a player
	 * 
	 * @param userId The user identifier of the player to be found
	 * @return A boolean reflecting the state of existence of the player
	 */
	public static boolean findPlayerById(String ApplicationRoot, String userId) {
		log.debug("*** Getter.findPlayerById ***");
		boolean userFound = false;
		// Get connection
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call playerFindById(?)");
			log.debug("Gathering playerFindById ResultSet");
			callstmt.setString(1, userId);
			ResultSet userFind = callstmt.executeQuery();
			log.debug("Opening Result Set from playerFindById");
			userFind.next(); // This will throw an exception if player not found
			log.debug("Player Found: " + userFind.getString(1)); // This line will not execute if player not found
			userFound = true;
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Player did not exist: " + e.toString());
			userFound = false;
		}
		log.debug("*** END findPlayerById ***");
		return userFound;
	}

	/**
	 * Used to gather all module information for internal functionality. This method
	 * is used in creating View's or in control class operations
	 * 
	 * @param ApplicationRoot The current runing context of the application
	 * @return An ArrayList of String arrays that contain the module identifier,
	 *         module name, module type and module category of each module in the
	 *         core database.
	 */
	public static ArrayList<String[]> getAllModuleInfo(String ApplicationRoot) {
		log.debug("*** Getter.getAllModuleInfo ***");
		ArrayList<String[]> modules = new ArrayList<String[]>();

		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleGetAll()");
			log.debug("Gathering moduleGetAll ResultSet");
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetAll");
			int i = 0;
			while (resultSet.next()) {
				String[] result = new String[4];
				i++;
				result[0] = resultSet.getString(1); // moduleId
				result[1] = resultSet.getString(2); // moduleName
				result[2] = resultSet.getString(3); // moduleType
				result[3] = resultSet.getString(4); // mdouleCategory
				modules.add(result);
			}
			log.debug("Returning Array list with " + i + " entries.");
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
		}
		log.debug("*** END getAllModuleInfo ***");
		return modules;
	}

	/**
	 * Returns HTML menu for challenges. Challenges are only referenced by their id,
	 * The user will have to go through another servlet to get the module's View
	 * address
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @return HTML menu for challenges
	 * @throws SQLException
	 */
	public static String getChallenges(String ApplicationRoot, String userId, Locale lang) throws SQLException {
		log.debug("*** Getter.getChallenges ***");
		String output = new String();
		// Getting Translated Level Names
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", lang);
		// Encoder to prevent XSS
		Connection conn = Database.getCoreConnection(ApplicationRoot);

		CallableStatement callstmt = conn.prepareCall("call moduleAllInfo(?, ?)");
		callstmt.setString(1, "challenge");
		callstmt.setString(2, userId);
		log.debug("Gathering moduleAllInfo ResultSet");
		ResultSet challenges = callstmt.executeQuery();
		log.debug("Opening Result Set from moduleAllInfo");
		String challengeCategory = new String();
		int rowNumber = 0; // Identifies the first row, ie the start of the list. This is slightly
							// different output to every other row
		while (challenges.next()) {
			if (!challengeCategory.equalsIgnoreCase(challenges.getString(2))) {
				challengeCategory = challenges.getString(2);
				// log.debug("New Category Detected: " + challengeCategory);
				if (rowNumber > 0) // output prepared for Every row after row 1
					output += "</ul></li><li><a href='javascript:;' class='challengeHeader' >"
							+ Encode.forHtml(bundle.getString("category." + challengeCategory))
							+ "</a><ul class='challengeList' style='display: none;'>";
				else // output prepared for First row in entire challenge
					output += "<li><a href='javascript:;' class='challengeHeader'>"
							+ Encode.forHtml(bundle.getString("category." + challengeCategory))
							+ "</a><ul class='challengeList' style='display: none;'>";
				// log.debug("Compiling Challenge Category - " + challengeCategory);
			}
			output += "<li>"; // Starts next LI element
			if (challenges.getString(4) != null) {
				output += "<img src='css/images/completed.png'/>"; // Completed marker
			} else {
				output += "<img src='css/images/uncompleted.png'/>"; // Incomplete marker
			}
			// Final out put compilation
			output += "<a class='lesson' id='" + Encode.forHtmlAttribute(challenges.getString(3))
					+ "' href='javascript:;'>" + Encode.forHtml(bundle.getString(challenges.getString(1))) + "</a>";
			output += "</li>";
			rowNumber++;
		}
		// Check if output is empty
		if (output.isEmpty()) {
			output = "<li><a href='javascript:;'>No challenges found</a></li>";
		} else {
			log.debug("Appending End tags");
			output += "</ul></li>";
		}

		Database.closeConnection(conn);
		log.debug("*** END getChallenges() ***");
		return output;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @return The amount of classes currently existing in the database
	 */
	public static int getClassCount(String ApplicationRoot) {
		int result = 0;
		ResultSet resultSet = null;
		log.debug("*** Getter.getClassCount ***");
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call classCount()");
			log.debug("Gathering classCount ResultSet");
			resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from classCount");
			resultSet.next();
			result = resultSet.getInt(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = 0;
		}
		log.debug("*** END getClassCount");
		return result;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @return Result set containing class info in the order classId, className and
	 *         then classYear
	 */
	public static ResultSet getClassInfo(String ApplicationRoot) {
		ResultSet result = null;
		log.debug("*** Getter.getClassInfo (All Classes) ***");
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call classesGetData()");
			log.debug("Gathering classesGetData ResultSet");
			result = callstmt.executeQuery();
			log.debug("Returning Result Set from classesGetData");
		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getClassInfo");
		return result;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param classId         The identifier of the class
	 * @return String Array with Class information with the format of {name, year}
	 */
	public static String[] getClassInfo(String ApplicationRoot, String classId) {
		String[] result = new String[2];
		log.debug("*** Getter.getClassInfo (Single Class) ***");
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call classFind(?)");
			callstmt.setString(1, classId);
			log.debug("Gathering classFind ResultSet");
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from classFind");
			resultSet.next();
			result[0] = resultSet.getString(1);// Name
			result[1] = resultSet.getString(2);// Year
			
		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getClassInfo");
		return result;
	}

	/**
	 * The CSRF forum is used in CSRF levels for users to deliver CSRF attacks
	 * against each other. URLs are contained in IFRAME tags
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param classId         Identifier of the class to populate the forum with
	 * @param moduleId        The module in which to return the forum for
	 * @param bundle          Strings Package for the Language Local of the user
	 *                        making the request
	 * @return A HTML table of a Class's CSRF Submissions for a specific module
	 */
	public static String getCsrfForumWithIframe(String ApplicationRoot, String classId, String moduleId,
			ResourceBundle bundle) {
		log.debug("*** Getter.getCsrfForum ***");
		log.debug("Getting stored messages from class: " + classId);
		String htmlOutput = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			if (classId != null) {
				CallableStatement callstmt = conn.prepareCall("call resultMessageByClass(?, ?)");
				log.debug("Gathering resultMessageByClass ResultSet");
				callstmt.setString(1, classId);
				callstmt.setString(2, moduleId);
				ResultSet resultSet = callstmt.executeQuery();
				log.debug("resultMessageByClass executed");

				// Table Header
				htmlOutput = "<table><tr><th>" + bundle.getString("forum.userName") + "</th><th>"
						+ bundle.getString("forum.message") + "</th></tr>";

				log.debug("Opening Result Set from resultMessageByClass");
				int counter = 0;
				while (resultSet.next()) {
					counter++;
					// Table content
					htmlOutput += "<tr><td>" + Encode.forHtml(resultSet.getString(1))
							+ "</td><td><iframe sandbox=\"allow-scripts allow-forms\" src=\""
							+ Encode.forHtmlAttribute(resultSet.getString(2)) + "\"></iframe></td></tr>";
				}
				if (counter > 0)
					log.debug("Added a " + counter + " row table");
				else
					log.debug("No results from query");
				// Table end
				htmlOutput += "</table>";
			} else {
				log.error("User with Null Class detected");
				htmlOutput = "<p><font color='red'>" + bundle.getString("error.noClass") + "</font></p>";
			}
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			htmlOutput = "<p>" + bundle.getString("error.occurred ") + "</p>";
		} catch (Exception e) {
			log.fatal("Could not return CSRF Forum: " + e.toString());
		}
		log.debug("*** END getCsrfForum ***");
		return htmlOutput;
	}

	/**
	 * The CSRF forum is used in CSRF levels for users to deliver CSRF attacks
	 * against each other. URLs are contained in IMG tags
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param classId         Identifier of the class to populate the forum with
	 * @param moduleId        The module in which to return the forum for
	 * @param bundle          The strings package for the language of the user
	 * @return A HTML table of a Class's CSRF Submissions for a specific module
	 */
	public static String getCsrfForumWithImg(String ApplicationRoot, String classId, String moduleId,
			ResourceBundle bundle) {
		log.debug("*** Getter.getCsrfForum ***");
		log.debug("Getting stored messages from class: " + classId);
		String htmlOutput = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			if (classId != null) {
				CallableStatement callstmt = conn.prepareCall("call resultMessageByClass(?, ?)");
				log.debug("Gathering resultMessageByClass ResultSet");
				callstmt.setString(1, classId);
				callstmt.setString(2, moduleId);
				ResultSet resultSet = callstmt.executeQuery();
				log.debug("resultMessageByClass executed");

				// Table Header
				htmlOutput = "<table><tr><th>" + bundle.getString("forum.userName") + "</th><th>"
						+ bundle.getString("forum.image") + "</th></tr>";

				log.debug("Opening Result Set from resultMessageByClass");
				int counter = 0;
				while (resultSet.next()) {
					counter++;
					// Table content
					htmlOutput += "<tr><td>" + Encode.forHtml(resultSet.getString(1)) + "</td><td><img src=\""
							+ Encode.forHtmlAttribute(resultSet.getString(2)) + "\"/></td></tr>";
				}
				if (counter > 0)
					log.debug("Added a " + counter + " row table");
				else
					log.debug("No results from query");
				// Table end
				htmlOutput += "</table>";
			} else {
				log.error("User with Null Class detected");
				htmlOutput = "<p><font color='red'>" + bundle.getString("error.noClass") + "</font></p>";
			}
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			htmlOutput = "<p>" + bundle.getString("error.occurred") + "</p>";
		} catch (Exception e) {
			log.fatal("Could not return CSRF Forum: " + e.toString());
		}
		log.debug("*** END getCsrfForum ***");
		return htmlOutput;
	}

	/**
	 * Used to present a modules feedback, including averages and raw results.
	 * 
	 * @param applicationRoot The current running context of the application.
	 * @param moduleId        The module identifier
	 * @return A HTML table of the feedback for a specific module
	 */
	public static String getFeedback(String applicationRoot, String moduleId) {
		log.debug("*** Getter.getFeedback ***");

		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(applicationRoot);

			log.debug("Preparing moduleFeedback call");
			CallableStatement callstmnt = conn.prepareCall("call moduleFeedback(?)");
			callstmnt.setString(1, moduleId);
			log.debug("Executing moduleFeedback");
			ResultSet resultSet = callstmnt.executeQuery();
			int resultAmount = 0;
			int before = 0;
			int after = 0;
			int difficulty = 0;
			boolean color = true;
			while (resultSet.next()) {
				if (resultSet.getString(1) != null) {
					resultAmount++;
					difficulty += resultSet.getInt(3);
					before += resultSet.getInt(4);
					after += resultSet.getInt(5);
					result += "<tr ";
					if (color) // Alternate row color
					{
						color = !color;
						result += "BGCOLOR='A878EF'";
					} else {
						color = !color;
						result += "BGCOLOR='D4BCF7'";
					}
					// A row off information
					result += "><td>" + Encode.forHtml(resultSet.getString(1)) + "</td><td>"
							+ Encode.forHtml(resultSet.getString(2)) + "</td><td>" + resultSet.getInt(3) + "</td><td>"
							+ resultSet.getInt(4) + "</td><td>" + resultSet.getInt(5) + "</td><td>"
							+ Encode.forHtml(resultSet.getString(6)) + "</td></tr>";
				}
			}
			if (resultAmount > 0)// Table header
				result = "<table><tr><th>Player</th><th>Time</th><th>Difficulty</th><th>Before</th><th>After</th><th>Comments</th></tr>"
						+ "<tr><td>Average</td><td></td><td>" + difficulty / resultAmount + "</td><td>"
						+ before / resultAmount + "</td><td>" + after / resultAmount + "</td><td></td></tr>" + result
						+ "<table>";
			else // If empty, Blank output
				result = new String();

			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("moduleFeedback Failure: " + e.toString());
			result = null;
		}
		log.debug("*** END getFeedback ***");
		return result;
	}

	/**
	 * This method prepares the incremental module menu. This is when Security
	 * Shepherd is in "Game Mode". Users are presented with one uncompleted module
	 * at a time. This method also returns a script to be executed every time the
	 * menu is chanegd. This is script defines the animation and operations to be
	 * carried out when the menu is interacted with
	 * 
	 * @param ApplicationRoot The running context of the application.
	 * @param userId          The user identifier of the user.
	 * @param csrfToken       The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for
	 *         interaction with this menu
	 */
	public static String getIncrementalModules(String ApplicationRoot, String userId, String lang, String csrfToken) {
		log.debug("*** Getter.getIncrementalChallenges ***");
		String output = new String();

		Locale.setDefault(new Locale("en"));
		Locale locale = new Locale(lang);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", locale);
		ResourceBundle levelNames = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", locale);

		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleIncrementalInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering moduleIncrementalInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleIncrementalInfo");
			boolean lastRow = false;
			boolean completedModules = false;

			// Preparing first Category header; "Completed"
			output = "<li><a id='completedList' href='javascript:;'><div class='menuButton'>"
					+ bundle.getString("getter.button.completed") + "</div></a>\n"
					+ "<ul id='theCompletedList' style='display: none;' class='levelList'>";

			while (modules.next() && !lastRow) {
				// For each row, prepair the modules the users can select
				if (modules.getString(4) != null) // If not Last Row
				{
					completedModules = true;
					output += "<li>";
					output += "<a class='lesson' id='" + Encode.forHtmlAttribute(modules.getString(3))
							+ "' href='javascript:;'>" + Encode.forHtml(levelNames.getString(modules.getString(1)))
							+ "</a>";
					output += "</li>";
				} else {
					lastRow = true;
					// Last Row - Highlighed Next Challenge
					if (completedModules) {
						output += "</ul></li><li>";
					} else {
						// NO completed modules, so dont show any...
						output = new String();
					}

					// Second category - Uncompleted
					output += "<a class='lesson' id='" + Encode.forHtmlAttribute(modules.getString(3))
							+ "' href='javascript:;'>" + "<div class='menuButton'>"
							+ bundle.getString("getter.button.nextChallenge") + "</div>" + "</a>";
					output += "</li>";
				}
			}

			if (!lastRow) // If true, then the user has completed all challenges
			{
				output += "<h2 id='uncompletedList'><a href='javascript:;'>"
						+ bundle.getString("getter.button.finished") + "</a></h2>\n" + "</li>";
			}
			if (output.isEmpty()) // If this method has gone so far without any output, create a error message
			{
				output = "<li><a href='javascript:;'>" + bundle.getString("getter.button.noModulesFound") + "</a></li>";
			} else // final tags to ensure valid HTML
			{
				log.debug("Appending End tags");
				// output += "</ul></li>"; //Commented Out to prevent Search Box being pushed
				// into Footer
			}

			// This is the script for menu interaction
			output += "<script>applyMenuButtonActionsCtfMode('" + Encode.forHtml(csrfToken) + "', \""
					+ Encode.forHtml(bundle.getString("generic.text.sorryError")) + "\");</script>";

			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Challenge Retrieval: " + e.toString());
		}
		log.debug("*** END getIncrementalChallenges() ***");
		return output;
	}

	/**
	 * This method prepares the incremental module menu. This is when Security
	 * Shepherd is in "Game Mode". Users are presented with one uncompleted module
	 * at a time. This method does not return the JS script describing how the menu
	 * used should work
	 * 
	 * @param ApplicationRoot The running context of the application.
	 * @param userId          The user identifier of the user.
	 * @param csrfToken       The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for
	 *         interaction with this menu
	 */
	public static String getIncrementalModulesWithoutScript(String ApplicationRoot, String userId, String lang,
			String csrfToken) {
		log.debug("*** Getter.getIncrementalChallengesWithoutScript ***");
		String output = new String();

		Locale.setDefault(new Locale("en"));
		Locale locale = new Locale(lang);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", locale);
		ResourceBundle levelNames = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", locale);

		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleIncrementalInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering moduleIncrementalInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleIncrementalInfo");
			boolean lastRow = false;
			boolean completedModules = false;

			// Preparing first Category header; "Completed"
			output = "<li><a id='completedList' href='javascript:;'><div class='menuButton'>"
					+ bundle.getString("getter.button.completed") + "</div></a>\n"
					+ "<ul id='theCompletedList' style='display: none;' class='levelList'>";

			while (modules.next() && !lastRow) {
				// For each row, prepair the modules the users can select
				if (modules.getString(4) != null) // If not Last Row
				{
					completedModules = true;
					output += "<li>";
					output += "<a class='lesson' id='" + Encode.forHtmlAttribute(modules.getString(3))
							+ "' href='javascript:;'>" + Encode.forHtml(levelNames.getString(modules.getString(1)))
							+ "</a>";
					output += "</li>";
				} else {
					lastRow = true;
					// Last Row - Highlighed Next Challenge
					if (completedModules) {
						output += "</ul></li><li>";
					} else {
						// NO completed modules, so dont show any...
						output = new String();
					}

					// Second category - Uncompleted
					output += "<a class='lesson' id='" + Encode.forHtmlAttribute(modules.getString(3))
							+ "' href='javascript:;'>" + "<div class='menuButton'>"
							+ bundle.getString("getter.button.nextChallenge") + "</div>" + "</a>";
					output += "</li>";
				}
			}

			if (!lastRow) // If true, then the user has completed all challenges
			{
				output += "<h2 id='uncompletedList'><a href='javascript:;'>"
						+ bundle.getString("getter.button.finished") + "</a></h2>\n" + "</li>";
			}
			if (output.isEmpty()) // If this method has gone so far without any output, create a error message
			{
				output = "<li><a href='javascript:;'>" + bundle.getString("getter.button.noModulesFound") + "</a></li>";
			} else // final tags to ensure valid HTML
			{
				log.debug("Appending End tags");
				// output += "</ul></li>"; //Commented Out to prevent Search Box being pushed
				// into Footer
			}

			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Challenge Retrieval: " + e.toString());
		}
		log.debug("*** END getIncrementalChallengesWithoutScript() ***");
		return output;
	}

	/**
	 * Use to return the current progress of a class in JSON format with information
	 * like userid, user name and score
	 * 
	 * @param applicationRoot The current running context of the application
	 * @param classId         The identifier of the class to use in lookup
	 * @return A JSON representation of a class's score in the order {id, username,
	 *         userTitle, score, scale, place, order, goldmedalcount, goldDisplay,
	 *         silverMedalCount, silverDisplay, bronzeDisplay, bronzeMedalCount}
	 */
	@SuppressWarnings("unchecked")
	public static String getJsonScore(String applicationRoot, String classId) {
		log.debug("classId: " + classId);
		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(applicationRoot);

			// Returns User's: Name, # of Completed modules and Score
			CallableStatement callstmnt = null;
			if (ScoreboardStatus.getScoreboardClass().isEmpty() && !ScoreboardStatus.isClassSpecificScoreboard())
				callstmnt = conn.prepareCall("call totalScoreboard()"); // Open Scoreboard not based on a class
			else {
				callstmnt = conn.prepareCall("call classScoreboard(?)"); // Class Scoreboard based on classId
				callstmnt.setString(1, classId);
			}
			// log.debug("Executing classScoreboard");
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
			while (resultSet.next()) // For each user in a class
			{
				resultAmount++;
				jsonInner = new JSONObject();
				if (resultSet.getString(1) != null) {
					int place = resultAmount;
					int score = resultSet.getInt(3);
					int goldMedals = resultSet.getInt(4);
					int silverMedals = resultSet.getInt(5);
					int bronzeMedals = resultSet.getInt(6);
					if (resultAmount == 1) // First Place is Returned First, so this will be the biggest bar on the
											// scoreboard
					{
						int highscore = score;
						// log.debug("Current Highscore Listing is " + highscore);
						// Use the high score to scale the width of the bars for the whole scoreboard
						float maxBarScale = 1.02f; // High Score bar will have a scale of 1 //This will get used when a
													// scale is added to the scoreboard
						baseBarScale = highscore * maxBarScale;
						// setting up variables for Tie Scenario Placings
						prevPlace = 1;
						prevScore = score;
					} else {
						// Does this score line match the one before (Score and Medals)? if so the place
						// shouldnt change
						if (score == prevScore && goldMedals == prevGold && silverMedals == prevSilver
								&& bronzeMedals == prevBronze) {
							place = prevPlace;
							tieBreaker = tieBreaker + 0.01f;
						} else {
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

					int barScale = (int) ((score * 100) / baseBarScale); // bar scale is the percentage the bar should
																			// be of the row's context (Highest Possible
																			// is depends on scale set in maxBarScale.
																			// eg: maxBarScale = 1.1 would mean the max
																			// scale would be 91% for a single row)

					String userMedalString = new String();
					if (goldMedals > 0 || silverMedals > 0 || bronzeMedals > 0) {
						userMedalString += " holding ";
						if (goldMedals > 0)
							userMedalString += goldMedals + " gold";
						if (silverMedals > 0) {
							if (goldMedals > 0) // Medals Before, puncuate
							{
								if (bronzeMedals > 0) // more medals after silver? Comma
								{
									userMedalString += ", ";
								} else // Say And
								{
									userMedalString += " and ";
								}
							}
							userMedalString += silverMedals + " silver";
						}
						if (bronzeMedals > 0) {
							if (goldMedals > 0 || silverMedals > 0) // Medals Before?
							{
								userMedalString += " and ";
							}
							userMedalString += bronzeMedals + " bronze";
						}
						// Say Medal(s) at the end of the string
						userMedalString += " medal";
						if (goldMedals + silverMedals + bronzeMedals > 1)
							userMedalString += "s";
					}

					jsonInner.put("id", new String(Encode.forHtml(resultSet.getString(1)))); // User Id
					jsonInner.put("username", new String(Encode.forHtml(resultSet.getString(2)))); // User Name
					jsonInner.put("userTitle", new String(
							Encode.forHtml(resultSet.getString(2)) + " with " + score + " points" + userMedalString)); // User
																														// name
																														// encoded
																														// for
																														// title
																														// attribute
					jsonInner.put("score", new Integer(score)); // Score
					jsonInner.put("scale", barScale); // Scale of score bar
					jsonInner.put("place", place); // Place on board
					jsonInner.put("order", (place + tieBreaker)); // Order on board
					jsonInner.put("goldMedalCount", new Integer(goldMedals));
					jsonInner.put("goldDisplay", goldDisplayStyle);
					jsonInner.put("silverMedalCount", new Integer(silverMedals));
					jsonInner.put("silverDisplay", silverDisplayStyle);
					jsonInner.put("bronzeMedalCount", new Integer(bronzeMedals));
					jsonInner.put("bronzeDisplay", bronzeDisplayStyle);
					// log.debug("Adding: " + jsonInner.toString());
					json.add(jsonInner);
				}
			}
			if (resultAmount > 0)
				result = json.toString();
			else
				result = new String();

			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("getJsonScore Failure: " + e.toString());
			result = null;
		} catch (Exception e) {
			log.error("getJsonScore Unexpected Failure: " + e.toString());
			result = null;
		}
		// log.debug("*** END getJsonScore ***");
		return result;
	}

	/**
	 * Used to gather a menu of lessons for a user, including markers for each
	 * lesson they have completed or not completed
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param userId          Identifier of the user
	 * @return HTML lesson menu for Open Floor Plan.
	 */
	public static String getLessons(String ApplicationRoot, String userId, Locale lang) {
		log.debug("*** Getter.getLesson ***");
		// Getting Translated Level Names
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", lang);
		String output = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			// Get the lesson modules
			CallableStatement callstmt = conn.prepareCall("call lessonInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering lessonInfo ResultSet for user " + userId);
			ResultSet lessons = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while (lessons.next()) {
				// log.debug("Adding " + lessons.getString(1));
				output += "<li>";
				// Markers for completion
				if (lessons.getString(4) != null) {
					output += "<img src='css/images/completed.png'/>";
				} else {
					output += "<img src='css/images/uncompleted.png'/>";
				}
				// Prepare lesson output
				output += "<a class='lesson' id='" + Encode.forHtmlAttribute(lessons.getString(3))
						+ "' href='javascript:;'>" + Encode.forHtml(bundle.getString(lessons.getString(1))) + "</a>";
				output += "</li>";
			}
			// If no output has been found, return an error message
			if (output.isEmpty()) {
				output = "<li><a href='javascript:;'>No lessons found</a></li>";
			} else {
				log.debug("Lesson List returned");
			}
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("lesson Retrieval: " + e.toString());
		}
		log.debug("*** END getLesson() ***");
		return output;
	}

	/**
	 * This method returns the address of a module based on the module identifier
	 * submitted. If user has not accessed this level before, they are put down as
	 * starting the level at this time. If the level is a client side attack, or
	 * other issues that cannot be abused to return a result key (like XSS, CSRF or
	 * network sniffing) the address is of the core server. Otherwise the modules
	 * sit on the vulnerable application server
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId        Identifier of the module the to return
	 * @param userId          The identifier of the user that wants to get the
	 *                        module
	 * @return The module address
	 */
	public static String getModuleAddress(String ApplicationRoot, String moduleId, String userId) {
		log.debug("*** Getter.getModuleAddress ***");
		String output = new String();
		String type = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleGetHash(?, ?)");
			callstmt.setString(1, moduleId);
			callstmt.setString(2, userId);
			log.debug("Gathering moduleGetHash ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetHash");
			modules.next(); // Exception thrown if no hash was found
			// Set Type. Used to ensure the URL points at the correct directory
			if (modules.getString(3).equalsIgnoreCase("challenge")) {
				type = "challenges";
			} else {
				type = "lessons";
			}
			output = type + "/" + modules.getString(1) + ".jsp";
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Module Hash Retrieval: " + e.toString());
			log.error("moduleID = " + moduleId);
			log.error("userID = " + userId);
		}
		log.debug("*** END getModuleAddress() ***");
		return output;
	}

	/**
	 * Retrieves the module category based on the moduleId submitted
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId        The id of the module that
	 * @return
	 */
	public static String getModuleCategory(String ApplicationRoot, String moduleId) {
		log.debug("*** Getter.getModuleResult ***");
		String theCategory = null;
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			PreparedStatement prepstmt = conn.prepareStatement("SELECT moduleCategory FROM modules WHERE moduleId = ?");
			prepstmt.setString(1, moduleId);
			ResultSet moduleFind = prepstmt.executeQuery();
			moduleFind.next();
			theCategory = moduleFind.getString(1);
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Module did not exist: " + e.toString());
			theCategory = null;
		}
		log.debug("*** END getModuleCategory ***");
		return theCategory;
	}

	/**
	 * @param applicationRoot The current running context of the application.
	 * @param moduleId        The identifier of a module
	 * @return The hash of the module specified
	 */
	public static String getModuleHash(String applicationRoot, String moduleId) {
		log.debug("*** Getter.getModuleHash ***");
		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(applicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleGetHashById(?)");
			log.debug("Gathering moduleGetHash ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetHash");
			resultSet.next();
			result = resultSet.getString(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute moduleGetHash: " + e.toString());
			result = null;
		}
		log.debug("*** END getModuleHash ***");
		return result;
	}

	/**
	 * Convert module hash to ID
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleHash      The module hash to use for look up
	 * @return The identifier of the module with the module hash of the moduleHash
	 *         parameter
	 */
	public static String getModuleIdFromHash(String ApplicationRoot, String moduleHash) {
		log.debug("*** Getter.getModuleIdFromHash ***");
		log.debug("Getting ID from Hash: " + moduleHash);
		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleGetIdFromHash(?)");
			log.debug("Gathering moduleGetIdFromHash ResultSet");
			callstmt.setString(1, moduleHash);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetIdFromHash");
			resultSet.next();
			result = resultSet.getString(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getModuleIdFromHash ***");
		return result;
	}

	/**
	 * Returns true if a module has a hard coded key, false if server encrypts it
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId        The id of the module
	 * @return Returns true if a module has a hard coded key, false if server
	 *         encrypts it
	 */
	public static boolean getModuleKeyType(String ApplicationRoot, String moduleId) {
		log.debug("*** Getter.getModuleKeyType ***");
		boolean theKeyType = true;
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			PreparedStatement prepstmt = conn.prepareStatement("SELECT hardcodedKey FROM modules WHERE moduleId = ?");
			prepstmt.setString(1, moduleId);
			ResultSet moduleFind = prepstmt.executeQuery();
			moduleFind.next();
			theKeyType = moduleFind.getBoolean(1);
			if (theKeyType)
				log.debug("Module has hard coded Key");
			else
				log.debug("Module has user specific Key");
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Module did not exist: " + e.toString());
			theKeyType = true;
		}
		log.debug("*** END getModuleKeyType ***");
		return theKeyType;
	}

	/**
	 * This method retrieves the i18n local key for a module's name.
	 * 
	 * @param applicationRoot Application Running Context
	 * @param moduleId        ID of the module to lookup
	 * @return Locale key for the Module's Name.
	 */
	public static String getModuleNameLocaleKey(String applicationRoot, String moduleId) {
		log.debug("*** Getter.getModuleNameLocaleKey ***");
		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(applicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleGetNameLocale(?)");
			log.debug("Gathering moduleGetNameLocale ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetNameLocale");
			resultSet.next();
			result = resultSet.getString(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute moduleGetNameLocale: " + e.toString());
			result = null;
		}
		log.debug("*** END getModuleNameLocaleKey ***");
		return result;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId        Identifier of module
	 * @return The db stored solution key value for the moduleId submitted
	 */
	public static String getModuleResult(String ApplicationRoot, String moduleId) {
		log.debug("*** Getter.getModuleResult ***");
		String moduleFound = null;
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call moduleGetResult(?)");
			log.debug("Gathering moduleGetResult ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet moduleFind = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetResult");
			moduleFind.next();
			log.debug("Module " + moduleFind.getString(1) + " Found");
			moduleFound = moduleFind.getString(2);
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Module did not exist: " + e.toString());
			moduleFound = null;
		}
		log.debug("*** END getModuleResult ***");
		return moduleFound;
	}

	/**
	 * Returns the result key for a module using the module's hash for the lookup
	 * procedure.
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleHash      The hash to use for module look up
	 * @return The db stored solution key value for the moduleHash submited
	 */
	public static String getModuleResultFromHash(String ApplicationRoot, String moduleHash) {
		log.debug("*** Getter.getModuleResultFromHash ***");
		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			log.debug("hash '" + moduleHash + "'");
			CallableStatement callstmt = conn.prepareCall("call moduleGetResultFromHash(?)");
			log.debug("Gathering moduleGetResultFromHash ResultSet");
			callstmt.setString(1, moduleHash);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleGetResultFromHash");
			resultSet.next();
			result = resultSet.getString(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getModuleResultFromHash ***");
		return result;
	}

	/**
	 * Used in creating functionality that requires a user to select a module. This
	 * method only prepares the option tags for this type of input. It must still be
	 * wrapped in select tags.
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @return All modules in HTML option tags
	 */
	public static String getModulesInOptionTags(String ApplicationRoot) {
		log.debug("*** Getter.getModulesInOptionTags ***");
		String output = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			PreparedStatement callstmt = conn
					.prepareStatement("SELECT moduleId, moduleName FROM modules ORDER BY moduleCategory, moduleName;");
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while (modules.next()) {
				// Each module name is embed in option tags, with a value of their module
				// identifier
				output += "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + "'>"
						+ Encode.forHtml(modules.getString(2)) + "</option>\n";
			}
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Challenge Retrieval: " + e.toString());
		}
		log.debug("*** END getModulesInOptionTags() ***");
		return output;
	}

	/**
	 * Used in creating functionality that requires a user to select a module. This
	 * method only prepares the option tags for this type of input. It must still be
	 * wrapped in select tags.
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @return All modules in HTML option tags ordered by incrementalRank
	 */
	public static String getModulesInOptionTagsCTF(String ApplicationRoot) {
		log.debug("*** Getter.getModulesInOptionTags ***");
		String output = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			PreparedStatement callstmt = conn
					.prepareStatement("SELECT moduleId, moduleName FROM modules ORDER BY incrementalRank;");
			log.debug("Gathering moduleAllInfo ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllInfo");
			while (modules.next()) {
				// Each module name is embed in option tags, with a value of their module
				// identifier
				output += "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + "'>"
						+ Encode.forHtml(modules.getString(2)) + "</option>\n";
			}
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Challenge Retrieval: " + e.toString());
		}
		log.debug("*** END getModulesInOptionTags() ***");
		return output;
	}

	/**
	 * Used to return a module cheat sheet
	 * 
	 * @param ApplicationRoot The current running context of the application
	 * @param moduleId        The identifier of the module to return the cheat sheet
	 *                        for
	 * @param lang            The Locale the user has enabled
	 * @return String[] containing {ModuleName, CheatSheetSolution}
	 */
	public static String[] getModuleSolution(String ApplicationRoot, String moduleId, Locale lang) {
		log.debug("*** Getter.getModuleSolution ***");
		String[] result = new String[2];
		// Getting Translations
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.cheatsheets.solutions", lang);
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call cheatSheetGetSolution(?)");
			log.debug("Gathering cheatSheetGetSolution ResultSet");
			callstmt.setString(1, moduleId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from cheatSheetGetSolution");
			resultSet.next();
			result[0] = resultSet.getString(1);
			result[1] = bundle.getString(resultSet.getString(2));
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getModuleSolution ***");
		return result;
	}

	/**
	 * This method returns modules in option tags in different &lt;select&gt;
	 * elements depending on their current open/closed status. The output assumes it
	 * is contained in a table context
	 * 
	 * @param ApplicationRoot The Running Context of the Application
	 * @return Tr/td elements containing a moduleStatusMenu that has lists of the
	 *         current open and closed modules
	 */
	public static String getModuleStatusMenu(String ApplicationRoot) {
		log.debug("*** Getter.getModuleStatusMenu ***");
		String openModules = new String();
		String closedModules = new String();
		String output = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			// Get the modules
			CallableStatement callstmt = conn.prepareCall("call moduleAllStatus()");
			log.debug("Gathering moduleAllStatus ResultSet");
			ResultSet modules = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleAllStatus");
			while (modules.next()) {
				String theModule = "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + "'>"
						+ Encode.forHtml(modules.getString(2)) + "</option>\n";
				if (modules.getString(3).equalsIgnoreCase("open")) {
					// Module is Open currently, so add it to the open side of the list
					openModules += theModule;
				} else {
					// If it is not open: It must be closed (NULL or not)
					closedModules += theModule;
				}
			}
			// This is the actual output: It assumes a <table> environment
			output = "<tr><th>To Open</th><th>To Close</th></tr><tr>\n"
					+ "<td><select style='width: 300px; height: 200px;' multiple id='toOpen'>" + closedModules
					+ "</select></td>\n" + "<td><select style='width: 300px; height: 200px;' multiple id='toClose'>"
					+ openModules + "</select></td>\n" + "</tr>\n";
			log.debug("Module Status Menu returned");
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Module Status Menu: " + e.toString());
		}
		return output;
	}

	/**
	 * This method returns the module categories in option tags that are to be open
	 * or closed in a &lt;select&gt; element for administration manipulation
	 * 
	 * @param ApplicationRoot
	 * @return Module Category List for Html (&lt;SELECT&gt; element)
	 */
	public static String getOpenCloseCategoryMenu(String ApplicationRoot) {
		log.debug("*** Getter.getOpenCloseCategoryMenu ***");
		String theModules = new String();
		String output = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			// Get the modules
			CallableStatement callstmt = conn
					.prepareCall("SELECT DISTINCT moduleCategory FROM modules ORDER BY moduleCategory");
			ResultSet modules = callstmt.executeQuery();
			while (modules.next()) {
				String theModule = "<option value='" + Encode.forHtmlAttribute(modules.getString(1)) + "'>"
						+ Encode.forHtml(modules.getString(1)) + "</option>\n";
				theModules += theModule;
			}
			// This is the actual output: It assumes a <table> environment
			output = "<select style='width: 300px; height: 200px;' multiple id='toDo'>" + theModules + "</select>\n";
			log.debug("Module Category Menu returned");
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Module Status Menu: " + e.toString());
		}
		return output;
	}

	/**
	 * This method is used to gather users according by class. Thanks to MySQL
	 * syntax, where class = null will return nothing, is null must be used. <br/>
	 * is 'validClass' will Error, = 'validclass' must be used.<br/>
	 * So there are two procedures this method calls. One that handles null classes,
	 * one that does not
	 * 
	 * @param ClassId         Identifier of class
	 * @param ApplicationRoot The current running context of the application
	 * @return ResultSet that contains users for the selected class in the formate
	 *         {userId, userName, userAddress}
	 */
	public static ResultSet getPlayersByClass(String ApplicationRoot, String classId) {
		ResultSet result = null;
		log.debug("*** Getter.getPlayersByClass (Single Class) ***");
		log.debug("classId: '" + classId + "'");
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = null;
			if (classId != null) {
				log.debug("Gathering playersByClass ResultSet");
				callstmt = conn.prepareCall("call playersByClass(?)");
				callstmt.setString(1, classId);
				log.debug("Returning Result Set from playersByClass");
			} else {
				log.debug("Gathering playersWithoutClass ResultSet");
				callstmt = conn.prepareCall("call playersWithoutClass()");
				log.debug("Returning Result Set from playersByClass");
			}
			ResultSet resultSet = callstmt.executeQuery();
			result = resultSet;
			resultSet.next();

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getPlayersByClass");
		return result;
	}

	/**
	 * Used to present the progress of a class in a series of loading bars
	 * 
	 * @param applicationRoot The current running context of the application
	 * @param classId         The identifier of the class to use in lookup
	 * @return A HTML representation of a class's progress in the application
	 */
	public static String getProgress(String applicationRoot, String classId) {
		log.debug("*** Getter.getProgress ***");

		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(applicationRoot);

			log.debug("Preparing userProgress call");
			CallableStatement callstmnt = conn.prepareCall("call userProgress(?)");
			callstmnt.setString(1, classId);
			log.debug("Executing userProgress");
			ResultSet resultSet = callstmnt.executeQuery();
			int resultAmount = 0;
			while (resultSet.next()) // For each user in a class
			{
				resultAmount++;
				if (resultSet.getString(1) != null) {
					result += "<tr><td>" + Encode.forHtml(resultSet.getString(1)) + // Output their progress
							"</td><td><div style='background-color: #A878EF; heigth: 25px; width: "
							+ widthOfUnitBar * resultSet.getInt(2) + "px;'>" + "<font color='white'><strong>"
							+ resultSet.getInt(2);
					if (resultSet.getInt(2) > 6)
						result += " Modules";
					result += "</strong></font></div></td></tr>";
				}
			}
			if (resultAmount > 0)
				result = "<table><tr><th>Player</th><th>Progress</th></tr>" + result + "</table>";
			else
				result = new String();

			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("getProgress Failure: " + e.toString());
			result = null;
		}
		log.debug("*** END getProgress ***");
		return result;
	}

	/**
	 * Use to return the current progress of a class in JSON format with information
	 * like user name, score and completed modules
	 * 
	 * @param applicationRoot The current running context of the application
	 * @param classId         The identifier of the class to use in lookup
	 * @return A JSON representation of a class's progress in the application
	 */
	@SuppressWarnings("unchecked")
	public static String getProgressJSON(String applicationRoot, String classId) {
		log.debug("*** Getter.getProgressJSON ***");

		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(applicationRoot);

			log.debug("Preparing userProgress call");
			// Returns User's: Name, # of Completed modules and Score
			CallableStatement callstmnt = conn.prepareCall("call userProgress(?)");
			callstmnt.setString(1, classId);
			log.debug("Executing userProgress");
			ResultSet resultSet = callstmnt.executeQuery();
			JSONArray json = new JSONArray();
			JSONObject jsonInner = new JSONObject();
			int resultAmount = 0;
			while (resultSet.next()) // For each user in a class
			{
				resultAmount++;
				jsonInner = new JSONObject();
				if (resultSet.getString(1) != null) {
					jsonInner.put("userName", new String(Encode.forHtml(resultSet.getString(1)))); // User Name
					jsonInner.put("progressBar", new Integer(resultSet.getInt(2) * widthOfUnitBar)); // Progress Bar
																										// Width
					jsonInner.put("score", new Integer(resultSet.getInt(3))); // Score
					log.debug("Adding: " + jsonInner.toString());
					json.add(jsonInner);
				}
			}
			if (resultAmount > 0)
				result = json.toString();
			else
				result = new String();
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("getProgressJSON Failure: " + e.toString());
			result = null;
		} catch (Exception e) {
			log.error("getProgressJSON Unexpected Failure: " + e.toString());
			result = null;
		}
		log.debug("*** END getProgressJSON ***");
		return result;
	}

	private static int getTounnamentSectionFromRankNumber(int rankNumber) {
		if (rankNumber < fieldTrainingCap)
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
			return 7; // Max level is 7.
	}

	/**
	 * This method prepares the Tournament module menu. This is when Security
	 * Shepherd is in "Tournament Mode". Users are presented with a list of that are
	 * specified as open.
	 * 
	 * @param ApplicationRoot The running context of the application.
	 * @param userId          The user identifier of the user.
	 * @param csrfToken       The cross site request forgery token
	 * @return A HTML menu of a users current module progress and a script for
	 *         interaction with this menu
	 */
	public static String getTournamentModules(String ApplicationRoot, String userId, Locale lang) {
		log.debug("*** Getter.getTournamentModules ***");
		String levelMasterList = new String();
		// Getting Translations
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", lang);
		ResourceBundle levelNames = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", lang);
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			String listEntry = new String();
			// Get the modules
			CallableStatement callstmt = conn.prepareCall("call moduleTournamentOpenInfo(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering moduleTournamentOpenInfo ResultSet for user " + userId);
			ResultSet levels = callstmt.executeQuery();
			log.debug("Opening Result Set from moduleTournamentOpenInfo");
			int currentSection = 0; // Used to identify the first row, as it is slightly different to all other rows
									// for output
			while (levels.next()) {
				// Create Row Entry First
				// log.debug("Adding " + lessons.getString(1));
				listEntry = "<li>";
				// Markers for completion
				if (levels.getString(4) != null) {
					listEntry += "<img src='css/images/completed.png'/>";
				} else {
					listEntry += "<img src='css/images/uncompleted.png'/>";
				}
				// Prepare entry output
				listEntry += "<a class='lesson' id='" + Encode.forHtmlAttribute(levels.getString(3))
						+ "' href='javascript:;'>" + Encode.forHtml(levelNames.getString(levels.getString(1)))
						+ "</a>\n";
				listEntry += "</li>";
				// What section does this belong in? Current or Next?
				if (getTounnamentSectionFromRankNumber(levels.getInt(5)) > currentSection) {
					// This level is not in the same level band as the previous level. So a new
					// Level Band Header is required on the master list before we add the entry.
					// Do we need to close a previous list?
					if (currentSection != 0) // If a Section Select hasn't been made before, we don't need to close any
												// previous sections
					{
						// We've had a section before, so need to close the previous one before we make
						// this new one
						levelMasterList += "</ul>\n";
					}
					// Update the current section to the one we have just added to the list
					currentSection = getTounnamentSectionFromRankNumber(levels.getInt(5));
					// Which to Add?
					switch (currentSection) {
					case 1: // fieldTraining
						// log.debug("Starting Field Training List");
						levelMasterList += "<a id=\"fieldTrainingList\" href=\"javascript:;\"><div class=\"menuButton\">"
								+ bundle.getString("getter.tournamentRank.1") + "</div></a>"
								+ "<ul id=\"theFieldTrainingList\" style=\"display: none;\" class='levelList'>\n";
						break;
					case 2: // private
						// log.debug("Starting Private List");
						levelMasterList += "<a id=\"privateList\" href=\"javascript:;\"><div class=\"menuButton\">"
								+ bundle.getString("getter.tournamentRank.2") + "</div></a>"
								+ "<ul id=\"thePrivateList\" style=\"display: none;\" class='levelList'>\n";
						break;
					case 3: // corporal
						// log.debug("Starting Corporal List");
						levelMasterList += "<a id=\"corporalList\" href=\"javascript:;\"><div class=\"menuButton\">"
								+ bundle.getString("getter.tournamentRank.3") + "</div></a>"
								+ "<ul id=\"theCorporalList\" style=\"display: none;\" class='levelList'>\n";
						break;
					case 4: // sergeant
						// log.debug("Starting Sergeant List");
						levelMasterList += "<a id=\"sergeantList\" href=\"javascript:;\"><div class=\"menuButton\">"
								+ bundle.getString("getter.tournamentRank.4") + "</div></a>"
								+ "<ul id=\"theSergeantList\" style=\"display: none;\" class='levelList'>\n";
						break;
					case 5: // Lieutenant
						// log.debug("Starting Lieutenant List");
						levelMasterList += "<a id=\"lieutenantList\" href=\"javascript:;\"><div class=\"menuButton\">"
								+ bundle.getString("getter.tournamentRank.5") + "</div></a>"
								+ "<ul id=\"theLieutenantList\" style=\"display: none;\" class='levelList'>\n";
						break;
					case 6: // major
						// log.debug("Starting Major List");
						levelMasterList += "<a id=\"majorList\" href=\"javascript:;\"><div class=\"menuButton\">"
								+ bundle.getString("getter.tournamentRank.6") + "</div></a>"
								+ "<ul id=\"theMajorList\" style=\"display: none;\" class='levelList'>\n";
						break;
					case 7: // admiral
						// log.debug("Starting Admiral List");
						levelMasterList += "<a id=\"admiralList\" href=\"javascript:;\"><div class=\"menuButton\">"
								+ bundle.getString("getter.tournamentRank.7") + "</div></a>"
								+ "<ul id=\"theAdmiralList\" style=\"display: none;\" class='levelList'>\n";
						break;
					}
				}
				// Now we can add the entry to the level master List and start again
				levelMasterList += listEntry;
				// log.debug("Put level in category: " + currentSection);
			}
			// If no output has been found, return an error message
			if (levelMasterList.isEmpty()) {
				levelMasterList = "<ul><li><a href='javascript:;'>" + bundle.getString("getter.button.noModulesFound")
						+ "</a></li></ul>";
			} else {
				// List is complete, but we need to close the last list we made, which deinfetly
				// exists as the levelmasterList is not empty
				levelMasterList += "</ul>";
				log.debug("Tournament List returned");
			}
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Tournament List Retrieval: " + e.toString());
		}
		return levelMasterList;
	}

	/**
	 * Return all modules in JSON for specific User
	 * 
	 * @param ApplicationRoot
	 * @param userId
	 * @param lang
	 * @return
	 */
	public static JSONArray getModulesJson(String userId, String floor, Locale locale) {
		log.debug("*** Getter.getModulesJson ***");
		JSONArray jsonOutput = new JSONArray();
		new String();
		Connection conn;
		try {
			conn = Database.getCoreConnection();
		} catch (SQLException | IOException e) {
			log.error("Could not connect to core database: " + e.toString());
			throw new RuntimeException(e);
		}
		ResourceBundle.getBundle("i18n.text", locale);
		ResourceBundle levelNames = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", locale);
		try {
			JSONObject jsonSection = new JSONObject();
			JSONArray jsonSectionModules = new JSONArray();
			JSONObject jsonObject = new JSONObject();
			jsonSection.put("levelMode", floor);
			jsonOutput.add(jsonSection);
			jsonSection = new JSONObject();

			// Get the modules
			CallableStatement callstmt = conn.prepareCall("call getMyModules(?)");
			callstmt.setString(1, userId);
			log.debug("Gathering getMyModules ResultSet for user " + userId);
			ResultSet levels = callstmt.executeQuery();
			boolean thisModuleIsOpen = true; // If Incremental Mode is enabled, after all the modules that have been
												// completed have been added to the JSON Array the next level will be
												// labeled as open and the rest as closed
			while (levels.next()) {
				jsonObject = new JSONObject();
				boolean moduleCompleted = levels.getString(4) != null;
				jsonObject.put("moduleCompleted", moduleCompleted);
				jsonObject.put("moduleId", levels.getString(3));
				jsonObject.put("moduleType", levels.getString(5));
				jsonObject.put("moduleName", levelNames.getString(levels.getString(1)));
				jsonObject.put("moduleCategory", levelNames.getString("category." + levels.getString(2)));
				jsonObject.put("difficultyCategory", getTounnamentSectionFromRankNumber(levels.getInt(7)));
				jsonObject.put("moduleScore", levels.getString(6));
				jsonObject.put("moduleRank", levels.getInt(7));
				jsonObject.put("scoredPoints", levels.getString(8)); // Could be null
				jsonObject.put("medalEarned", levels.getString(9)); // Could be null
				if (ModulePlan.isIncrementalFloor()) {
					boolean moduleOpen;
					if (moduleCompleted || (!moduleCompleted && thisModuleIsOpen)) // If its completed or if this is the
																					// first not completed
					{
						moduleOpen = true;
						if (!moduleCompleted && thisModuleIsOpen) {
							log.debug(levelNames.getString(levels.getString(1)) + " is the Next Module for user "
									+ userId);
							thisModuleIsOpen = false; // Stop this from being set again
						}
					} else {
						moduleOpen = false;
					}
					jsonObject.put("moduleOpen", moduleOpen);
				}
				jsonSectionModules.add(jsonObject);
			}
			jsonSection.put("modules", jsonSectionModules);
			jsonOutput.add(jsonSection);
		} catch (Exception e) {
			log.error("Module List Retrieval: " + e.toString());
		}
		Database.closeConnection(conn);
		return jsonOutput;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param userName        The username of the user
	 * @return The class id of the submitted user name
	 */
	public static String getUserClassFromName(String ApplicationRoot, String userName) {
		log.debug("*** Getter.getUserClass ***");
		String result = new String();
		userName = userName.toLowerCase();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call userClassId(?)");
			log.debug("Gathering userClassId ResultSet");
			callstmt.setString(1, userName);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from userClassId");
			resultSet.next();
			result = resultSet.getString(1);
			log.debug("Found " + result);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute userClassId: " + e.toString());
			result = new String();
		}
		log.debug("*** END getUserClass ***");
		return result;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param userName        The username of the user
	 * @return The user id of the submitted user name
	 */
	public static String getUserIdFromName(String ApplicationRoot, String userName) {
		log.debug("*** Getter.getUserIdFromName ***");
		String result = new String();

		userName = userName.toLowerCase();

		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call userGetIdByName(?)");
			log.debug("Gathering userGetIdByName ResultSet");
			callstmt.setString(1, userName);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from userGetIdByName");
			resultSet.next();
			result = resultSet.getString(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getUserIdFromName ***");
		return result;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @param userId          The identifier of a user
	 * @return The user name of the submitted user identifier
	 */
	public static String getUserName(String ApplicationRoot, String userId) {
		log.debug("*** Getter.getUserName ***");
		String result = new String();
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call userGetNameById(?)");
			log.debug("Gathering userGetNameById ResultSet");
			callstmt.setString(1, userId);
			ResultSet resultSet = callstmt.executeQuery();
			log.debug("Opening Result Set from userGetNameById");
			resultSet.next();
			result = resultSet.getString(1);
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END getUserName ***");
		return result;
	}

	/**
	 * This method is used to determine if a CSRF level has been completed. A call
	 * is made to the DB that returns the CSRF counter for a level. If this counter
	 * is greater than 0, the level has been completed
	 * 
	 * @param applicationRoot Running context of the application
	 * @param moduleHash      Hash ID of the CSRF module you wish to check if a user
	 *                        has completed
	 * @param userId          the ID of the user to check
	 * @return True or False value depicting if the user has completed the module
	 */
	public static boolean isCsrfLevelComplete(String applicationRoot, String moduleId, String userId) {
		log.debug("*** Setter.isCsrfLevelComplete ***");

		boolean result = false;

		try {
			Connection conn = Database.getCoreConnection(applicationRoot);

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
			if (result)
				log.debug("CSRF Level is complete");
			Database.closeConnection(conn);

		} catch (SQLException e) {
			log.error("csrfLevelComplete Failure: " + e.toString());
			result = false;
		}
		log.debug("*** END isCsrfLevelComplete ***");
		return result;
	}

	public static boolean isModuleOpen(String ApplicationRoot, String moduleId) {
		log.debug("*** Getter.isModuleOpen ***");
		boolean result = false;
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			// Get the modules
			PreparedStatement prepStmt = conn.prepareCall("SELECT moduleStatus FROM modules WHERE moduleId = ?");
			prepStmt.setString(1, moduleId);
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next()) {
				if (rs.getString(1).equalsIgnoreCase("open")) {
					result = true;
				}
			}
			rs.close();
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("isModuleOpen Error: " + e.toString());
		}
		return result;
	}

	/**
	 * @param ApplicationRoot The current running context of the application
	 * @return Result set containing admin info in the order userId, userName and
	 *         userAddress
	 */
	public static ResultSet getAdmins(String ApplicationRoot) {
		ResultSet result = null;
		log.debug("*** Getter.adminGetAll () ***");
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call adminGetAll()");
			log.debug("Gathering adminGetAll ResultSet");
			result = callstmt.executeQuery();
			log.debug("Returning Result Set from adminGetAll");

		} catch (SQLException e) {
			log.error("Could not execute query: " + e.toString());
			result = null;
		}
		log.debug("*** END adminGetAll ***");
		return result;
	}

	/**
	 * Used to decipher whether or not a user exists as an admin
	 * 
	 * @param userId The user identifier of the admin to be found
	 * @return A boolean reflecting the state of existence of the admin
	 */
	public static boolean findAdminById(String ApplicationRoot, String userId) {
		log.debug("*** Getter.findAdminById ***");
		boolean userFound = false;
		// Get connection
		try {
			Connection conn = Database.getCoreConnection(ApplicationRoot);

			CallableStatement callstmt = conn.prepareCall("call adminFindById(?)");
			log.debug("Gathering adminFindById ResultSet");
			callstmt.setString(1, userId);
			ResultSet userFind = callstmt.executeQuery();
			log.debug("Opening Result Set from adminFindById");
			userFind.next(); // This will throw an exception if player not found
			log.debug("Admin Found: " + userFind.getString(1)); // This line will not execute if admin not found
			userFound = true;
			Database.closeConnection(conn);

		} catch (Exception e) {
			log.error("Admin does not exist: " + e.toString());
			userFound = false;
		}
		log.debug("*** END findAdminById ***");
		return userFound;
	}

	public static boolean getAdminCheatStatus(String ApplicationRoot) throws SQLException {
		boolean adminCheatStatus = false;
		log.debug("*** Getter.getAdminCheatStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting admin cheat setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "adminCheatsEnabled");

		ResultSet cheatResult = callstmt.executeQuery();

		cheatResult.next();

		adminCheatStatus = cheatResult.getBoolean(1);

		log.debug("Value found: " + adminCheatStatus);

		Database.closeConnection(conn);
		log.debug("*** END getAdminCheatStatus ***");
		return adminCheatStatus;
	}

	public static boolean getPlayerCheatStatus(String ApplicationRoot) throws SQLException {
		boolean getPlayerCheatStatus = false;
		log.debug("*** Getter.getPlayerCheatStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting player cheat setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "playerCheatsEnabled");

		ResultSet cheatResult = callstmt.executeQuery();

		cheatResult.next();

		getPlayerCheatStatus = cheatResult.getBoolean(1);

		log.debug("Value found: " + getPlayerCheatStatus);

		Database.closeConnection(conn);
		log.debug("*** END getPlayerCheatStatus ***");
		return getPlayerCheatStatus;
	}

	public static String getModuleLayout(String ApplicationRoot) throws SQLException {
		String theModuleLayout = "";
		log.debug("*** Getter.getModuleLayout ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting module layout setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "moduleLayout");

		ResultSet layoutResult = callstmt.executeQuery();

		layoutResult.next();

		theModuleLayout = layoutResult.getString(1);

		log.debug("Value found: " + theModuleLayout);

		Database.closeConnection(conn);
		log.debug("*** END getModuleLayout ***");
		return theModuleLayout;
	}

	public static boolean getFeedbackStatus(String ApplicationRoot) throws SQLException {
		boolean theFeedbackStatus = false;
		log.debug("*** Getter.getFeedbackStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting feedback status setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "enableFeedback");

		ResultSet feedbackResult = callstmt.executeQuery();

		feedbackResult.next();

		theFeedbackStatus = feedbackResult.getBoolean(1);

		log.debug("Value found: " + theFeedbackStatus);

		Database.closeConnection(conn);
		log.debug("*** END getFeedbackStatus ***");
		return theFeedbackStatus;
	}

	public static boolean getRegistrationStatus(String ApplicationRoot) throws SQLException {
		boolean theRegistrationStatus = false;
		log.debug("*** Getter.getRegistrationStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting registration status setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "openRegistration");

		ResultSet registrationResult = callstmt.executeQuery();

		registrationResult.next();

		theRegistrationStatus = registrationResult.getBoolean(1);

		log.debug("Value found: " + theRegistrationStatus);

		Database.closeConnection(conn);
		log.debug("*** END getRegistrationStatus ***");
		return theRegistrationStatus;
	}

	public static String getScoreboardStatus(String ApplicationRoot) throws SQLException {
		String theScoreboardStatus = "";
		log.debug("*** Getter.getScoreboardStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Setting scoreboard status setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "scoreboardStatus");

		ResultSet scoreboardResult = callstmt.executeQuery();

		scoreboardResult.next();

		theScoreboardStatus = scoreboardResult.getString(1);

		log.debug("Value found: " + theScoreboardStatus);

		Database.closeConnection(conn);
		log.debug("*** END getScoreboardStatus ***");
		return theScoreboardStatus;
	}

	public static String getScoreboardClass(String ApplicationRoot) throws SQLException {
		String theScoreboardClass = "";
		log.debug("*** Getter.getScoreboardClass ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting scoreboard class setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "scoreboardClass");

		ResultSet scoreboardResult = callstmt.executeQuery();

		scoreboardResult.next();

		theScoreboardClass = scoreboardResult.getString(1);

		log.debug("Value found: " + theScoreboardClass);

		Database.closeConnection(conn);
		log.debug("*** END getScoreboardClass ***");
		return theScoreboardClass;
	}

	public static Boolean getStartTimeStatus(String ApplicationRoot) throws SQLException {
		Boolean theStartTimeStatus = null;
		log.debug("*** Getter.getStartTimeStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting start time setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "hasStartTime");

		ResultSet timestampResult = callstmt.executeQuery();

		timestampResult.next();

		theStartTimeStatus = timestampResult.getBoolean(1);

		log.debug("Value found: " + theStartTimeStatus);

		Database.closeConnection(conn);
		log.debug("*** END getStartTimeStatus ***");
		return theStartTimeStatus;
	}

	public static LocalDateTime getStartTime(String ApplicationRoot) throws SQLException {
		LocalDateTime theStartTimeStatus = null;
		log.debug("*** Getter.getStartTimeStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting start time");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "startTime");

		ResultSet timestampResult = callstmt.executeQuery();

		timestampResult.next();

		String dateTimeString = timestampResult.getString(1);

		log.debug("Value found: " + dateTimeString);

		theStartTimeStatus = LocalDateTime.parse(dateTimeString);

		Database.closeConnection(conn);
		log.debug("*** END getStartTime ***");
		return theStartTimeStatus;
	}

	public static Boolean getLockTimeStatus(String ApplicationRoot) throws SQLException {
		Boolean theLockTimeStatus = null;
		log.debug("*** Getter.getLockTimeStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting lock time setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "hasLockTime");

		ResultSet timestampResult = callstmt.executeQuery();

		timestampResult.next();

		theLockTimeStatus = timestampResult.getBoolean(1);

		log.debug("Value found: " + theLockTimeStatus);

		Database.closeConnection(conn);
		log.debug("*** END getLockTimeStatus ***");
		return theLockTimeStatus;
	}

	public static LocalDateTime getLockTime(String ApplicationRoot) throws SQLException {
		LocalDateTime theLockTimeStatus = null;
		log.debug("*** Getter.getLockTimeStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting lock time");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "lockTime");

		ResultSet timestampResult = callstmt.executeQuery();

		timestampResult.next();

		String dateTimeString = timestampResult.getString(1);

		log.debug("Value found: " + dateTimeString);

		theLockTimeStatus = LocalDateTime.parse(dateTimeString);

		Database.closeConnection(conn);
		log.debug("*** END getLockTime ***");
		return theLockTimeStatus;
	}

	public static Boolean getEndTimeStatus(String ApplicationRoot) throws SQLException {
		Boolean theEndTimeStatus = null;
		log.debug("*** Getter.getEndTimeStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting end time setting");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "hasEndTime");

		ResultSet timestampResult = callstmt.executeQuery();

		timestampResult.next();

		theEndTimeStatus = timestampResult.getBoolean(1);

		log.debug("Value found: " + theEndTimeStatus);

		Database.closeConnection(conn);
		log.debug("*** END getEndTimeStatus ***");
		return theEndTimeStatus;
	}

	public static LocalDateTime getEndTime(String ApplicationRoot) throws SQLException {
		LocalDateTime theEndTimeStatus = null;
		log.debug("*** Getter.getEndTimeStatus ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting end time");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "endTime");

		ResultSet timestampResult = callstmt.executeQuery();

		timestampResult.next();

		String dateTimeString = timestampResult.getString(1);

		log.debug("Value found: " + dateTimeString);

		theEndTimeStatus = LocalDateTime.parse(dateTimeString);

		Database.closeConnection(conn);
		log.debug("*** END getEndTime ***");
		return theEndTimeStatus;
	}
	
	public static String getDefaultClass(String ApplicationRoot) throws SQLException {
		String theDefaultClass = null;
		log.debug("*** Getter.getDefaultClass ***");

		Connection conn = Database.getCoreConnection(ApplicationRoot);

		log.debug("Getting default class");
		PreparedStatement callstmt = conn.prepareStatement("SELECT value FROM settings WHERE setting= ?");

		callstmt.setString(1, "defaultClass");

		ResultSet classResult = callstmt.executeQuery();

		classResult.next();

		theDefaultClass = classResult.getString(1);

		log.debug("Value found: " + theDefaultClass);

		Database.closeConnection(conn);
		log.debug("*** END getDefaultClass ***");
		return theDefaultClass;
	}

}
