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

import utils.Hash;
import dbProcs.Database;
/**
 * Level : SQL Injection 6
 * <br><br>
 * 
 * @author Mark Denihan
 *
 */
public class SqlInjection6 extends HttpServlet
{
	private static final String levelName = "SQLi C6";
	private static String levelSolution = "17f999a8b3fbfde54124d6e94b256a264652e5087b14622e1644c884f8a33f82";
	private static String levelHash = "d0e12e91dafdba4825b261ad5221aae15d28c36c7981222eb59f7fc8d8f212a2";
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjection6.class);
	/**
	 * //TODO - JavaDoc
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		String htmlOutput = new String();
		String applicationRoot = getServletContext().getRealPath("");
		Encoder encoder = ESAPI.encoder();
		try
		{
			String userPin = (String) request.getParameter("pinNumber");
			log.debug("userPin - " + userPin);
			userPin = userPin.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", ""); // Escape single quotes
			log.debug("userPin scrubbed - " + userPin);
			userPin = java.net.URLDecoder.decode(userPin.replaceAll("\\\\x", "%"), "UTF-8"); //Decode \x encoding 
			log.debug("searchTerm decoded to - " + userPin);
			Connection conn = Database.getChallengeConnection(applicationRoot, "SqlChallengeSix");
			log.debug("Looking for users");
			PreparedStatement prepstmt = 
					conn.prepareStatement("SELECT userName FROM users WHERE userPin = '" + userPin + "'");
			ResultSet users = prepstmt.executeQuery();
			try
			{
				if(users.next())
				{
					htmlOutput = "<h3>Welcome back " + encoder.encodeForHTML(users.getString(1)) + "</h3>"
							+ "<p>You're authentication number is now " + encoder.encodeForHTML(Hash.randomString()) + "</p>";
				}
				else
				{
					htmlOutput = "<h3>Incorrect Password / Username</h3><p>Careful now!</p>";
				}
			}
			catch(Exception e)
			{
				htmlOutput = "<h3>Incorrect Password / Username</h3><p>Careful now!</p>";
				log.debug("Could Not Find User: " + e.toString());
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e1)
				{
					log.error("Failed to Pause: " + e1.toString());
				}
			}
			conn.close();
		}
		catch(Exception e)
		{
			log.debug("Could not Search for User: " + e.toString());
			htmlOutput += "<p>Bad Request? Please be careful!</p>";
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception e2)
			{
				log.error("Failed to Pause: " + e2.toString());
			}
		}
		log.debug("*** SQLi C6 End ***");
		out.write(htmlOutput);
	}
}
