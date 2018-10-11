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
import org.owasp.encoder.Encode;

import utils.ShepherdLogManager;
import utils.Validate;
import utils.ModuleBlock;
import dbProcs.Getter;

/**
 * Responsable for returning the directories of modules
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
public class GetModule extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GetModule.class);
	/**
	 * Initiated by an Ajax call defined in index.jsp, this method takes a module identifier and returns the valid directory of where the module's View structure is stored.
	 * @param moduleId The identifier of the module to be returned
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& servlets.module.GetModule &&&");
		
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
				boolean notNull = false;
				String storedResult = null;
				try
				{
					String ApplicationRoot = getServletContext().getRealPath("");
					
					log.debug("Getting Parameters");
					String moduleId = (String)request.getParameter("moduleId");;
					log.debug("moduleId = " + moduleId.toString());
					
					log.debug("Getting session parameters");
					String userId = (String)ses.getAttribute("userStamp");
					log.debug("userId = " + userId);
					
					//Validation
					notNull = (moduleId != null);
					if(notNull)
					{
						storedResult = Getter.getModuleResult(ApplicationRoot, moduleId);
					}
					boolean moduleOpen = false;
					if(notNull && storedResult != null)
					{
						moduleOpen = Getter.isModuleOpen(ApplicationRoot, moduleId);
					}
					if(notNull && storedResult != null && moduleOpen)
					{
						//Data is good, Add result: Now to check if there is a block in place
						if(ModuleBlock.blockerEnabled && ModuleBlock.blockerId.compareTo(moduleId) == 0)
						{
							log.debug("Blocker Detected; Returning Message");
							out.write("../blockedMessage.jsp");
						}
						else
						{
							String theHash = Encode.forHtmlAttribute(Getter.getModuleAddress(ApplicationRoot, moduleId, userId));
							out.write(theHash);
							log.debug("Returning: " + theHash);
						}
					}
					else
					{
						//Validation Error Responses
						if(!notNull)
						{
							log.error("Null values detected");
						}
						else if(storedResult == null)
						{
							log.error("Module not found");
						}
						else 
						{
							log.fatal("Module Not Open");
						}
					}
				}
				catch (Exception e)
				{
					log.error("Get Module Error: " + e.toString());
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
			out.write("css/images/loggedOutSheep.jpg");
		}
		log.debug("&&& END GetModule &&&");
	}
}
