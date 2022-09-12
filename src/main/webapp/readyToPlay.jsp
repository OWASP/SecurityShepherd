<%@ page import="org.owasp.encoder.Encode, utils.ShepherdLogManager"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="utils.*" errorPage=""%>
<%@ include file="translation.jsp"%>
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
<link href="css/lessonCss/theCss.css" rel="stylesheet" type="text/css"
	media="screen" />
<style>
	.points th, td {
		border: 1px solid #ddd;
		padding: 8px;
	}
	.points tr:nth-child(even){background-color: #f2f2f2;}
	.points tr:hover {background-color: #ddd;}
	.points table {
		border-collapse: unset;
		box-sizing: unset;
		text-indent: unset;
		white-space: unset;
		line-height: unset;
		font-size: unset;
		border-spacing: unset;
		border-color: unset;
		font-variant: unset;
	}
	.points th {
		vertical-align: unset;
		font-weight: unset;
		padding-top: 12px;
		padding-bottom: 12px;
		text-align: left;
		background-color: #0077CC;
		color: white;
	}
	.points h3 {
		font-size: 18px;
	}
</style>
</head>
<body>
	<script type="text/javascript" src="js/jquery.js"></script>
	<div class="points" style="visibility: hidden; display: none">
		<h2 class="title">Points</h2>
		<p>Below is a breakdown of the points on offer for helping out your fellow participants and completing a level.</p>
		<h3>Levels</h3>
		<table>
			<tr>
				<th>Rank</th>
				<th>Points Range</th>
			</tr>
			<tr>
				<td>Field Training</td>
				<td>10 - 20</td>
			</tr>
			<tr>
				<td>Private</td>
				<td>25 - 40</td>
			</tr>
			<tr>
				<td>Corporal</td>
				<td>45 - 50</td>
			</tr>
			<tr>
				<td>Sergeant</td>
				<td>55 - 65</td>
			</tr>
			<tr>
				<td>Lieutenant</td>
				<td>70 - 75</td>
			</tr>
			<tr>
				<td>Major</td>
				<td>80 - 85</td>
			</tr>
			<tr>
				<td>Admiral</td>
				<td>90 - 180</td>
			</tr>
		</table>
		<br />
		<h3>Medals</h3>
		<p>Be the 1st, 2nd or 3rd person to complete a level to gain a medal and bonus points.</p>
		<table>
			<tr>
				<th>Rank</th>
				<th>Points</th>
			</tr>
			<tr>
				<td>Gold</td>
				<td>5</td>
			</tr>
			<tr>
				<td>Silver</td>
				<td>4</td>
			</tr>
			<tr>
				<td>Bronze</td>
				<td>3</td>
			</tr>
		</table>
	</div>
	<div id="contentDiv">
		<p><br /></p>
		<h2 class="title">
			<fmt:message key="readyToPlay.title.enteredGame" />
		</h2>
		<p>
			<fmt:message key="readyToPlay.text.info.enteredGame" />
		</p>
		<%= Analytics.sponsorshipMessage(new Locale(Validate.validateLanguage(request.getSession()))) %>
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %>
		<% } %>
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
