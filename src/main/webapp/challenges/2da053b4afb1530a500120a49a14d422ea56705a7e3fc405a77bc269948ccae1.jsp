<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
/**
 * This level uses XOR's user input with a key. the vulnerability in the cipher is if the attacker submits spaces, the key will be revealed after the XOR.
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
String levelName = "Insecure Cyrpto Challenge 3";
//Alphanumeric Only
String levelHash = "2da053b4afb1530a500120a49a14d422ea56705a7e3fc405a77bc269948ccae1";

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.challenges.insecureCryptoStorage.insecureCryptoStorage", locale);
//Used more than once translations
String i18nLevelName = bundle.getString("insecureCryptoStorage.3.challengename");
//Level blurb can be writen here in HTML OR go into the HTML body and write it there. Nobody will update this but you
String levelBlurb = bundle.getString("insecureCyrptoStorage.3.whatToDo");

//Logs the IP, Forwarded IP that acceeded this level with the level name in the debug for convience. If you want to log more stuff in the JSP use this as an example
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

%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= i18nLevelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= i18nLevelName %></h2>
			<p> 
				<%= levelBlurb %>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<%= bundle.getString("insecureCyrptoStorage.3.ciphertextToDecrypt") %>
					</td><td>
						<input type="text" width="130px" id="userInput" autocomplete="off">
					</td></tr>
					<tr><td colspan="2">			
						<div id="submitButton">
						<input type="submit" value="<%= bundle.getString("insecureCyrptoStorage.decrypt") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("insecureCyrptoStorage.loading") %></p>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv">
					<h2 class="title"><%= bundle.getString("insecureCyrptoStorage.3.ciphertextExample") %></h2>
					<p><%= bundle.getString("insecureCyrptoStorage.3.tryDecryptThis") %> IAAAAEkQBhEVBwpDHAFJGhYHSBYEGgocAw==</p>
				</div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				var theUserInput = $("#userInput").val();
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							userData: theUserInput
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv").html(ajaxCall.responseText);
					}
					else
					{
						$("#resultsDiv").html("<p> <%= bundle.getString("insecureCyrptoStorage.errorOccurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv").show("slow", function(){
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
						});
					});
				});
			});
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