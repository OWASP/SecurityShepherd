package dbProcs;

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
 *  @author Mark Denihan		
 */
public class Database 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(Database.class);
	/**
	 * This method is used by the application to get a connection to the vulnerable database sever
	 * @param ApplicationRoot The running context of the application.
	 * @return A connection to the secure database server
	 */
	public static Connection getConnection(String ApplicationRoot)
	{
	   Connection conn = null;
	   try
	   {
		   String props = ApplicationRoot+"/WEB-INF/site.properties";
		  
		   String DriverType = FileInputProperties.readfile(props, "DriverType"); 
		   Class.forName(DriverType).newInstance();
		   
		   String connectionURL=FileInputProperties.readfile(props, "databaseConnectionURL");
		   
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
	 * This method is used by the application to get a connection to the secure database sever's SQL injection Lesson schema
	 * @param ApplicationRoot The running context of the application.
	 * @return A connection to the secure database server
	 */
	public static Connection getSqlInjLessonConnection(String ApplicationRoot)
	{
	   Connection conn = null;
	   try
	   {
		   String props = ApplicationRoot+"/WEB-INF/lessons/SqlInjLesson.properties";
		  
		   String DriverType = FileInputProperties.readfile(props, "DriverType"); 
		   Class.forName(DriverType).newInstance();
		   
		   String connectionURL=FileInputProperties.readfile(props, "databaseConnectionURL");
		   
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
	 * This method is used by the application to get a connection to the secure database sever based on the inputted path to a specific properties file.
	 * @param ApplicationRoot The running context of the application.
	 * @param path The path to the properties file to use for this connection. this is filtered for path traversal attacks
	 * @return A connection to the secure database server
	 */
	public static Connection getChallengeConnection(String ApplicationRoot, String path)
	{
		path = path.replaceAll("\\.", "").replaceAll("/", "");
	   log.debug("Path = " + path);
	   Connection conn = null;
	   try
	   {
		   String props = ApplicationRoot+"/WEB-INF/challenges/" + path + ".properties";
		  
		   log.debug("property file = " + props);
		   
		   String DriverType = FileInputProperties.readfile(props, "DriverType"); 
		   Class.forName(DriverType).newInstance();
		   
		   String connectionURL=FileInputProperties.readfile(props, "databaseConnectionURL");
		   
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
	 * Closes an open database connection
	 * @param conn Connection to vulnerable database
	 */
	public static void closeConnection(Connection conn)
	{
		try
		{
			log.debug("Closing database connection");
			conn.close();
		}
		catch(Exception e)
		{
			log.error("Error closing connection:" + e.toString());
		}
	}
	
}
