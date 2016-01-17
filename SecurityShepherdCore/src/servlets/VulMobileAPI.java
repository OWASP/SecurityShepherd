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
 * @author Seán Duggan
 *
 */
public class VulMobileAPI extends HttpServlet
{ 
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(MobileLogin.class);
	/** 
	 * A valid request must contain the API key. If the expected key is sent, level key is returned.
	 * 
	 * @param mobileKey
	 * 	
	 */
	@SuppressWarnings("unchecked")
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException
    {
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("**** servlets.VulnerableMobileAPI ***");
		HttpSession ses = request.getSession(true);
		PrintWriter out = response.getWriter(); 
		out.print(getServletInfo());
		response.setContentType("application/json"); 
		   // params
		   String p_apiKey = request.getParameter("mobileKey");
		   log.debug("mobileKey: " + p_apiKey);
		   String csrfToken = new String();
		 
		   boolean keyIsCorrect = false;
	   
		   // session is not new, try to set credentials
		   p_apiKey = nvl(p_apiKey, (String)ses.getAttribute("mobileKey"));
		   // get credentials
		   String ApplicationRoot = getServletContext().getRealPath("");
		   try
		   {
			   String keyCheck = Getter.returnMobileKey(ApplicationRoot, p_apiKey);
			   if(keyCheck != null)
			   {
				   //return level key
				   //if(request.getRequestURL().toString().startsWith("https"))//If Requested over HTTPs
				   keyIsCorrect = true;
				   
			   }
		   }
		   catch(Exception e)
		   {
			   log.error("Could not get Key: " + e.toString());
		   }
		   if (keyIsCorrect) 
		   {
			  //returning key
			   JSONObject jsonObj = new JSONObject();
			   jsonObj.put("LEVELKEY", "This Key is: RetroMagicFuturePunch");
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
			   out.write("ERROR: Could not Respond");
			   return;
		   }	
	}
	
	//Handy 
	 private static String nvl(String x, String def) 
	 {
		 return (x == null? def : x);
	 }
	 
	 
}
