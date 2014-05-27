<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: openFloor.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(openFloor.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateAdminSession(ses) && tokenCookie != null)
{
// Getting Session Variables
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	<div id="formDiv" class="post">
		<h1 class="title">Open Module Floor Plan</h1>
		<div class="entry">
			<form id="theForm" action="javascript:;">
				<% if (!ModulePlan.isOpenFloor()) {%>
					<p>If you enable an open floor plan, players will be able to attempt any level regardless of previous progress.</p>
					<div id="badData"></div>
					<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
					<table align="center">
						<tr><td colspan="2" align="center">
							<input type="submit" id="submitButton" value="Enable Open Floor Plan"/>
						</td></tr>
					</table>
					<script>					
					$("#theForm").submit(function(){
						var theCsrfToken = $('#csrfToken').val();
						//The Ajax Operation
						var ajaxCall = $.ajax({
							type: "POST",
							url: "openFloorModules",
							data: {
								csrfToken: theCsrfToken
							},
							async: false
						});
						if(ajaxCall.status == 200)
						{
							$("#contentDiv").hide("fast", function(){
								$("#contentDiv").html(ajaxCall.responseText);
								$("#contentDiv").show("fast");
							});
						}
						else
						{
							$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
						}
					});
					</script>
				<% } else { %>
					<p>Open Floor Plan is already enabled!</p>
				<% } %>
			</form>
		</div>
	</div>
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