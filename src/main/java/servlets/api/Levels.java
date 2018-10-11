package servlets.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;

import dbProcs.Getter;
import utils.ModulePlan;
import utils.Validate;

@WebServlet("/api/levels")
public class Levels extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(Levels.class);
	
	/**
	 * Get request just returns if the session can access the scoreboard or not
	 */
	public void doGet (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter(); 
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		Locale locale = new Locale(Validate.validateLanguage(ses));
		boolean validSession = Validate.validateSession(ses);
		boolean validRequest = Validate.validateTokens(Validate.getToken(request.getCookies()), request.getParameter("csrfToken"));
		if(validSession && validRequest)
		{
			JSONArray theModules =  new JSONArray();
			String userId = ses.getAttribute("userStamp").toString();
			String floor = new String();
			//Return All Level Information with Completed/NotCompleted Flags for this user's session
			if(ModulePlan.isIncrementalFloor())
			{
				floor = "ctf";
			}
			else if(ModulePlan.isOpenFloor())
			{
				floor = "by-type";
			}
			else
			{
				floor = "by-difficulty";
			}
			theModules = Getter.getModulesJson(userId, floor, locale);
			response.setContentType("application/json");
			out.write(theModules.toJSONString());
		} 
		else 
		{
			if(!validSession)
			{
				log.debug("Unauthentiated Module List Call");
				response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Return 403
			}
			else
			{
				log.debug("CSRF Attack Detected");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //Return 400
			}
		}
	}
	
}
