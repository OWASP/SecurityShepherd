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

public class OpenMobileModules extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(OpenMobileModules.class);
	/**
	 * Control class used to open all modules when called by an administrator
	 * @param csrfToken The csrf protection token for this funciton
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		String servletName = "servlets.module.OpenMobileModules";
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& " + servletName + " &&&");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		String htmlOutput = new String();
		HttpSession ses = request.getSession(true);
		Cookie tokenCookie = Validate.getToken(request.getCookies());
		Object tokenParmeter = request.getParameter("csrfToken");
		if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				String ApplicationRoot = getServletContext().getRealPath("");
				Setter.openOnlyMobileCategories(ApplicationRoot);
				htmlOutput = "<h3 class='title'>Only Mobile Levels Are Now Open</h3>"
						+ "<p>All of the Security Shepherd Mobile Security levels are now open! All other categories have been closed.</p>";
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				htmlOutput = "<h3 class='title'>Error</h3><p>CSRF Tokens Did Not Match. Function Aborted</p>";
			}
		}
		else
		{
			log.error("Invalid Session Detected");
			htmlOutput = "<img src=\"css/images/loggedOutSheep.jpg\" /><br/>";
		}
		out.write(htmlOutput);
		log.debug("&&& END " + servletName + " &&&");
	}

}
