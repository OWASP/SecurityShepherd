<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage="" %>
<%
// Broken Authentication and Session Management Challenge 7

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

String levelName = "Session Management Challenge 7";
String levelHash = "269d55bc0e0ff635dcaeec8533085e5eae5d25e8646dcd4b05009353c9cf9c80";
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
	<title>Security Shepherd - <%= levelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= levelName %></h2>
			<p> 
				To complete this challenge you must sign in as one of the users with an privileged user role. 
				<br />
				<form id="leForm" action="javascript:;">
					<div id="resultsDiv">
						<table>
							<tr><td>
								User name:
							</td><td>
								<input type="text" id="subName" autocomplete="off"/>
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
						<h2 class='title'>Log In with via Security Question</h2>
						<p>Please enter your <a>email address</a>. to retrieve your Security Question;</p>
						<table>
							<tr></td>
								<div id="resetSubmit"><input id="resetEmail" type="text" width="300px" autocomplete="off"/><input type="submit" value="Get Security Question"/></div>
								<p style="display: none;" id="resetLoadingSign">Loading...</p>
							</td></tr>
						</table>
					</form>
					<div id="resultsDiv2"></div>
					<div id="answerQuestionDiv" style="display:none;">
					<form id="leForm3" action="javascript:;">
						<table>
							<tr></td>
								<div id="answerQuestion"><input id="questionAnswer" type="text" width="300px" autocomplete="off"/><input type="submit" value="Submit Answer"/></div>
								<p style="display: none;" id="answerLoadingSign">Loading...</p>
							</td></tr>
						</table>
					</form>
					</div>
					<div id="resultsDiv3"></div>
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
						url: "<%= levelHash %>",
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
						type: "GET",
						url: "<%= levelHash %>SecretQuestion",
						data: {
							subEmail: theResetEmail
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv2").html("<p>" + ajaxCall.responseText + "</p>");
					}
					else
					{
						$("#resultsDiv2").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv2").show("slow", function(){
						$("#resetLoadingSign").hide("fast", function(){
							$("#resetSubmit").show("slow");
							$("#answerQuestionDiv").show("slow");
						});
					});
				});
			});
			
			$("#leForm3").submit(function(){
				var theResetEmail = $("#resetEmail").val();
				var theAnswer = $("#questionAnswer").val();
				$("#answerQuestion").hide("fast");
				$("#answerLoadingSign").show("slow");
				$("#resultsDiv3").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>SecretQuestion",
						data: {
							subEmail: theResetEmail,
							subAnswer: theAnswer
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv3").html(ajaxCall.responseText);
					}
					else
					{
						$("#resultsDiv3").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv3").show("slow", function(){
						$("#answerLoadingSign").hide("fast", function(){
							$("#answerQuestion").show("slow");
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
			
			//Answer Controller
			document.cookie="ac=ZG9Ob3RSZXR1cm5BbnN3ZXJz";
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