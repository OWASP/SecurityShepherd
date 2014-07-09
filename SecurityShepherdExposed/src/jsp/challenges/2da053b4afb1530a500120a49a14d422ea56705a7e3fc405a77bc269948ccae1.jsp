<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage="" %>
<%
	//No Quotes In level Name
String levelName = "Insecure Cyrpto Challenge 3";
//Alphanumeric Only
String levelHash = "2da053b4afb1530a500120a49a14d422ea56705a7e3fc405a77bc269948ccae1";
//Level blurb can be writen here in HTML OR go into the HTML body and write it there. Nobody will update this but you
String levelBlurb = "The result key to this level is the same as the encryption key used in the following sub application. Break the cipher and recover the encryption! The result key is in all caps.";
//Logs the IP, Forwarded IP that acceeded this level with the level name in the debug for convience. If you want to log more stuff in the JSP use this as an example
try
{
	if (request.getSession() != null)
	{
		HttpSession ses = request.getSession();
		String userName = (String) ses.getAttribute("decyrptedUserName");
		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + userName);
	}
}
catch (Exception e)
{
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed");
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not recover username: " + e.toString());
}
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
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= levelName %></title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= levelName %></h2>
			<p> 
				<%= levelBlurb %>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						Cipher text to decrypt: 
					</td><td>
						<input type="text" width="130px" id="userInput" autocomplete="off">
					</td></tr>
					<tr><td colspan="2">			
						<div id="submitButton">
						<input type="submit" value="Decrypt"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"><h2>Cipher text Example</h2><p>Try to decrypt this: IAAAAEkQBhEVBwpDHAFJGhYHSBYEGgocAw==</p></div>
			</p>
		</div>
		<% /*If you need to call the Server Do it like this */ %>
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
						$("#resultsDiv").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
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