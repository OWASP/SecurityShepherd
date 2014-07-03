package servlets.admin.moduleManagement;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.ModulePlan;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class that modifies the server floor plan to a CTF/Incremental state. This is due a name change
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
public class SetTornyFloorPlan extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SetTornyFloorPlan.class);
	/**
	 * Called to change the status of the utils.ModulePlan class. Once this has been called by a valid administrator, the utils.ModulePlan will be changed.
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.SetTornyFloorPlan ***");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter) && ModulePlan.isOpenFloor())
			{
				ModulePlan.setTornyFloor();
				log.debug("Tournament Mode enabled");
				out.write("<h2 class='title'>Tournament Mode Enabled</h2>" +
				"<p>Security Shepherd User is now using the tournament floor plan</p>");
			}
			else
			{
				out.write("Error Occurred!");
			}
		}
		log.debug("*** END servlets.Admin.SetTornyFloorPlan ***");
	}
}