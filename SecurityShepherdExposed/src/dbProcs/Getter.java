package dbProcs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

/** 
 * Used to retrieve information from the Database
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
 *
 * @author Mark Denihan
 */
public class Getter 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(Getter.class);
	
	/**
	 * Method used to handle the SQL injection lesson database functionality. Calls are made in a manner vulnerable to SQL injection.
	 * @param ApplicationRoot The current running context of the vulnerable application server
	 * @param username The user defined input been used to filter the results from the database query
	 * @return A string array of results to be consumed by the Controller of the the SQL injection lesson.
	 */
	public static String[][] getSqlInjectionResult (String ApplicationRoot, String username)
	{
		Encoder encoder = ESAPI.encoder();
		String[][] result = new String[10][3];
		try 
		{
			Connection conn = Database.getSqlInjLessonConnection(ApplicationRoot);
			Statement stmt;
			stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM tb_users WHERE username = '" + username + "'");
			log.debug("Opening Result Set from query");
			for(int i = 0; resultSet.next(); i++)
			{
				log.debug("Row " + i + ": User ID = " + resultSet.getString(1));
				result[i][0] = encoder.encodeForHTML(resultSet.getString(1));
				result[i][1] = encoder.encodeForHTML(resultSet.getString(2));
				result[i][2] = encoder.encodeForHTML(resultSet.getString(3));
			}
			log.debug("Thats All");
		} 
		catch (SQLException e)
		{
			log.debug("SQL Error caught - " + e.toString());
			result[0][0] = "error";
			result[0][1] = encoder.encodeForHTML(e.toString());
		}
		catch (Exception e)
		{
			log.fatal("Error: " + e.toString());
		}
		return result;
	}
}
