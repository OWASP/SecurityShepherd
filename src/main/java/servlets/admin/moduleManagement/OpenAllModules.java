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
import servlets.module.lesson.XxeLesson;
import utils.ShepherdLogManager;
import utils.Validate;

public class OpenAllModules extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(OpenAllModules.class);
	/**
	 * Control class used to open all modules when called by an administrator
	 * @param request
	 * @param response
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		String servletName = "servlets.module.OpenAllModules";
		String unsafeLevels = "disable";
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
				unsafeLevels = request.getParameter("enableUnsafeLevels");
				String ApplicationRoot = getServletContext().getRealPath("");
				if (unsafeLevels.equals("enable")) {
					Setter.openAllModules(ApplicationRoot, 0);
					Setter.openAllModules(ApplicationRoot, 1);
					XxeLesson.createXxeLessonSolutionFile();
					htmlOutput = "<p style='color:red'>[WARNING] Server is vulnerable. Unsafe levels open!<p>"
							+ "<h3 class='title'>All Modules are Now Open (including unsafe levels)</h3>"
							+ "<p>All of the Security Shepherd levels are now open and available for any user to access!</p>";
				}
				else if (unsafeLevels.equals("disable")) {
				    Setter.closeAllModules(ApplicationRoot);
                    Setter.openAllModules(ApplicationRoot, 0);
                    htmlOutput = "<h3 class='title'>All Modules are Now Open</h3>"
                            + "<p>All of the Security Shepherd levels are now open and available for any user to access!</p>";
                }
				else{
					Setter.openAllModules(ApplicationRoot, 0);
					htmlOutput = "<h3 class='title'>All Modules are Now Open</h3>"
							+ "<p>All of the Security Shepherd levels are now open and available for any user to access!</p>";
				}
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
