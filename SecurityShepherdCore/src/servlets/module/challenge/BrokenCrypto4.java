package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Database;
import dbProcs.Getter;
/**
 * Level : Broken Crypto NEW
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
public class BrokenCrypto4 extends HttpServlet
{
	private static final String levelName = new String("Broken Crypto 4");
	private static final String levelHash = new String("b927fc4d8c9f70a78f8b6fc46a0cc18533a88b2363054a1f391fe855954d12f9");
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(BrokenCrypto4.class);
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			String applicationRoot = getServletContext().getRealPath("");
			Encoder encoder = ESAPI.encoder();
			try
			{
				//Get and validate cart amounts
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
				Connection conn = Database.getChallengeConnection(applicationRoot, "CryptoChallengeShop");
				log.debug("Looking for Coupons");
				PreparedStatement prepstmt = conn.prepareStatement("SELECT itemId, perCentOff FROM coupons WHERE couponCode = ?");
				prepstmt.setString(1, couponCode);
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
					else
					{
						log.debug("Invalid Coupon Code");
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
				int finalCost = megustaCost + rageCost + notBadAmount + trollCost;
				
				//Output Order
				htmlOutput = "<h3>Order Complete</h3>"
						+ "Your order has been made and has been sent to our magic shipping department that knows where you want this to be delivered via brain wave sniffing techniques.<br/><br/>"
						+ "Your order comes to a total of <a><strong>$" + finalCost + "</strong></a>";
				if (trollAmount > 0 && trollCost == 0)
				{
					htmlOutput += "<br><br>Trolls were free, Well Done - <a><b>" + encoder.encodeForHTML(Hash.generateUserSolution(Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash), (String)ses.getAttribute("userName"))) + "</b></a>";
				}
			}
			catch(Exception e)
			{
				log.debug("Didn't complete order: " + e.toString());
				htmlOutput += "<p> Order Failed - Please try again later</p>";
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
	
	private static int validateAmount (int amount)
	{
		if(amount < 0 || amount > 9000)
			amount = 0;
		return amount;
	}
}
