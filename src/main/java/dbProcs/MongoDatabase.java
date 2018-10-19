package dbProcs;

import com.mongodb.*;
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
     * This method is used by the application to close an open MongoDb connection
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

    public static DBCollection getMongoChallengeConnection(String ApplicationRoot, String path)
    {

        //Some over paranoid input validation never hurts.
        path = path.replaceAll("\\.", "").replaceAll("/", "");
        log.debug("Path = " + path);

        //Mongo DB URL out of mongo.properties
        String props = Constants.MONGO_DB_PROP;

        MongoClient mongoClient = null;
        MongoCredential credential;
        DB mongoDb;
        DBCollection dbCollection = null;

        // Properties file for all of mongo
        String connectionHost = FileInputProperties.readfile(props, "connectionHost");
        String connectionPort = FileInputProperties.readfile(props, "connectionPort");
        log.debug("Connection URI = " + connectionHost + ":" + connectionPort);

        //Pull info from level specific properties File
        props = new File(Database.class.getResource("/challenges/" + path + ".properties").getFile()).getAbsolutePath();
        log.debug("Level Properties File = " + path + ".properties");

        String username = FileInputProperties.readfile(props, "databaseUsername");
        //char[] password = FileInputProperties.readfile(props, databasePassword);
        String password = FileInputProperties.readfile(props, "databasePassword");
        String dbname = FileInputProperties.readfile(props, "databaseName");
        String dbCollectionName = FileInputProperties.readfile(props, "databaseCollection");

        log.debug("Mongo db & collection = " + dbname + " " + dbCollectionName);

        credential = MongoCredential.createCredential(username, dbname, password.toCharArray());

        try
        {
            mongoClient = new MongoClient(new ServerAddress(connectionHost, Integer.parseInt(connectionPort)),
                    Arrays.asList(credential));
            mongoDb = mongoClient.getDB(dbname);
            dbCollection = mongoDb.getCollection(dbCollectionName);
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

        return dbCollection;
    }


    public static MongoClient getMongoDbConnection(String ApplicaitonRoot){

        //Mongo DB URL out of mongo.properties
        String props = Constants.MONGO_DB_PROP;
        MongoClient mongoClient = null;

        // Properties file for all of mongo
        String connectionHost = FileInputProperties.readfile(props, "connectionHost");
        String connectionPort = FileInputProperties.readfile(props, "connectionPort");
        String databaseName = FileInputProperties.readfile(props, "databaseName");

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

}
