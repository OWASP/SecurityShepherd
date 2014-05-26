<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*, org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder" errorPage="" %>

<%
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Sombody Conntected to login.jsp ...");

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
 * 
 * @author Mark Denihan
 */
 
HttpSession ses = request.getSession();
if(request.getSession() != null)
{
	if(ses.getAttribute("loginFailed") == null 
	&& ses.getAttribute("registrationSuccess") == null
	&& ses.getAttribute("registerError") == null)
	{
		ses.invalidate();
		ses = request.getSession(true);
	}
}
String url = (request.getRequestURL()).toString();
if(url.contains("login.jsp"))
{
	url = url.substring(0, url.lastIndexOf("/") + 1);
}
else
{
	response.sendRedirect("login.jsp");
}

String registrationSuccess = new String();
String loginFailed = new String();
String registerError = new String();
Encoder encoder = ESAPI.encoder();
String theUrl = encoder.encodeForHTMLAttribute(ExposedServer.getSecureUrl());

if(ses.getAttribute("loginFailed") != null)
{
	loginFailed = ses.getAttribute("loginFailed").toString();
	ses.removeAttribute("loginFailed");
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Login - Security Shepherd</title>

<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<script type="text/javascript" src="js/jquery.js"></script>
<div id="wrapper">
<!-- start header -->
<div id="header">
	<h1>Security Shepherd</h1>
</div>
<!-- end header -->
<!-- start page -->
<div id="page">
	<!-- start content -->
	<div id="content">
		<div class="post">
			<h1 class="title">Login</h1>
			<p>Use your <a><span>Security Shepherd Credentials</span></a> to Login.</p>
			<% if(OpenRegistration.isEnabled()) { %>
				<p>Register a <a href="register.jsp"><span>Security Shepherd Account</span></a> here!</p>
			<% } if(!loginFailed.isEmpty()) {%>
				<p><strong><font color="red"><%= loginFailed %></font></strong></p>
			<% } %>
			<form name="loginForm" method="POST" action="login">
				<table>
					<tr><td><p>Username:</td><td><input type="text" name="login" value="" autocomplete="OFF"/></p></td></tr>
					<tr><td><p>Password:&nbsp;&nbsp;&nbsp;&nbsp;
										&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										</td><td><input type="password" name="pwd" autocomplete="OFF"/><br /></td></tr>
					<tr><td colspan="2" align="center">
						<input type="submit" name="submit" value="Login" />
					</td></tr>
				</table>
			</form>
			<br/>
			<br/>
			<center>
			<a id="tools" href="javascript:;">Do you need a Proxy?</a>
			<div id="toolsTable" style="display: none;">
			<p>Download a HTTP Proxy here;</p>
			<table>
				<tr><td align="center"><a href="<%= theUrl %>stuff/WindowsZap.zip">ZAP for Windows</a></td></tr>
				<tr><td align="center"><a href="<%= theUrl %>stuff/LinuxZap.gz">ZAP for Linux</a></td></tr>
				<tr><td align="center"><a href="<%= theUrl %>stuff/MacZap.zip">ZAP for Mac</a></td></tr>
			</table>
			</div>
			</center>
		</div>
	</div>
	<!-- end content -->
	<!-- start sidebar -->
	<!-- end sidebar -->
</div>
</div>
<!-- end page -->
<script>
	jQuery.fn.center = function () 
	{
		this.css("position","absolute");
		this.css("left", (($(window).width() - this.outerWidth()) / 2) + $(window).scrollLeft() + "px");
		return this;
	}

	$("#content").center();
	
	$(window).resize(function() 
	{
		$("#content").center();
	});
	
	$("#tools").click(function(){
		$("#toolsTable").show("slow");
	});
</script>
</body>
</html>
