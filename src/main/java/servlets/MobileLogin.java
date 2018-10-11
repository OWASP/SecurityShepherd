package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import utils.Hash;
import utils.ShepherdLogManager;
import utils.UserKicker;
import dbProcs.Getter;

/**
 * Control class for the authentication procedure. Response tuned for Shepherd Mobile Auth
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
public class MobileLogin extends HttpServlet
{ 
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(MobileLogin.class);
	/** 
	 * Initiated by login.jsp. Once this post request has been completely processed, the user will be logged in, the account will be one count closer to been temporarily been locked or will be locked out temporarily.
	 * This method takes the credentials submitted and determines if they are correct. If they are correct, a session is prepared for the user and they are assigned a CSRF token.
	 * @param login User's User Name
	 * @param pwd User's Password		
	 */
	@SuppressWarnings("unchecked")
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException
    {
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("**** servlets.MobileLogin ***");
		HttpSession ses = request.getSession(true);
		PrintWriter out = response.getWriter(); 
		out.print(getServletInfo());
		response.setContentType("application/json"); 
		   // params
		   String p_login = request.getParameter("login");
		   log.debug("userName: " + p_login);
		   String p_pwd = request.getParameter("pwd");
		   String csrfToken = new String();
		 
		   boolean authenticated = false;
	   
		   // session is not new, try to set credentials
		   p_login = nvl(p_login, (String)ses.getAttribute("login"));
		   p_pwd = nvl(p_pwd, (String)ses.getAttribute("password"));
		   // get credentials
		   String ApplicationRoot = getServletContext().getRealPath("");
		   try
		   {
			   String user[] = Getter.authUser(ApplicationRoot, p_login, p_pwd);
			   if(user != null && !user[0].isEmpty())
			   {
				   
				   //Kill Session and Create a new one with user logged in
				   log.debug("Creating new session for " + user[2] + " " + user[1]);
				   ses.invalidate();
				   ses = request.getSession(true);
				   ses.setAttribute("userStamp", user[0]);
				   ses.setAttribute("userName", user[1]);
				   ses.setAttribute("userRole", user[2]);
				   log.debug("userClassId = " + user[4]);
				   
				   ses.setAttribute("userClass", user[4]);
				   log.debug("Setting CSRF cookie");
				   csrfToken = Hash.randomString();
				   Cookie token = new Cookie("token", csrfToken);
				   if(request.getRequestURL().toString().startsWith("https"))//If Requested over HTTPs
				   token.setSecure(true);
				   response.addCookie(token);
				   authenticated = true;
				   
				   if(user[3].equalsIgnoreCase("true"))
				   {
					   log.debug("Temporary Password Detected, user will be prompted to change");
					   ses.setAttribute("ChangePassword", "true");
				   }
				   //Removing user from kick list. If they were on it before, their suspension must have ended if they DB authentication Succeeded
				   UserKicker.removeFromKicklist(user[1]);
			   }
		   }
		   catch(Exception e)
		   {
			   log.error("Could not Find User: " + e.toString());
		   }
		   if (authenticated) 
		   {
			  //returning SessionID and CSRF Token
			   JSONObject jsonObj = new JSONObject();
			   jsonObj.put("JSESSIONID", ses.getId());
			   jsonObj.put("token", csrfToken);
			   out.write(jsonObj.toString());
			   return;
		   }
		   else
		   {
				//Lagging Response
				try 
				{
				    Thread.sleep(2000);
				}
				catch(InterruptedException ex)
				{
				    Thread.currentThread().interrupt();
				}
			   out.write("ERROR: Could not Authenticate");
			   return;
		   }	
	}
	
	//Handy 
	 private static String nvl(String x, String def) 
	 {
		 return (x == null? def : x);
	 }
	 
	 /**
	  * Redirects user to index.jsp
	  */
		public void doGet (HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException
	    {
			response.sendRedirect("index.jsp");
	    }
}
