<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="dbProcs.*, utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
String levelName = "What is Mobile Insufficient Cryptography?";
String levelHash = "dbf3915eaaf531b9a95b0404b9a7186fb6dafe67ba96dc56d91f09e3f5edeea6";
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle mobile = ResourceBundle.getBundle("i18n.moduleGenerics.mobileGenericStrings", locale);
ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
String owaspMoreInfo = generic.getString("module.generic.owasp.more.info");
String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
String owaspUrl = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.mobile.m10.insufficientCryptography");

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
			Insufficient cryptography occurs when a mobile application uses deprecated or weak
			encryption algorithms, short key lengths, insecure modes of operation, or poor key
			management. Common examples include using DES (56-bit key), RC4, MD5 or SHA-1 for
			security-sensitive operations, using ECB block cipher mode (which leaks patterns in
			plaintext), hardcoding encryption keys in source code, or deriving keys from static
			or guessable values.
			<br /><br />
			Even when a well-known algorithm such as AES is chosen, a flawed implementation —
			for example, a static initialisation vector or a key that is simply the app's package
			name — can make the encryption trivially breakable. Because mobile applications can
			be decompiled, any key or algorithm choice embedded in the APK is visible to an
			attacker.
			<br /><br />
			This lesson demonstrates how to identify and exploit weak cryptography in the
			Security Shepherd mobile app: the app encrypts a stored value using a short,
			guessable key. Intercept or extract the ciphertext and crack the key to read the
			plaintext.
			<br /><br />
			In the app, trigger the encryption action and use a proxy or ADB to observe the
			ciphertext being stored or transmitted. Decompile the APK with
			<a href="https://github.com/skylot/jadx" target="_blank">jadx</a> to locate the
			encryption routine and identify the algorithm and key. Use a tool such as
			<a href="https://cyberchef.org/" target="_blank">CyberChef</a> to decrypt the value
			once you have identified the key.
			<br /><br />
			OWASP identifies the most common cryptographic failures as: using deprecated algorithms
			(DES, RC4, MD5 for security purposes), insufficient key length, ECB block cipher mode
			(which reveals plaintext patterns in the ciphertext), static initialisation vectors, and
			poor key management — such as hardcoding keys in source code or storing them in
			accessible locations. Because Android APKs can be decompiled with tools like jadx, any
			key or algorithm choice embedded in the app binary is visible to an attacker.
			<br /><br />
			<%= owaspMoreInfo %> <a href="<%= owaspUrl %>" target="_blank"><%= owaspGuideTo %> Insufficient Cryptography (M10)</a>
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
