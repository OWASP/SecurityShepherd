package testUtils;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import dbProcs.Constants;
import dbProcs.Database;
import dbProcs.Getter;
import dbProcs.Setter;
import servlets.Login;
import utils.InstallationException;

public class TestProperties {
	private static org.apache.log4j.Logger log = Logger.getLogger(TestProperties.class);

	public static void failAndPrint(String message) {
		log.fatal(message);
		fail(message);
	}

	public static void executeSql(org.apache.log4j.Logger log) throws IOException, SQLException {

		File file = new File(System.getProperty("user.dir") + "/src/main/resources/database/coreSchema.sql");
		String data = FileUtils.readFileToString(file, Charset.defaultCharset());

		Connection databaseConnection = Database.getDatabaseConnection(null, true);
		Statement psProcToexecute = databaseConnection.createStatement();
		psProcToexecute.executeUpdate(data);

		file = new File(System.getProperty("user.dir") + "/src/main/resources/database/moduleSchemas.sql");
		data = FileUtils.readFileToString(file, Charset.defaultCharset());
		psProcToexecute = databaseConnection.createStatement();
		psProcToexecute.executeUpdate(data);

	}

	public static void createFileSystemKey(org.apache.log4j.Logger log, String fileProp, String solutionProp)
			throws InstallationException {

		String userDir = System.getProperty("user.dir");
		String propFile = userDir + "/src/main/resources/fileSystemKeys.properties";

		Properties prop = new Properties();

		// Pull Driver and DB URL out of database.properties

		try (InputStream mysql_input = new FileInputStream(propFile)) {

			prop.load(mysql_input);

		} catch (IOException e) {
			log.error("Could not load properties file: " + e.toString());
			throw new RuntimeException(e);
		}

		String errorBase = "Missing property :";

		String filename = prop.getProperty(fileProp);
		if (filename == null) {
			throw new RuntimeException(errorBase + fileProp);
		}
		String solution = prop.getProperty(solutionProp);
		if (solution == null) {
			throw new RuntimeException(errorBase + solutionProp);
		}

		File lessonFile = null;

		lessonFile = new File(filename);
		try {
			FileUtils.write(lessonFile, solution, "UTF-8");
		} catch (IOException e) {
			log.error("Can't write to lesson file " + lessonFile + ": " + e.toString());
			throw new RuntimeException(e);
		}

	}

	/**
	 * Bit of a Hack to get JUnits to run inside of
	 * 
	 * @param log
	 */
	public static void setTestPropertiesFileDirectory(org.apache.log4j.Logger log) {
		if (System.getProperty("catalina.base") == null) {
			String userDir = System.getProperty("user.dir");
			log.debug("catalina.base returns null. Creating it with base of user.dir; " + userDir + File.separator
					+ "target" + File.separator + "test-classes");
			System.setProperty("catalina.base", userDir + File.separator + "target" + File.separator + "test-classes");
		}
	}

	/**
	 * Method to simulate login servlet interaction. Can't seem to recyle the method
	 * in LoginTest with the MockRequests
	 * 
	 * @param userName User to Sign in
	 * @param password User Password to use to Sign in
	 * @param theClass Class of the User
	 */
	public static void loginDoPost(org.apache.log4j.Logger log, MockHttpServletRequest request,
			MockHttpServletResponse response, String userName, String password, String theClass, String lang) {

		int expectedResponseCode = 302;

		log.debug("Creating Login Servlet Instance");
		Login servlet = new Login();
		try {
			servlet.init(new MockServletConfig("Login"));
		} catch (ServletException e) {
			failAndPrint("Could not create login Servlet instance: " + e.toString());
			throw new RuntimeException(e);
		}

		// Setup Servlet Parameters and Attributes
		log.debug("Setting Up Params and Atrributes");
		request.addParameter("login", userName);
		request.addParameter("pwd", password);
		request.getSession().setAttribute("lang", lang);

		log.debug("Running doPost");
		try {
			servlet.doPost(request, response);
		} catch (ServletException | IOException e) {
			failAndPrint("Could not post Servlet: " + e.toString());
			throw new RuntimeException(e);
		}

		if (response.getStatus() != expectedResponseCode) {
			failAndPrint("Login Servlet returned " + response.getStatus() + " instead of expected code 302.");
		} else if (response.getHeader("Location").endsWith("login.jsp")) {
			log.debug("User \"" + userName + "\" is unauthenticated");
		} else {
			log.debug("302 OK Detected");
			String location = response.getHeader("Location");
			log.debug("302 pointing at: " + location);
			if (!location.endsWith("index.jsp")) {
				failAndPrint("Login not Redirecting to index.jsp. Login Proceedure Failed");
			}
		}
	}

	/**
	 * This method will sign in as a User, or create the user and sign in as them.
	 * If this fails it will throw an Exception
	 * 
	 * @param applicationRoot Context of running application
	 * @param userName        The user name of the user you want to create or sign
	 *                        in as
	 * @param password        The password of the user you want to create or sign in
	 *                        as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * 
	 */
	public static boolean verifyTestUser(org.apache.log4j.Logger log, String applicationRoot, String userName,
			String password) throws SQLException {
		boolean result = false;

		String user[] = Getter.authUser(applicationRoot, userName, userName);
		if (user == null || user[0].isEmpty()) {
			log.debug("User not found in DB. Adding user to DB and Retesting before giving up");
			Setter.userCreate(applicationRoot, null, userName, userName, "player", userName + "@test.com", false);
			user = Getter.authUser(applicationRoot, userName, userName);
		}
		if (user != null && !user[0].isEmpty()) {
			log.debug(userName + " could authenticate. returning true");
			result = true;
		} else {
			failAndPrint("Could not Verify User " + userName + " could authenticate at all.");
		}

		return result;
	}

	/**
	 * This method will sign in as a User, or create the user and sign in as them.
	 * If this fails it will throw an Exception
	 * 
	 * @param applicationRoot Context of running application
	 * @param userName        The user name of the user you want to create or sign
	 *                        in as
	 * @param password        The password of the user you want to create or sign in
	 *                        as
	 * @param classId         Class to create the user in
	 * @return Boolean value depicting if the user exists and can be authenticated
	 */
	public static boolean verifyTestUser(org.apache.log4j.Logger log, String applicationRoot, String userName,
			String password, String classId) throws SQLException {
		boolean result = false;

		String user[] = Getter.authUser(applicationRoot, userName, userName);
		if (user == null || user[0].isEmpty()) {
			log.debug("User not found in DB. Adding user to DB");
			Setter.userCreate(applicationRoot, classId, userName, userName, "player", userName + "@test.com", false);
			user = Getter.authUser(applicationRoot, userName, userName);
		}
		if (user != null && !user[0].isEmpty()) {
			log.debug(userName + " could authenticate. returning true");
			result = true;
		} else {
			failAndPrint("Could not verify that " + userName + " could authenticate at all.");
		}

		return result;

	}

	/**
	 * This method will sign in as a User, or create the user and sign in as them.
	 * If this fails it will throw an Exception
	 * 
	 * @param applicationRoot Context of running application
	 * @param userName        The user name of the user you want to create or sign
	 *                        in as
	 * @param password        The password of the user you want to create or sign in
	 *                        as
	 * @param classId         Class to create the user in
	 * @return Boolean value depicting if the user exists and can be authenticated
	 */
	public static boolean verifyTestAdmin(org.apache.log4j.Logger log, String applicationRoot, String userName,
			String password, String classId) throws SQLException {
		boolean result = false;

		String user[] = Getter.authUser(applicationRoot, userName, userName);
		if (user == null || user[0].isEmpty()) {
			log.debug("User not found in DB. Adding user to DB and Retesting before giving up");
			Setter.userCreate(applicationRoot, classId, userName, userName, "admin", userName + "@test.com", false);
			user = Getter.authUser(applicationRoot, userName, userName);
		}
		if (user != null && !user[0].isEmpty()) {
			log.debug(userName + " could authenticate. returning true");
			result = true;
		} else {
			failAndPrint("Could not Verify User " + userName + " could authenticate at all.");
		}

		return result;
	}

	/**
	 * This method will sign in as an admin, or create the admin and sign in as
	 * them. If this fails it will throw an Exception
	 * 
	 * @param applicationRoot Context of running application
	 * @param userName        The user name of the user you want to create or sign
	 *                        in as
	 * @param password        The password of the user you want to create or sign in
	 *                        as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * 
	 */
	public static boolean verifyTestAdmin(org.apache.log4j.Logger log, String applicationRoot, String userName,
			String password) throws SQLException {
		boolean result = false;

		String user[] = Getter.authUser(applicationRoot, userName, userName);
		if (user == null || user[0].isEmpty()) {
			log.debug("User not found in DB. Adding user to DB and Retesting before giving up");
			Setter.userCreate(applicationRoot, null, userName, userName, "admin", userName + "@test.com", false);
			user = Getter.authUser(applicationRoot, userName, userName);
		}
		if (user != null && !user[0].isEmpty()) {
			log.debug(userName + " could authenticate. returning true");
			result = true;
		} else {
			failAndPrint("Could not Verify User " + userName + " could authenticate at all.");
		}

		return result;
	}

	/**
	 * Searches for class based on class name. If nothing is found, the class is
	 * created and the new class Id is returned
	 * 
	 * @param className Name of the class you wish to search / create
	 * @return The Identifier of the class owning the name submitted
	 */
	public static String findCreateClassId(org.apache.log4j.Logger log, String className, String applicationRoot)
			throws SQLException {
		String classId = new String();
		ResultSet rs = Getter.getClassInfo(applicationRoot);
		while (rs.next()) {
			if (rs.getString(2).compareTo(className) == 0) {
				classId = rs.getString(1);
				break;
			}
		}
		rs.close();
		if (classId.isEmpty()) {
			log.debug("Could not find class. Creating it");
			if (Setter.classCreate(applicationRoot, className, "2015")) {
				log.debug("Class Created. Getting ID");
				classId = findCreateClassId(log, className, applicationRoot);
			} else {
				failAndPrint("Could not Create Class " + className);
			}
		}
		return classId;
	}

	/**
	 * This method will login/create a PLAYER, open all modules, Collect the Module
	 * Adddress and Mark the moduleId as complete
	 * 
	 * @param log             Logger
	 * @param userName        Username to complete level with
	 * @param userPass        Password to complete level with
	 * @param moduleId        If of level to complete
	 * @param feedbackString  Leave as null for default
	 * @param applicationRoot
	 */
	public static boolean completeModuleForUser(org.apache.log4j.Logger log, String userName, String userPass,
			String moduleId, String feedbackString, String applicationRoot) throws SQLException {
		boolean result = false;

		if (verifyTestUser(log, applicationRoot, userName, userPass)) {
			String userId = Getter.getUserIdFromName(applicationRoot, userName);
			// Open all Modules First so that the Module Can Be Opened
			if (Setter.openAllModules(applicationRoot, false) && Setter.openAllModules(applicationRoot, true)) {
				// Simulate user Opening Level
				if (!Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty()) {
					// Then, Mark the Challenge Complete for user (Insecure Data Storage Lesson)
					String feedbackSearchCode = "RwarUNiqueFeedbackCodeToSEARCHFor1182371723";
					String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, moduleId, userId,
							feedbackSearchCode, 1, 1, 1);
					if (markLevelCompleteTest != null) {
						String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, moduleId, userId);
						log.debug("checkPlayerResultTest" + checkPlayerResultTest);
						if (checkPlayerResultTest == null) {
							result = true;
						} else {
							fail("Function says user has not completed module"); // Even though this test just
																					// marked it as Completed
						}
					} else
						fail("Could not mark data storage lesson as complete for user");
				} else
					fail("Could not Mark Data Storage Lesson as Opened by Default admin");
			} else
				fail("Could not Open All Modules");
		} else {
			fail("Could not verify user (No Exception Failure)");
		}

		return result;
	}

	/**
	 * Create a mysql database properties file
	 */
	public static void createMysqlResource(String dbHost, int dbPort, String dbSchema, String dbUsername,
			String dbPassword) throws IOException {

		log.debug("Creating mysql db file at " + Constants.MYSQL_DB_PROP);

		File file = new File(Constants.MYSQL_DB_PROP);
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(writer);
		bw.write("databaseConnectionURL=jdbc:mysql://" + dbHost + ":" + dbPort + "/");
		bw.newLine();
		bw.write("DriverType=org.gjt.mm.mysql.Driver");
		bw.newLine();
		bw.write("databaseOptions=useUnicode=true&character_set_server=utf8mb4");
		bw.newLine();
		bw.write("databaseSchema=" + dbSchema);
		bw.newLine();
		bw.write("databaseUsername=" + dbUsername);
		bw.newLine();
		bw.write("databasePassword=" + dbPassword);
		bw.close();

		log.debug("Created mysql db file at " + Constants.MYSQL_DB_PROP);

	}

	/**
	 * Create a mysql database properties file
	 * 
	 * @throws IOException
	 */
	public static void createMysqlResource() throws IOException {
		createMysqlResource("localhost", 3306, "core", "root", "");
	}

	/**
	 * Delete the mysql database properties file
	 * 
	 * @throws IOException
	 */
	public static void deleteMysqlResource() {
		FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
	}

	/**
	 * Create a mongo database properties file
	 */
	public static void createMongoResource(String dbHost, long dbPort, String dbName, long connectTimeout,
			long socketTimeout, long serverSelectionTimeout) throws IOException {

		log.debug("Creating mongo db file at " + Constants.MONGO_DB_PROP);

		File file = new File(Constants.MONGO_DB_PROP);
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(writer);

		bw.write("connectionHost=" + dbHost);
		bw.newLine();
		bw.write("connectionPort=" + dbPort);
		bw.newLine();
		bw.write("databaseName=" + dbName);
		bw.newLine();
		bw.write("connectTimeout=" + connectTimeout);
		bw.newLine();
		bw.write("socketTimeout=" + socketTimeout);
		bw.newLine();
		bw.write("serverSelectionTimeout=" + serverSelectionTimeout);
		bw.newLine();
		bw.close();

		log.debug("Created mongo db file at " + Constants.MONGO_DB_PROP);
	}

	/**
	 * Create a mongo database properties file
	 * 
	 * @throws IOException
	 */
	public static void createMongoResource() throws IOException {
		createMongoResource("0.0.0.0", 27017, "shepherdGames", 10000, 0, 30000);
	}

	/**
	 * Delete the mongo database properties file
	 * 
	 * @throws IOException
	 */
	public static void deleteMongoResource() {
		FileUtils.deleteQuietly(new File(Constants.MONGO_DB_PROP));
	}

}