<%@ page contentType="text/html; charset=iso-8859-1" language="java" errorPage="" %>
<%

//No Quotes In level Name
String levelName = "Client Side Injection";
//Alphanumeric Only
String levelHash = "f758a97011ec4452cc0707e546a7c0f68abc6ef2ab747ea87e0892767152eae1";
//Level blurb can be writen here in HTML OR go into the HTML body and write it there. Nobody will update this but you
String levelBlurb = "Client Side Injection occurs when the user can execute SQLite commands through inputs in order to change the query run by the App. This APK encrypts it's databases using AES. It would be difficult to steal login details but a lot easier to use SQL Injection to bypass the login. We don't need to know  the user credentials. We do know that the APK uses a textbook example of running SQL by appending the Username and Password to a String in a Select statement in order to verify if a username and password exist. If this statement returns true, the user is logged in.";

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
 * @author Sean Duggan
 */
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= levelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
	</script> 
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= levelName %>Insecure Data Storage 3</h2>
			<p> 
				<% /* Put Your Blurb Here Instead of the following scriptlet. Not this comment Bren. Jeesh*/ %>
				
				<%= levelBlurb %>
				<br/>
					
				<br/>
				<% /* IF you need a form - Present it like this */ %>
				<%
				/*
				<br />
				<br />
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>			
						<div id="submitButton">
						<input type="submit" value="Get Info"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
				*/
				%>
			</p>
		</div>
		<% /*If you need to call the Server Do it like this */ %>
		<%
		/*
		<script>
			$("#leForm").submit(function(){
				var theVariableName = $("#variableName").val();
				var theSecondVariableName = $("#secondVariableName").val();
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<ChangeThis= levelHash ChangeThis>",
						data: {
							variableName: theVariableName, 
							secondVariableName: theSecondVariableName
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
		*/
		%>
</body>
</html>