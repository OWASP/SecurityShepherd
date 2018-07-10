<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="servlets.module.challenge.SecurityMisconfigStealTokens, java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle" %>
<%
/**
 * Security Misconfiguration Cookie Challenge
 * 
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
 String levelName = new String("Security Misconfiguration Cookie Flag Challenge");
 String levelHash = new String("c4285bbc6734a10897d672c1ed3dd9417e0530a4e0186c27699f54637c7fb5d4");
 
//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.challenges.securityMisconfig." + levelHash, locale);
//Used more than once translations
String i18nLevelName = bundle.getString("securityMisconfig.stealTokens.challengeName");
 
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
 		// Getting Session Variables
		//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety
		
		String applicationRoot = getServletContext().getRealPath("");
		String csrfToken = Encode.forHtml(tokenCookie.getValue());
		String userClass = null;
		if(ses.getAttribute("userClass") != null)
		{
			userClass = Encode.forHtml(ses.getAttribute("userClass").toString());
		}
		String userId = Encode.forHtml(ses.getAttribute("userStamp").toString());
		String challengeUrl = request.getRequestURL().toString();
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName +".jsp: DEBUG: Challenge URL " + challengeUrl);
		//Changing URL to HTTP
		challengeUrl = challengeUrl.replaceAll("(?i)https", "http");
		//Set User  Cookie
		try
		{
			Cookie userCookie = new Cookie("securityMisconfigLesson", SecurityMisconfigStealTokens.getUserToken(userId, applicationRoot));
	        response.addCookie(userCookie);
		}
		catch(Exception e)
		{
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName +".jsp: Error: Could not Get Token: " + e.toString());
		}
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= i18nLevelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= i18nLevelName %></h2>
			<p> 
				<%= bundle.getString("securityMisconfig.stealTokens.description") %>
				<br/>
				<br/>
				<img height="25px" width="25px" style="display: block;" alt="<%= bundle.getString("securityMisconfig.stealTokens.whyThisImageIsHere") %>" title="<%= bundle.getString("securityMisconfig.stealTokens.whyThisImageIsHere") %>" src="<%= Encode.forHtmlAttribute(challengeUrl) %>">
				<br/>
				<br/>
				<%= bundle.getString("securityMisconfig.stealTokens.haveSomebodyOnYourNetwork") %>
				<br/>
				<br/>
				<%= bundle.getString("securityMisconfig.stealTokens.stealTokenThenDoThis") %>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<div id="submitButton"><input type="submit" value="<%= bundle.getString("form.securityMisconfig.stealTokens.submit") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("form.challenge.loading") %></p>
					</td></tr>
					</table>
				</form>
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						dataType: "text",
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							csrfToken: "<%= csrfToken %>"
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
					$("#resultsDiv").show("slow", function(){
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
						});
					});
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
