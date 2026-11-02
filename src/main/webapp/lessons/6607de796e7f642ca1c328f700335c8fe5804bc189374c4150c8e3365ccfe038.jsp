<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="dbProcs.*, utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
String levelName = "What is Mobile Privacy Controls?";
String levelHash = "6607de796e7f642ca1c328f700335c8fe5804bc189374c4150c8e3365ccfe038";
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle mobile = ResourceBundle.getBundle("i18n.moduleGenerics.mobileGenericStrings", locale);
ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
String owaspMoreInfo = generic.getString("module.generic.owasp.more.info");
String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
String owaspUrl = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.mobile.m6.inadequatePrivacyControls");

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
			Mobile applications sometimes embed sensitive information in file metadata without
			the developer realising. <a href="https://en.wikipedia.org/wiki/Exif" target="_blank">EXIF (Exchangeable Image File Format)</a> data, for example, can
			contain GPS coordinates, device identifiers, timestamps and other details that were
			never intended to be shared publicly.
			<br /><br />
			This lesson demonstrates how to extract and read metadata embedded within image files
			served by the Security Shepherd mobile app using a tool such as <a href="https://exiftool.org/" target="_blank">ExifTool</a>.
			<br /><br />
			In the app, tap <strong>Load Sample Image</strong> to load the preloaded photo, then tap
			<strong>Analyze Metadata</strong> to view all EXIF fields displayed on screen.
			The key is hidden in one of the EXIF tags.
			<br /><br />
			OWASP defines this risk broadly as inadequate protection of Personally Identifiable
			Information (PII) — which includes not just obvious fields like names and addresses, but
			also GPS coordinates, device identifiers, and timestamps embedded in file metadata.
			The recommended first step is data minimisation: never collect or retain PII that is not
			strictly necessary, and strip sensitive metadata before sharing any user-generated files.
			<br /><br />
			<%= owaspMoreInfo %> <a href="<%= owaspUrl %>" target="_blank"><%= owaspGuideTo %> Inadequate Privacy Controls (M6)</a>
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
