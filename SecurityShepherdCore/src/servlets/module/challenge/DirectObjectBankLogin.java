package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

/**
 * Insecure Direct Object Reference Bank Challenge
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
public class DirectObjectBankLogin extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(DirectObjectBankLogin.class);
	private static String levelName = "Insecure Direct Object Bank Challenge";
	private static String levelHash = "1f0935baec6ba69d79cfb2eba5fdfa6ac5d77fadee08585eb98b130ec524d00c";
	private static String levelResult = "4a1df02af317270f844b56edc0c29a09f3dd39faad3e2a23393606769b2dfa35";
	/**
	 * TODO - This Servlet is used to Sign In as Bank Account
	 */
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
			try
			{
				String accountHolder = request.getParameter("accountHolder");
				log.debug("Account Holder - " + accountHolder);
				String accountPass = request.getParameter("accountPass");
				log.debug("Account Pass - " + accountPass);
				String applicationRoot = getServletContext().getRealPath("");
				String htmlOutput = new String();
				
				Connection conn = Database.getChallengeConnection(applicationRoot, "directObjectBank");
				CallableStatement callstmt = conn.prepareCall("CALL bankAuth(?, ?)");
				callstmt.setString(1, accountHolder);
				callstmt.setString(2, accountPass);
				ResultSet resultSet = callstmt.executeQuery();
				if(resultSet.next())
				{
					String accountNumber = resultSet.getString(1);
					log.debug("Found Account Number: " + accountNumber);
					ses.setAttribute("directObjectBankAccount", accountNumber);
					htmlOutput += bankForm(accountNumber, applicationRoot, ses);
				}
				else
				{
					log.debug("Authentication Failed");
					Encoder encoder = ESAPI.encoder();
					htmlOutput = "ERROR: User '" + encoder.encodeForHTML(accountHolder) + "' could not be logged in";
				}
				log.debug("Outputting HTML");
				out.write(htmlOutput);
				Database.closeConnection(conn);
			}
			catch(SQLException e)
			{
				out.write("An Error Occurred! You must be getting funky! Could not get Balance!");
				log.fatal(levelName + " SQL Error - " + e.toString());
			}
			catch(Exception e)
			{
				out.write("An Error Occurred! You must be getting funky!");
				log.fatal(levelName + " - " + e.toString());
			}
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
	
	public static String bankForm(String accountNumber, String applicationRoot, HttpSession ses) throws SQLException 
	{
		Encoder encoder = ESAPI.encoder();
		float currentBalance = getAccountBalance(accountNumber, applicationRoot);
		String bankForm = "<h2 class='title'>Your Account</h2>" +
				"<p>Your account balance is currently: <div id='currentAccountBalanceDiv'><b>" + currentBalance + "</b></div></p>";
		if(currentBalance > 5000000)
		{
			//Level Complete As the user has more than 5000000 in account. Return Key
			bankForm += "<h2 class='title'>Challenge Complete</h2><p>Congradulations, you have sucessfully completed this challenge. Use the following result key at the top of the page to mark this level as complete in the sytem.<br><br>"
					+ "The result key for this challenge is <a>" + encoder.encodeForHTML(Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName"))) + "</a>";
		}
		bankForm += ""
				+ "<input type='hidden' value='" + encoder.encodeForHTMLAttribute(accountNumber) + "' id='currentAccountNumber'>"
				+ "<h2 class='title'>Transfer Funds</h2><p>Use this form to send money to other accounts in this bank. All you need to do is enter their account number and the ammount you want to send!</p>"
				+ "<div id='transferFundsForm'><form id='transferFunds' action='javascript:transferFunds();'>"
				+ "<table><tr><td>Reciever Account Number: </td><td><input type='text' id='recieverAccountNumber'></td></tr>"
				+ "<tr><td>Amount to Send: </td><td><input type='text' id='transferAmount'></td></tr>"
				+ "<tr><td colspan='2'><input type='submit' value='Transfer Funds'></td></tr>"
				+ "</table></form></div><div id='transferLoadingDiv' style='display: none;'>Loading...</div>"
				+ "<div id='transferResultsDiv'></div>"
				+ "<h2 class='title'>Refresh Balance</h2><p>Use this form to refresh your balance above. That way you can see if any money came in recently!</p>"
				+ "<div id='refreshFormDiv'>"
				+ "<table><tr><td><input type='button' id='refreshFunds' onclick='refreshFunds();' value='Refresh Balance'></td></tr></table>"
				+ "</div><div id='refreshLoadingSign' style='display: none;'>Loading...</div>"
				+ "<div id='refreshResultsDiv'></div>"
				+ "<h2 class='title'>Logout of Account</h2><p>Use this form to sign out of your bank account when your done giving your money away.</p>"
				+ "<div id='logoutFormDiv'>"
				+ "<table><tr><td><input type='button' id='logoutButton' onclick='logout();' value='Log Out Of Bank Account'></td></tr></table>"
				+ "</div><div id='logoutLoadingSign' style='display: none;'>Loading...</div>"
				+ "<div id='logoutResultsDiv'></div>";
		return bankForm;
	}

	/**
	 * Method to get the account balance from the DirectObjectBank for a specific account
	 * @param accountNumber The Account Number to Check the Balance Of
	 * @param applicationRoot Running Context of the application
	 * @return Returns a Float Value representing the balance
	 * @throws SQLException If no rows found or if SQL error occurs
	 */
	public static float getAccountBalance(String accountNumber, String applicationRoot) throws SQLException {
		Connection conn = Database.getChallengeConnection(applicationRoot, "directObjectBank");
		CallableStatement callstmt;
		float toReturn = 0;
		try 
		{
			callstmt = conn.prepareCall("CALL currentFunds(?)");
			callstmt.setString(1, accountNumber);
			ResultSet rs = callstmt.executeQuery();
			if(rs.next())
			{
				toReturn = rs.getFloat(1);
			}
			else
			{
				throw new SQLException("Could not Get Funds. No Rows Found From Query");
			}
		} 
		catch (SQLException e) 
		{
			throw e;
		}
		conn.close();
		return toReturn;
	}
}
