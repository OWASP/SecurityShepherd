<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>

<%
	/**
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
	 */

	final String LEVEL_NAME = "Unvalidated Redirects and Forwards Lesson";
	final String LEVEL_HASH = "f15f2766c971e16e68aa26043e6016a0a7f6879283c873d9476a8e7e94ea736f";

	//Translation Stuff
	Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
	ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.unvalidated_redirects_forwards." + LEVEL_HASH, locale);
	//Used more than once translations
	String translatedLevelName = bundle.getString("title.question.unvalidated_redirects_forwards");

	ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
	String owaspMoreInfo = 	generic.getString("module.generic.owasp.more.info");
	String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
	String owaspUrlAttack = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.defend.unvalidatedRedirectsAndForwards");

	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), LEVEL_NAME + " Accessed");
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
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), LEVEL_NAME +".jsp: tokenCookie Error:" + htmlE.toString());
		}
		// validateSession ensures a valid session, and valid role credentials
		// If tokenCookie == null, then the page is not going to continue loading
		if (Validate.validateSession(ses) && tokenCookie != null)
		{
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), LEVEL_NAME + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
			//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety

			String csrfToken = Encode.forHtml(tokenCookie.getValue());
			String hex = (String) ses.getAttribute("userName");
			String tempId = new Integer(hex.getBytes().hashCode() + hex.substring(0, hex.length() / 2).hashCode()).toString();
			ses.setAttribute("tempId", tempId);
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= translatedLevelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= translatedLevelName %></h2>
			<p>
				<div id="lessonIntro">
					<%= bundle.getString("paragraph.info.1") %>
					<br />
					<br />
					<%= bundle.getString("paragraph.info.2") %>
					<br />
					<br />
					<%= bundle.getString("paragraph.info.3") %>
					<br/>
					<br/>
					<%= owaspMoreInfo %> <a href="<%= owaspUrlAttack %>" target="_blank"> <%= owaspGuideTo %> Unvalidated Redirects and Forwards </a>
					<br/>
					<br/>
					<input type="button" value="<%= bundle.getString("button.hideIntro") %>" id="hideLesson"/>
				</div>

				<input type="button" value="<%= bundle.getString("button.showIntro") %>" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				<%= bundle.getString("challenge.description") %>
				<br />
				<br />
				<%= bundle.getString("challenge.description.function") %> <a href="../user/redirect?to=exampleUrl">/user/redirect?to=exampleUrl</a>
				<br />
				<br />
				<%= bundle.getString("challenge.description.request") %> <a href="../root/grantComplete/unvalidatedredirectlesson?userid=exampleId">/root/grantComplete/unvalidatedredirectlesson?userid=exampleId</a> <%= bundle.getString("challenge.description.request.2") %>
				<br />
				<br />
				<%= bundle.getString("challenge.description.id") %> <a><%= tempId %></a>
				<br />
				<br />
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
					<%= bundle.getString("challenge.description.admin.message") %>
					</td></tr>
					<tr><td>
						<input type="input" type="text" id="theURL" style="width: 600px;"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="<%= bundle.getString("send.message.button") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("word.info.loading")%></p>
					</td></tr>
					</table>
				</form>

				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				var theMessageForAdmin = $("#theURL").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "f15f2766c971e16e68aa26043e6016a0a7f6879283c873d9476a8e7e94ea736f",
						data: {
							messageForAdmin: theMessageForAdmin,
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
						$("#resultsDiv").html("<p> <%= bundle.getString("generic.error") %> " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
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
