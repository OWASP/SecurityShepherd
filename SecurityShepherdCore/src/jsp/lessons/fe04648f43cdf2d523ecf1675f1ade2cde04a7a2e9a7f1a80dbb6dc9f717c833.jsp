<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>

<fmt:setBundle basename="i18n.lessons.security_misconfig.fe04648f43cdf2d523ecf1675f1ade2cde04a7a2e9a7f1a80dbb6dc9f717c833" />

<%
String levelName = "Security Misconfiguration";
String levelHash = "fe04648f43cdf2d523ecf1675f1ade2cde04a7a2e9a7f1a80dbb6dc9f717c833";

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.security_misconfig." + levelHash, locale);
//Used more than once translations
String translatedLevelName = bundle.getString("title.question.security_misconfig");

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
		<div id="contentDiv">
			<h2 class="title"><%= translatedLevelName %></h2>
			<p> 
				<div id="lessonIntro">
					Security misconfiguration can happen in any part of an application, from the database server, third-party libraries to custom code settings. A security misconfiguration is any configuration which can be exploited by an attacker to perform any action they should not be able to. The impact of these issues vary from which configuration is being exploited. 
					<br/>
					<br/>
					Attackers can exploit security misconfiguration by logging in with <a>default log in credentials</a> to the application, the operating system or any of the public services it is running (Such as Database or Samba services)  to gain unauthorized access to or knowledge of the system. Attackers can also exploit bad security configurations through unpatched flaws, unprotected files and directories to gain unauthorized access to or knowledge of the system.
					<br/>
					<br/>
					Developers and system administrators need to work together to ensure that the entire stack is configured properly. Automated scanners are useful for detecting missing patches, misconfigurations, use of default accounts or unnecessary services. A process should be implemented for <a>keeping all software up to date</a>, with patches occurring in a timely manner to each deployed environment.
					<br/>
					<br/>
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				To get the result key to this lesson, you must sign in with the default admin credentials which were never removed or updated.
				<div id="hint" style="display: none;">
					<h2 class="title">Lesson Hint</h2>
					The user name is admin. Try a seiries of generic passwords.
					<br />
					<br />
				</div>
				<br />
				<br />
				<div id="badData"></div>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						User Name 
						</td><td>
						<input type="text" id="userName" autocomplete="off"/>
						</td>
					<tr><td>
						Password 
						</td><td>
						<input type="password" id="userPass" autocomplete="off"/>
						</td>
					<tr><td colspan="2">
						<div id="submitButton"><input type="submit" value="Sign In"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			var counter=0;
			$("#leForm").submit(function(){
				counter = counter + 1;
				console.log("Counter: " + counter);
				var theUserName = $("#userName").val();
				var theUserPass = $("#userPass").val();
				var theError = "";
				$("#badData").hide("fast");
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							userName: theUserName,
							userPass: theUserPass
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
				if (counter == 3)
				{
					console.log("Showing Hint Button");
					$("#hintButton").show("slow");
				}
				if (theError.length > 0)
				{
					$("#badData").html("<p> An Error Occurred: " + theError + "</p>");
					$("#badData").show("slow");
				}
			});
			
			$("#theHintButton").click(function() {
				$("#hintButton").hide("fast", function(){
					$("#hint").show("fast");
				});
			});;
			
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
