<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*, servlets.module.challenge.BrokenCryptoHomeMade" errorPage="" %>
<%@ page import="java.util.Locale,java.util.ResourceBundle,java.util.ArrayList,java.util.List,org.owasp.encoder.Encode"%>

<%
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
//No Quotes In level Name
String levelName = "Insecure Cryptographic Storage Home Made Keys";
//Alphanumeric Only
String levelHash = "9e5ed059b23632c8801d95621fa52071b2eb211d8c044dde6d2f4b89874a7bc4"; 

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.challenges.insecureCryptoStorage.insecureCryptoHomeMade", locale);
//Used more than once translations
String i18nChallengeName = bundle.getString("challenge.challengeName");

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
		String csrfToken = Encode.forHtml(tokenCookie.getValue());
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
		BrokenCryptoHomeMade.initLists();
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%=levelName%></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<div>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%=levelName%></h2>
			<p> 
				<%=bundle.getString("challenge.whatToDo")%>
				<br/>
				<br/>
				<h1 class='title'><%=bundle.getString("badCrypto.title")%></h1>
				<table>
					<tr><th>Challenge Name</th><th>Base Key</th><th>Your User Specific Solution</th></tr>
					<%
					for(int i = 0; i < BrokenCryptoHomeMade.challenges.size(); i++)
					{
						%>
						<tr>
							<td><%= BrokenCryptoHomeMade.challenges.get(i).get(0) %></td>
							<td><%= BrokenCryptoHomeMade.challenges.get(i).get(1) %></td>
							<% if(!BrokenCryptoHomeMade.challenges.get(i).get(0).equalsIgnoreCase("This Challenge")) { %>
								<td><%= BrokenCryptoHomeMade.generateUserSolution(BrokenCryptoHomeMade.challenges.get(i).get(1), ses.getAttribute("userName").toString()) %></td>
							<% } else { %>
								<td>
									<form action="javascript:;" id="submitAnswerForm">
										<textarea id='theSubmission' rows=2 style='height: 30px; display: inline-block; float: left; padding-right: 1em; overflow: hidden; width:65%' required></textarea>
										<input type="submit" id="theSubmit" value="Submit" style="height: 32;"/>
									</form>
								</td>
							<% } %>
						</tr>
						<%	
					}
					%>
				</table>
				</div>
				<p style="display: none;" id="loadingSign"><%=bundle.getString("form.loading")%></p>
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			$("#submitAnswerForm").submit(function(){
				var attemptedSolution = $("#theSubmission").val();
				$("#resultsDiv").hide("fast");
				$("#loadingSign").show("slow");
				$("#theSubmit").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							theSubmission: attemptedSolution, 
							csrfToken: "<%= csrfToken %>"
						},
						async: false
					});
					// This is to get refresh data not submit a potential answer
					//var ajaxCall = $.ajax({
					//	type: "GET",
					//	url: "<%= levelHash %>?csrfToken=<%= csrfToken %>&name=//PutNameHere",
					//	async: false
					//});
					$("#loadingSign").hide("fast", function(){
						if(ajaxCall.status == 200)
						{
							$("#resultsDiv").html(ajaxCall.responseText);
						}
						else
						{
							$("#resultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
						}
						$("#resultsDiv").show("fast");
						$("#theSubmit").slideDown("slow");
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