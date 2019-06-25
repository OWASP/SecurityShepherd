package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;
/**
 * Level : SQL Injection 5
 * <br><br>
 * 
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
public class SqlInjection5 extends HttpServlet
{
	private static final String levelName = "SQLi C5 Shop";
	public static String levelHash = "8edf0a8ed891e6fef1b650935a6c46b03379a0eebab36afcd1d9076f65d4ce62";
	private static String levelSolution = "343f2e424d5d7a2eff7f9ee5a5a72fd97d5a19ef7bff3ef2953e033ea32dd7ee";
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjection5.class);
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.sqli.sqli5", locale);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			String applicationRoot = getServletContext().getRealPath("");
			
			try
			{
				int megustaAmount = validateAmount(Integer.parseInt(request.getParameter("megustaAmount")));
				log.debug("megustaAmount - " + megustaAmount);
				int trollAmount = validateAmount(Integer.parseInt(request.getParameter("trollAmount")));
				log.debug("trollAmount - " + trollAmount);
				int rageAmount = validateAmount(Integer.parseInt(request.getParameter("rageAmount")));
				log.debug("rageAmount - " + rageAmount);
				int notBadAmount = validateAmount(Integer.parseInt(request.getParameter("notBadAmount")));
				log.debug("notBadAmount - " + notBadAmount);
				String couponCode = request.getParameter("couponCode");
				log.debug("couponCode - " + couponCode);
				
				//Working out costs
				int megustaCost = megustaAmount * 30;
				int trollCost = trollAmount * 3000;
				int rageCost = rageAmount * 45;
				int notBadCost = notBadAmount * 15;
				int perCentOffMegusta = 0; // Will search for coupons in DB and update this int
				int perCentOffTroll = 0; // Will search for coupons in DB and update this int
				int perCentOffRage = 0; // Will search for coupons in DB and update this int
				int perCentOffNotBad = 0; // Will search for coupons in DB and update this int
				
				htmlOutput = new String();
				Connection conn = Database.getChallengeConnection(applicationRoot, "SqlInjectionChallenge5Shop");
				log.debug("Looking for Coupons");
				PreparedStatement prepstmt = conn.prepareStatement("SELECT itemId, perCentOff FROM coupons WHERE couponCode = ?"
						+ "UNION SELECT itemId, perCentOff FROM vipCoupons WHERE couponCode = ?");
				prepstmt.setString(1, couponCode);
				prepstmt.setString(2, couponCode);
				ResultSet coupons = prepstmt.executeQuery();
				try
				{
					if(coupons.next())
					{
						if(coupons.getInt(1) == 1) // MeGusta
						{
							log.debug("Found coupon for %" + coupons.getInt(2) + " off MeGusta");
							perCentOffMegusta = coupons.getInt(2);
						}
						else if (coupons.getInt(1) == 2) // Troll
						{
							log.debug("Found coupon for %" + coupons.getInt(2) + " off Troll");
							perCentOffTroll = coupons.getInt(2);
						}
						else if (coupons.getInt(1) == 3) // Rage
						{
							log.debug("Found coupon for %" + coupons.getInt(2) + " off Rage");
							perCentOffRage = coupons.getInt(2);
						}
						else if (coupons.getInt(1) == 4) // NotBad
						{
							log.debug("Found coupon for %" + coupons.getInt(2) + " off NotBad");
							perCentOffNotBad = coupons.getInt(2);
						}
						
					}
				}
				catch(Exception e)
				{
					log.debug("Could Not Find Coupon: " + e.toString());
				}
				conn.close();
				
				//Work Out Final Cost
				megustaCost = megustaCost - (megustaCost * (perCentOffMegusta/100));
				rageCost = rageCost - (rageCost * (perCentOffRage/100));
				notBadCost = notBadCost - (notBadCost * (perCentOffNotBad/100));
				trollCost = trollCost - (trollCost * (perCentOffTroll/100));
				int finalCost = megustaCost + rageCost + notBadCost + trollCost;
				
				//Output Order
				htmlOutput = "<h3>" + bundle.getString("response.orderComplete")+ "</h3>"
						+ "" + bundle.getString("response.orderComplete.p1")+ "<br/><br/>"
						+ "" + bundle.getString("response.orderComplete.p2")+ "<a><strong>$" + finalCost + "</strong></a>";
				if (trollAmount > 0 && trollCost == 0)
				{
					htmlOutput += "<br><br>" + bundle.getString("response.trollsFreeSolution")+ "<a><b>" + Encode.forHtml(levelSolution) + "</b></a>";
				}
			}
			catch (IllegalArgumentException e)
			{
				log.debug("Didn't complete order: " + e.toString());
				htmlOutput += "<p>" + bundle.getString("response.invalidAmount")+ "</p>";
			}
			catch(Exception e)
			{
				log.debug("Didn't complete order: " + e.toString());
				htmlOutput += "<p>" + bundle.getString("response.orderFailed")+ "</p>";
			}
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception e)
			{
				log.error("Failed to Pause: " + e.toString());
			}
			out.write(htmlOutput);
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
	
	private static int validateAmount (int amount) throws IllegalArgumentException
	{
		if(amount < 0 || amount > 9000)
			throw new IllegalArgumentException();
		return amount;
	}
}
