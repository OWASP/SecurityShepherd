package servlets.admin.userManagement;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Getter;

/**
 * This class is used by View classes to generate class displays that change based on user input.
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
 */
public class GetPlayersByClass extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GetPlayersByClass.class);
	
	/**
	 * Initiated by assignPlayers.jsp. This servlet returns options for inside html select
	 * @param classId theClass in which users must be found
	 * @param csrfToken User's CSRF Token	
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.GetPlayersByClass ***");
	
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		Cookie tokenCookie = Validate.getToken(request.getCookies());
		Object tokenParmeter = request.getParameter("csrfToken");
		if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				boolean notNull = false;
				boolean notEmpty = false;
				String[] classInfo = new String[2];
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String classId = (String)request.getParameter("classId");
					log.debug("classId = '" + classId + "'");
					
					//Validation
					log.debug("Ensuring not empty");
					if(classId != null)
					{
						log.debug("classId was not null");
						notEmpty = (!classId.isEmpty());
					}
					if(notEmpty && classId != null)
					{
						log.debug("classId was not empty");
						classInfo = Getter.getClassInfo(ApplicationRoot, classId);
						if(classInfo == null)
						{
							classId = null;
							log.debug("classInfo was not returned, nulling classId");
						}
					}
					else
					{
						log.debug("classId was empty, nulling");
						classId = null;
					}
					if(classId == null || classInfo != null)
					{
						ResultSet playerList = Getter.getPlayersByClass(ApplicationRoot, classId);
						String players = playersInOptionTags(playerList);
						out.print(players);
					}
					else
					{
						//Validation Error Responses
						if(!notNull || !notEmpty)
						{
							log.error("Null values detected");
						}
						out.print("fail");
					}
				}
				catch (Exception e)
				{
					log.error("Get Players by Class Error: " + e.toString());
					out.print("fail");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("fail");
			}
		}
		else
		{
			log.debug("Not an Administrator!");
			out.print("fail");
		}
		log.debug("*** GetPlayersByClass END ***");
	}

	public static String playersInOptionTags(ResultSet playerList) 
	{
		String players = new String();
		
		log.debug("Iterating through playerList");
		try
		{
			playerList.beforeFirst();
			while(playerList.next())
			{
				
				players +=
					"<option value=\"" + Encode.forHtmlAttribute(playerList.getString(1)) + "\">" +
					Encode.forHtml(playerList.getString(2)) +
					"</option>";
				log.debug("Adding " + playerList.getString(2) + " to output");
			}
		}
		catch(SQLException e1)
		{
			log.error("Error Occurred when handling playerList ResultSet");
		}
		log.debug("Returning: " + players);
		return players;
	}

}
