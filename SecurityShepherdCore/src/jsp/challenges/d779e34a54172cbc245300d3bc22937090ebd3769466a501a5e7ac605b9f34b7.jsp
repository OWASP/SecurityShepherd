<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage="" %>
<%
// Broken Authentication and Session Management Challenge Two

/**
 * <br/><br/>
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

String levelName = "Session Management Challenge Two";
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " Accessed");
if (request.getSession() != null)
{
 	HttpSession ses = request.getSession();
 	//Getting CSRF Token from client
 	Cookie tokenCookie = null;
 	try
 	{
 		tokenCookie = Validate.getToken(request.getCookies());
 	}
 	catch(Exception htmlE)
 	{
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName +".jsp: tokenCookie Error:" + htmlE.toString());
 	}
 	// validateSession ensures a valid session, and valid role credentials
 	// If tokenCookie == null, then the page is not going to continue loading
 	if (Validate.validateSession(ses) && tokenCookie != null)
 	{
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));

%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Session Management Challenge Two</title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">Broken Authentication and Session Management Challenge Two</h2>
			<p> 
				Only an <a>admin</a> of the following sub-application can retrieve the result key to this challenge.
				<br />
				<form id="leForm" action="javascript:;">
					<div id="resultsDiv">
						<table>
							<tr><td>
								User name:
							</td><td>
								<input type="text" id="subName"/>
							</td></tr>
							<tr><td>
								Password:
							</td><td>
								<input type="password" id="subPassword"/>
							</td></tr>
							<tr><td colspan="2">
								<div id="submitButton"><input type="submit" value="Sign In"/></div>
							</td></tr>
						</table>
					</div>
					<small><a id="forgottenPasswordLink" href="#">Have you forgotten your password?</a></small>
				</form>
				<p style="display: none;" id="loadingSign">Loading...</p>
				<br/>
				<div id="forgottenPassDiv" style="display: none;">
					<form id="leForm2" action="javascript:;">
						<h2 class='title'>Reset Password</h2>
						<p>Please enter your <a>email address</a>. You will be sent an email with a new temporary password</p>
						<table>
							<tr></td>
								<div id="resetSubmit"><input id="resetEmail" type="text"/><input type="submit" value="Reset Password"/></div>
								<p style="display: none;" id="resetLoadingSign">Loading...</p>
							</td></tr>
						</table>
					</form>
					<div id="resultsDiv2"></div>
				</div>
				
				
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				
				var theSubName = $("#subName").val();
				var theSubPassword = $("#subPassword").val();
				$("#loadingSign").show("slow");
				$("#forgottenPassDiv").hide("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "d779e34a54172cbc245300d3bc22937090ebd3769466a501a5e7ac605b9f34b7",
						data: {
							subName: theSubName,
							subPassword: theSubPassword
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv").html(ajaxCall.responseText);
					}
					else
					{
						$("#resultsDiv").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#loadingSign").hide("fast", function(){
						$("#resultsDiv").show("slow");
					});
				});
			});
			
			$("#leForm2").submit(function(){
				var theResetEmail = $("#resetEmail").val();
				$("#resetSubmit").hide("fast");
				$("#resetLoadingSign").show("slow");
				$("#resultsDiv2").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "f5ddc0ed2d30e597ebacf5fdd117083674b19bb92ffc3499121b9e6a12c92959",
						data: {
							subEmail: theResetEmail
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv2").html("<p>Password reset request sent.</p>");
					}
					else
					{
						$("#resultsDiv2").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv2").show("slow", function(){
						$("#resetLoadingSign").hide("fast", function(){
							$("#resetSubmit").show("slow");
						});
					});
				});
			});
			
			$("#forgottenPasswordLink").click(function(){
				$("#forgottenPassDiv").toggle("slow", function(){
					//Animation Complete
					;
				});
			});
		</script>
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>
<% 
	}
	else
	{
		response.sendRedirect("../loggedOutSheep.html");
	}
}
else
{
	response.sendRedirect("../loggedOutSheep.html");
}
%>