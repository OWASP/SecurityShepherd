<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: stopHere.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(stopHere.jsp): tokenCookie Error:" + htmlE.toString());
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
String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	<div id="formDiv" class="post">
		<h1 class="title">Set a Module Blocker</h1>
		<div class="entry">
			<div id="badData"></div>
			<form id="theForm" action="javascript:;">
					<p>Selecting a module blocker will not allow users to take that specific module. In a CTF environement this will stop them from progress past this point until you disable it or enable it to a further down the line module.<br/><br/>
					
					Select the module you would like to see the feedback from and the message informing them when the blocker will be lifted. </p>
					<div id="badData"></div>
					<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
					<div  id="submitButton" align="center">
						<div>
							<select id="theModule" style="width: 300px">
								<%= Getter.getModulesInOptionTagsCTF(ApplicationRoot) %>
							</select>
						</div>
						<div>
							<textarea style="width: 350px; height: 100px;" id="message"></textarea>
						</div>
						<div><input type="submit" value="Enable Block"/></div>
					</div>
					<div id="loadingSign" style="display: none;"><p>Loading...</p></div> 
					<div id="resultDiv"></div>
					<script>					
					$("#theForm").submit(function(){
						var theCsrfToken = $('#csrfToken').val();
						var theModule = $("#theModule").val();
						//The Ajax Operation
						$("#badData").hide("fast");
						$("#submitButton").hide("fast");
						$("#loadingSign").show("slow");
						$("#resultDiv").hide("fast", function(){
							var ajaxCall = $.ajax({
								type: "POST",
								url: "stopHere",
								data: {
									moduleId: theModule,
									csrfToken: theCsrfToken
								},
								async: false
							});
							if(ajaxCall.status == 200)
							{
								$("#resultDiv").html(ajaxCall.responseText);
								$("#resultDiv").show("fast");
							}
							else
							{
								$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
								$("#badData").show("slow");
							}
						});
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
						});
					});
					</script>
					<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
			</form>
		</div>
	</div>
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