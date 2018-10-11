<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: updateRegistration.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(updateRegistration.jsp): tokenCookie Error:" + htmlE.toString());
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

String csrfToken = Encode.forHtmlAttribute(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	
	<div id="formDiv" class="post">
		<h1 class="title"><div id="displayTitle" style="display: inline;"><% if (OpenRegistration.isEnabled()) { %>Close<% } else { %>Open<% } %></div> Registration</h1>
		<div class="entry">
			<form id="theForm" action="javascript:;">
				<div id="toggleDiv">
					<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
					<div id="closeDiv" <% if (!OpenRegistration.isEnabled()) {%>style="display: none;"<% } %>>
						<p>This function will close the registration functionality. This will prevent users from visiting registration pages and will block requests made to Registration Servlets.</p>
						<table align="center">
							<tr><td colspan="2" align="center">
								<input type="submit" id="submitButton" value="Close Registration"/>
							</td></tr>
						</table>
					</div>
					<div id="openDiv" <% if (OpenRegistration.isEnabled()) {%>style="display: none;"<% } %>>
						<p>This function will open the registration functionality. Users will have to refresh the login page to see the link pointing to the Shepherd registration page.</p>
						<table align="center">
							<tr><td colspan="2" align="center">
								<input type="submit" id="submitButton" value="Open Registration"/>
							</td></tr>
						</table>
					</div>
				</div>
				<br />
				<div id="loadingDiv" style="display:none;" class="menuButton">Loading...</div>
				<div id="resultDiv" style="display:none;" class="informationBox"></div>
				<div id="badData"></div>
			</form>
			<script>					
				$("#theForm").submit(function(){
					//Get Data
					var theCsrfToken = $('#csrfToken').val();
					//Hide&Show Stuff
					$("#loadingDiv").show("fast");
					$("#badData").hide("fast");
					$("#resultDiv").hide("fast");
					$("#toggleDiv").slideUp("fast", function(){
						//The Ajax Operation
						var ajaxCall = $.ajax({
							type: "POST",
							url: "updateRegistration",
							data: {
								csrfToken: theCsrfToken
							},
							async: false
						});
						$("#loadingDiv").hide("fast", function(){
							if(ajaxCall.status == 200)
							{
								//Function Worked, Flip the Close/Open Menu
								console.log("Current State: " + $('#displayTitle').text());
								if($('#displayTitle').text() == "Open"){
									console.log("Displaying Close Dialog");
									$("#closeDiv").show();
									$("#openDiv").hide();
									$("#displayTitle").fadeOut("fast", function(){
										$("#displayTitle").text("Close");
									});
									$("#displayTitle").fadeIn("slow");
								}
								else {
									console.log("Displaying Open Dialog");
									$("#openDiv").show();
									$("#closeDiv").hide();
									$("#displayTitle").fadeOut("slow", function(){
										$("#displayTitle").text("Open");
									});
									$("#displayTitle").fadeIn("slow");
								}
								//Now output Result Div and Show
								$("#resultDiv").html(ajaxCall.responseText);
								$("#resultDiv").show("fast");
							}
							else
							{
								$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
								$("#badData").show("slow");
							}
							$("#toggleDiv").slideDown("slow");
						});
					});
				});
			</script>
			<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
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