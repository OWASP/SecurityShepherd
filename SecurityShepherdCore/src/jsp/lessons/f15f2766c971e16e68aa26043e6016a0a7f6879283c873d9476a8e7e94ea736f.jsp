<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%

String levelName = new String("Unvalidated Redirects and Forwards Lesson");

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
 */
 
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
 		//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
 		Encoder encoder = ESAPI.encoder();
 		String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
	String hex = (String) ses.getAttribute("userName");
	String tempId = new Integer(hex.getBytes().hashCode() + hex.substring(0, hex.length() / 2).hashCode()).toString();
	ses.setAttribute("tempId", tempId);
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Unvalidated Redirects and Forwards</title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What are Unvalidated Redirects and Forwards?</h2>
			<p> 
				<div id="lessonIntro">
					Unvalidated redirects and forwards occur in applications that <a>redirect</a> or <a>forward</a> 
					its users to a <a>target</a> that is specified by a unvalidated parameter. An unvalidated parameter 
					that is used to redirect a user to a normally safe location can be used by an attacker to 
					trick victims into visiting phishing pages or even have malware installed on their machines. 
					<br />
					<br />
					This attack takes advantage of a users trust in an application. A victim is more likely to click 
					on a link from a site that they trust than one they have never seen before. 
					<br />
					<br />
					These attacks can also be used to bypass access control schemes. 
					This is done when a page that a user would not normally have access to, 
					such as <a>administrator</a> pages, is included in a unvalidated redirect. 
					<br/>
					<br/>
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				To mark this lesson as complete, you must exploit this <a>Cross Site Request Forgery</a> vulnerability using an <a>Unvalidated Redirect</a> security risk. 
				The CSRF protection that has been implemented in this function is insufficient and can be bypassed easily with 
				an unvalidated redirect vulnerability. To protect against CSRF attacks the application is checking that the requests <a>Referer</a> HTTP header 
				is from the same host name the application is running on. This is easily bypassed when the request originates 
				from inside the application. When an unvalidated redirect is used, the Referer header will be the URL of 
				the redirect page.
				<br />
				<br />
				The function vulnerable to unvalidated redirects is <a href="../user/redirect?to=exampleUrl">/user/redirect?to=exampleUrl</a>
				<br />
				<br />
				The request to mark this lesson as complete is <a href="../root/grantComplete/unvalidatedredirectlesson?userid=exampleId">/root/grantComplete/unvalidatedredirectlesson?userid=exampleId</a> where the exampleId is a users TempId. <a></a>
				<br />
				<br />
				Your temporary ID is <a><%= tempId %></a>
				<br />
				<br />
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						The administrator promises to go to any URL you send him. So please use the following form to send him something of interest!
					</td></tr>
					<tr><td>
						<input type="input" type="text" id="theURL" style="width: 600px;"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="Send Message"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
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
				var theMessageForAdmin = $("#theURL").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "f15f2766c971e16e68aa26043e6016a0a7f6879283c873d9476a8e7e94ea736f",
						data: {
							messageForAdmin: theMessageForAdmin,
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
			
			$('#hideLesson').click(function(){
				$("#lessonIntro").hide("slow", function(){
					$("#showLesson").show("fast");
				});
			});
			
			$("#showLesson").click(function(){
				$('#showLesson').hide("fast", function(){
					$("#lessonIntro").show("slow");
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