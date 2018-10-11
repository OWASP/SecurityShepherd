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
 * Level : SQL Injection Challenge 5
 * <br><br>
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
 * 
 * @author Mark Denihan
 *
 */
public class SqlInjection5CouponCheck extends HttpServlet
{
	private static final String levelName = "SQLi C5 CouponCheck";
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjection5CouponCheck.class);
	/**
	 * //TODO - JavaDoc
	 */
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
				String couponCode = request.getParameter("couponCode");
				log.debug("couponCode - " + couponCode);
				if (couponCode == null || couponCode.isEmpty())
					couponCode = new String();
				
				htmlOutput = new String("");
				Connection conn = Database.getChallengeConnection(applicationRoot, "SqlInjectionChallenge5ShopCoupon");
				log.debug("Looking for Coupons Insecurely");
				PreparedStatement prepstmt = conn.prepareStatement("SELECT itemId, perCentOff, itemName FROM coupons JOIN items USING (itemId) WHERE couponCode = '" + couponCode + "';");
				ResultSet coupons = prepstmt.executeQuery();
				try
				{
					if(coupons.next())
					{
						htmlOutput = new String("Valid Coupon for ");
						log.debug("Found coupon for %" + coupons.getInt(2));
						log.debug("For Item Name " + coupons.getString(3));
						htmlOutput += "" + bundle.getString("response.percent")+ "" + coupons.getInt(2) + " " + bundle.getString("response.off")+ " " + Encode.forHtml(coupons.getString(3)) + " " + bundle.getString("response.items")+ "";
					}
					else
					{
						htmlOutput = "" + bundle.getString("response.noCoupon")+ "";
					}
				}
				catch(Exception e)
				{
					log.debug("Could Not Find Coupon: " + e.toString());
					
				}
				conn.close();
			}
			catch(Exception e)
			{
				log.debug("Did complete Check: " + e.toString());
				htmlOutput = "" + bundle.getString("errors.occured")+ "" + Encode.forHtml(e.toString());
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
}
