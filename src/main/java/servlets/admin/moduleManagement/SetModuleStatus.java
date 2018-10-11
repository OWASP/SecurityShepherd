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

import dbProcs.Setter;
import utils.ShepherdLogManager;
import utils.Validate;

public class SetModuleStatus extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SetModuleStatus.class);
	/**
	 * Controller class used to specify what modules to mark as closed/open
	 * @param toOpen Array of moduleId's to open
	 * @param toClose Array of moduleId's to close
	 * @param csrfToken The csrf protection token for this function
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& servlets.module.SetModuleStatus &&&");
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
				String ApplicationRoot = getServletContext().getRealPath("");
				//ToDo - Itterate through input and run open/Close functions on them
				if(request.getParameterValues("toOpen[]") != null)
				{
					String[] toOpen = request.getParameterValues("toOpen[]");;
					log.debug("toOpen = " + toOpen.toString());
					for(int i = 0; i < toOpen.length; i++)
						Setter.setModuleStatusOpen(ApplicationRoot, toOpen[i]);
					log.debug("Modules Opened");
				}
				else
				{
					log.debug("Nothing to Open");
				}
				
				if(request.getParameterValues("toClose[]") != null)
				{
				String[] toClose = request.getParameterValues("toClose[]");;
				log.debug("toClose = " + toClose.toString());
				for(int i = 0; i < toClose.length; i++)
					Setter.setModuleStatusClosed(ApplicationRoot, toClose[i]);
				log.debug("Modules Closed");
				}
				else
				{
					log.debug("Nothing to Close");
				}
				out.write("<h3 class='title'>Modules Updated</h3><p>The modules you selected have been updated accordingly</p>");
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.write("<h3 class='title'>Error Occurred</h3><p>Please Refresh Your Browser</p>");
			}
		}
		else
		{
			log.error("Invalid Session Detected");
			out.write("css/images/loggedOutSheep.jpg");
		}
		log.debug("&&& END SetModuleStatus &&&");
	}

}
