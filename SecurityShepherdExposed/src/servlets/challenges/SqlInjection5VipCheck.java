package servlets.challenges;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import dbProcs.Database;

/**
 * Level : SQL Injection 5
 * <br><br>
 * 
 * @author mark
 *
 */
public class SqlInjection5VipCheck extends HttpServlet
{
	private static final String levelName = "SQLi C5 VIPCouponCheck";
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjection5VipCheck.class);
	/**
	 * //TODO - JavaDoc
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{

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
			Connection conn = Database.getChallengeConnection(applicationRoot, "SqlInjectionChallenge5ShopVipCoupon");
			log.debug("Looking for VipCoupons Insecurly");
			PreparedStatement prepstmt = conn.prepareStatement("SELECT itemId, perCentOff, itemName FROM vipCoupons JOIN items USING (itemId) WHERE couponCode = '" + couponCode + "';");
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
					htmlOutput = "No coupon Found";
				}
			}
			catch(Exception e)
			{
				log.debug("Could Not Find Vip Coupon: " + e.toString());
				htmlOutput += "<p> Check Failed - Please try again later</p>";
			}
			conn.close();
		}
		catch(Exception e)
		{
			log.debug("Did complete Vip Check: " + e.toString());
			htmlOutput += "<p> Check Failed - Please try again later</p>";
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
