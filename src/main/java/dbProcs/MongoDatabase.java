package dbProcs;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import org.apache.log4j.Logger;

import java.io.File;
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

        props = new File(Database.class.getResource("/challenges/" + path + ".properties").getFile()).getAbsolutePath();
        log.debug("Level Properties File = " + path + ".properties");

        String username = FileInputProperties.readfile(props, "databaseUsername");
        log.debug("Connecting to DB with: " + username);
        String password = FileInputProperties.readfile(props, "databasePassword");
        String dbname = FileInputProperties.readfile(props, "databaseName");
        String dbCollectionName = FileInputProperties.readfile(props, "databaseCollection");
        log.debug("Mongo db & collection = " + dbname + " " + dbCollectionName);

        credential = MongoCredential.createScramSha1Credential(username, dbname, password.toCharArray());

        return credential;
    }

    /**
     * Method to get a MongoDb collection name from property file
     * @return A MongoDb collection name
     */
    public static String getMongoChallengeCollName(String ApplicationRoot, String path){
        //Some over paranoid input validation never hurts.
        path = path.replaceAll("\\.", "").replaceAll("/", "");
        log.debug("Path = " + path);

        String props;

        props = new File(Database.class.getResource("/challenges/" + path + ".properties").getFile()).getAbsolutePath();

        String dbCollectionName = FileInputProperties.readfile(props, "databaseCollection");

        return  dbCollectionName;

    }


    /**
     * Method to get a MongoDb Connection
     * @return A MongoDb Connection
     */
    public static MongoClient getMongoDbConnection(String ApplicationRoot){

        //Mongo DB URL out of mongo.properties
        String props = Constants.MONGO_DB_PROP;
        MongoClient mongoClient = null;

        // Properties file for all of mongo
        String connectionHost = FileInputProperties.readfile(props, "connectionHost");
        String connectionPort = FileInputProperties.readfile(props, "connectionPort");

        try
        {
            mongoClient = new MongoClient(new ServerAddress(connectionHost, Integer.parseInt(connectionPort)));
        }
        catch (MongoException e){
            log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
            e.printStackTrace();
            closeConnection(mongoClient);
        }
        catch (Exception e){
            log.fatal("Something went wrong with Mongo: " + e);
            e.printStackTrace();
            closeConnection(mongoClient);
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
        catch (MongoException e){
            log.fatal("Unable to get Mongodb connection (Is it on?): " + e);
            e.printStackTrace();
            closeConnection(mongoClient);
        }
        catch (Exception e){
            log.fatal("Something went wrong with Mongo: " + e);
            e.printStackTrace();
            closeConnection(mongoClient);
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
}
