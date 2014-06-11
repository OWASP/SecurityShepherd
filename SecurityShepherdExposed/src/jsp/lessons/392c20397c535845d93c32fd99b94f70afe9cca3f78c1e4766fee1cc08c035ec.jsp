<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage=""%>
<%
	//No Quotes In level Name
	String levelName = "What is Unintended Data Leakage?";
	//Alphanumeric Only
	String levelHash = "392c20397c535845d93c32fd99b94f70afe9cca3f78c1e4766fee1cc08c035ec";
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
			
			<h2 class="title"> <% levelName %></h2>
			<p> 
				<div id="lessonIntro">

			<%=levelBlurb%>
			<br /> Unintended data leakage occurs when an App inadvertantly
			places sensitive information or data in a location on the mobile
			device that is easily accessible by attackers or other Apps on the
			device. </br> Unintended Data Leakage comes in many forms, including:
			 </br> 
			 
			 <ul>
			 
			 <li>URL Caching (Both request and response) </li> 
			  
			 <li>Keyboard Press Caching </li>
			
			 <li>Copy/Paste buffer Caching </li>
			 
			 <li>Application backgrounding </li>
			  
			 <li>Logging </bli>
			  
			 <li>HTML5 data storage </li> 
			   
			 <li>Browser cookie objects </li>
			 
			 <li>Analytics data sent to 3rd parties </li>
			
			</ul>
			
			<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
			
			
			 Apps won't always use a SQLite database to store data, in
			some cases logs can yield useful information about the App and it's
			users. Use this information to find out the key. In this lesson, the App <a>caches logs</a> on the device. The App itself acts as a notice board or to do list. Everything a user adds to the <a>ListView</a> in the App is logged. <br />
			
			<br>
			<br/>
				<%= Analytics.sourceForgeMobileVmLinkBlurb %>
			
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
