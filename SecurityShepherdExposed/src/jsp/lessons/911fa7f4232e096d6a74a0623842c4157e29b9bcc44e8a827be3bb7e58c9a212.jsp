<%@ page contentType="text/html; charset=iso-8859-1" language="java"
	import="utils.*" errorPage=""%>

<%
	//No Quotes In level Name
	String levelName = "What is Mobile Broken Crypto?";
	//Alphanumeric Only
	String levelHash = "911fa7f4232e096d6a74a0623842c4157e29b9bcc44e8a827be3bb7e58c9a212.jsp";
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

		<h2 class="title"><%=levelName %></h2>
			<p> 
				<div id="lessonIntro">

			<%=levelBlurb%>
			<br/>
			
			Cryptography is difficult to get right, as a result many Apps use Cryptography poorly and become vulnerable to attack. An App can transfer keys insecurely, use a known broken or deprecated cryptographic algorithm or developers can create their own Crypto algorithms.			
			<br/>
			
			Poor key management can be due to hard coded keys, keys stored in directories, transferring the key in an insecure way or using the same key all the time. If the developers of an App make use a of custom, unproven, untested encryption algorithm then it is highly likely that the encrypted data is vulnerable.
			Finally
			<br/>
			
			
			<br/>
			
			<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
			
			
			 <br/> The developers of this App are holding a competition, whoever can crack their secure chat wins. Unfortunatly, the developers have misunderstood the definition of Cryptography. Reduce the intercepted messages exchanged to plaintext to reveal the key. <br />
				<br/>
				<%= Analytics.sourceForgeMobileVmLinkBlurb %>
			  The App for this lesson is <a>BrokenCrypto.apk</a> <br />

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
				
				
				</script>
				
			
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
	<% if (Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>
