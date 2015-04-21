<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	// Cross Site Request Forgery Challenge 4

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
String levelName = "CSRF Challenge 4";
String levelHash = "84118752e6cd78fecc3563ba2873d944aacb7b72f28693a23f9949ac310648b5";
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
		//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
		Encoder encoder = ESAPI.encoder();
		String ApplicationRoot = getServletContext().getRealPath("");
		String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
		String userClass = null;
		if(ses.getAttribute("userClass") != null)
		{
			userClass = encoder.encodeForHTML(ses.getAttribute("userClass").toString());
		}
		String userId = encoder.encodeForHTML(ses.getAttribute("userStamp").toString());
		
		//Set CSRF Challenge 4 CsrfToken
		String csrfChal4Token = Setter.setCsrfChallengeFourCsrfToken(userId, Hash.randomString().trim(), ApplicationRoot);
		ses.setAttribute("csrfChallengeFourNonce", csrfChal4Token);
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
				To complete this challenge, you must get your CSRF counter above 0. The request to increment your counter is as follows
				<br/>
				<br/>
				<a> POST /user/csrfchallengefour/plusplus</a>
				<br/>
				With the following parameters; <a>userId = exampleId</a> and <a>csrf = yourCsrfToken</a>
				<br/>
				<br/>
				Where exampleId is the ID of the user who's CSRF counter is been incremented. 
				Your ID is <%= userId %> and your CSRF token is <a><%= csrfChal4Token %></a>. Any user than you may increment your counter for this challenge, except you. Exploit the CSRF vulnerability in the request described above against other users to complete this challenge. Once you have successfully CSRF'd another Security Shepherd user, the solution key will appear just below this message.
				<br/>
				<br/>
				You can use the CSRF forum below to post web site
				<% 
				String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, levelHash);	
				if (Getter.isCsrfLevelComplete(ApplicationRoot, moduleId, userId)) 
				{ %>
					<h2 class='title'>This CSRF Challenge has been Completed</h2>
					<p>
					Congratulations, you have completed this CSRF challenge by successfully carrying out a CSRF attack on another user for this level's target. The result key is 
					<b><a><%= encoder.encodeForHTML(Hash.generateUserSolution(Getter.getModuleResult(ApplicationRoot, moduleId), (String)ses.getAttribute("userName"))) %></a></b><br/><br/>
				<% } %>			
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						Please enter your <a>Message</a> that you would like to share with your class
					</td></tr>
					<tr><td>
						<input style="width: 400px;" id="myMessageAris" type="text"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="Post Message"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv">
					<%= Getter.getCsrfForumWithIframe(ApplicationRoot, userClass, Getter.getModuleIdFromHash(ApplicationRoot, levelHash)) %>
				</div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				var theMessage = $("#myMessageAris").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						dataType: "text",
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							myMessage: theMessage,
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
						$("#resultsDiv").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
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
