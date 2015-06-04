<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,java.util.ArrayList,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: upgradePlayer.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(upgradePlayers.jsp): tokenCookie Error:" + htmlE.toString());
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
	//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
	Encoder encoder = ESAPI.encoder();
	String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
	String userName = encoder.encodeForHTML(ses.getAttribute("userName").toString());
	String userRole = encoder.encodeForHTML(ses.getAttribute("userRole").toString());
	String userId = encoder.encodeForHTML(ses.getAttribute("userStamp").toString());
	String ApplicationRoot = getServletContext().getRealPath("");
	ArrayList modules = Getter.getAllModuleInfo(ApplicationRoot);
%>
	<div id="formDiv" class="post">
		<h1 class="title">Create a Cheat Sheet</h1>
		<div class="entry">
			<form id="theForm" action="javascript:;">
					<p>Pick the module that you want to update the cheat cheat for;</p>
					<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
					<table id="cheatSheetTable" align="center">
						<tr><td align="center">
							<select id="moduleId" style='width: 300px;' multiple>
								<% int h = 0;
								while(h < modules.size()) 
								{
									String[] theModule = (String[])modules.get(h); 
								%>
									<option value="<%= encoder.encodeForHTML(theModule[0]) %>"><%= encoder.encodeForHTML(theModule[1]) %></option>
								<% 
								h++; 
								} %>
							</select>
						</td></tr>
						<tr><td align="center">
							<p>Enter the new information for the cheat sheet here;
							<textarea id='newSolution' style="width: 660px; height: 140px;"></textarea>
						</td></tr>
						<tr><td align="center">
							<input type="submit" id="submitButton" value="Create New Cheat Sheet"/>
						</td></tr>
					</table>
					<br>
					<div id="loadingDiv" style="display:none;" class="menuButton">Loading...</div>
					<div id="resultDiv" style="display:none;" class="informationBox"></div>
					<div id="badData"></div>
					<script>					
					$("#theForm").submit(function(){
						//Get Data
						var theCsrfToken = $('#csrfToken').val();
						var theModule = $('#moduleId').val();
						var theSolution = $("#newSolution").val();
						//validation
						if(theModule != null && theSolution != null && theSolution.length > 5)
						{
							$("#loadingDiv").show("fast");
							$("#badData").hide("fast");
							$("#resultDiv").hide("fast");
							$("#cheatSheetTable").slideUp("fast", function(){
								//The Ajax Operation
								var ajaxCall = $.ajax({
									type: "POST",
									url: "createCheat",
									data: {
										moduleId: theModule,
										newSolution: theSolution,
										csrfToken: theCsrfToken
									},
									async: false
								});
								$("#loadingDiv").hide("fast", function(){
									if(ajaxCall.status == 200)
									{
										//Now output Result Div and Show
										$("#resultDiv").html(ajaxCall.responseText);
										$("#resultDiv").show("fast");
									}
									else
									{
										$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
										$("#badData").show("slow");
									}
									$("#cheatSheetTable").slideDown("slow");
								});
							});
						}
						else
						{
							$("#badData").hide("fast", function(){
								$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: Please check your input</p></div>");
								$("#badData").show("fast");
							});
						}
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