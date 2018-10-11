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
import org.owasp.encoder.Encode;

import dbProcs.Setter;
import utils.ShepherdLogManager;
import utils.Validate;

public class OpenOrCloseByCategory extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(OpenOrCloseByCategory.class);
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
		log.debug("&&& servlets.module.OpenOrCloseByCategory &&&");
		String htmlOutput = new String();
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
				String openOrClose = new String();
				boolean validData = false;
				if (request.getParameter("openOrClose") != null)
				{
					openOrClose = request.getParameter("openOrClose").toString().toLowerCase();
					validData = (openOrClose.equalsIgnoreCase("open")) || (openOrClose.equalsIgnoreCase("closed"));
				}
				if(validData && request.getParameterValues("toOpenOrClose[]") != null)
				{
					htmlOutput = "<h2 class='title'>Categories Set to " + Encode.forHtml(openOrClose) + "</h2>";
					String[] toDo = request.getParameterValues("toOpenOrClose[]");
					log.debug("toOpen = " + toDo.toString());
					for(int i = 0; i < toDo.length; i++)
						Setter.setModuleCategoryStatusOpen(ApplicationRoot, toDo[i], openOrClose);
					log.debug("Categories have been set to " + openOrClose);
					if(openOrClose.equalsIgnoreCase("open"))
						htmlOutput += "<p>The categories selected have been opened and are now available to users to access.</p>";
					else
						htmlOutput += "<p>The categories selected have been closed and are no longer available for users to access or submit solutions for.</p>";
				}
				else
				{
					log.debug("Nothing to Open or Close");
					htmlOutput = "<h2 class='title'>Invalid Request</h2><p>Please try again</p>";
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
			}
		}
		else
		{
			log.error("Invalid Admin Session Detected");
			out.write("css/images/loggedOutSheep.jpg");
		}
		out.write(htmlOutput);
		log.debug("&&& END OpenOrCloseByCategory &&&");
	}

}
