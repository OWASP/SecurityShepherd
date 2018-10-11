<%@ page import="org.owasp.encoder.Encode, utils.ShepherdLogManager" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  language="java" import="utils.*" errorPage="" %>
<%@ include file="translation.jsp" %>
<%
/**
 * This file assigns the tracking cookie for the exposed server
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
 String levelName = new String("ReadyToPlay.jsp");
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
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName +": tokenCookie Error:" + htmlE.toString());
 	}
 	// validateSession ensures a valid session, and valid role credentials
 	// If tokenCookie == null, then the page is not going to continue loading
 	if (Validate.validateSession(ses) && tokenCookie != null)
 	{
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title><fmt:message key="readyToPlay.title.readyToPlay" /></title>
	<link href="css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title"><fmt:message key="readyToPlay.title.enteredGame" /></h2>
			<p> <fmt:message key="readyToPlay.text.info.enteredGame" /> </p>
			<%= Analytics.sponsorshipMessage(new Locale(Validate.validateLanguage(request.getSession()))) %>
			<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
		</div>
</body>
</html>
<%
	}
	else
	{
		response.sendRedirect("loggedOutSheep.html");
	}
}
else
{
	response.sendRedirect("loggedOutSheep.html");
}
%>
