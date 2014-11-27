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

import utils.ShepherdLogManager;
import dbProcs.Database;

/**
 * Level : SQL Injection Challenge 5
 * <br><br>
 * 
 * @author mark
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
		//Attempting to recover user name of session that made request
		try
		{
			if (request.getSession() != null)
			{
				HttpSession ses = request.getSession();
				String userName = (String) ses.getAttribute("decyrptedUserName");
				log.debug(userName + " accessed " + levelName + " Servlet");
			}
		}
		catch (Exception e)
		{
			log.debug(levelName + " Servlet Accessed");
			log.error("Could not retrieve user name from session");
		}
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		Encoder encoder = ESAPI.encoder();
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
					htmlOutput += "%" + coupons.getInt(2) + " off " + encoder.encodeForHTML(coupons.getString(3)) + " items";
				}
				else
				{
					htmlOutput = "No Coupon Found";
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
			htmlOutput = "Error Occurred: " + encoder.encodeForHTML(e.toString());
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
}
