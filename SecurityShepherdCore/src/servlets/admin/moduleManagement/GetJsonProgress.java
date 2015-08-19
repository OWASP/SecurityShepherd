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

import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Getter;

/**
 * This control class is responsible for the server operation in the Get Progress use case where the result of which is a JSON object of JSON arrays. This is used to create a scoreboard environment that updates in real time.
 * This Servlet should be poled regularly to achieve this.
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
public class GetJsonProgress extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GetFeedback.class);
	/**
	 * Used to return an administrator with the current progress of each player in a class.
	 * This will require a complex client page to parse the returned JSON information to make a very pretty score board
	 * @param classId
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.GetJsonProgress ***");
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
				String classId = Validate.validateParameter(request.getParameter("classId"), 64);
				log.debug("classId: " + classId);
				String ApplicationRoot = getServletContext().getRealPath("");
				String jsonOutput = Getter.getProgressJSON(ApplicationRoot, classId);
				if(jsonOutput.isEmpty())
					jsonOutput = "No Progress Found for that class!";
				out.write(jsonOutput);
			}
			else
			{
				out.write("Error Occurred!");
			}
		}
		else
		{
			out.write("<img src='css/images/loggedOutSheep.jpg'/>");
		}
		log.debug("*** END servlets.Admin.GetProgress ***");
	}
}
