<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
String levelName = "Insecure Direct Object References";

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

 try
 {
 	if (request.getSession() != null)
 	{
 		HttpSession ses = request.getSession();
 		String userName = (String) ses.getAttribute("decyrptedUserName");
 		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + userName);
 	}
 }
 catch (Exception e)
 {
 	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed");
 	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not recover username: " + e.toString());
 }
 %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Insecure Direct Object References Lesson</title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What are Insecure Direct Object References?</h2>
			<p> 
				<div id="lessonIntro">
					Imagine a web page that allows you to view your personal information. The web page that shows the user their information is generated based on a user ID. If this page was vulnerable to <a>insecure Direct Object References</a> an attack would be able to modify the user identifier to reference any user object in the system. Insecure Direct Object References occur when an application references an object by it's actual ID or name. This object that is referenced directly is used to generate a web page. If the application does not verify that the user is allowed to reference this object, then the object is <a>insecurely referenced</a>.
					<br />
					<br />
					Attackers can use insecure object references to compromise any information that can be referenced by the parameter in question. In the above example, the attacker can access any users personal information. 
					<br />
					<br />
					The severity of insecure direct object references varies depending on the data that is been compromised. If the data been compromised is publicly available or not supposed to be restricted, it becomes a very low severity vulnerability. Consider an scenario where one company is able to retrieve their competitors information. Suddenly, the business impact of the vulnerability is critical. These vulnerabilities still need to be fixed and should never be found in professional applications.
					<br/>
					<br/>
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				To key to complete this lesson is stored in the administrators profile.
				<br />
				<br />				
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<div id="submitButton"><input type="submit" value="Refresh your Profile"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv">
				<h2 class='title'>User: Guest</h2>
				<table>
					<tr><th>Age:</th><td>22</td></tr>
					<tr><th>Address:</th><td>54 Kevin Street, Dublin</td></tr>
					<tr><th>Email:</th><td>guestAccount@securityShepherd.com</td></tr>
					<tr><th>Private Message:</th><td>No Private Message Set</td></tr>
				</table>
				</div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100",
						data: {
							username: "guest"
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
