package servlets.mobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import sun.org.mozilla.javascript.internal.json.JsonParser;
import utils.GetJson;
import utils.Hash;

/**
 * Template Servlet for Mobile Levels
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
public class templateServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(templateServlet.class);
	private static String levelName = "Example Challenge"; //Used for Logging
	/**
	 * Describe level here
	 * @param paramName Put the JSON parameters you are expecting here
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Logging Servlet access
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(levelName + " servlet accessed");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		String jsonOutput = new String();
		try
		{
			log.debug("Collecting JSON From request.");
			JSONArray jsonArray = GetJson.getJssonArrayFromPost(request.getReader());
			log.debug("JsonArray = " + jsonArray.toString());
			// DO stuff with array
			
			//Make new array and put it in jsonOutput
			out.write(jsonOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occurred! You must be getting funky!");
			log.fatal("Session management lesson - " + e.toString());
		}
	}
}
