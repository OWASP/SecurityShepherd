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
 * Control class of the Downgrade player to admin functionality
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
 *
 */
public class DowngradeAdmin extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DowngradeAdmin.class);
	/**
	 * Initiated by downgradeAdmins.jsp. This method downgrades a single admin to player at a time. Changing the actual role of the admin is handed by Setter.updateUserRole.
	 * @param classId
	 * @param players	
	 * @param csrfToken
	 */	
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.DowngradeAdmin ***");
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
				boolean validPlayer = false;
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String admin = (String)request.getParameter("admin");;
					log.debug("admin = " + admin.toString());
					
					
					
					//Validation
					notNull = (admin != null);
					if(notNull)
					{
							validPlayer = Getter.findAdminById(ApplicationRoot, admin);
					}
					if(notNull && validPlayer)
					{
						//Data is good, Add user
						log.debug("Updating Admin Role");
						String reponseMessage = new String();
						String userName = new String();
						userName = Setter.updateUserRole(ApplicationRoot, admin, "player");
						if(userName != null)
						{
							reponseMessage += "<a>" + Encode.forHtml(userName) + "</a> downgraded successfully to player.<br>";
						}
						else
						{
							reponseMessage += "<font color='red'>User could not be updated. Please try again.</font><br/>";
						}
						out.print("<h3 class=\"title\">Admin Downgrade Result</h3>" +
								"<p>" +
								reponseMessage +
								"<p>");
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occurred: ";
						if(!notNull)
						{
							log.error("Null values detected");
							errorMessage += "Invalid Request. Please try again";
						}
						else if(!validPlayer)
						{
							log.error("Admin not found");
							errorMessage += "Admin(s) Not Found. Please try again";
						}
						out.print("<h3 class=\"title\">Admin Downgrade Failure</h3>" +
								"<p><font color=\"red\">" +
								Encode.forHtml(errorMessage) +
								"</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Assign Players Error: " + e.toString());
					out.print("<h3 class=\"title\">Admin Downgrade Failure</h3>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("<h3 class=\"title\">Admin Downgrade Failure</h3>" +
						"<p>" +
						"<font color=\"red\">CSRF Tokens Did Not Match. Function Aborted.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h3 class=\"title\">Admin Downgrade Failure</h3>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** DowngradeAdmin END ***");
	}
}