package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import dbProcs.Setter;
import utils.OpenRegistration;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class for the Registration process.
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
public class Register extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(Register.class);
	private static String defaultClass = new String();
	/**
	 * Initiated by register.jsp. If successful a player is added to the system, otherwise there is no change.
	 * Adding the player to the database is handled by the dbProcs.Setter class. Email is stored for future application expansion
	 * This function will request requests if the application's registration functionality has been marked as closed by administration.
	 * @param userName User's User Name
	 * @param passWord User's Password
	 * @param passWordConfirm Password Confirmation
	 * @param userAddress User's Email
	 * @param userAddressCnf User's Email Confirmation
	*/
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("**** servlets.Register ***");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		if(OpenRegistration.isEnabled())
		{
			HttpSession ses = request.getSession(true);
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
				
				log.debug("Ensuring not a CSRF");
				String paramToken = (String)request.getParameter("csrfToken");
				String sessToken = (String)ses.getAttribute("csrfToken");
				if(paramToken.compareTo(sessToken) == 0)
				{
					log.debug("Getting Registration Parameters");
					String userName = (String)request.getParameter("userName");
					log.debug("userName = " + userName);
					String passWord = (String)request.getParameter("passWord");
					log.debug("passWord retrieved");
					String passWordConfirm = (String)request.getParameter("passWordConfirm");
					log.debug("passWordConfirm retrieved");
					String userAddress = (String)request.getParameter("userAddress");
					log.debug("userAddress = " + userAddress);
					String userAddressCnf = (String)request.getParameter("userAddressCnf");
					log.debug("userAddressCnf = " + userAddressCnf);
					
					//Validation
					log.debug("Checking for nulls");
					notNull = (userName != null && passWord != null);
					log.debug("Ensuring strings are not empty");
					notEmpty = (!userName.isEmpty() && !passWord.isEmpty());
					log.debug("Validating passwords");
					validPasswords = passWord.compareTo(passWordConfirm) == 0; // 0 returned if the same
					log.debug("Validating addresses");
					validAddress = userAddress.compareTo(userAddressCnf) == 0;
					validAddress = (Validate.isValidEmailAddress(userAddress) && validAddress);
					if(!validAddress)
						userAddress = new String();
					boolean basicValidation = validPasswords && notNull && notEmpty;
					if(basicValidation && !validAddress)
						userValidate = (Validate.isValidUser(userName, passWord));
					else
						userValidate = (Validate.isValidUser(userName, passWord, userAddress));
					if(basicValidation && userValidate)
					{
						//Data is good, Add user
						//Any Class Set to Add them to?
						if(defaultClass.isEmpty())
						{
							log.debug("Adding player to database, with null classId");
							Setter.userCreate(ApplicationRoot, null, userName, passWord, "player", userAddress, false);
						}
						else //defaultClass is not empty, so It must be set to a class!
						{
							log.debug("Adding player to database, to class " + defaultClass);
							Setter.userCreate(ApplicationRoot, defaultClass, userName, passWord, "player", userAddress, false);
						}
						response.sendRedirect("login.jsp");
					}
					else
					{
						//Validation Error Responses
						if(!notNull || !notEmpty)
						{
							log.error("Null values detected");
							ses.setAttribute("errorMessage", "Invalid Request. Please try again");
						}
						else if(!validPasswords)
						{
							log.error("Passwords did not match");
							ses.setAttribute("errorMessage", "Password fields did not match");
						}
						else if(!validAddress)
						{
							log.error("Invalid Addresses Detected");
							ses.setAttribute("errorMessage", "Invalid Request. Please try again");
						}
						else if(!userValidate)
						{
							log.error("Invalid Addresses Detected");
							ses.setAttribute("errorMessage", "");
						}
						response.sendRedirect("register.jsp");
					}
				}
				else
				{
					log.debug("paramToken = " + paramToken);
					log.debug("sessToken = " + sessToken);
				}
			}
			catch (Exception e)
			{
				log.error("Registration Error: " + e.toString());
				ses.setAttribute("errorMessage", "An error Occurred. Please try again");
				response.sendRedirect("register.jsp");
			}
		}
		else
		{
			out.write("Registration is not open");
		}
		log.debug("*** Register END ***");
	}
	
	/**
	 * Redirects to index.jsp
	 */
	public void doGet (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		response.sendRedirect("index.jsp");
	}
	
	public static String getDefaultClass ()
	{
		return defaultClass;
	}
	
	public static void setDefaultClass (String newDefaultClass)
	{
		defaultClass = newDefaultClass;
	}
}
