<%@page import="servlets.module.challenge.DirectObjectBankLogin"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*, servlets.module.challenge.DirectObjectBankLogin" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
/**
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
 *
 * @author Mark Denihan
 */
//No Quotes In level Name
String levelName = "Insecure Direct Object Reference Bank Challenge";
//Alphanumeric Only
String levelHash = "1f0935baec6ba69d79cfb2eba5fdfa6ac5d77fadee08585eb98b130ec524d00c"; 

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.challenges.directObject." + levelHash, locale);
//Used more than once translations
String i18nChallengeName = bundle.getString("challenge.challengeName");

ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " Accessed");
if (request.getSession() != null)
{
	HttpSession ses = request.getSession();
	//Getting CSRF Token from client
	Cookie tokenCookie = null;
	try
	{
		tokenCookie = Validate.getToken(request.getCookies());
	}
	catch(Exception htmlE)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName +".jsp: tokenCookie Error:" + htmlE.toString());
	}
	// validateSession ensures a valid session, and valid role credentials
	// If tokenCookie == null, then the page is not going to continue loading
	if (Validate.validateSession(ses) && tokenCookie != null)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
		//Is the user signed in?
		boolean bankSessionDetected = false;
		String currentBankAccountNumber = new String();
		if(ses.getAttribute("directObjectBankAccount") != null)
		{
			currentBankAccountNumber = ses.getAttribute("directObjectBankAccount").toString();
			bankSessionDetected = true;
		}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= levelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= levelName %></h2>
			<p> 
				<%= bundle.getString("challenge.whatToDo") %>
				<br/>
				<br/>
				<h1 class='title'><%= bundle.getString("insecureBank.title") %></h1>
				<p><%= bundle.getString("insecureBank.message") %></p>
				<div id="unauthDiv" <% if(bankSessionDetected){ %> style="display: none;" <% } %>>
					<h2 class="title"><%= bundle.getString("insecureBank.signInNow") %></h2>
					<p><%= bundle.getString("insecureBank.signInNow.message") %></p>
					<div id="loginFormDiv">
						<form id="loginForm" action="javascript:;">
							<table>
								<tr><td><%= bundle.getString("loginForm.holder") %> </td><td><input type="text" id="loginAccountHolder" autocomplete="off"></td></tr>
								<tr><td><%= bundle.getString("loginForm.password") %> </td><td><input type="password" id="loginAccountPassword" autocomplete="off"></td></tr>
								<tr><td colspan="2"><input type="submit" value="<%= bundle.getString("loginForm.signIn") %>"></td></tr>
							</table>
						</form>
					</div>
					<p style="display: none;" id="loginLoadingSign"><%= bundle.getString("form.loading") %></p>
					<div id="loginResultsDiv"></div>
					<br>
					<br>
					<h2 class="title"><%= bundle.getString("register.makeAccount") %></h2>
					<p><%= bundle.getString("register.makeAccount.message") %></p>
					<div id="registrationFormDiv">
						<form id="registrationForm" action="javascript:;">
							<table>
								<tr><td><%= bundle.getString("loginForm.holder") %> </td><td><input type="text" id="registrationAccountHolder" autocomplete="off"></td></tr>
								<tr><td><%= bundle.getString("loginForm.password") %> </td><td><input type="password" id="registrationAccountPassword" autocomplete="off"></td></tr>
								<tr><td colspan="2"><input type="submit" value="<%= bundle.getString("register.createAccount") %>"></td></tr>
							</table>
						</form>
					</div>
					<p style="display: none;" id="registrationLoadingSign"><%= bundle.getString("form.loading") %></p>
					<div id="registrationResultsDiv"></div>
				</div>
				<div id="authenticatedDiv" <% if(!bankSessionDetected){ %> style="display: none;"<% } %>>
				<% if(bankSessionDetected){ %>
					<%= DirectObjectBankLogin.bankForm(currentBankAccountNumber, getServletContext().getRealPath(""), ses) %>
				<% } %>
				</div>
			</p>
		</div>
		<script>
			$("#loginForm").submit(function(){
				var theAccountHolder = $("#loginAccountHolder").val();
				var theAccountPass = $("#loginAccountPassword").val();
				$("#loginResultsDiv").hide("fast");
				$("#loginLoadingSign").show("slow");
				$("#loginFormDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							accountHolder: theAccountHolder, 
							accountPass: theAccountPass
						},
						async: false
					});
					$("#loginLoadingSign").hide("fast", function(){
						if(ajaxCall.status == 200 && !ajaxCall.responseText.startsWith("ERROR"))
						{
							$("#authenticatedDiv").html(ajaxCall.responseText);
							$("#unauthDiv").slideUp("fast");
							$("#authenticatedDiv").slideDown("slow");
						}
						else
						{
							if(ajaxCall.responseText.startsWith("ERROR"))
								$("#loginResultsDiv").html(ajaxCall.responseText);
							else
								$("#loginResultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
							$("#loginResultsDiv").show("fast");
							$("#loginFormDiv").slideDown("slow");
						}
					});
				});
			});
			
			$("#registrationForm").submit(function(){
				var theAccountHolder = $("#registrationAccountHolder").val();
				var theAccountPass = $("#registrationAccountPassword").val();
				$("#registrationResultsDiv").hide("fast");
				$("#registrationLoadingSign").show("slow");
				$("#registrationFormDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>Reg",
						data: {
							accountHolder: theAccountHolder, 
							accountPass: theAccountPass
						},
						async: false
					});
					$("#registrationLoadingSign").hide("fast", function(){
						if(ajaxCall.status == 200 && !ajaxCall.responseText.startsWith("ERROR"))
						{
							$("#registrationResultsDiv").html(ajaxCall.responseText);
						}
						else
						{
							$("#registrationResultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + ".</p>");
						}
						$("#registrationResultsDiv").show("fast");
						$("#registrationFormDiv").slideDown("slow");
					});
				});
			});
			
			function refreshFunds(){
				console.log("Updating Balance");
				var theAccountNumber = $("#currentAccountNumber").val();
				console.log("The Account Number: " + theAccountNumber);
				$("#refreshLoadingSign").show("slow");
				$("#refreshFormDiv").slideUp("fast", function(){
					console.log("Sending Ajax Request");
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>CurrentBalance",
						data: {
							accountNumber: theAccountNumber
						},
						async: false
					});
					$("#refreshLoadingSign").hide("fast", function(){
						if(ajaxCall.status == 200 && !ajaxCall.responseText.startsWith("ERROR"))
						{
							console.log("Updating Balance...");
							//Relist Current mode
							$("#currentAccountBalanceDiv").fadeOut("slow", function(){
								$("#currentAccountBalanceDiv").text(ajaxCall.responseText);
							});
							$("#currentAccountBalanceDiv").fadeIn("slow");
						}
						else
						{
							console.log("Error Detected");
							if(ajaxCall.responseText.startsWith("ERROR"))
								$("#refreshResultsDiv").html("ajaxCall.responseText");
							else
								$("#refreshResultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
						}
						$("#refreshResultsDiv").show("fast");
						$("#refreshFormDiv").slideDown("slow");
					});
				});
				console.log("Finished!");
			}
			
			function transferFunds(){
				console.log("Transfering Funds");
				var theRecieverNumber = $("#recieverAccountNumber").val();
				console.log("theRecieverNumber: " + theRecieverNumber);
				var theSenderNumber = $("#currentAccountNumber").val();
				console.log("theSenderNumber: " + theSenderNumber);
				var theTransferAmount = $("#transferAmount").val();
				console.log("theTransferAmount: " + theTransferAmount);
				console.log("Got Data from Form");
				console.log("Hiding results div");
				$("#transferResultsDiv").hide("fast");
				console.log("Showing Loading Div");
				$("#transferLoadingDiv").show("slow");
				console.log("Hiding transferFundsForm");
				$("#transferFundsForm").slideUp("fast", function(){
					console.log("Sending Ajax Request");
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>Transfer",
						data: {
							senderAccountNumber: theSenderNumber, 
							recieverAccountNumber: theRecieverNumber,
							transferAmount: theTransferAmount
						},
						async: false
					});
					$("#transferLoadingDiv").hide("fast", function(){
						console.log("Putting Response Info In DOM");
						if(ajaxCall.status == 200)
						{
							$("#transferResultsDiv").html(ajaxCall.responseText);
						}
						else
						{
							$("#transferResultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
						}
						$("#transferResultsDiv").show("fast");
						$("#transferFundsForm").slideDown("slow");
					});
				});
				console.log("Done!");
			}
			
			function logout(){
				console.log("Signing Out");
				console.log("Hiding Results Div");
				$("#logoutResultsDiv").hide("fast");
				console.log("Showing Loading Div");
				$("#logoutLoadingSign").show("slow");
				console.log("Hiding Logout Form Div");
				$("#logoutFormDiv").slideUp("fast", function(){
					console.log("Sending Ajax Request");
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>Logout",
						async: false
					});
					console.log("Ajax Send, Hiding Loading Div");
					$("#logoutLoadingSign").hide("fast", function(){
						console.log("Checking Ajax Response");
						if(ajaxCall.status == 200 && !ajaxCall.responseText.startsWith("ERROR"))
						{
							console.log("Hiding Authenticated Div and showing Login Div");
							$("#authenticatedDiv").slideUp("fast");
							$("#authenticatedDiv").html("");
							$("#unauthDiv").slideDown("slow");
							$("#loginAccountHolder").val("");
							$("#loginAccountPassword").val("");
							$("#loginFormDiv").show("slow");
							$('html, body').animate({
						        scrollTop: $("#loginFormDiv").offset().top
						    }, 1000);
						}
						else
						{
							console.log("Something Went Wrong: " + ajaxCall.responseText);
							if(ajaxCall.responseText.startsWith("ERROR"))
								$("#logoutResultsDiv").html(ajaxCall.responseText);
							else
								$("#logoutResultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
							$("#logoutResultsDiv").show("fast");
							$("#logoutFormDiv").slideDown("slow");
						}
					});
				});
				console.log("Logout Function Complete");
			}
		</script>
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>
<%
	}
	else
	{
		response.sendRedirect("../loggedOutSheep.html");
	}
}
else
{
	response.sendRedirect("../loggedOutSheep.html");
}
%>