package servlets.module;

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

import utils.ExposedServer;
import utils.ModulePlan;
import utils.Validate;
import dbProcs.Getter;

/**
 * Class used to return a fresh incremental menu upon completion of a module in incremental mode.
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
public class RefreshMenu extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GetModule.class);
	/**
	 * This method refreshs the user's menu after they complete a level incremental mode. This ensures that the process of continuing challenges is a fluid one.
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		log.debug("&&& servlets.module.RefreshMenu &&&");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		Encoder encoder = ESAPI.encoder();
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				if(ModulePlan.isIncrementalFloor())
				{
					try
					{
						log.debug("Getting ApplicationRoot");
						String ApplicationRoot = getServletContext().getRealPath("");
						log.debug("Servlet root = " + ApplicationRoot );
						
						out.write(Getter.getIncrementalModules(ApplicationRoot, (String)ses.getAttribute("userStamp"), (String)tokenParmeter));
					}
					catch (Exception e)
					{
						log.error("Refresh Menu Error: " + e.toString());
					}
				}
				else
				{
					//Incremental Mode is not enabled, so No
					out.write("No");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
			}
		}
		else
		{
			log.error("Invalid Session Detected");
			out.write("<img src='" + encoder.encodeForHTMLAttribute(ExposedServer.getSecureUrl()) + "css/images/loggedOutSheep.jpg'/>");
		}
		log.debug("&&& END RefreshMenu &&&");
	}
}
