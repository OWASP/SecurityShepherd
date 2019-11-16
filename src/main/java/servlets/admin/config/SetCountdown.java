package servlets.admin.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.CountdownHandler;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class responsible for enabling cheat sheet functionality <br/>
 * <br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mark Denihan
 *
 */
public class SetCountdown extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SetCountdown.class);

	/**
	 * If this functionality is validly called by an administrator, the cheat sheets
	 * will become or remain unavailable.
	 * 
	 * @param csrfToken
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Setting IpAddress To Log and taking header for original IP if forwarded from
		// proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.SetCountdown ***");
		PrintWriter out = response.getWriter();
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		Cookie tokenCookie = Validate.getToken(request.getCookies());
		Object tokenParameter = request.getParameter("csrfToken");
		if (Validate.validateAdminSession(ses, tokenCookie, tokenParameter)) {
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
					ses.getAttribute("userName").toString());
			log.debug("Current User: " + ses.getAttribute("userName").toString());
			if (Validate.validateTokens(tokenCookie, tokenParameter)) {

				LocalDateTime startTime = LocalDateTime.parse(request.getParameter("startTime"));
				boolean hasStartTime = Boolean.parseBoolean(request.getParameter("hasStartTime"));
				LocalDateTime lockTime = LocalDateTime.parse(request.getParameter("lockTime"));
				boolean hasLockTime = Boolean.parseBoolean(request.getParameter("hasLockTime"));
				LocalDateTime endTime = LocalDateTime.parse(request.getParameter("lockTime"));
				boolean hasEndTime = Boolean.parseBoolean(request.getParameter("hasEndTime"));

				CountdownHandler.setStartTime(startTime);
				if (!hasStartTime) {
					CountdownHandler.disableStartTime();
				}
				
				CountdownHandler.setLockTime(lockTime);
				if (!hasLockTime) {
					CountdownHandler.disableLockTime();
				}
				
				CountdownHandler.setEndTime(endTime);
				if (!hasEndTime) {
					CountdownHandler.disableEndTime();
				}
				
			}
		} else {
			out.write("<img src='css/images/loggedOutSheep.jpg'/>");
		}
		log.debug("*** END servlets.Admin.SetCountdown ***");
	}
}
