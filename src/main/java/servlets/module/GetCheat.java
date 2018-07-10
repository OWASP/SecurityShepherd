package servlets.module;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import dbProcs.Getter;
import utils.CheatSheetStatus;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Control class responsible for returning a cheat sheet for a module
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
public class GetCheat extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GetCheat.class);
	
	/**
	 * This method will reject requests if cheat sheet availability is marked as unavailable by administration.
	 * @param moduleId
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.GetCheat ***");
		String[] result = null;
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug("Current User: " + ses.getAttribute("userName").toString());
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				if(CheatSheetStatus.showCheat(ses.getAttribute("userRole").toString()))
				{
					String ApplicationRoot = getServletContext().getRealPath("");
					String moduleId = request.getParameter("moduleId");
					Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
					log.debug(ses.getAttribute("userName") + " submitted the following moduleId: " + moduleId);
					if(moduleId != null)
					{
						result = Getter.getModuleSolution(ApplicationRoot, moduleId, locale);
						if(result != null)
						{
							out.write(
									"<div id='theActualCheat' class='cheatBox'>" + 
									"<big style='color:#A878EF;'>" + Encode.forHtml(result[0]) + " Cheat</big>" +
									"<p>" +
									result[1] +
									"</div></p><br>");
						}
					}
				}
			}
			else
			{
				log.error("CSRF Attack Detected: Made Against" + ses.getAttribute("userName"));
			}
		}
		else
		{
			out.write("<img src='css/images/loggedOutSheep.jpg'/>");
		}
		if(result == null)
		{
			out.write("An error Occurred...");
		}
		log.debug("*** END servlets.Admin.GetCheat ***");
	}
}
