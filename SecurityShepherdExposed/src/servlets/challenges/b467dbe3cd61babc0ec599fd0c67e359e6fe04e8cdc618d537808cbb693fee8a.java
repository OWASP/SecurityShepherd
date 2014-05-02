package servlets.challenges;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.commons.codec.binary.Base64;

import dbProcs.Database;

/**
 * Session Management Challenge Three - Change Password
 * This is a level function - DOES NOT RETURN KEY
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
public class b467dbe3cd61babc0ec599fd0c67e359e6fe04e8cdc618d537808cbb693fee8a extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(t193c6634f049bcf65cdcac72269eeac25dbb2a6887bdb38873e57d0ef447bc3.class);

	/**
	 * Function used by Session Managment Challenge Three to change the password of the submitted user name specified in the "Current" cookie
	 * @param current User cookie used to store the current user (encoded twice with base64)
	 * @param newPassword the password which to use to update an accounts password
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();
		Base64 base64 = new Base64();
		out.print(getServletInfo());
		String htmlOutput = new String();
		log.debug("Broken Auth and Session Management Challenge Three - Change Password - Servlet");
		try
		{
			log.debug("Getting Challenge Parameters");
			Cookie userCookies[] = request.getCookies();
			int i = 0;
			Cookie theCookie = null;
			for(i = 0; i < userCookies.length; i++)
			{
				if(userCookies[i].getName().compareTo("current") == 0)
				{
					theCookie = userCookies[i];
					break; //End Loop, because we found the token
				}
			}
			Object passNewObj = request.getParameter("newPassword");
			String subName = new String();
			String subNewPass = new String();
			if(theCookie != null)
				subName = theCookie.getValue();
			if(passNewObj != null)
				subNewPass = (String) passNewObj;
			log.debug("subName = " + subName);
			subName = base64.decode(base64.decode(subName).toString()).toString();
			log.debug("subName Decoded = " + subName);
			log.debug("subPass = " + subNewPass);
			
			if(subNewPass.length() >= 6)
			{
				log.debug("Getting ApplicationRoot");
				String ApplicationRoot = getServletContext().getRealPath("");
				log.debug("Servlet root = " + ApplicationRoot );
				
				Connection conn = Database.getChallengeConnection(ApplicationRoot, "BrokenAuthAndSessMangChalThree");
				log.debug("Changing password for user: " + subName);
				log.debug("Changing password to: " + subNewPass);
				PreparedStatement callstmt;
				
				callstmt = conn.prepareStatement("UPDATE users SET userPassword = SHA(?) WHERE userName = ?");
				callstmt.setString(1, subNewPass);
				callstmt.setString(2, subName);
				log.debug("Executing changePassword");
				callstmt.execute();
				
				log.debug("Commiting changes made to database");
				callstmt = conn.prepareStatement("COMMIT");
				callstmt.execute();
				log.debug("Changes commited.");
				
				htmlOutput = "<p>Password change request success.</p>";
			}
			else
			{
				log.debug("invalid password submited: " + subNewPass);
				htmlOutput = "<p>Change Password Failed.</p>";
			}
			log.debug("Outputing HTML");
			out.write(htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occured! You must be getting funky!");
			log.fatal("Session Management Challenge Three - Change Password - " + e.toString());
		}
	}
	
}
