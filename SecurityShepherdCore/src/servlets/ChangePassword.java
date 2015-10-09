package servlets;

import java.io.IOException;
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import dbProcs.Getter;
import dbProcs.Setter;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class for the Change Password function
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
public class ChangePassword extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangePassword.class);
	/** 
	 * Initiated by index.jsp, getStarted.jsp. This changes a users password. If the user gets it wrong 3 times in a row, they'll be locked out (This is handed by database)
	 * @param csrfToken
	 * @param currentPassword User's current password
	 * @param newPassword Submitted new password
	 * @param passwordConfirmation	Confirmation of the new password
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.ChangePassword ***");
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug("Current User: " + ses.getAttribute("userName").toString());
				Cookie tokenCookie = Validate.getToken(request.getCookies());
				Object tokenParmeter = request.getParameter("csrfToken");
				if(Validate.validateTokens(tokenCookie, tokenParmeter))
				{
					log.debug("Getting Parameters");
					String userName = (String) ses.getAttribute("userName");
					String currentPassword = (String) request.getParameter("currentPassword");
					String newPassword = (String) request.getParameter("newPassword");
					String passwordConfirm = (String) request.getParameter("passwordConfirmation");
					String ApplicationRoot = getServletContext().getRealPath("");
					
					boolean validData = false;
					boolean passwordChange = false;
					boolean validPassword = false;
					validData = newPassword.equalsIgnoreCase(passwordConfirm) && !newPassword.isEmpty() && newPassword != null;
					passwordChange = !currentPassword.equalsIgnoreCase(newPassword);
					validPassword = newPassword.length() > 4 && newPassword.length() <= 512;
					if(validData && passwordChange && validPassword)
					{
						log.debug("Validating Current Password");
						String user[] = Getter.authUser(ApplicationRoot, userName, currentPassword);
						if(user != null)
						{
							log.debug("User Credentials were good! Password Change gets the go ahead");
							Setter.updatePassword(ApplicationRoot, userName, currentPassword, newPassword);
							ses.setAttribute("ChangePassword", "false");
						}
						else
						{
							log.error("Incorrect Password");
							ses.setAttribute("errorMessage", "Incorrect Password... Don't lock yourself out!");
							response.sendRedirect("index.jsp");
						}
					}
					else
					{
						if(validData && passwordChange)
						{
							try
							{
								//User Account is Locked
								log.debug("The user account is locked. Logging the user out");
								Cookie cookieToken = Validate.getToken(request.getCookies());
								BigInteger temp = new BigInteger(cookieToken.getValue());
								response.sendRedirect("logout?csrfToken="+temp);
							}
							catch (Exception e)
							{
								log.error("Cant Log the user out because they dont have a valid CSRF token : " + e.toString());
								response.sendRedirect("login.jsp");
							}
						}
						//Return error message
						else if(!validData)
						{
							log.error("Bad Data Received");
							ses.setAttribute("errorMessage", "Invalid Request! Please try again.");
						}
						else if(!validPassword)
						{
							log.error("Invalid Password Submitted (Too Short/Long)");
							ses.setAttribute("errorMessage", "Invalid Password! Please try again.");
						}
						else
						{
							log.error("No password Change Detected");
							ses.setAttribute("errorMessage", "You have to CHANGE your password! Please try again.");
						}
					}
				}
				else
				{
					log.error("CSRF Attack Detected");
				}
			}
			else
			{
				log.error("Change Password Function Called with no valid session");
				response.sendRedirect("login.jsp");
			}
		}
		catch(Exception e)
		{
			log.fatal("ChangePassword Error: " + e.toString());
		}
		log.debug("*** END ChangePassword ***");
		response.sendRedirect("index.jsp");
	}
}
