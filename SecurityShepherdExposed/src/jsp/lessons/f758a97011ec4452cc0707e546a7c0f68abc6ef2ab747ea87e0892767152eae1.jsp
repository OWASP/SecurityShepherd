<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage=""%>
<%
	//No Quotes In level Name
	String levelName = "Client Side Injection Lesson";
	//Alphanumeric Only
	String levelHash = "f758a97011ec4452cc0707e546a7c0f68abc6ef2ab747ea87e0892767152eae1";
	//Level blurb can be written here in HTML OR go into the HTML body and write it there. Nobody will update this but you
	String levelBlurb = "";

	try {
		if (request.getSession() != null) {
			HttpSession ses = request.getSession();
			String userName = (String) ses
					.getAttribute("decyrptedUserName");
			ShepherdExposedLogManager.logEvent(request.getRemoteAddr(),
					request.getHeader("X-Forwarded-For"), levelName
							+ " has been accessed by " + userName);
		}
	} catch (Exception e) {
		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(),
				request.getHeader("X-Forwarded-For"), levelName
						+ " has been accessed");
		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(),
				request.getHeader("X-Forwarded-For"),
				"Could not recover username: " + e.toString());
	}
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
	 * @author Sean Duggan
	 */
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Security Shepherd - <%=levelName%></title>
<link href="../css/theCss.css" rel="stylesheet" type="text/css"
	media="screen" />

</script>
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<div id="contentDiv">
		<p>
			<%
				/* Put Your Blurb Here Instead of the following scriptlet. Not this comment Bren. Jeesh*/
			%>
		
		<h2 class="title">What is Mobile Client Side Injection?</h2>
		<p>
		<div id="lessonIntro">


			<%=levelBlurb%>
			<br /> <a>Client Side Injection</a>occurs when the user can execute <a>SQLite</a>
			commands through inputs in order to change the query run by the App.
			This APK encrypts it's databases using <a>AES</a>. It would be
			difficult to steal login details by attacking the encryption but a
			lot easier to use SQL Injection to bypass the login.
			
			<br />
			
			<br />
			An ineffective defense against Client Side Injection is <a>Filtering user input</a>.
			This technique involves trying to predict the query which an attacker would execute and then replacing key words like
			 SELECT, WHERE, FROM with a dot or blank space. 
			 Filtering will make it slightly harder for an attacker to perform injection but it generally will not stop them as there are ways of bypassing filtering. 

			<br />
			
			<br />
			
			A security
			system is only as strongest as it's weakest link. We don't
			need to know the user credentials. We do know that the APK uses a
			textbook example of running an SQL query by appending the Username and
			Password to a String in a Select statement in order to verify if a
			Username and Password exist. If this statement returns true, the user
			is logged in.
			
			
			
			 <br />
			 <br/>
				<%= Analytics.sourceForgeMobileVmLinkBlurb %>
			  
			  <input type="button"
				value="Hide Lesson Introduction" id="hideLesson" />
	
		</div>
	
		<input type="button" value="Show Lesson Introduction" id="showLesson"
			style="display: none;" />
			 <br />

			Use SQL Injection to bypass the Client Side Login in this App, if you can manage to log in as the admin then you will get the key. This App uses no filtering. The App is <a>CSInjection.apk</a>
</br>



		<script>
				
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




		<%
			/* IF you need a form - Present it like this */
		%>
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
	<%
		/*If you need to call the Server Do it like this */
	%>
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
