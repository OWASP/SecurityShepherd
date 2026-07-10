<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="dbProcs.*, utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
String levelName = "What is Mobile Input Validation?";
String levelHash = "65ba38b8f569f52f0648d7fca0c25954827e9f8dd658928f302f30914d778e62";
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle mobile = ResourceBundle.getBundle("i18n.moduleGenerics.mobileGenericStrings", locale);
ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
String owaspMoreInfo = generic.getString("module.generic.owasp.more.info");
String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
String owaspUrl = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.mobile.m4.insufficientInputValidation");

/**
 * This file is part of the Security Shepherd Project.
 *
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * @author Security Shepherd Project
 */

ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " Accessed");
if (request.getSession() != null)
{
	HttpSession ses = request.getSession();
	Cookie tokenCookie = null;
	try
	{
		tokenCookie = Validate.getToken(request.getCookies());
	}
	catch(Exception htmlE)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + ".jsp: tokenCookie Error:" + htmlE.toString());
	}
	if (Validate.validateSession(ses) && tokenCookie != null)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Security Shepherd - <%= levelName %></title>
<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
	<div id="contentDiv">
		<h2 class="title"><%= levelName %></h2>
		<p>
		<div id="lessonIntro">
			Input validation on mobile applications is intended to prevent malicious or malformed data
			from being processed. However, when an app performs its filtering logic client-side only,
			an attacker can bypass those checks entirely by intercepting requests with a <a href="https://portswigger.net/burp" target="_blank">proxy tool</a> and
			modifying the data before it reaches the server.
			<br /><br />
			This lesson demonstrates how to identify and exploit client-side input validation weaknesses
			using the Security Shepherd mobile app.
			<br /><br />
			Configure a proxy tool (e.g. <a href="https://portswigger.net/burp" target="_blank">Burp Suite</a>) on your Android device.
			With the app connected, trigger the input validation and observe the HTTP request.
			Modify the request parameters to send values that bypass the app's local checks.
			<br /><br />
			OWASP highlights that insufficient input/output validation is not just a client-side
			problem: the same unvalidated data reaching a backend server can trigger SQL injection,
			command injection, XSS or path traversal. The correct fix is server-side validation using
			parameterised queries and strict input length and type checks — never relying on the
			client to enforce constraints.
			<br /><br />
			<%= owaspMoreInfo %> <a href="<%= owaspUrl %>" target="_blank"><%= owaspGuideTo %> Input/Output Validation (M4)</a>
			<br /> </br> </br>
			<input type="button" value="Hide Lesson Introduction" id="hideLesson" />
		</div>
		<input type="button" value="Show Lesson Introduction" id="showLesson" style="display: none;" />
		<br />
		<br /> <br />
		<%= mobile.getString("mobileBlurb.appLink") %>
		</p>
		<script>
			$('#hideLesson').click(function(){
				$("#lessonIntro").hide("slow", function(){ $("#showLesson").show("fast"); });
			});
			$("#showLesson").click(function(){
				$('#showLesson').hide("fast", function(){ $("#lessonIntro").show("slow"); });
			});
		</script>
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
	</div>
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
