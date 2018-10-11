package servlets.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import utils.CheatSheetStatus;

@WebServlet("/api/cheats")
public class Cheats extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Get request just returns if the session can access the scoreboard or not
	 */
	public void doGet (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter(); 
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		
		if(CheatSheetStatus.showCheat((String)ses.getAttribute("userRole"))){
			out.write("true");
		} else {
			// Return 403
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
