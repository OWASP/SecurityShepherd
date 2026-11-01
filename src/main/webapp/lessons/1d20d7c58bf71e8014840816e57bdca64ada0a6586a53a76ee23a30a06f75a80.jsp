<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="dbProcs.*, utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
String levelName = "What is Mobile Security Misconfiguration?";
String levelHash = "1d20d7c58bf71e8014840816e57bdca64ada0a6586a53a76ee23a30a06f75a80";
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.m_security_misconfiguration." + levelHash, locale);
ResourceBundle mobile = ResourceBundle.getBundle("i18n.moduleGenerics.mobileGenericStrings", locale);
ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
String translatedLevelName = bundle.getString("title.question.m_security_misconfiguration");
String owaspMoreInfo = generic.getString("module.generic.owasp.more.info");
String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
String owaspUrl = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.mobile.m8.securityMisconfiguration");

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
			<br /><br />
			<%= bundle.getString("paragraph.info.2") %>
			<br /><br />
			<%= bundle.getString("paragraph.info.3") %>
			<br /><br />
			<%= bundle.getString("paragraph.info.4") %>
			<br /><br />
			<%= owaspMoreInfo %> <a href="<%= owaspUrl %>" target="_blank"><%= owaspGuideTo %> Security Misconfiguration (M8)</a>
			<br /> </br> </br>
			<input type="button" value="<%= bundle.getString("button.hideIntro") %>" id="hideLesson" />
		</div>
		<input type="button" value="<%= bundle.getString("button.showIntro") %>" id="showLesson" style="display: none;" />
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
