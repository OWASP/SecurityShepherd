<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

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
if (Validate.validateAdminSession(ses) && tokenCookie != null)
{
// Getting Session Variables
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
	%>
	<h1 class="title">Core Database Server Info</h1>
	If you are using a non-standard database configuration for Security Shepherd, you will need to specify the following information for your core database.
	The core database and exposed databases are normally seperated to seperate database servser to minimise any potential attacks on the core server data. 
	If you run both schemas on the same database server you will still be secure, unless a custom level has been added that exposed your entire database to takeover! Database seperation is recommended.
	
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
				<div id="loadingSign" style="display: none;"><p>Loading...</p></div>
			</td></tr>
		</table>
	</form>
	</div>
	<script>
	$("#leForm").submit(function(){
		$("#badData").hide("fast");
		var url = $("#databaseURL").val();
		var username = $("#databaseUsername").val();
		var password = $("#databasePassword").val();
		$("#submitButton").hide("fast");
		$("#loadingSign").show("slow", function(){
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
			$("#theStep").hide("fast", function(){
				if(ajaxCall.status == 200)
				{
					$("#theStep").html(ajaxCall.responseText);
				}
				else
				{
					$("#badData").html("<p> An Error Occured: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					$("#badData").show("slow");
				}
			});
			$("#loadingSign").hide("fast", function(){
				$("#theStep").show("slow");
			});
		});
	});
	</script>
	<%
}
else
{
response.sendRedirect("login.jsp");
}
}
else
{
response.sendRedirect("login.jsp");
}
%>