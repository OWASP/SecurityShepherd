<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
/**
 * Cross Site Request Forgery Challenge 3
 * 
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
 String levelName = "CSRF Challenge 3";
 String levelHash = new String("z6b2f5ebbe112dd09a6c430a167415820adc5633256a7b44a7d1e262db105e3c");

 //Translation Stuff
 Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
 ResourceBundle bundle = ResourceBundle.getBundle("i18n.challenges.csrf.csrfStrings", locale);
 
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
 		// Getting Session Variables
		//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety
		
		String ApplicationRoot = getServletContext().getRealPath("");
		String csrfToken = Encode.forHtml(tokenCookie.getValue());
		String userClass = null;
		if(ses.getAttribute("userClass") != null)
		{
			userClass = Encode.forHtml(ses.getAttribute("userClass").toString());
		}
		String userId = Encode.forHtml(ses.getAttribute("userStamp").toString());
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= bundle.getString("title.csrf3") %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= bundle.getString("title.csrf3") %></h2>
			<p> 
				<%= bundle.getString("challenge.intro") %>
				<br/>
				<br/>
				<a> POST /user/csrfchallengethree/plusplus</a>
				<br/>
				<%= bundle.getString("challenge.withTheseParameters") %> <a>userid=<%= bundle.getString("challenge.userIdExample") %> & csrfToken=<%= bundle.getString("challenge.userTokenExample") %></a>
				<br/>
				<br/>
				<%= bundle.getString("challenge.csrfTokenGenerated") %>&nbsp;<%= bundle.getString("challenge.userIdExample") %>&nbsp;<%= bundle.getString("challenge.whereIdIsUserBeenIncremented.2") %>&nbsp;<%= bundle.getString("challenge.yourIdIs") %>&nbsp;<a><%= userId %></a><%= bundle.getString("challenge.yourIdIs.1") %>
				<br/>
				<br/>
				<%= bundle.getString("challenge.useForumForIframe") %>
				<% 
				String moduleId = Getter.getModuleIdFromHash(ApplicationRoot, levelHash);
				if (Getter.isCsrfLevelComplete(ApplicationRoot, moduleId, userId)) 
				{ %>
					<h2 class='title'><%= bundle.getString("result.challengeCompleted") %></h2>
					<p>
					<%= bundle.getString("result.congratsTheKeyIs") %> 
					<b> <a><%=Hash.generateUserSolution(Getter.getModuleResult(ApplicationRoot, moduleId), (String)ses.getAttribute("userName")) %></a></b><br/><br/>
				<% } %>		
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<%= bundle.getString("forum.iframe.whatToDo") %>
					</td></tr>
					<tr><td>
						<input style="width: 400px;" id="myMessage" type="text"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="<%= bundle.getString("forum.postMessage") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("forum.loading") %></p>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv">
					<%= Getter.getCsrfForumWithIframe(ApplicationRoot, userClass, Getter.getModuleIdFromHash(ApplicationRoot, levelHash), bundle) %>
				</div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				var theMessage = $("#myMessage").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						dataType: "text",
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							myMessage: theMessage,
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
						$("#resultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv").show("slow", function(){
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
						});
					});
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
