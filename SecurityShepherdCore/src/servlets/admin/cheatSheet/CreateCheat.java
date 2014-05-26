package servlets.admin.cheatSheet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import dbProcs.Getter;
import dbProcs.Setter;
import utils.ExposedServer;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class for the Create Cheat sheet function.
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
public class CreateCheat extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(CreateCheat.class);
	
	/**
	 * This method validates input and then attempts to update the cheat sheet for the specified module
	 * @param newSolution The new solution to store as a cheat sheet
	 * @param moduleId[] The identifier of the module to update.
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.CreateCheat ***");
		Encoder encoder = ESAPI.encoder();
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				String errorMessage = null;
				String newSolution = request.getParameter("newSolution");
				log.debug("User submitted new solution - " + newSolution);
				String moduleId = request.getParameter("moduleId[]");
				log.debug("User submitted moduleId: " + moduleId);
				if(newSolution != null && !newSolution.isEmpty())
				{
					String ApplicationRoot = getServletContext().getRealPath("");
					String moduleCheck = Getter.getModuleResult(ApplicationRoot, moduleId);
					if(moduleCheck != null)
					{
						if(!Setter.updateCheatSheet(ApplicationRoot, moduleId, encoder.encodeForHTML(newSolution)))
							errorMessage = "A database level error occured. Please contact your administrator";
					}
					else
					{
						errorMessage = "Invalid Module submitted";
					}
				}
				else
				{
					errorMessage = "Invalid Module submitted";
				}
				String output = new String();
				if(errorMessage != null)
				{
					output = "<h2 class='title'>Create Cheat Sheet Failure</h2>" +
							"<p>" + encoder.encodeForHTML(errorMessage) + "</p>";
				}
				else
				{
					output = "<h2 class='title'>Create Cheat Sheet Success</h2>" +
					"<p>Cheat Sheet successfully created</p>";
				}
				out.write(output);
			}
		}
		else
		{
			out.write("<img src='" + encoder.encodeForHTMLAttribute(ExposedServer.getSecureUrl()) + "css/images/loggedOutSheep.jpg'/>");
		}
		log.debug("*** END servlets.Admin.CreateCheat ***");
	}
}
