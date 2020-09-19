package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.mongodb.MongoClient;

import dbProcs.Constants;
import dbProcs.Database;
import dbProcs.MongoDatabase;
import dbProcs.Setter;

import servlets.module.challenge.XxeChallenge1;
import servlets.module.lesson.XxeLesson;
import utils.Validate;

public class Setup extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(Setup.class);
	private static final long serialVersionUID = -892181347446991016L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));

		ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", locale);
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");

		// Output Stuff
		PrintWriter out = response.getWriter();
		String htmlOutput = "";
		boolean success = false;
		boolean validateInput = true;
		boolean saveMysqlProperties = false;
		boolean hasDBFile = false;

		// Parameters From Form
		String dbHost = request.getParameter("dbhost");
		String dbPort = request.getParameter("dbport");
		String dbUser = request.getParameter("dbuser");
		String dbPass = request.getParameter("dbpass");

		String dbOptions;
		String connectionURL;
		String driverType;

		String dbOverride = request.getParameter("dboverride");

		Properties mysql_props = Setup.getDBProps();
		Properties mongo_props = new Properties();

		hasDBFile = (mysql_props != null);

		if (hasDBFile) {
			// Db auth file exists, try to load from it

			if (!dbHost.isEmpty() || !dbPort.isEmpty()) {
				// One of db host and db port are missing, we can't handle this situation!

				htmlOutput += "If you override db host and db port, both must be entered!";
				validateInput = false;
				connectionURL = "";
			} else if (dbHost.isEmpty() || dbPort.isEmpty()) {
				// Both db host and db port are missing, good, load from props file instead
				connectionURL = mysql_props.getProperty("databaseConnectionURL");
				String databaseSchema = mysql_props.getProperty("databaseSchema");

				if (connectionURL == null || databaseSchema == null) {
					// Nothing useful given in user input or from properties file, bail out.
					validateInput = false;
				}
			} else {
				// Override db properties from request parameters
				connectionURL = "jdbc:mysql://" + dbHost + ":" + dbPort + "/";

				// Store the overridden data in properties file
				saveMysqlProperties = true;
			}

			dbOptions = mysql_props.getProperty("databaseOptions");
			if (dbOptions == null) {
				dbOptions = "useUnicode=true&character_set_server=utf8mb4";
			}
			driverType = mysql_props.getProperty("DriverType");
			if (driverType == null) {
				driverType = "org.gjt.mm.mysql.Driver";
			}
			if (dbUser.isEmpty()) {
				dbUser = mysql_props.getProperty("databaseUsername");
				if (dbUser == null) {
					validateInput = false;
				}
			}
			if (dbPass.isEmpty()) {
				dbPass = mysql_props.getProperty("databasePassword");
				if (dbPass == null) {
					validateInput = false;
				}
			}
		} else {
			connectionURL = "jdbc:mysql://" + dbHost + ":" + dbPort + "/";
			driverType = "org.gjt.mm.mysql.Driver";
			dbOptions = "useUnicode=true&character_set_server=utf8mb4";
			validateInput = true;
			saveMysqlProperties = true;

		}

		if (!validateInput) {
			htmlOutput += "Data validation failed.";
			success = false;
		} else {

			String dbAuth = request.getParameter("dbauth");

			String mongodbHost = request.getParameter("mhost");
			String mongodbPort = request.getParameter("mport");
			String nosqlprops = new File(Database.class.getResource("/challenges/NoSqlInjection1.properties").getFile())
					.getAbsolutePath();

			try (InputStream mongo_input = new FileInputStream(nosqlprops)) {

				mongo_props.load(mongo_input);

			}

			String mongodbName = mongo_props.getProperty("databaseName");
			if (mongodbName == null) {
				String message = "Could not find databaseName in nosql properties file";
				log.fatal(message);
				throw new RuntimeException(message);
			}

			log.debug("Starting database setup...");

			String auth = "";

			String enableMongoChallenge = request.getParameter("enableMongoChallenge");

			String enableUnsafeLevels = request.getParameter("unsafeLevels");

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
				log.debug("Generating auth file");

				generateAuth();
			}

			if (!auth.equals(dbAuth)) {
				log.debug("Invalid auth supplied");

				// The supplied auth data was incorrect
				htmlOutput += bundle.getString("generic.text.setup.authentication.failed");
				log.error("Authorization mismatch: " + auth + " does not equal " + dbAuth);

			} else {
				// Test the user's entered database properties
				Boolean connectionSuccess = false;
				log.debug("Attempting to connect to database");

				try {
					Connection conn = Database.getConnection(driverType, connectionURL, dbOptions, dbUser, dbPass);
					Database.closeConnection(conn);
					connectionSuccess = true;
					log.debug("Database connection successful");

				} catch (SQLException e) {
					htmlOutput += bundle.getString("generic.text.setup.connection.failed") + e.getMessage();

					log.error("DB connection error: " + e.toString());
					connectionSuccess = false;

				}

				if (!connectionSuccess) {
					htmlOutput += bundle.getString("generic.text.setup.connection.failed");
				} else {
					// Write the user's entered mysql database properties to file

					if (saveMysqlProperties) {

						try (OutputStream mysql_output = new FileOutputStream(Constants.MYSQL_DB_PROP)) {

							mysql_props = new Properties();

							mysql_props.setProperty("databaseConnectionURL", connectionURL);
							mysql_props.setProperty("DriverType", driverType);
							mysql_props.setProperty("databaseOptions", dbOptions);
							mysql_props.setProperty("databaseSchema", "core");
							mysql_props.setProperty("databaseUsername", dbUser);
							mysql_props.setProperty("databasePassword", dbPass);

							// save properties to project root folder
							mysql_props.store(mysql_output, null);
							success = true;

						} catch (IOException e) {

							success = false;

							htmlOutput = bundle.getString("generic.text.setup.failed") + ": " + e.getMessage();

							log.error("Could not save mysql properties file: " + e.toString());
						}

					} else {
						success = true;
					}

					if (success) {
						// Writing db file succeeded

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
							log.error(bundle.getString("generic.text.setup.failed") + ": " + e.getMessage());
							if (!hasDBFile) {
								FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
							}
						}
						// Clean up File as it is not needed anymore. Will Cause a new one to be
						// generated next time too
						removeAuthFile();
					}

					if (enableMongoChallenge.equalsIgnoreCase("enable")) {
						if (!Validate.isValidPortNumber(mongodbPort)) {
							htmlOutput = bundle.getString("generic.text.setup.error.valid.port");
							FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
						} else {
							Files.write(Paths.get(Constants.MONGO_DB_PROP), mongoProp.toString().getBytes(),
									StandardOpenOption.CREATE);
							if (MongoDatabase.getMongoDbConnection(null).listDatabaseNames() == null) {
								htmlOutput = bundle.getString("generic.text.setup.connection.mongo.failed");
								if (!hasDBFile) {
									FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
								}
							} else {
								try {
									executeMongoScript();
								} catch (IOException e) {
									htmlOutput = bundle.getString("generic.text.setup.failed") + ": " + e.getMessage();
									if (!hasDBFile) {
										FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
									}
								}
							}
						}
					}

					if (enableUnsafeLevels.equalsIgnoreCase("enable")) {
						openUnsafeLevels();
						if (!executeCreateChallengeFile()) {
							htmlOutput = bundle.getString("generic.text.setup.file.failed");
							if (!hasDBFile) {
								FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
							}
						}
					}
				}

			}
		}
		if (success) {
			htmlOutput = "<h2 class=\"title\" id=\"login_title\">"
					+ bundle.getString("generic.text.setup.response.success") + "</h2><p>" + htmlOutput + " "
					+ bundle.getString("generic.text.setup.response.success.redirecting") + "</p>";
		} else {
			log.error("Could not create database...");
			if (!hasDBFile) {
				FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
			}
			htmlOutput = "<h2 class=\"title\" id=\"login_title\">"
					+ bundle.getString("generic.text.setup.response.failed") + "</h2><p>" + htmlOutput + "</p>";
		}
		out.write(htmlOutput);

		out.close();
	}

	public static boolean isInstalled() {
		boolean isInstalled = false;

		Properties prop = getDBProps();

		if (prop != null) {

			try (Connection coreConnection = Database.getCoreConnection(null)) {
				if (coreConnection != null) {
					isInstalled = true;
				}
			} catch (SQLException e) {
				log.info("isInstalled got SQL exception " + e.toString() + ", assuming not installed.");
			}
		}

		if (!isInstalled) {
			generateAuth();
		}

		return isInstalled;
	}

	public static Properties getDBProps() {

		Properties prop = new Properties();

		// Pull Driver and DB URL out of database.properties

		String mysql_props = Constants.MYSQL_DB_PROP;

		try (InputStream mysql_input = new FileInputStream(mysql_props)) {

			prop.load(mysql_input);

			return prop;

		} catch (IOException e) {
			log.info("Could not load properties file, assuming doesn't exist: " + e.toString());
			return null;
		}

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

		log.debug("Initializing core database");
		Connection databaseConnection = Database.getDatabaseConnection(null, true);
		Statement psProcToexecute = databaseConnection.createStatement();
		psProcToexecute.executeUpdate(data);

		file = new File(getClass().getClassLoader().getResource("/database/moduleSchemas.sql").getFile());
		data = FileUtils.readFileToString(file, Charset.defaultCharset());
		log.debug("Initializing module database");

		psProcToexecute = databaseConnection.createStatement();
		psProcToexecute.executeUpdate(data);

	}

	private synchronized void executeMongoScript() throws IOException {

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
		if (XxeLesson.createXxeLessonSolutionFile() && XxeChallenge1.createXxeChallenge1SolutionFile())
			return true;

		return false;
	}
}
