package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;
import dbProcs.Getter;
import dbProcs.Setter;

/**
 * Cross Site Request Forgery Challenge Six - Does not return result Key
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
public class CsrfChallengeSixGetToken extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfChallengeSixGetToken.class);
	private static final String levelHash = "7d79ea2b2a82543d480a63e55ebb8fef3209c5d648b54d1276813cd072815df3";
	/**
	 * Allows users to set their CSRF attack string to complete this module. They should be using this to force users to visit their own pages that
	 * forces the victim to submit a post request to the CSRFChallengeTargetTwo
	 * @param myMessage To Be stored as the users message for this module
	 */
	public void doGet (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("Cross-SiteForegery Challenge Get Token Six Servlet");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				String htmlOutput = new String("Your csrf Token for this Challenge is: ");
				String userId = request.getParameter("userId").toString();
				Encoder encoder = ESAPI.encoder();
				Connection conn = Database.getcsrfChallengeSixConnection(getServletContext().getRealPath(""));
				try
				{
					log.debug("Prepairing setCsrfChallengeSixToken call");
					PreparedStatement callstmnt = conn.prepareStatement("SELECT csrfTokenscol FROM csrfchallengesix.csrfTokens WHERE userId LIKE ?");
					callstmnt.setString(1, userId);
					log.debug("Executing setCsrfChallengeSixTokenQuery");
					ResultSet rs = callstmnt.executeQuery();
					int i = 0;
					while(rs.next())
					{
						i++;
						htmlOutput += encoder.encodeForHTML("\"" + rs.getString(1) + "\"") + " <br/>";
					}
					log.debug("Returned " + i + " CSRF Tokens for ID: " + userId);
					conn.close();
				}
				catch (Exception e)
				{
					log.debug("Could not retrieve Challenge CSRF Tokens");
					htmlOutput = "Was unable to retrieve CSRF Token. Funky";
				}
				out.write(htmlOutput);
					
			}
		}
		catch(Exception e)
		{
			out.write("An Error Occured! You must be getting funky!");
		}
	}

}
