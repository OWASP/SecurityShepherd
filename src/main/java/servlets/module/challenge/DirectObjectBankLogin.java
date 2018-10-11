package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;


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
	public static String levelHash = "1f0935baec6ba69d79cfb2eba5fdfa6ac5d77fadee08585eb98b130ec524d00c";
	private static String levelResult = "4a1df02af317270f844b56edc0c29a09f3dd39faad3e2a23393606769b2dfa35";
	/**
	 * This Servlet is used in the Insecure Direct Object Bank to sign in to a specific bank account. 
	 * It does this by checking the user DB credentials and then returns the bank form the user needs 
	 * to call Bank Functions.
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		HttpSession ses = request.getSession(true);

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.directObject.directObjectBank", locale);
		
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
					htmlOutput += bankForm(accountNumber, applicationRoot, ses, bundle, errors);
				}
				else
				{
					log.debug("Authentication Failed");
					
					htmlOutput = bundle.getString("login.authFailedMessage.1") + " '" + Encode.forHtml(accountHolder) + "' " + bundle.getString("login.authFailedMessage.2");
				}
				log.debug("Outputting HTML");
				out.write(htmlOutput);
				Database.closeConnection(conn);
			}
			catch(SQLException e)
			{
				out.write(errors.getString("error.funky") + " " + bundle.getString("login.error.couldNotGetBalance"));
				log.fatal(levelName + " SQL Error - " + e.toString());
			}
			catch(Exception e)
			{
				out.write(errors.getString("error.funky"));
				log.fatal(levelName + " - " + e.toString());
			}
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
	
	/**
	 * Method used to return the bank interaction view for the user that is signed into the Direct Object Bank challenge
	 * @param accountNumber
	 * @param applicationRoot
	 * @param ses
	 * @param bundle
	 * @param errors
	 * @return
	 * @throws SQLException
	 */
	public static String bankForm(String accountNumber, String applicationRoot, HttpSession ses, ResourceBundle bundle, ResourceBundle errors) throws SQLException 
	{
		
		float currentBalance = getAccountBalance(accountNumber, applicationRoot);
		String bankForm = "<h2 class='title'>" + bundle.getString("bankForm.yourAccount") + "</h2>" +
				"<p>" + bundle.getString("bankForm.yourAccount.balance") + " <div id='currentAccountBalanceDiv'><b>" + currentBalance + "</b></div></p>";
		if(currentBalance > 5000000)
		{
			//Level Complete As the user has more than 5000000 in account. Return Key
			bankForm += "<h2 class='title'>" + bundle.getString("result.complete") + "</h2><p>" + bundle.getString("result.wellDone") + "<br><br>"
					+ "" + bundle.getString("result.theKeyIs") + " <a>" + Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName")) + "</a>";
		}
		bankForm += ""
				+ "<input type='hidden' value='" + Encode.forHtmlAttribute(accountNumber) + "' id='currentAccountNumber'>"
				+ "<h2 class='title'>" + bundle.getString("bankForm.transferFunds") + "</h2><p>" + bundle.getString("bankForm.transferFunds.whatToDo") + "</p>"
				+ "<div id='transferFundsForm'><form id='transferFunds' action='javascript:transferFunds();'>"
				+ "<table><tr><td>" + bundle.getString("bankForm.recieverNumber") + " </td><td><input type='text' id='recieverAccountNumber'></td></tr>"
				+ "<tr><td>" + bundle.getString("bankForm.amountToSend") + " </td><td><input type='text' id='transferAmount'></td></tr>"
				+ "<tr><td colspan='2'><input type='submit' value='" + bundle.getString("bankForm.transferFunds") + "'></td></tr>"
				+ "</table></form></div><div id='transferLoadingDiv' style='display: none;'>" + bundle.getString("bankForm.loading") + "</div>"
				+ "<div id='transferResultsDiv'></div>"
				+ "<h2 class='title'>" + bundle.getString("bankForm.refreshBalance") + "</h2><p>" + bundle.getString("bankForm.refreshBalance.whatToDo") + "</p>"
				+ "<div id='refreshFormDiv'>"
				+ "<table><tr><td><input type='button' id='refreshFunds' onclick='refreshFunds();' value='" + bundle.getString("bankForm.refreshBalance") + "'></td></tr></table>"
				+ "</div><div id='refreshLoadingSign' style='display: none;'>" + bundle.getString("bankForm.loading") + "</div>"
				+ "<div id='refreshResultsDiv'></div>"
				+ "<h2 class='title'>" + bundle.getString("bankForm.logoutOfAccount") + "</h2><p>" + bundle.getString("bankForm.logoutOfAccount.whatToDo") + "</p>"
				+ "<div id='logoutFormDiv'>"
				+ "<table><tr><td><input type='button' id='logoutButton' onclick='logout();' value='" + bundle.getString("bankForm.logoutFromBankAccount") + "'></td></tr></table>"
				+ "</div><div id='logoutLoadingSign' style='display: none;'>" + bundle.getString("bankForm.loading") + "</div>"
				+ "<div id='logoutResultsDiv'></div>";
		return bankForm;
	}
	
	/**
	 * Method used to return the bank interaction view for the user that is signed into the Direct Object Bank challenge. This method pulls the local level translation from the session submitted
	 * @param accountNumber
	 * @param applicationRoot
	 * @param ses
	 * @return
	 * @throws SQLException
	 */
	public static String bankForm(String accountNumber, String applicationRoot, HttpSession ses) throws SQLException 
	{
		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(ses));
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.challenges.directObject.directObjectBank", locale);
				
		
		float currentBalance = getAccountBalance(accountNumber, applicationRoot);
		String bankForm = "<h2 class='title'>" + bundle.getString("bankForm.yourAccount") + "</h2>" +
				"<p>" + bundle.getString("bankForm.yourAccount.balance") + " <div id='currentAccountBalanceDiv'><b>" + currentBalance + "</b></div></p>";
		if(currentBalance > 5000000)
		{
			//Level Complete As the user has more than 5000000 in account. Return Key
			bankForm += "<h2 class='title'>" + bundle.getString("result.complete") + "</h2><p>" + bundle.getString("result.wellDone") + "<br><br>"
					+ "" + bundle.getString("result.theKeyIs") + " <a>" + Encode.forHtml(Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName"))) + "</a>";
		}
		bankForm += ""
				+ "<input type='hidden' value='" + Encode.forHtmlAttribute(accountNumber) + "' id='currentAccountNumber'>"
				+ "<h2 class='title'>" + bundle.getString("bankForm.transferFunds") + "</h2><p>" + bundle.getString("bankForm.transferFunds.whatToDo") + "</p>"
				+ "<div id='transferFundsForm'><form id='transferFunds' action='javascript:transferFunds();'>"
				+ "<table><tr><td>" + bundle.getString("bankForm.recieverNumber") + " </td><td><input type='text' id='recieverAccountNumber'></td></tr>"
				+ "<tr><td>" + bundle.getString("bankForm.amountToSend") + " </td><td><input type='text' id='transferAmount'></td></tr>"
				+ "<tr><td colspan='2'><input type='submit' value='" + bundle.getString("bankForm.transferFunds") + "'></td></tr>"
				+ "</table></form></div><div id='transferLoadingDiv' style='display: none;'>" + bundle.getString("bankForm.loading") + "</div>"
				+ "<div id='transferResultsDiv'></div>"
				+ "<h2 class='title'>" + bundle.getString("bankForm.refreshBalance") + "</h2><p>" + bundle.getString("bankForm.refreshBalance.whatToDo") + "</p>"
				+ "<div id='refreshFormDiv'>"
				+ "<table><tr><td><input type='button' id='refreshFunds' onclick='refreshFunds();' value='" + bundle.getString("bankForm.refreshBalance") + "'></td></tr></table>"
				+ "</div><div id='refreshLoadingSign' style='display: none;'>" + bundle.getString("bankForm.loading") + "</div>"
				+ "<div id='refreshResultsDiv'></div>"
				+ "<h2 class='title'>" + bundle.getString("bankForm.logoutOfAccount") + "</h2><p>" + bundle.getString("bankForm.logoutOfAccount.whatToDo") + "</p>"
				+ "<div id='logoutFormDiv'>"
				+ "<table><tr><td><input type='button' id='logoutButton' onclick='logout();' value='" + bundle.getString("bankForm.logoutFromBankAccount") + "'></td></tr></table>"
				+ "</div><div id='logoutLoadingSign' style='display: none;'>" + bundle.getString("bankForm.loading") + "</div>"
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
