<%@ page import="org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, utils.ShepherdExposedLogManager" %>
<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.ShepherdExposedLogManager" errorPage="" %>
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

Encoder encoder = ESAPI.encoder();
String parameter = (String)request.getParameter("ThreadSequenceId");
try
{
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Parameter = " + parameter);
	Cookie cookie = new Cookie("JSESSIONID3", parameter);
    ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Cookie Value = " + cookie.getValue());
    response.addCookie(cookie);
}
catch(Exception e)
{
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Parameter Decription Fail: " + parameter);
	parameter = "";
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Ready to Go?</title>
	<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">		
			<% if(parameter.isEmpty()) { %>
			<h2 class="title">You are not ready!</h2>
			<p>
				Refresh the home page! If this error persists; Log out and back in! If this error continues to persit, please contact an Administrator
			</p>
			<% } else { %>
			<h2 class="title">You have entered the game!</h2>
			<p> 
				Now that you can see this, you're good to go! Get cracking on lessons and challenges! 
				<br/><br/>
				Remeber, the levels you are playing are sub applications. Keep the game play in these applications! Stay away from your session ID's! You'll just log yourself out of you change them!
				<br/><br/>
				If you havn't already configured a web proxy, you better! It makes things much easier!
			</p>
			<% } %>
		</div>
</body>
</html>
