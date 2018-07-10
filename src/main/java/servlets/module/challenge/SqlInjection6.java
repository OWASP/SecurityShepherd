package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;
/**
 * Level : SQL Injection 6
 * <br><br>
 * 
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
public class SqlInjection6 extends HttpServlet
{
	private static final String levelName = "SQLi C6";
	// private static String levelSolution = "17f999a8b3fbfde54124d6e94b256a264652e5087b14622e1644c884f8a33f82";
	public static String levelHash = "d0e12e91dafdba4825b261ad5221aae15d28c36c7981222eb59f7fc8d8f212a2";
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjection6.class);
	/**
	 * This controller makes an insecure call to a MySQL interpreter. 
	 * User Input is first filtered for UTF-8 attacks and afterwards is decoded from \xHEX format to UTF-8 before sent to the interpreter
	 * 
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.sqli.sqli6", locale);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			String applicationRoot = getServletContext().getRealPath("");
			
			try
			{
				String userPin = (String) request.getParameter("pinNumber");
				log.debug("userPin - " + userPin);
				userPin = userPin.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", ""); // Escape single quotes
				log.debug("userPin scrubbed - " + userPin);
				userPin = java.net.URLDecoder.decode(userPin.replaceAll("\\\\\\\\x", "%"), "UTF-8"); //Decode \x encoding 
				log.debug("searchTerm decoded to - " + userPin);
				Connection conn = Database.getChallengeConnection(applicationRoot, "SqlChallengeSix");
				log.debug("Looking for users");
				PreparedStatement prepstmt = 
						conn.prepareStatement("SELECT userName FROM users WHERE userPin = '" + userPin + "'");
				ResultSet users = prepstmt.executeQuery();
				try
				{
					if(users.next())
					{
						htmlOutput = "<h3>" + bundle.getString("response.welcomeBack")+ "" + Encode.forHtml(users.getString(1)) + "</h3>"
								+ "<p>" + bundle.getString("response.authNumber")+ "" + Encode.forHtml(Hash.randomString()) + "</p>";
					}
					else
					{
						htmlOutput = "<h3>" + bundle.getString("response.incorrectCreds")+ "</h3><p>" + bundle.getString("response.carefulNow")+ "</p>";
					}
				}
				catch(Exception e)
				{
					htmlOutput = "<h3>" + bundle.getString("response.incorrectCreds")+ "</h3><p>" + bundle.getString("response.carefulNow")+ "</p>";
					log.debug("Could Not Find User: " + e.toString());
					try
					{
						Thread.sleep(1000);
					}
					catch(Exception e1)
					{
						log.error("Failed to Pause: " + e1.toString());
					}
				}
				conn.close();
			}
			catch(Exception e)
			{
				log.debug("Could not Search for User: " + e.toString());
				htmlOutput += "<p>" + bundle.getString("response.badRequest")+ "</p>";
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e2)
				{
					log.error("Failed to Pause: " + e2.toString());
				}
			}
			log.debug("*** SQLi C6 End ***");
			out.write(htmlOutput);
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}
