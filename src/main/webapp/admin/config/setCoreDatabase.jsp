<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: setCoreDatabase.jsp *************************");

/**
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
 
if (request.getSession() != null)
{
HttpSession ses = request.getSession();
Getter get = new Getter();
//Getting CSRF Token from client
Cookie tokenCookie = null;
try
{
	tokenCookie = Validate.getToken(request.getCookies());
}
catch(Exception htmlE)
{
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(setCoreHostAddress.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
// Token is now validated when accessing admin pages to stop attackers causing other users to tigger logs of access attempts
Object tokenParmeter = request.getParameter("csrfToken");
if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
{
	//Logging Username
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
// Getting Session Variables
//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety

String csrfToken = Encode.forHtml(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	<h1 class="title">Core Database Server Info</h1>
	<p>
		If you are using a non-standard database configuration for Security Shepherd, you will need to specify the following information for your core database.
	</p>
	
	<br/>
	<br/>
	<div id="badData" style="display: none;"></div>
	<div id="theStep">
	<form action="javascript:;" id="leForm">
		<table align="center">
			<tr><td><p>Database URL:</p></td><td><input type="text" id="databaseURL" value=""/></td></tr>
			<tr><td><p>Username:</p></td><td><input type="text" id="databaseUsername" /></td></tr>
			<tr><td><p>Password:</p></td><td><input type="password" id="databasePassword" /></td></tr>
			<tr><td colspan="2" align="center">
				<input type="submit" id="submitButton" value="Set Core Database Data"/>
			</td></tr>
		</table>
	</form>
	<div id="loadingSign" style="display:none;" class="menuButton">Loading...</div>
	<div id="resultsDiv" class="informationBox" style="display: none;"></div>
	</div>
	<script>
	$("#leForm").submit(function(){
		$("#badData").hide("fast");
		$("#resultsDiv").hide("fast");
		$("#loadingSign").show("fast");
		var url = $("#databaseURL").val();
		var username = $("#databaseUsername").val();
		var password = $("#databasePassword").val();
		$("#submitButton").hide("fast");
		$("#theStep").slideUp("fast", function(){
			var ajaxCall = $.ajax({
				dataType: "text",
				type: "POST",
				url: "changeCoreDatabase",
				data: {
					databaseUrl: url,
					databaseUsername: username,
					databasePassword: password,
					csrfToken: "<%= csrfToken %>"
				},
				async: false
			});
			$("#loadingSign").hide("fast", function(){
				if(ajaxCall.status == 200)
				{
					$("#resultsDiv").html(ajaxCall.responseText);
					$("#resultsDiv").show("fast");
				}
				else
				{
					$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
					$("#badData").show("slow");
				}
				$("#theStep").slideDown("slow");
				$('html, body').animate({
			        scrollTop: $("#resultsDiv").offset().top
			    }, 1000);
			});
		});
	});
	</script>
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
	<%
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
%>