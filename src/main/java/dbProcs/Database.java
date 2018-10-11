package dbProcs;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

/**  
 * Used to create database connections using the FileInputProperties.readfile method to gather property information
 * Initiated by Getter.java, Setter.java
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
public class Database 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(Database.class);
	/**
	 * This method is used by the application to close an open connection to a database server
	 * @param conn The connection to close
	 */
	public static void closeConnection(Connection conn)
	{
		try
		{
			//log.debug("Closing database connection");
			conn.close();
		}
		catch(Exception e)
		{
			log.error("Error closing connection:" + e.toString());
		}
	}
	
	/**
	 * This method is used by the application to get a connection to the secure database sever based on the input path to a specific properties file.
	 * @param ApplicationRoot The running context of the application.
	 * @param path The path to the properties file to use for this connection. this is filtered for path traversal attacks
	 * @return A connection to the secure database server
	 */
	public static Connection getChallengeConnection(String ApplicationRoot, String path)
	{
		//Some over paranoid input validation never hurts.
		path = path.replaceAll("\\.", "").replaceAll("/", "");
		log.debug("Path = " + path);
		Connection conn = null;
		try
		{
		   //Pull Driver and DB URL out of database.properties
		   String props = Constants.DBPROP;
		   
		   String DriverType = FileInputProperties.readfile(props, "DriverType");
		   Class.forName(DriverType).newInstance();
		   String connectionURL=FileInputProperties.readfile(props, "databaseConnectionURL");
		   
		   //Pull DB Schema, Schema User name and Schema Password from level specific properties File
		   
		   props = new File(Database.class.getResource("/challenges/" + path + ".properties").getFile()).getAbsolutePath();
		   log.debug("Level Properties File = " + path + ".properties");
		   //Add DB Schema to the end of the connectionURL
		   connectionURL= connectionURL + FileInputProperties.readfile(props, "databaseConnectionURL");
		   //log.debug("Connection URL: " + connectionURL);
		   String username=FileInputProperties.readfile(props, "databaseUsername");
		   String password=FileInputProperties.readfile(props, "databasePassword");
		   
		   conn = DriverManager.getConnection(connectionURL,username,password);
	   }
	   catch(Exception e)
	   {
		   log.fatal("Unable to create database connection: " + e);
		   e.printStackTrace();
	   }
	   return conn;
	}
	
	/**
	 * Returns connection to core schema in database
	 * @param ApplicationRoot
	 * @return Connection to core schema with admin privileges
	 */
	public static Connection getCoreConnection(String ApplicationRoot)
	{
	   Connection conn = null;
	   try
	   {
		   //log.debug("Getting Prop File");
		   //Pull Driver and DB URL out of database.properties
		   
		   String props = Constants.DBPROP;
		   
		   //log.debug("Getting Driver");
		   String DriverType = FileInputProperties.readfile(props, "DriverType");
		   //log.debug("Getting Driver Instance");
		   Class.forName(DriverType).newInstance();
		   String connectionURL=FileInputProperties.readfile(props, "databaseConnectionURL");
		   
		   //Pull Schema, User name and Password from SqlInjLesson.properties
		   
		   //log.debug("Reading Prop File");
		   connectionURL= connectionURL + FileInputProperties.readfile(props, "databaseSchema");
		   String username=FileInputProperties.readfile(props, "databaseUsername");
		   String password=FileInputProperties.readfile(props, "databasePassword");
		   
		   //log.debug("Creating Connection to DB");
		   conn = DriverManager.getConnection(connectionURL,username,password);
	   }
	   catch(Exception e)
	   {
		   log.fatal("Unable to create database connection: " + e);
		   e.printStackTrace();
	   }
	   return conn;
	}
	
	 public static Connection getDatabaseConnection(String ApplicationRoot) {
		 return getDatabaseConnection(ApplicationRoot, false);
	 }
	
	/**
	 * This method is used by the application to get a connection to the secure database sever
	 * @param ApplicationRoot The running context of the application.
	 * @return A connection to the secure database server
	 */
	public static Connection getDatabaseConnection(String ApplicationRoot, boolean allowMulti)
	{
	   Connection conn = null;
	   try
	   {
		   String props = Constants.DBPROP;
		  
		   String DriverType = FileInputProperties.readfile(props, "DriverType"); 
		   Class.forName(DriverType).newInstance();
		   
		   String connectionURL=FileInputProperties.readfile(props, "databaseConnectionURL");
		   if (allowMulti) {
			   connectionURL += "?allowMultiQueries=yes";
		   }
		   
		   String username=FileInputProperties.readfile(props, "databaseUsername");
		   String password=FileInputProperties.readfile(props, "databasePassword");
		   
		   conn = DriverManager.getConnection(connectionURL,username,password);
	   }
	   catch(Exception e)
	   {
		   log.fatal("Unable to create database connection: " + e);
	   }
	   return conn;
	}
	
	/**
	 * This method is used by the application to get a connection to the secure database sever's 
	 * SQL injection Lesson schema
	 * @param ApplicationRoot The running context of the application.
	 * @return A connection to the secure database server
	 */
	public static Connection getSqlInjLessonConnection(String ApplicationRoot)
	{
	   Connection conn = null;
	   try
	   {
		   //Pull Driver and DB URL out of database.properties
		   String props = Constants.DBPROP;
		   String DriverType = FileInputProperties.readfile(props, "DriverType");
		   Class.forName(DriverType).newInstance();
		   String connectionURL=FileInputProperties.readfile(props, "databaseConnectionURL");
		   
		   //Pull Schema, User name and Password from SqlInjLesson.properties
		   props = ApplicationRoot+"/WEB-INF/classes/lessons/SqlInjLesson.properties";
		   
		   connectionURL= connectionURL + FileInputProperties.readfile(props, "databaseConnectionURL");
		   String username=FileInputProperties.readfile(props, "databaseUsername");
		   String password=FileInputProperties.readfile(props, "databasePassword");
		   
		   conn = DriverManager.getConnection(connectionURL,username,password);
	   }
	   catch(Exception e)
	   {
		   log.fatal("Unable to create database connection: " + e);
		   e.printStackTrace();
	   }
	   return conn;
	}
}
