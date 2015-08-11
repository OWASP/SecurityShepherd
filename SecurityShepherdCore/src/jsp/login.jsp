<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*, org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder" errorPage="" %>
<%@ include file="translation.jsp" %>

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
 * 
 * @author Mark Denihan
 */
 
HttpSession ses = request.getSession();
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Sombody ("+ ses.getAttribute("lang") +") Conntected to login.jsp ...");

if(request.getSession() != null)
{
	if(ses.getAttribute("loginFailed") == null 
	&& ses.getAttribute("registrationSuccess") == null
	&& ses.getAttribute("registerError") == null)
	{
		ses.invalidate();
		ses = request.getSession(true);
		String language = request.getParameter("lang");
		if(language != null){
			ses.setAttribute("lang", language);
		}
		
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

if(ses.getAttribute("loginFailed") != null)
{
	loginFailed = ses.getAttribute("loginFailed").toString();
	ses.removeAttribute("loginFailed");
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>OWASP Security Shepherd - Login</title>

	<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="js/jquery.js"></script>
	<div id="wrapper">
	<jsp:include page="translation-select.jsp" />
	<!-- start header -->
	<div id="header">
		<h1>Security Shepherd</h1>
	</div>
	<!-- end header -->
	<!-- start page -->
	<div id="page">
		<!-- start content -->
		<div id="content" style="margin-left: auto; margin-right: auto; width:320px">
			<div class="post">
				<h1 class="title" id="login_title"><fmt:message key="login.label.title" /></h1>
				<p id="login-info"><fmt:message key="login.label.info" />
				<% if(OpenRegistration.isEnabled()) { %>
					<p id="register_info"><fmt:message key="login.label.regInfo" /></p>
				<% } if(!loginFailed.isEmpty()) {%>
					<p><strong><font color="red"><%= loginFailed %></font></strong></p>
				<% } %>
				<form name="loginForm" method="POST" action="login">
					<table>
						<tr><td><p><fmt:message key="login.label.username" />:</td><td><input type="text" name="login" value="" autocomplete="OFF" autofocus/></p></td></tr>
						<tr><td><p><fmt:message key="login.label.password" />:&nbsp;&nbsp;&nbsp;&nbsp;
											&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
											&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
											</td><td><input type="password" name="pwd" autocomplete="OFF"/><br /></td></tr>
						<tr><td colspan="2" align="center">
							<fmt:message key="login.button.submit" var="buttonValue" />
								<input type="submit" name="submit" value="${buttonValue}" />
						</td></tr>
					</table>
				</form>
				<br/>
				<br/>
				<div align="center">
					<a id="tools" href="javascript:;"><fmt:message key="login.label.proxy_question" /></a>
					<div id="toolsTable" style="display: none;">
					<p><fmt:message key="login.label.download_proxy" />;</p>
					<table>
						<tr><td align="center"><a href="http://bit.ly/zapWindows"><fmt:message key="login.label.zap_win" /></a></td></tr>
						<tr><td align="center"><a href="http://bit.ly/zapLinux"><fmt:message key="login.label.zap_lin" /></a></td></tr>
						<tr><td align="center"><a href="http://bit.ly/zapForMac"><fmt:message key="login.label.zap_mac" /></a></td></tr>
					</table>
					</div>
					<br><a id="showAbout" href="javascript:;"><fmt:message key="login.label.about_shepherd" /></a>
				</div>
			</div>
		</div>
		<div align="justify">
			
			<div id="aboutDiv" style="display: none;">
			<h2 class="title"><fmt:message key="login.label.about_shepherd" /></h2>
			<p id="about_shepherd_blurb"><fmt:message key="login.label.about_blurb" /></p>
			<%= Analytics.sponsorshipMessage %>
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
		
		$("#tools").click(function(){
			$("#toolsTable").show("slow");
		});
		
		$("#showAbout").click(function(){
			$("#aboutDiv").show("slow");
		});
	</script>
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>
