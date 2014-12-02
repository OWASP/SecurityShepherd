<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: scoreboard.jsp *************************");

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
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(scoreboard.jsp): tokenCookie Error:" + htmlE.toString());
	}
	// validateAdminSession ensures a valid session, and valid administrator credentials
	// Also, if tokenCookie != null, then the page is good to continue loading
	if (Validate.validateAdminSession(ses) && tokenCookie != null) //Valid Session if
	{
		//Logging Username
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString());
		// Getting Session Variables
		//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
		Encoder encoder = ESAPI.encoder();
		String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
		String ApplicationRoot = getServletContext().getRealPath("");
		if (!ModulePlan.isOpenFloor()) //Floor Plan If
		{
	ResultSet classList = Getter.getClassInfo(ApplicationRoot);
	boolean showClasses = true;
	try
	{
		showClasses = classList.next();
	}
	catch(SQLException e)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not open classList: " + e.toString());
		showClasses = false;
	}
%>
			<style type="text/css">
				div.Numbers {clear:both}
				
				div.Numbers div {border: solid 2px #FFF; margin:2px; color:#FFF;}
				
				div.header div { width:33%; float:left }
				
				.hide{visibility:hidden}
				.vanish{display:none}
				.static{position:static}
				.absolute{position:absolute}	
			</style>
			<div id="formDiv" class="post">
				<h1 class="title">Scoreboard</h1>
				<div class="entry">
					<div id="badData"></div>
					<form id="theForm" action="javascript:;">
							<p>Select the class you would like to show the scoreboard for</p>
							<div id="badData"></div>
							<input type="hidden" id="csrfToken" value="<%=csrfToken%>"/>
							<table align="center">
								<tr>
									<td>
									<select id="classId">
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
													ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Error occured when manipulating classList: " + e.toString());
													showClasses = false;
												}
											}
												%>
									</select>
									</td>
								</tr>
								<tr><td align="center">
									<div id="submitButton"><input type="submit" value="Get Scoreboard"/><input type="button" value="Sort" onclick="SortNumbers()"/></div>
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
									$("#submitButton").hide("fast");
								setInterval(function(){
									//Need to fix the timer for the first run!
									theCsrfToken = $('#csrfToken').val();
									theClass = $("#classId").val();
									//The Ajax Operation
									//$("#badData").hide("fast");
									$("#resultDiv").hide("fast", function(){
										var ajaxCall = $.ajax({
											type: "POST",
											url: "getJsonProgress",
											data: {
												classId: theClass,
												csrfToken: theCsrfToken
											},
											async: false
										});
										var htmlHeap = "";
										if(ajaxCall.status == 200)
										{
											//Do JSON stuff here
											var jsonObj = jQuery.parseJSON(ajaxCall.responseText);
											$.each(jsonObj, function(i, user){
												theUsers[topOfStack] = user.userName;
												topOfStack++;
												htmlHeap = htmlHeap + "<div id='" + user.userName + "' class='unsorted'><table style='font-size: 12px; text-align: left;'><tr><td style='width: 150px;'>" + user.userName + "</td><td style='width:" + user.progressBar + "px; background-color: #A878EF; font-size:18px; font-weight:bold; color:#FFF;' id='userScore'>" + user.score + "</td></tr></table></div>";
											});
											$("#resultDiv").html("<p><div id='numberDiv' class='Numbers'>" + htmlHeap + "</div><p>");
											$("#resultDiv").show("fast");
										}
										else
										{
											$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
											$("#badData").show("slow");
										}
										$("#loadingSign").hide("fast");
									});
								}, 30000);
							});
							
							function blank(){;}
							</script>
							<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
					</form>
				</div>
			</div>
			<%
		}
		else // CTF Mode is not enabled
		{%>
			<div id="formDiv" class="post">
				<h1 class="title">No Scoreboards Available</h1>
				<div class="entry">
				<p>CTF Mode is not enabled, so there are no scoreboards to show.</p>
				</div>
				<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
			</div>
		<%}
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
