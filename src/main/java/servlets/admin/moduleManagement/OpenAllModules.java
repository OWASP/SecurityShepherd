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
import servlets.module.challenge.XxeChallenge1;
import servlets.module.lesson.XxeLesson;
import utils.ShepherdLogManager;
import utils.Validate;

public class OpenAllModules extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final  String SERVLET_NAME = "servlets.module.OpenAllModules";
	private static org.apache.log4j.Logger log = Logger.getLogger(OpenAllModules.class);
	/**
	 * Control class used to open all modules when called by an administrator
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		String unsafeLevels;
		String htmlOutput;

		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& " + SERVLET_NAME + " &&&");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());

		HttpSession ses = request.getSession(true);
		if(request.getCookies() == null)
		{
			log.warn("Cookies are null");
			htmlOutput = "<img src=\"css/images/loggedOutSheep.jpg\" /><br/>";
		}
		else {
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			log.debug("Token Param: " + tokenParmeter.toString());

			if (Validate.validateAdminSession(ses, tokenCookie, tokenParmeter)) {
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				if (Validate.validateTokens(tokenCookie, tokenParmeter)) {
					unsafeLevels = request.getParameter("unsafeLevels");
					if (unsafeLevels == null) {
						unsafeLevels = "disable";
					}
					String ApplicationRoot = getServletContext().getRealPath("");

					switch (unsafeLevels) {
						case "enable":
							Setter.openAllModules(ApplicationRoot, false);
							Setter.openAllModules(ApplicationRoot, true);
							XxeLesson.createXxeLessonSolutionFile();
							XxeChallenge1.createXxeChallenge1SolutionFile();
							htmlOutput = "<p style='color:red'>[WARNING] Server is vulnerable. Unsafe levels open!<p>"
									+ "<h3 class='title'>All Modules are Now Open (including unsafe levels)</h3>"
									+ "<p>All of the Security Shepherd levels are now open and available for any user to access!</p>";
							break;
						case "disable":
							Setter.closeAllModules(ApplicationRoot);
							Setter.openAllModules(ApplicationRoot, false);
							htmlOutput = "<h3 class='title'>All Modules are Now Open</h3>"
									+ "<p>All of the Security Shepherd levels are now open and available for any user to access!</p>";
							break;
						default:
							Setter.openAllModules(ApplicationRoot, false);
							htmlOutput = "<h3 class='title'>All Modules are Now Open</h3>"
									+ "<p>All of the Security Shepherd levels are now open and available for any user to access!</p>";

					}
				} else {
					log.debug("CSRF Tokens did not match");
					htmlOutput = "<h3 class='title'>Error</h3><p>CSRF Tokens Did Not Match. Function Aborted</p>";
				}
			} else {
				log.error("Invalid Session Detected");
				htmlOutput = "<img src=\"css/images/loggedOutSheep.jpg\" /><br/>";
			}
		}
		out.write(htmlOutput);
		log.debug("&&& END " + SERVLET_NAME + " &&&");
	}

}
