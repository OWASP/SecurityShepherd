<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage="" %>
<%
// Broken Authentication and Session Management Challenge Five

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

String levelName = "Broken Auth and Session Management Challenge Five";
String levelHash = "7aed58f3a00087d56c844ed9474c671f8999680556c127a19ee79fa5d7a132e1";
 try
 {
 	if (request.getSession() != null)
 	{
 		HttpSession ses = request.getSession();
 		String userName = (String) ses.getAttribute("decyrptedUserName");
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + userName);
 	}
 }
 catch (Exception e)
 {
 	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed");
 	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not recover username: " + e.toString());
 }
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= levelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= levelName %></h2>
			<p> 
				Only an account with <a>admin</a> privileges in the following sub-application can retrieve the result key 
				to this challenge. 
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
					<input type="button" id="showUserControl" value="Forgotton Password?"/>
				</form>
				<p style="display: none;" id="loadingSign">Loading...</p>
				<br/>
				<div id="userControl" style="display: none;">
					<form id="leForm2" action="javascript:;">
						<h2 class='title'>Get Change Password Email</h2>
						<p>Please enter your user name to have your password reset link for this sub application send to your email address. Please click the link in the email quickly as tokens automatically expire after a few minutes</p>
						<table>
							<tr><td>
								User Name:
							</td><td>
								<input type="text" id="userToReset"/>
							</td></tr>
							<tr><td colspan="2">
								<div id="resetSubmit"><input type="submit" value="Send Email"/></div>
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
						url: "<%= levelHash %>",
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
			
			//Reset Password Form
			$("#leForm2").submit(function(){
				var theUserToReset = $("#userToReset").val();
				$("#resetSubmit").hide("fast");
				$("#resetLoadingSign").show("slow");
				$("#resultsDiv2").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>SendToken",
						data: {
							subUserName: theUserToReset
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
			});
			
			$("#showUserControl").click(function(){
				$("#userControl").toggle("slow", function(){
					//Animation Complete
					;
				});
			});
			
			//Change Password Form (Requires Valid Token)
			//Token life = 10 mins
			$("#leForm3").submit(function(){
				var theUserName = $("#subUserName").val();
				var theNewPassword = $("#subNewPass").val();
				var theToken = $("#updatePasswordToken").val();
				$("#resetSubmit").hide("fast");
				$("#resetLoadingSign").show("slow");
				$("#resultsDiv2").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>ChangePass",
						data: {
							userName: theUserName,
							newPassword: theNewPassword,
							resetPasswordToken: theToken
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
			});
		</script>
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>