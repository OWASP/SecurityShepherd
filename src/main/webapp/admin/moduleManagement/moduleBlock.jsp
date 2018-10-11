<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

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
		<h1 class="title">Set a Module Blocker</h1>
		<div class="entry">
			
			<form id="theForm" action="javascript:;">
					<p>Selecting a module blocker will not allow users to take that specific module. In a CTF environment this will stop them from progress past this point until you disable it or enable it to a further down the line module.<br/><br/>
					
					Select the module you would like to see the feedback from and the message informing them when the blocker will be lifted. </p>
					<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
					<div id="enableBlockForm" align="center">
						<table>
							<tr>
								<td>The Module To Block: </td>
								<td>
									<select id="theModule" style="width: 300px">
										<%= Getter.getModulesInOptionTagsCTF(ApplicationRoot) %>
									</select>
								</td>
							</tr><tr>
								<td>Blocked Message to Give: </td>
								<td><textarea id="blockedMessage" maxlength="500" style="width: 300px; height: 80px;"></textarea>
							</tr><tr>
								<td colspan="2" align="center">
									<input type="submit" value="Enable Block" style="width: 150px; height: 25px;"/>
								</td>
							</tr>
						</table>
					</div>
					<% if(ModuleBlock.blockerEnabled){ %>
					<div id="removeModuleBlockDiv">
						<h2 class="title">Remove Module Block</h2>
						<p>
							Do you wish to remove the current module blocker? You can do this by clicking the following button;
						</p>
						<a href="javascript:;" style="text-decoration: none;" id="removeModuleBlockButton" title="Remove Module Block">
							<div class="menuButton">Remove Module Block</div>
						</a>
					</div>
					<% } %>
					<br>
					<div id="loadingSign" style="display:none;" class="menuButton">Loading...</div>
					<div id="resultDiv" class="informationBox" style="display:none;"></div>
					<div id="badData"></div>
					
					<script>					
					$("#theForm").submit(function(){
						var theCsrfToken = $('#csrfToken').val();
						var theMessage = $("#blockedMessage").val();
						var theModule = $("#theModule").val();
						console.log("message: " + theMessage);
						//The Ajax Operation
						$("#badData").hide("fast");
						$("#resultDiv").hide("fast");
						$("#loadingSign").show("fast");
						$("#enableBlockForm").slideUp("fast", function(){
							var ajaxCall = $.ajax({
								type: "POST",
								url: "enableModuleBlock",
								data: {
									moduleId: theModule,
									blockedMessage: theMessage,
									csrfToken: theCsrfToken
								},
								async: false
							});
							$("#loadingSign").hide("fast", function(){
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
								$("#enableBlockForm").slideDown("slow");
							});
						});
					});
					<% if(ModuleBlock.blockerEnabled){ %>
						$("#removeModuleBlockButton").click(function(){
							var theCsrfToken = $('#csrfToken').val();
							//The Ajax Operation
							$("#badData").hide("fast");
							$("#resultDiv").hide("fast");
							$("#loadingSign").show("fast");
							$("#removeModuleBlockDiv").slideUp("fast", function(){
								var ajaxCall = $.ajax({
									type: "POST",
									url: "removeModuleLock",
									data: {
										csrfToken: theCsrfToken
									},
									async: false
								});
								$("#loadingSign").hide("fast", function(){
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
									$("#removeModuleBlockDiv").slideDown("slow");
								});
							});
						});
					<% } %>
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