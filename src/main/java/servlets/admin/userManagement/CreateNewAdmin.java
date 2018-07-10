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
import dbProcs.Setter;

/**
 * The control class of the Create New Administrator functionality.
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
public class CreateNewAdmin extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(CreateNewAdmin.class);
	/**
	 * Initiated by createNewAdmin.jsp. This method adds administrators to the system if valid data is submitted, otherwise no admin will be added to the database.
	 * Email is gathered for future functionality. Adding of the player to the core database is handed by Setter.playerCreate
	 * @param userName User's User Name
	 * @param passWord User's Password
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
		log.debug("*** servlets.Admin.createNewAdmin ***");
		
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
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
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
					log.debug("String Casting should have set off any null alarms... Checking again anyway");
					notNull = (userName != null && passWord != null);
					log.debug("Ensuring strings are not empty");
					notEmpty = (!userName.isEmpty() && !passWord.isEmpty());
					log.debug("Validating passwords");
					validPasswords = passWord.compareTo(passWordConfirm) == 0; // 0 returned if the same
					log.debug("Validating addresses");
					if(userAddress.isEmpty())
						validAddress = true;
					else
					{
						validAddress = userAddress.compareTo(userAddressCnf) == 0;
						validAddress = (Validate.isValidEmailAddress(userAddress) && validAddress);
					}
					boolean basicValidation = validPasswords && validAddress && notNull && notEmpty;
					if(basicValidation)
						userValidate = (Validate.isValidUser(userName, passWord, userAddress));
					if(basicValidation && userValidate)
					{
						//Data is good, Add user
						log.debug("Adding player to database, with null classId");
						Setter.userCreate(ApplicationRoot, null, userName, passWord, "admin", userAddress, true);
						if(userAddress.isEmpty())
							userAddress = "blank"; //For Output Message
						String reponseMessage = "<a>" + Encode.forHtml(userName) +"</a> created successfully with email address " + Encode.forHtml(userAddress);
						out.print("<h3 class=\"title\">Admin Created</h3>" +
								"<p>" +
								reponseMessage +
								"<p>");
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
						out.print("<h3 class=\"title\">Admin Create Failure</h3>" +
								"<p><font color=\"red\">" +
								Encode.forHtml(errorMessage) +
								"</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Create New Admin Error: " + e.toString());
					out.print("<h3 class=\"title\">Admin Create Failure</h3>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h3 class=\"title\">Admin Create Failure</h3>" +
						"<p>" +
						"<font color=\"red\">An error Occurred! CSRF Tokens did not match.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h3 class=\"title\">Admin Create Failure</h3>" +
				"<p>" +
				"<font color=\"red\">An error Occurred! Please try non administrator functions!</font>" +
				"<p>");
		}
		log.debug("*** createNewAdmin END ***");
	}
}
