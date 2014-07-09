<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage=""%>
<%
String levelName =  "SQL Injection Lesson";

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
	<title>Security Shepherd - SQL Injection Lesson</title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What is SQL Injection?</h2>
			<p> 
				<div id="lessonIntro">
					Injection flaws, such as <a>SQL injection</a>, occur when hostile data is sent to an interpreter as part of a command or query. The hostile data can trick the interpreter into executing unintended commands or accessing unauthorized data. Injections attacks are of a high severity. Injection flaws can be exploited to remove a system's confidentiality by accessing any information held on the system. These security risks can then be extended to execute updates to existing data affecting the systems integrity and availability. These attacks are easily exploitable as they can be initiated by anyone who can interact with the system through any data they pass to the application.
					<br />
					<br />
					The following form's parameters are concatenated to a string that will be passed to a SQL server. This means that the data can be interpreted as part of the code. 
					<br />
					<br />
					The objective here is to modify the result of the query with <a>SQL Injection</a> so that all of the table's rows are returned. This means you want to change the <a>boolean</a> result of the query's <a>WHERE</a> clause to return true for every row in the table. The easiest way to ensure the <a>boolean</a> result is always true is to inject a <a>boolean 'OR'</a> operator followed by a true statement like <a>1 = 1</a>.
					<br />
					<br />
					If the parameter is been interpreted as a string, you can escape the string with an apostrophe. That means that everything after the apostrophe will be interpreted as SQL code.
					<br />
					<br />
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				
				Exploit the <a>SQL Injection</a> flaw in the following example to retrieve all of the rows in the table. The lesson's solution key will be found in one of these rows! The results will be posted beneath the search form.
				<br />
				<br />
				<div id="hint" style="display: none;">
					<h2 class="title">Lesson Hint</h2>
					This is the query that you are adding data to. See if you can input something that will cause the <a>WHERE</a> clause to return <a>true</a> for every row in the table. Remember, you can escape a string using an apostrophe.
					<br />
					<br />
					<div>SELECT * FROM tb_users WHERE username ='<a id="userContent"></a>';</div>
					<br />
					<br />
				</div>
				
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						Please enter the <a>user name</a> of the user that you want to look up
					</td></tr>
					<tr><td>
						<input style="width: 400px;" id="aUserName" type="text"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="Get this user"/></div>
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
				$("#userContent").text($("#aUserName").val());
				var theName = $("#aUserName").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594",
						data: {
							aUserName: theName
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
							if (counter == 3)
							{
								$("#hintButton").show("slow");
							}
						});
					});
				});
			});
			
			$("#aUserName").keyup(function () {
				$("#userContent").text($(this).val());
			}).keyup();
			
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