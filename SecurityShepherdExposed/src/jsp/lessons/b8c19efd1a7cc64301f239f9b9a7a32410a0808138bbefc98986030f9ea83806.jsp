<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.ShepherdExposedLogManager" errorPage="" %>
<%
// Broken Authentication and Session Management Lesson

/*
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

String levelName = "Session Management Lesson Accessed";

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
<script>
	document.cookie="lessonComplete=lessonNotComplete";
</script>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Session Management Lesson</title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What is Broken Authentication and Session Management?</h2>
			<p> 
				<div id="lessonIntro">
					Attacks against an application's <a>authentication</a> and <a>session management</a> can be performed with security risks that other vulnerabilities present. For example, and applications session management can be overcome when a <a>Cross Site Scripting</a> vulnerability is used to steal user sessions. This topic is more about flaws that exist in the applications authentication and session management schema.
					<br />
					<br />
					Broken authentication and session management flaws are commonly found in areas such as logout, password management, secret question, account update. An attack can potentially abuse these functions to modify other user's credentials by guessing their secret question. Finding such flaws can sometimes be difficult, as each implementation is unique.
					<br />
					<br />
					The following scenarios are vulnerable to these security risks;
					<br /><a>1)</a> User credentials are <a>stored</a> with insufficient <a>cryptographic</a> levels.
					<br /><a>2)</a> User credentials can be guessed or changed through poor <a>account management</a>.
					<br /><a>3)</a> Session identifiers are exposed in the URL.
					<br /><a>4)</a> The application does not use sufficient transport protection (Such as <a>HTTPs</a> or <a>sFTP</a>).
					<br /><a>5)</a> Session parameters can be manually changed by the user through application functionality.
					<br />
					<br />
					Broken authentication and session management flaws allow an attack to potentially compromise every account in the application. Once the attack has been performed, the attack has the ability to do anything the user could do. For this reason, privileged accounts, such as administrators, are targeted.
					<br />
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				
				This lesson implements bad session management that performed on the client side. Investigate the following function to see if you can retrieve the result key.
				<br />
				<br />
				<div id="hint" style="display: none;">
					<h2 class="title">Lesson Hint</h2>
					This lesson stores unencrypted session information in a user's <a>cookies</a>
					<br />
					<br />
				</div>
				
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<div id="submitButton"><input type="submit" value="Complete This Lesson"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			var counter = 0;
			$("#leForm").submit(function(){
				counter = counter + 1;
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "b8c19efd1a7cc64301f239f9b9a7a32410a0808138bbefc98986030f9ea83806",
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
					document.cookie="lessonComplete=lessonNotComplete";
					$("#resultsDiv").show("slow", function(){
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
							if (counter == 3)
							{
								$("#hintButton").show("slow");
							}
						});
					});
				});
			});
			$("#aUserName").change(function () {
				$("#userContent").text($(this).val());
			}).change();
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
</body>
</html>