package servlets.challenges;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.Hash;

import dbProcs.Database;

/**
 * Session Management Challenge Two - Password Reset Servlet
 * Does not return result key
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
 * @author Mark Denihan
 *
 */
public class f5ddc0ed2d30e597ebacf5fdd117083674b19bb92ffc3499121b9e6a12c92959 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(f5ddc0ed2d30e597ebacf5fdd117083674b19bb92ffc3499121b9e6a12c92959.class);
	/**
	 * A user with the submitted email address is set a new random password, the password is also returned from the database procedure and is forwards through to the HTTP response.
	 * This response is not consumed by the client interface by default, and the user will have to discover it.
	 * @param subEmail Sub schema user email address
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		Encoder encoder = ESAPI.encoder();
		String htmlOutput = new String();
		log.debug("Broken Auth and Session Management Challenge Two - Reset Password -Servlet");
		try
		{
			log.debug("Getting Challenge Parameter");
			Object emailObj = request.getParameter("subEmail");
			String subEmail = new String();
			if(emailObj != null)
				subEmail = (String) emailObj;
			log.debug("subEmail = " + subEmail);
			
			log.debug("Getting ApplicationRoot");
			String ApplicationRoot = getServletContext().getRealPath("");
			log.debug("Servlet root = " + ApplicationRoot );
			
			String newPassword = Hash.randomString();
			try
			{
				Connection conn = Database.getChallengeConnection(ApplicationRoot, "BrokenAuthAndSessMangChalTwo");
				log.debug("Checking credentials");
				PreparedStatement callstmt = conn.prepareStatement("UPDATE users SET userPassword = SHA(?) WHERE userAddress = ?");
				callstmt.setString(1, newPassword);
				callstmt.setString(2, subEmail);
				log.debug("Executing resetPassword");
				callstmt.execute();
				log.debug("Statement executed");
				
				log.debug("Commiting changes made to database");
				callstmt = conn.prepareStatement("COMMIT");
				callstmt.execute();
				log.debug("Changes commited.");
				
				htmlOutput = encoder.encodeForHTML(newPassword);
				Database.closeConnection(conn);
			}
			catch(SQLException e)
			{
				log.error("SessMng2 SQL Error: " + e.toString());
			}
			log.debug("Outputing HTML");
			out.write("Changed to: " + htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occured! You must be getting funky!");
			log.fatal("Session Management Challenge Two - " + e.toString());
		}
	}
}
