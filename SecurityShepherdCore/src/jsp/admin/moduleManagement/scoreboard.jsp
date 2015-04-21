<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: scoreboard Config.jsp *************************");

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
 
if (request.getSession() != null) //Session If
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
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(scoreboardConfig.jsp): tokenCookie Error:" + htmlE.toString());
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
		String ApplicationRoot = getServletContext().getRealPath("");
		ResultSet classList = Getter.getClassInfo(ApplicationRoot);
		boolean showClasses = true;
		try
		{
			showClasses = classList.next();
		}
		catch(SQLException e)
		{
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not open classList: " + e.toString(), ses.getAttribute("userName"));
			showClasses = false;
		}
%>
			<div id="formDiv" class="post">
				<h1 class="title">Configure Scoreboard</h1>
				<div class="entry">
					<br/>
					<div id="badData"></div>
					<a href="javascript:;" style="text-decoration: none;" id="enableScoreboard" title="Enable scoreboard that lists all users regardless of class"><div class="menuButton">Enable Scoreboard</div></a>
					<a href="javascript:;" style="text-decoration: none;" id="enableAdminScoreboard" title="Disable the scoreboard"><div class="menuButton">Enable Scoreboard for Admins</div></a>
					<a href="javascript:;" style="text-decoration: none;" id="disableScoreboard" title="Disable the scoreboard"><div class="menuButton">Disable Scoreboard</div></a>
					
					<br/>
					<h2 class="title">Enable Class Specific Scoreboard</h2>
					<form id="theForm" action="javascript:;">
							<p>Use this form to enable a scoreboard that only lists user from the specific class chosen below;</p>
							<div id="badData"></div>
							<input type="hidden" id="csrfToken" value="<%=csrfToken%>"/>
							<table align="center">
								<tr>
									<td>
										Class: 
									</td>
									<td>
									<select id="classId" style="width: 100%">
										<option value=""></option>
										<%
											if(showClasses)
											{
												try
												{
													do
													{
														String classId = encoder.encodeForHTMLAttribute(classList.getString(1));
														String classYearName = encoder.encodeForHTML(classList.getString(3)) + " " + encoder.encodeForHTML(classList.getString(2));
												%>
													<option value="<%=classId%>"><%=classYearName%></option>
												<%
													}
													while(classList.next());
												}
												catch(SQLException e)
												{
													ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Error occured when manipulating classList: " + e.toString(), ses.getAttribute("userName"));
													showClasses = false;
												}
											}
												%>
									</select>
									</td>
								</tr>
								<tr><td colspan="2" align="center">
									<div id="submitDiv">
										<div id="submitButton" style="display: inline;"><input type="submit" value="Enable Class Scoreboard"/></div>
										 or <div id="submitButton2" style="display: inline;"><input type="button" value="Enable Class Scoreboard for Admins"/></div>
									 </div>
									<div id="loadingSign" style="display: none;"><p>Loading...</p></div>
								</td></tr>
							</table>
							<div id="resultDiv">
							
							</div>
							<script>
							
							
							var theCsrfToken;
							var theClass;
							var theUsers = new Array();
							var topOfStack = 0;
							
							$("#theForm").submit(function(){
								$("#loadingSign").show("slow");
								$("#submitDiv").hide("fast");
								theCsrfToken = $('#csrfToken').val();
								theClass = $("#classId").val();
								//The Ajax Operation
								//$("#badData").hide("fast");
								$("#resultDiv").hide("fast", function(){
									var ajaxCall = $.ajax({
										type: "POST",
										url: "EnableScoreboard",
										data: {
											classId: theClass,
											csrfToken: theCsrfToken
										},
										async: false
									});
									var htmlHeap = "";
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
									$("#loadingSign").hide("fast", function(){
										$("#submitDiv").show("slow");										
									});
								});
							});
							
							$("#submitButton2").click(function(){
								$("#loadingSign").show("slow");
								$("#submitDiv").hide("fast");
								theCsrfToken = $('#csrfToken').val();
								theClass = $("#classId").val();
								//The Ajax Operation
								//$("#badData").hide("fast");
								$("#resultDiv").hide("fast", function(){
									var ajaxCall = $.ajax({
										type: "POST",
										url: "EnableScoreboard",
										data: {
											classId: theClass,
											restricted: "true",
											csrfToken: theCsrfToken
										},
										async: false
									});
									var htmlHeap = "";
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
									$("#loadingSign").hide("fast", function(){
										$("#submitDiv").show("slow");										
									});
								});
							});
							
							$("#enableScoreboard").click(function(){
								$("#loadingSign").show("slow");
								$("#submitDiv").hide("fast");
								theCsrfToken = $('#csrfToken').val();
								//The Ajax Operation
								//$("#badData").hide("fast");
								$("#resultDiv").hide("fast", function(){
									var ajaxCall = $.ajax({
										type: "POST",
										url: "EnableScoreboard",
										data: {
											classId: "",
											csrfToken: theCsrfToken
										},
										async: false
									});
									var htmlHeap = "";
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
									$("#loadingSign").hide("fast", function(){
										$("#submitDiv").show("slow");										
									});
								});
							});
							
							$("#enableAdminScoreboard").click(function(){
								$("#loadingSign").show("slow");
								$("#submitDiv").hide("fast");
								theCsrfToken = $('#csrfToken').val();
								//The Ajax Operation
								//$("#badData").hide("fast");
								$("#resultDiv").hide("fast", function(){
									var ajaxCall = $.ajax({
										type: "POST",
										url: "EnableScoreboard",
										data: {
											classId: "",
											restricted: "true",
											csrfToken: theCsrfToken
										},
										async: false
									});
									var htmlHeap = "";
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
									$("#loadingSign").hide("fast", function(){
										$("#submitDiv").show("slow");										
									});
								});
							});
							
							$("#disableScoreboard").click(function(){
								$("#loadingSign").show("slow");
								$("#submitDiv").hide("fast");
								theCsrfToken = $('#csrfToken').val();
								//The Ajax Operation
								$("#resultDiv").hide("fast", function(){
									var ajaxCall = $.ajax({
										type: "POST",
										url: "DisableScoreboard",
										data: {
											classId: "",
											csrfToken: theCsrfToken
										},
										async: false
									});
									var htmlHeap = "";
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
									$("#loadingSign").hide("fast", function(){
										$("#submitDiv").show("slow");										
									});
								});
							});
							</script>
							<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
					</form>
				</div>
			</div>
	<% 
	} //Valid Session If
	else
	{
	response.sendRedirect("../../loggedOutSheep.html");
	}
} //Session If
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
%>
