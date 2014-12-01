<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage="" %>
<%
/**
 * Broken Authentication and Session Management Challenge Three
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

String levelName = "Session Management Challenge Three";
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
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString());
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Session Management Challenge Three</title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">Broken Authentication and Session Management Challenge Three</h2>
			<p> 
				Only an <a>admin</a> of the following sub-application can retrieve the result key 
				to this challenge. You have been granted user privileges because the admins need somebody 
				to boss around.
				<br />
				<form id="leForm" action="javascript:;">
					<div id="resultsDiv">
						<table>
							<tr><td>
								User name:
							</td><td>
								<input type="text" id="subUserName"/>
							</td></tr>
							<tr><td>
								Password:
							</td><td>
								<input type="password" id="subUserPassword"/>
							</td></tr>
							<tr><td colspan="2">
								<div id="submitButton"><input type="submit" value="Sign In"/></div>
							</td></tr>
						</table>
					</div>
					<input type="button" id="showUserControl" value="Toggle user functions"/>
				</form>
				<p style="display: none;" id="loadingSign">Loading...</p>
				<br/>
				<div id="userControl" style="display: none;">
					<form id="leForm2" action="javascript:;">
						<h2 class='title'>Change Password</h2>
						<p>Please enter your new password for this sub application!</p>
						<table>
							<tr><td>
								New Password:
							</td><td>
								<input type="password" id="passOne"/>
							</td></tr>
							<tr><td>
								Confirm Password:
							</td><td>
								<input type="password" id="passTwo"/>
							</td></tr>
							<tr><td colspan="2">
								<div id="resetSubmit"><input type="submit" value="Change Password"/></div>
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
				var theSubName = $("#subUserName").val();
				var theSubPassword = $("#subUserPassword").val();
				$("#loadingSign").show("slow");
				$("#forgottenPassDiv").hide("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "t193c6634f049bcf65cdcac72269eeac25dbb2a6887bdb38873e57d0ef447bc3",
						data: {
							subUserName: theSubName,
							subUserPassword: theSubPassword
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
				document.cookie="current=WjNWbGMzUXhNZz09";
				var theNewPassword = $("#passOne").val();
				var theNewPasswordAgain = $("#passTwo").val();
				if(theNewPassword == theNewPasswordAgain && theNewPassword.length > 6)
				{
					$("#resetSubmit").hide("fast");
					$("#resetLoadingSign").show("slow");
					$("#resultsDiv2").hide("slow", function(){
						var ajaxCall = $.ajax({
							type: "POST",
							url: "b467dbe3cd61babc0ec599fd0c67e359e6fe04e8cdc618d537808cbb693fee8a",
							data: {
								newPassword: theNewPassword
							},
							async: false
						});
						if(ajaxCall.status == 200)
						{
							$("#resultsDiv2").html(ajaxCall.responseText);
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
				}
				else
				{
					if(theNewPassword != theNewPasswordAgain)
					{
						alert("Passwords do not match");
					}
					else
					{
						alert("Password too short");
					}
				}
			});
			
			$("#showUserControl").click(function(){
				$("#userControl").toggle("slow", function(){
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
		response.sendRedirect("login.jsp");
	}
}
else
{
	response.sendRedirect("login.jsp");
}
%>