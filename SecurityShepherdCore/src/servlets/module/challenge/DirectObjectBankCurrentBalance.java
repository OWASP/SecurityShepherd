package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Insecure Direct Object Reference Bank Challenge Get Balance Function
 * DOES NOT RETURN RESULT KEY
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
public class DirectObjectBankCurrentBalance extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DirectObjectBankCurrentBalance.class);
	private static String levelName = "Insecure Direct Object Bank Challenge (Refresh Balance)";
	public static String levelHash = "1f0935baec6ba69d79cfb2eba5fdfa6ac5d77fadee08585eb98b130ec524d00c";
	/**
	 * This Servlet is used by users to register a new bank account in the Insecure Direct Object Bank Challenge. 
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.directObject.directObjectBank", locale);
		
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();
			out.print(getServletInfo());
			try
			{
				String accountNumber = request.getParameter("accountNumber");
				log.debug("Account Number - " + accountNumber);
				String applicationRoot = getServletContext().getRealPath("");
				String htmlOutput = new String();
				float currentBalance = DirectObjectBankLogin.getAccountBalance(accountNumber, applicationRoot);
				log.debug("Outputting HTML");
				htmlOutput = Float.toString(currentBalance);
				out.write(htmlOutput);
			}
			catch(SQLException e)
			{
				out.write(errors.getString("error.funky") + " " + bundle.getString("login.error.couldNotGetBalance"));
				log.fatal(levelName + " SQL Error - " + e.toString());
			}
			catch(Exception e)
			{
				out.write(errors.getString("error.funky"));
				log.fatal(levelName + " - " + e.toString());
			}
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}
