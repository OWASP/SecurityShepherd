<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle" %>
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

String levelHash = "d779e34a54172cbc245300d3bc22937090ebd3769466a501a5e7ac605b9f34b7";
String levelName = "Session Management Challenge Two";

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.challenges.sessionManagement." + levelHash, locale);
//Used more than once translations
String translatedLevelName = bundle.getString("challenge.challengeName");

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
	<title>Security Shepherd - <%= translatedLevelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= translatedLevelName %></h2>
			<p> 
				<%= bundle.getString("challenge.description") %>
				<br />
				<form id="leForm" action="javascript:;">
					<div id="resultsDiv">
						<table>
							<tr><td>
								<%= bundle.getString("challenge.form.userName") %>
							</td><td>
								<input type="text" id="subName" autocomplete="off"/>
							</td></tr>
							<tr><td>
								<%= bundle.getString("challenge.form.password") %>
							</td><td>
								<input type="password" id="subPassword" autocomplete="off"/>
							</td></tr>
							<tr><td colspan="2">
								<div id="submitButton"><input type="submit" value="<%= bundle.getString("challenge.form.signIn") %>"/></div>
							</td></tr>
						</table>
					</div>
					<small><a id="forgottenPasswordLink" href="#"><%= bundle.getString("challenge.form.forgotPass") %></a></small>
				</form>
				<p style="display: none;" id="loadingSign"><%= bundle.getString("challenge.form.loading") %></p>
				<br/>
				<div id="forgottenPassDiv" style="display: none;">
					<form id="leForm2" action="javascript:;">
						<h2 class='title'><%= bundle.getString("reset.header") %></h2>
						<p><%= bundle.getString("reset.whatToDo") %></p>
						<table>
							<tr></td>
								<div id="resetSubmit"><input id="resetEmail" type="text" autocomplete="off"/><input type="submit" value="<%= bundle.getString("reset.header") %>"/></div>
								<p style="display: none;" id="resetLoadingSign"><%= bundle.getString("challenge.form.loading") %></p>
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
						$("#resultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
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
						$("#resultsDiv2").html("<p><%= bundle.getString("reset.requestSent") %></p>");
					}
					else
					{
						$("#resultsDiv2").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
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