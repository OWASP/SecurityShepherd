<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="dbProcs.*, utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
String levelName = "What is Mobile Insecure Authorization?";
String levelHash = "749ea8a43debf521810ce13efe8f8f8a92318f51094957fc010507e4083d3dc7";
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle mobile = ResourceBundle.getBundle("i18n.moduleGenerics.mobileGenericStrings", locale);
ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
String owaspMoreInfo = generic.getString("module.generic.owasp.more.info");
String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
String owaspUrl = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.mobile.m3.insecureAuthentication");

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
			Insecure authorization occurs when a mobile application enforces access controls purely on
			the client side. A common pattern is storing a user role or permission flag in Android
			<a href="https://developer.android.com/reference/android/content/SharedPreferences" target="_blank">SharedPreferences</a>. Because SharedPreferences files are stored in the app's data directory,
			a user with a rooted device or <a href="https://developer.android.com/tools/adb" target="_blank">ADB</a> access can read and modify those values directly,
			escalating their own privileges without any server-side check.
			<br /><br />
			This lesson demonstrates how client-side authorization can be bypassed by modifying a
			locally stored role value in the Security Shepherd mobile app.
			<br /><br />
			Log in to the app with the demo credentials. Use <a href="https://developer.android.com/tools/adb" target="_blank">ADB</a> to inspect the app's SharedPreferences:
			<code>adb shell run-as org.owasp.mobileshepherd cat shared_prefs/UserSession.xml</code>
			Find the <code>user_role</code> value and change it from <code>user</code> to <code>admin</code>.
			<br /><br />
			OWASP states that backend systems must independently verify the roles and permissions of
			the authenticated user — never trusting role or permission data transmitted from the
			mobile device. The risk is heightened when authorization decisions are made on the device
			rather than the server, particularly when offline usability requirements push developers
			toward client-side checks. All client-side authorization controls must be assumed
			bypassable.
			<br /><br />
			<%= owaspMoreInfo %> <a href="<%= owaspUrl %>" target="_blank"><%= owaspGuideTo %> Insecure Authentication/Authorization (M3)</a>
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
