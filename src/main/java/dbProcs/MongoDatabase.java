package dbProcs;

import com.mongodb.*;

import utils.PropertyNotFoundException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Used to create MongoDb connections <br/>
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
 * @author Paul
 */

public class MongoDatabase {

	private static org.apache.log4j.Logger log = Logger.getLogger(MongoDatabase.class);

	/**
	 * Method to close a MongoDb connection
	 * 
	 * @param conn The connection to close
	 */
	public static void closeConnection(MongoClient conn) {

		conn.close();

	}

	/**
	 * Method to get a MongoDb Challenge collection
	 * 
	 * @param ApplicationRoot The running context of the application.
	 * @param path            The path to the properties file to use for this
	 *                        connection (filtered for path traversal attacks)
	 * @return A MongoDb Collection
	 * @throws FileNotFoundException
	 */
	public static MongoCredential getMongoChallengeCredentials(String ApplicationRoot, String path)
			throws FileNotFoundException {
		// Some over paranoid input validation never hurts.
		path = path.replaceAll("\\.", "").replaceAll("/", "");
		log.debug("Path = " + path);

		String props;
		MongoCredential credential;

		props = new File(MongoDatabase.class.getResource("/challenges/" + path + ".properties").getFile())
				.getAbsolutePath();
		log.debug("Level Properties File = " + path + ".properties");

		String username = "";
		char[] password = null;
		String dbname = "";

		try {
			username = FileInputProperties.readfile(props, "databaseUsername");
			log.debug("Username for Mongo level: " + username);
			password = FileInputProperties.readfile(props, "databasePassword").toCharArray();
			log.debug("Password for Mongo level read");
			dbname = FileInputProperties.readfile(props, "databaseName");
			log.debug("Mongo database name: " + dbname);
		} catch (FileNotFoundException e) {
			// db props file doesn't exist
			throw e;
		} catch (IOException | PropertyNotFoundException e) {
			throw new RuntimeException(e);
		}

		credential = MongoCredential.createScramSha1Credential(username, dbname, password);

		return credential;
	}

	/**
	 * Method to get a MongoDb collection name from property file
	 * 
	 * @return A MongoDb collection name
	 */
	public static String getMongoChallengeCollName(String ApplicationRoot, String path) {

		String props;
		// Some over paranoid input validation never hurts.
		path = path.replaceAll("\\.", "").replaceAll("/", "");
		log.debug("Path = " + path);

		props = new File(Database.class.getResource("/challenges/" + path + ".properties").getFile()).getAbsolutePath();

		log.debug("Properties File: " + props);
		String dbCollectionName;
		try {
			dbCollectionName = FileInputProperties.readfile(props, "databaseCollection");
		} catch (IOException | PropertyNotFoundException e) {
			throw new RuntimeException(e);
		}

		return dbCollectionName;

	}

	/**
	 * Method to get a MongoDb Connection
	 * 
	 * @return A MongoDb Connection
	 */
	public static MongoClient getMongoDbConnection(String ApplicationRoot) {

		// Mongo DB URL from mongo.properties
		String props = Constants.MONGO_DB_PROP;

		// Properties file for mongodb
		String connectionHost = "";
		String connectionPort = "";
		String connectTimeout = "";
		String socketTimeout = "";
		String serverSelectionTimeout = "";

		try {

			connectionHost = FileInputProperties.readfile(props, "connectionHost");
			connectionPort = FileInputProperties.readfile(props, "connectionPort");
			connectTimeout = FileInputProperties.readfile(props, "connectTimeout");
			socketTimeout = FileInputProperties.readfile(props, "socketTimeout");
			serverSelectionTimeout = FileInputProperties.readfile(props, "serverSelectionTimeout");
		} catch (IOException | PropertyNotFoundException e) {
			throw new RuntimeException(e);
		}

		MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
		optionsBuilder.connectTimeout(Integer.parseInt(connectTimeout));
		optionsBuilder.socketTimeout(Integer.parseInt(socketTimeout));
		optionsBuilder.serverSelectionTimeout(Integer.parseInt(serverSelectionTimeout));
		MongoClientOptions mongoOptions = optionsBuilder.build();

		try (MongoClient mongoClient = new MongoClient(
				new ServerAddress(connectionHost, Integer.parseInt(connectionPort)), mongoOptions)) {

			log.debug("Mongo Client: " + mongoClient);
			return mongoClient;

		} catch (NumberFormatException e) {
			log.fatal("The port in the properties file is not a number: " + e);
			throw new RuntimeException(e);

		} catch (MongoSocketOpenException e) {
			log.fatal("Mongo Doesn't seem to be running: " + e);
			e.printStackTrace();
			throw new RuntimeException(e);

		} catch (MongoSocketException e) {
			log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
			throw new RuntimeException(e);

		} catch (MongoException e) {
			log.fatal("Something went wrong with Mongo: " + e);
			throw new RuntimeException(e);

		}

	}

	/**
	 * Method to get a MongoDb Connection
	 * 
	 * @param credential to connect to the MongoDB
	 * @return A MongoDb Connection
	 */
	public static MongoClient getMongoDbConnection(String ApplicationRoot, MongoCredential credential) {

		// Mongo DB URL out of mongo.properties
		String props = Constants.MONGO_DB_PROP;
		MongoClient mongoClient = null;

		// Properties file for all of mongo

		String connectionHost = "";
		String connectionPort = "";
		String connectTimeout = "";
		String socketTimeout = "";
		String serverSelectionTimeout = "";

		try {

			connectionHost = FileInputProperties.readfile(props, "connectionHost");
			connectionPort = FileInputProperties.readfile(props, "connectionPort");
			connectTimeout = FileInputProperties.readfile(props, "connectTimeout");
			socketTimeout = FileInputProperties.readfile(props, "socketTimeout");
			serverSelectionTimeout = FileInputProperties.readfile(props, "serverSelectionTimeout");
		} catch (IOException | PropertyNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		log.debug("Loaded mongo config from " + props);

		MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
		log.debug("Created builder. connectTimeout= " + connectTimeout);
		optionsBuilder.connectTimeout(Integer.parseInt(connectTimeout));
		log.debug("connectTimeout");
		optionsBuilder.socketTimeout(Integer.parseInt(socketTimeout));
		log.debug("socketTimeout");
		optionsBuilder.serverSelectionTimeout(Integer.parseInt(serverSelectionTimeout));
		log.debug("serverSelectionTimeout");
		MongoClientOptions mongoOptions = optionsBuilder.build();
		log.debug("Options created");


		try {
			mongoClient = new MongoClient(new ServerAddress(connectionHost, Integer.parseInt(connectionPort)),
					Arrays.asList(credential), mongoOptions);

			log.debug("Connection Host: " + connectionHost);
			log.debug("Connection Port: " + Integer.parseInt(connectionPort));
			log.debug("Connection Creds: " + Arrays.asList(credential));
		} catch (NumberFormatException e) {
			log.fatal("The port in the properties file is not a number: " + e);
			throw new RuntimeException(e);

		} catch (MongoSocketException e) {
			log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
			throw new RuntimeException(e);

		} catch (MongoTimeoutException e) {
			log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
			throw new RuntimeException(e);

		} catch (MongoException e) {
			log.fatal("Something went wrong with Mongo: " + e);
			e.printStackTrace();
			throw new RuntimeException(e);

		} catch (Exception e) {
			log.fatal("Something went wrong: " + e);
			e.printStackTrace();
			throw new RuntimeException(e);

		}

		log.debug("Mongo Client: " + mongoClient);

		return mongoClient;
	}

	/**
	 * Method to get a MongoDb Database
	 * 
	 * @param mongoClient mongoDb connection
	 * @return A MongoDb Database
	 */
	public static DB getMongoDatabase(MongoClient mongoClient) {
		String props = Constants.MONGO_DB_PROP;
		DB mongoDb = null;
		String dbname;
		
		try {
			dbname = FileInputProperties.readfile(props, "databaseName");
		} catch (IOException | PropertyNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		try {
			mongoDb = mongoClient.getDB(dbname);
		} catch (MongoSocketException e) {
			log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
		} catch (MongoTimeoutException e) {
			log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
		} catch (MongoException e) {
			log.fatal("Something went wrong with Mongo: " + e);
			e.printStackTrace();
		} catch (Exception e) {
			log.fatal("Something went wrong: " + e);
			e.printStackTrace();
		}

		return mongoDb;
	}

	/**
	 * Method to execute a mongo database JS file in a Mongo Database
	 * 
	 * @param file        the file to run
	 * @param mongoClient to get connection to db
	 */
	public static void executeMongoScript(File file, MongoClient mongoClient) throws IOException {
		String data = FileUtils.readFileToString(file, Charset.defaultCharset());

		DB db = MongoDatabase.getMongoDatabase(mongoClient);

		DBObject script = new BasicDBObject();
		script.put("eval", String.format(data));

		CommandResult result = db.command(script);

		log.debug("Mongo Result: " + result);
	}
}
