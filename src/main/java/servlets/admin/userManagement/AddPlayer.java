package servlets.admin.userManagement;

import java.io.IOException;
import java.io.PrintWriter;

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
import dbProcs.Setter;
/**
 * The add players server side functionality is hanled by this control class
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
public class AddPlayer extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(AddPlayer.class);
	
	/** Initiated by addPlayers.jsp. Player is ether added to the system under a specific or null class or invalid data is detected and no player is added to the database
	 * Adding of player to Core Database is handed by Setter.createUser
	 * @param classId Class to add player to (ensure exists)
	 * @param userName User's User Name
	 * @param passWord 	User's Password
	 * @param passWordConfirm Password Confirmation
	 * @param userAddress User's Email
	 * @param userAddressCnf User's Email Confirmation
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.addPlayer ***");
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
				boolean validPasswords = false;
				boolean validAddress = false;
				boolean userValidate = false;
				String[] classInfo = new String[2];
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String classId = (String)request.getParameter("classId");
					log.debug("classId = " + classId);
					String userName = (String)request.getParameter("userName");
					log.debug("userName = " + userName);
					String passWord = (String)request.getParameter("passWord");
					log.debug("passWord retrieved");
					String passWordConfirm = (String)request.getParameter("passWordConfirm");
					log.debug("passWordConfirm retrieved");
					String userAddress = Validate.validateParameter(request.getParameter("userAddress"), 128);
					log.debug("userAddress = " + userAddress);
					String userAddressCnf = Validate.validateParameter(request.getParameter("userAddressCnf"), 128);
					log.debug("userAddressCnf = " + userAddressCnf);
					
					//Validation
					notNull = (userName != null && passWord != null);
					log.debug("Ensuring strings are not empty");
					notEmpty = (!userName.isEmpty() && !passWord.isEmpty());
					log.debug("Validating passwords");
					validPasswords = passWord.compareTo(passWordConfirm) == 0; // 0 returned if the same
					log.debug("Validating addresses");
					validAddress = userAddress.compareTo(userAddressCnf) == 0;
					if(userAddress.isEmpty())
						validAddress = true; //Null Addresses are ok
					else
						validAddress = (Validate.isValidEmailAddress(userAddress) && validAddress); //Malformed Addresses are Not ok
					
					boolean basicValidation = validPasswords && validAddress && notNull && notEmpty;
					if(basicValidation)
						userValidate = (Validate.isValidUser(userName, passWord, userAddress));
					if(userValidate)
					{
						classInfo = Getter.getClassInfo(ApplicationRoot, classId);
					}
					if(basicValidation && userValidate && classInfo != null)
					{
						String reponseMessage = new String();
						//Data is good, Add user
						log.debug("Adding player to class " + classInfo[0] + " " + classInfo[1]);
						if(Setter.userCreate(ApplicationRoot, classId, userName, passWord, "player", userAddress, true))
						{
							if(userAddress.isEmpty())
								userAddress = "blank"; //For Output Message
							reponseMessage = "<p>" +
								"Player <a>" + Encode.forHtml(userName) +"</a> added to <a>" + Encode.forHtml(classInfo[0] + " " + classInfo[1]) + "</a> created successfully." +
								"</p>";
						}
						else
						{
							reponseMessage = "<div id='error' class='informationBox'><p colour='red'><strong>" +
									Encode.forHtml(userName) + " was not added to the system due to an error." +
							"</strong></p></div>";
						}
						out.print(reponseMessage);
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occurred: ";
						if(!notNull || !notEmpty)
						{
							log.error("Null values detected");
							errorMessage += "Invalid Request. Please try again";
						}
						else if(!validPasswords)
						{
							log.error("Passwords did not match");
							errorMessage += "Password fields did not match";
						}
						else if(!validAddress)
						{
							log.error("Invalid Addresses Detected");
							errorMessage += "Invalid Addresses Detected";
						}
						else if(!userValidate)
						{
							log.error("JavaScript validation bypassed");
							errorMessage += "Invalid Request. Please try again";
						}
						else
						{
							log.debug("Class Not Found");
							errorMessage += "Class not found.";
						}
						out.print("<div id='error' class='informationBox'><p colour='red'><strong>" +
								Encode.forHtml(errorMessage) +
								"</strong></p></div>");
					}
				}
				catch (Exception e)
				{
					log.error("Create New Class Error: " + e.toString());
					out.print("<div id='error' class='informationBox'><p colour='red'><strong>" +
							"An error Occurred! Please try again." +
							"</strong></p></div>");
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<div id='error' class='informationBox'><p colour='red'><strong>" +
						"An error Occurred! CSRF Tokens did not match." +
						"</strong><p></div>");
			}
		}
		else
		{
			out.print(
					"<div id='error' class='informationBox'><p colour='red'><strong>" +
					"An error Occurred! Please try non administrator functions!" +
					"</strong></p></div>");
		}
		log.debug("*** addPlayer END ***");
	}

}
