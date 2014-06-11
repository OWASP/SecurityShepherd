<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: setHostAddress.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(setHostAddress.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateAdminSession(ses) && tokenCookie != null)
{
	//Logging Username
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString());
// Getting Session Variables
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	<h1 class="title">Exposed Server Address</h1>
	Your Host's IP address can be accessed by going to command prompt of the server and typing; <a>ipconfig</a> for windows servers or <a>ifconfig</a> for linux/mac servers<br/><br/>
	Current Exposed Server Address = <a><%= encoder.encodeForHTML(ExposedServer.getUrl()) %></a>
	<br/>
	<br/>
	<div id="badData" style="display: none;"></div>
	<div id="theStep">
	<form action="javascript:;" id="leForm">
	New Host Address <input type="text" id="exposedHostAddress" style="width: 300px;"/><input type="submit" id="submitButton" value="Set Host Address"/>
	<div id="loadingSign" style="display: none;"><p>Loading...</p></div>
	</form>
	</div>
	<script>
	$("#leForm").submit(function(){
		$("#badData").hide("fast");
		var theHostAddress = $("#exposedHostAddress").val();
		if(theHostAddress.length > 8)
		{
			$("#submitButton").hide("fast");
			$("#loadingSign").show("slow", function(){
				var ajaxCall = $.ajax({
					dataType: "text",
					type: "POST",
					url: "changeHostAddress",
					data: {
						hostAddress: theHostAddress,
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
		}
		else
		{
			$("#badData").html("<font color='red'>Invalid Application Root. Too Short.</font>");
			$("#badData").show("slow");
		}
	});
	</script>
	<% if(ExposedServer.googleAnalyticsOn) { %>
				<%= ExposedServer.googleAnalyticsScript %>
			<% } %>
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