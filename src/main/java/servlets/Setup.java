package servlets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.MongoClient;
import dbProcs.Constants;
import dbProcs.Database;
import dbProcs.FileInputProperties;
import dbProcs.MongoDatabase;
import dbProcs.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import servlets.module.lesson.XxeLesson;
import utils.InstallationException;
import utils.Validate;

public class Setup extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(Setup.class);
	private static final long serialVersionUID = -892181347446991016L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", locale);

		// Output Stuff
		PrintWriter out = response.getWriter();
		String htmlOutput = "";
		boolean success = false;

		// Parameters From Form
		String dbHost = request.getParameter("dbhost");
		String dbPort = request.getParameter("dbport");
		String dbUser = request.getParameter("dbuser");
		String dbPass = request.getParameter("dbpass");
		String dbAuth = request.getParameter("dbauth");
		String dbOverride = request.getParameter("dboverride");

		String connectionURL = "jdbc:mysql://" + dbHost + ":" + dbPort + "/";
		String driverType = "org.gjt.mm.mysql.Driver";

		String mongodbHost = request.getParameter("mhost");
		String mongodbPort = request.getParameter("mport");
		String nosqlprops = new File(Database.class.getResource("/challenges/NoSqlInjection1.properties").getFile())
				.getAbsolutePath();
		String mongodbName = FileInputProperties.readfile(nosqlprops, "databaseName");
		String auth = "";

		String enableMongoChallenge = request.getParameter("enableMongoChallenge");

		String enableUnsafeLevels = request.getParameter("unsafeLevels");

		StringBuffer dbProp = new StringBuffer();
		dbProp.append("databaseConnectionURL=" + connectionURL);
		dbProp.append("\n");
		dbProp.append("DriverType=" + driverType);
		dbProp.append("\n");
		dbProp.append("databaseSchema=core");
		dbProp.append("\n");
		dbProp.append("databaseUsername=" + dbUser);
		dbProp.append("\n");
		dbProp.append("databasePassword=" + dbPass);
		dbProp.append("\n");

		// Mongo DB properties
		StringBuffer mongoProp = new StringBuffer();
		mongoProp.append("connectionHost=" + mongodbHost);
		mongoProp.append("\n");
		mongoProp.append("connectionPort=" + mongodbPort);
		mongoProp.append("\n");
		mongoProp.append("databaseName=" + mongodbName);
		mongoProp.append("\n");
		mongoProp.append("connectTimeout=10000");
		mongoProp.append("\n");
		mongoProp.append("socketTimeout=0");
		mongoProp.append("\n");
		mongoProp.append("serverSelectionTimeout=30000");
		mongoProp.append("\n");

		try {
			auth = new String(Files.readAllBytes(Paths.get(Constants.SETUP_AUTH)));
		} catch (NoSuchFileException e) {
			// Auth file could not be found.
			htmlOutput += "Auth file could not be found";
			log.error("Auth file could not be found: " + e.toString());

		}

		if (auth == "") {
			// No auth loaded, could be because user never reloaded setup page after an
			// error. Generate it again
			generateAuth();
		}

		if (!auth.equals(dbAuth)) {
			// The supplied auth data was incorrect
			htmlOutput += bundle.getString("generic.text.setup.authentication.failed");
			log.error("Authorization mismatch: " + auth + " does not equal " + dbAuth);

		} else {
			// Test the user's entered database properties
			Boolean connectionSuccess = false;
			try {
				Connection conn = Database.getConnection(driverType, connectionURL, dbUser, dbPass);
				Database.closeConnection(conn);
				connectionSuccess = true;
			} catch (SQLException e) {
				htmlOutput += bundle.getString("generic.text.setup.connection.failed") + e.getMessage();

				log.error("DB connection error: " + e.toString());

			}

			if (!connectionSuccess) {
				htmlOutput += bundle.getString("generic.text.setup.connection.failed");
			} else {
				// Write the user's entered mysql database properties to file
				Files.write(Paths.get(Constants.DBPROP), dbProp.toString().getBytes(), StandardOpenOption.CREATE);
				try {
					if (dbOverride.equalsIgnoreCase("override")) {
						executeSqlScript();
						htmlOutput = bundle.getString("generic.text.setup.success") + " "
								+ bundle.getString("generic.text.setup.success.overwrittendb");
					} else if (dbOverride.equalsIgnoreCase("upgrade")) {
						executeUpdateScript();
						htmlOutput = bundle.getString("generic.text.setup.success") + " "
								+ bundle.getString("generic.text.setup.success.updatedb");
					} else {
						htmlOutput = bundle.getString("generic.text.setup.success");
					}
					success = true;
				} catch (SQLException e) {
					htmlOutput = bundle.getString("generic.text.setup.failed") + ": " + e.getMessage();
					FileUtils.deleteQuietly(new File(Constants.DBPROP));
				}
				// Clean up File as it is not needed anymore. Will Cause a new one to be
				// generated next time too
				removeAuthFile();

				if (enableMongoChallenge.equalsIgnoreCase("enable")) {
					if (!Validate.isValidPortNumber(mongodbPort)) {
						htmlOutput = bundle.getString("generic.text.setup.error.valid.port");
						FileUtils.deleteQuietly(new File(Constants.DBPROP));
					} else {
						Files.write(Paths.get(Constants.MONGO_DB_PROP), mongoProp.toString().getBytes(),
								StandardOpenOption.CREATE);
						if (MongoDatabase.getMongoDbConnection(null).listDatabaseNames() == null) {
							htmlOutput = bundle.getString("generic.text.setup.connection.mongo.failed");
							FileUtils.deleteQuietly(new File(Constants.DBPROP));
						} else {
							try {
								executeMongoScript();
							} catch (IOException e) {
								htmlOutput = bundle.getString("generic.text.setup.failed") + ": " + e.getMessage();
								FileUtils.deleteQuietly(new File(Constants.DBPROP));
							}
						}
					}
				}

				if (enableUnsafeLevels.equalsIgnoreCase("enable")) {
					openUnsafeLevels();
					if (!executeCreateChallengeFile()) {
						htmlOutput = bundle.getString("generic.text.setup.file.failed");
						FileUtils.deleteQuietly(new File(Constants.DBPROP));
					}
				}
			}

		}
		if (success) {
			htmlOutput = "<h2 class=\"title\" id=\"login_title\">"
					+ bundle.getString("generic.text.setup.response.success") + "</h2><p>" + htmlOutput + " "
					+ bundle.getString("generic.text.setup.response.success.redirecting") + "</p>";
		} else {
			FileUtils.deleteQuietly(new File(Constants.DBPROP));
			htmlOutput = "<h2 class=\"title\" id=\"login_title\">"
					+ bundle.getString("generic.text.setup.response.failed") + "</h2><p>" + htmlOutput + "</p>";
		}
		out.write(htmlOutput);

		out.close();
	}

	public static boolean isInstalled() {
		boolean isInstalled = false;

		try (Connection coreConnection = Database.getDatabaseConnection(null)) {
			if (coreConnection == null) {
				isInstalled = false;
			} else {
				isInstalled = true;
			}
		} catch (FileNotFoundException e) {
			// DB properties file not found, we're not installed
			log.fatal("Database properties file not found, assuming not installed: " + e.toString());

		} catch (SQLException e) {
			// Some other database error occurred
			log.fatal("Cannot connect to database: " + e.toString());
			isInstalled = true;
			throw new RuntimeException(e);

		}

		if (!isInstalled) {
			generateAuth();
		}

		return isInstalled;
	}

	private static void generateAuth() {
		try {
			if (!Files.exists(Paths.get(Constants.SETUP_AUTH), LinkOption.NOFOLLOW_LINKS)) {
				UUID randomUUID = UUID.randomUUID();
				log.info("Auth file not found, creating: " + Constants.SETUP_AUTH);

				Files.write(Paths.get(Constants.SETUP_AUTH), randomUUID.toString().getBytes(),
						StandardOpenOption.CREATE);
				log.info("Generated UUID " + randomUUID + " in " + Constants.SETUP_AUTH);
			}
		} catch (IOException e) {
			log.fatal("Unable to generate auth: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private static void removeAuthFile() {
		if (!Files.exists(Paths.get(Constants.SETUP_AUTH), LinkOption.NOFOLLOW_LINKS)) {
			log.info("Could not find " + Constants.SETUP_AUTH);
		} else {
			FileUtils.deleteQuietly(new File(Constants.SETUP_AUTH));
		}
	}

	private synchronized void executeSqlScript() throws IOException, SQLException {

		File file = new File(getClass().getClassLoader().getResource("/database/coreSchema.sql").getFile());
		String data = FileUtils.readFileToString(file, Charset.defaultCharset());

		Connection databaseConnection = Database.getDatabaseConnection(null, true);
		Statement psProcToexecute = databaseConnection.createStatement();
		psProcToexecute.executeUpdate(data);

		file = new File(getClass().getClassLoader().getResource("/database/moduleSchemas.sql").getFile());
		data = FileUtils.readFileToString(file, Charset.defaultCharset());
		psProcToexecute = databaseConnection.createStatement();
		psProcToexecute.executeUpdate(data);

	}

	private synchronized void executeMongoScript() throws IOException  {

		MongoClient mongoConnection = null;

		try {
			File file = new File(getClass().getClassLoader().getResource("/mongodb/moduleSchemas.js").getFile());
			mongoConnection = MongoDatabase.getMongoDbConnection(null);
			MongoDatabase.executeMongoScript(file, mongoConnection);
		} catch (IOException e) {
			throw e;
		} finally {
			MongoDatabase.closeConnection(mongoConnection);
		}

	}

	private synchronized void executeUpdateScript() throws IOException, SQLException {

		File file = new File(getClass().getClassLoader().getResource("/database/updatev3_0tov3_1.sql").getFile());

		String data;

		data = FileUtils.readFileToString(file, Charset.defaultCharset());

		Connection databaseConnection = Database.getDatabaseConnection(null, true);
		Statement psProcToexecute = databaseConnection.createStatement();
		psProcToexecute.executeUpdate(data);

	}

	private synchronized void openUnsafeLevels() {
		String ApplicationRoot = getServletContext().getRealPath("");
		Setter.openAllModules(ApplicationRoot, true);
	}

	private synchronized Boolean executeCreateChallengeFile() {
		return XxeLesson.createXxeLessonSolutionFile();
	}
}
