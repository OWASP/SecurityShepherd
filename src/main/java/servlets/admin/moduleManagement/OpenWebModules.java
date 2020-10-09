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

public class OpenWebModules extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(OpenWebModules.class);
	/**
	 * Control class used to open all Only Web Categories when called by an administrator
	 * @param request the HTTP Request
	 * @param response the HTTP Response
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		String servletName = "servlets.module.OpenWebModules";
		String unsafeLevels;
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& " + servletName + " &&&");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		String htmlOutput;
		HttpSession ses = request.getSession(true);
		Cookie tokenCookie = Validate.getToken(request.getCookies());
		Object tokenParmeter = request.getParameter("csrfToken");
		if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				unsafeLevels = request.getParameter("unsafeLevels");
				if (unsafeLevels == null){
					unsafeLevels = "disable";
				}
				log.debug("Unsafe Levels: " + unsafeLevels);
				String ApplicationRoot = getServletContext().getRealPath("");

				switch (unsafeLevels) {
					case "enable":
						Setter.openOnlyWebCategories(ApplicationRoot, 0);
						Setter.openOnlyWebCategories(ApplicationRoot, 1);
						XxeLesson.createXxeLessonSolutionFile();
						XxeChallenge1.createXxeChallenge1SolutionFile();
						htmlOutput = "<p style='color:red'>[WARNING] Server is vulnerable. Unsafe levels open!<p>"
								+ "<h3 class='title'>Only Web Levels Are Now Open (including unsafe levels)</h3>"
								+ "<p>All of the Security Shepherd Web Application Security levels are now open! All other categories have been closed.</p>";
						break;
					case "disable":
						Setter.closeAllModules(ApplicationRoot);
						Setter.openOnlyWebCategories(ApplicationRoot, 0);
						htmlOutput = "<h3 class='title'>Only Web Levels Are Now Open</h3>"
								+ "<p>All of the Security Shepherd Web Application Security levels are now open! All other categories have been closed.</p>";
						break;
					default:
						Setter.openOnlyWebCategories(ApplicationRoot, 0);
						htmlOutput = "<h3 class='title'>Only Web Levels Are Now Open</h3>"
								+ "<p>All of the Security Shepherd Web Application Security levels are now open! All other categories have been closed.</p>";
						break;
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
