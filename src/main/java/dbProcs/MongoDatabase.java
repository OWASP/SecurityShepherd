package dbProcs;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoException;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Used to create MongoDb connections
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
 *  @author Paul
 */

public class MongoDatabase {

    private static org.apache.log4j.Logger log = Logger.getLogger(MongoDatabase.class);

    /**
     * Method to close a MongoDb connection
     * @param conn The connection to close
     */
    public static void closeConnection(MongoClient conn)
    {
        try
        {
            conn.close();
        }
        catch(Exception e)
        {
            log.error("Error closing connection:" + e.toString());
        }
    }

    /**
     * Method to get a MongoDb Challenge collection
     * @param ApplicationRoot The running context of the application.
     * @param path The path to the properties file to use for this connection (filtered for path traversal attacks)
     * @return A MongoDb Collection
     */
    public static MongoCredential getMongoChallengeCredentials(String ApplicationRoot, String path)
    {
        //Some over paranoid input validation never hurts.
        path = path.replaceAll("\\.", "").replaceAll("/", "");
        log.debug("Path = " + path);

        String props;
        MongoCredential credential;

        props = new File(MongoDatabase.class.getResource("/challenges/" + path + ".properties").getFile()).getAbsolutePath();
        log.debug("Level Properties File = " + path + ".properties");

        String username = FileInputProperties.readfile(props, "databaseUsername");
        log.debug("Username for Mongo level: " + username);
        char[] password = FileInputProperties.readfile(props, "databasePassword").toCharArray();
        log.debug("Password for Mongo level read");
        String dbname = FileInputProperties.readfile(props, "databaseName");
        log.debug("Mongo database name: " + dbname);

        credential = MongoCredential.createScramSha1Credential(username, dbname, password);

        return credential;
    }

    /**
     * Method to get a MongoDb collection name from property file
     * @return A MongoDb collection name
     */
    public static String getMongoChallengeCollName(String ApplicationRoot, String path){

        String props;
        //Some over paranoid input validation never hurts.
        path = path.replaceAll("\\.", "").replaceAll("/", "");
        log.debug("Path = " + path);

        props = new File(Database.class.getResource("/challenges/" + path + ".properties").getFile()).getAbsolutePath();

        log.debug(props);
        String dbCollectionName = FileInputProperties.readfile(props, "databaseCollection");

        return  dbCollectionName;

    }

    /**
     * Method to get a MongoDb Connection
     * @return A MongoDb Connection
     */

    public static MongoClient getMongoDbConnection(String ApplicationRoot){

        //Mongo DB URL from mongo.properties
        String props = Constants.MONGO_DB_PROP;
        MongoClient mongoClient = null;

        // Properties file for mongodb
        String connectionHost = FileInputProperties.readfile(props, "connectionHost");
        String connectionPort = FileInputProperties.readfile(props, "connectionPort");

        try
        {
            mongoClient = new MongoClient(new ServerAddress(connectionHost, Integer.parseInt(connectionPort)));
        }
        catch (NumberFormatException e){ log.fatal("The port in the properties file is not a number: " + e); }
        catch (MongoSocketException e) { log.fatal("Unable to get Mongodb connection (Is it on?): " + e); }
        catch (MongoException e){
            log.fatal("Something went wrong with Mongo: " + e);
            e.printStackTrace();
        }
        catch (Exception e){
            log.fatal("Something went wrong: " + e);
            e.printStackTrace();
        }

        return mongoClient;
    }

    /**
     * Method to get a MongoDb Connection
     * @param credential to connect to the MongoDB
     * @return A MongoDb Connection
     */
    public static MongoClient getMongoDbConnection(String ApplicationRoot, MongoCredential credential){

        //Mongo DB URL out of mongo.properties
        String props = Constants.MONGO_DB_PROP;
        MongoClient mongoClient = null;

        // Properties file for all of mongo
        String connectionHost = FileInputProperties.readfile(props, "connectionHost");
        String connectionPort = FileInputProperties.readfile(props, "connectionPort");

        try
        {
            mongoClient = new MongoClient(new ServerAddress(connectionHost, Integer.parseInt(connectionPort)),
                    Arrays.asList(credential));
        }
        catch (MongoSocketException e)
        {
            log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
            e.printStackTrace();
        }
        catch (MongoException e){
            log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
            e.printStackTrace();
        }
        catch (Exception e){
            log.fatal("Something went wrong with Mongo: " + e);
            e.printStackTrace();
        }

        return mongoClient;
    }

    /**
     * Method to get a MongoDb Database
     * @param mongoClient mongoDb connection
     * @return A MongoDb Database
     */
    public static DB getMongoDatabase(MongoClient mongoClient)
    {
        String props = Constants.MONGO_DB_PROP;
        DB mongoDb;
        String dbname = FileInputProperties.readfile(props, "databaseName");
        mongoDb = mongoClient.getDB(dbname);

        return mongoDb;
    }

    /**
     * Method to execute a mongo database JS file in a Mongo Database
     * @param file the file to run
     * @param mongoClient to get connection to db
     */
    public static void executeMongoScript(File file, MongoClient mongoClient) throws IOException
    {
        String data = FileUtils.readFileToString(file, Charset.defaultCharset() );

        DB db = MongoDatabase.getMongoDatabase(mongoClient);

        DBObject script = new BasicDBObject();
        script.put("eval", String.format(data));

        CommandResult result = db.command(script);

        log.debug("Mongo Result: " + result);
    }
}
