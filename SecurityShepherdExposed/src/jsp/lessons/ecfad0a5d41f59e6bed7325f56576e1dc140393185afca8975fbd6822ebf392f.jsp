<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage=""%>
<%
	//No Quotes In level Name
	String levelName = "What is Mobile Insecure Data Storage?";
	//Alphanumeric Only
	String levelHash = "ecfad0a5d41f59e6bed7325f56576e1dc140393185afca8975fbd6822ebf392f";
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
		<h2 class="title"><%=levelName%></h2>
		<p>
			<%
				/* Put Your Blurb Here Instead of the following scriptlet. Not this comment Bren. Jeesh*/
			%>
			<p> 
				<div id="lessonIntro">

			<%=levelBlurb%>
			<br /> Insecure Data Storage occurs when an App stores sensitive data
			such as user credentials, API keys, Credit Card information
			insecurely. This issue occurs in numerous ways. Generally, for
			storing client side information, an App will use an <a>Sqlite database</a>.
			
			<br/>
			This can be a favoured, cheaper method of storage instead of using a more expensive back end service.
			 As a result, any user can access the data stored by the App. Insecure Data Storage becomes a
			danger when a user's App caches sensitive data, their phone is stolen
			and the attacker steals this information from local databases. Malware can also access this information easily. This
			is made worse by the popularity of <a>rooting devices</a> which makes it
			much easier for an attacker to access this information.
			
			<br/>
			There are
			other ways to store data insecurely. Using known broken hashing
			algorithms can lead to pain for the Apps users. Not only are they
			susceptible to <a>collisions</a>, where two different passwords can
			potentially generate the same hash and be interpreted as the same
			password, the developer would have to assume that their user's use strong
			passwords. This is generally never the case and once a hashed value
			has been cracked, an attacker merely needs to update their tables.
			
			<br/>
			This method still uses no key, Therefore one could assume it is not
			truly encryption? Hashing algorithms are useful for comparing two
			different files but should not be used for storage of passwords(Unless done correctly). <br />
			
			<br/>
			<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
			
			<br/>
			Typically and Android App will store it's database in the <a>/data/data/com.app.exampleApp/database/</a> directory. Anyone with a rooted device can access this directory. This Android App stores it's under credentials in an <a>SQLite database</a>. The Admin password is the key.
			
			<br/>
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