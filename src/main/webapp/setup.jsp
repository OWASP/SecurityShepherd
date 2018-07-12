<%@page import="dbProcs.Constants"%>
<%@page import="servlets.Setup"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="utils.*, org.owasp.encoder.Encode" errorPage=""%>
<%@ include file="translation.jsp"%>

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
String error = "";

if (ses.getAttribute("dbConnectionFailed") != null) {
	error = ses.getAttribute("dbConnectionFailed").toString();
	ses.removeAttribute("dbConnectionFailed");
}

if (ses.getAttribute("dbAuthFailed") != null) {
	error = ses.getAttribute("dbAuthFailed").toString();
	ses.removeAttribute("dbAuthFailed");
}

if (ses.getAttribute("dbSetupFailed") != null) {
	error = ses.getAttribute("dbSetupFailed").toString();
	ses.removeAttribute("dbSetupFailed");
}

if (Setup.isInstalled()) {
	response.sendRedirect("login.jsp");
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>OWASP Security Shepherd - Login</title>

<link href="css/theCss.css" rel="stylesheet" type="text/css"
	media="screen" />
<link href="css/theResponsiveCss.css" rel="stylesheet" type="text/css"
	media="screen">
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
			<div id="content"
				style="margin-right: auto; margin-left: auto; width: 40%; max-width: 320px;">
				<div class="post">
					<h1 class="title" id="login_title">
						<fmt:message key="generic.text.setup" />
					</h1>
					<%
						if (!error.isEmpty()) {
					%>
					<p>
						<strong><font color="red"><%=error%></font></strong>
					</p>
					<%
						}
					%>					
					<form name="loginForm" method="POST" action="setup">
						<table>
							<tr>
								<td><p>
										<fmt:message key="generic.text.setup.host" />
										:</td>
								<td><input type="text" name="dbhost" value=""
									autocomplete="OFF" autofocus />
									</p></td>
							</tr>
							<tr>
								<td><p>
										<fmt:message key="generic.text.setup.port" />
										:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								<td><input type="text" name="dbport" autocomplete="OFF" /><br /></td>
							</tr>
							<tr>
								<td><p>
										<fmt:message key="generic.text.setup.user" />
										:</td>
								<td><input type="text" name="dbuser" value=""
									autocomplete="OFF" autofocus />
									</p></td>
							</tr>
							<tr>
								<td><p>
										<fmt:message key="generic.text.setup.pwd" />
										:</td>
								<td><input type="password" name="dbpass" value=""
									autocomplete="OFF" autofocus />
									</p></td>
							</tr>
							<tr>
								<td colspan="2">
								<p>	<fmt:message key="generic.text.setup.authentication.help" />	:
								<%=Constants.SETUP_AUTH%>
								</p></td>
							</tr>								
							<tr>
								<td><p>
										<fmt:message key="generic.text.setup.authentication" />
										:</td>
								<td><input type="text" name="dbauth" value=""
									autocomplete="OFF" autofocus />
									</p></td>
							</tr>
							<tr>
								<td colspan="2" align="center"><fmt:message
										key="generic.text.submit" var="buttonValue" /> <input
									type="submit" name="submit" value="${buttonValue}" /></td>
							</tr>
						</table>
					</form>
					<br /> <br />
					<div align="center">
						<a id="tools" href="javascript:;"><fmt:message
								key="login.text.proxy_question" /></a>
						<div id="toolsTable" style="display: none;">
							<p>
								<fmt:message key="login.text.download_proxy" />
								;
							</p>
							<table>
								<tr>
									<td align="center"><a href="http://bit.ly/zapWindows"><fmt:message
												key="login.link.zap_win" /></a></td>
								</tr>
								<tr>
									<td align="center"><a href="http://bit.ly/zapLinux"><fmt:message
												key="login.link.zap_lin" /></a></td>
								</tr>
								<tr>
									<td align="center"><a href="http://bit.ly/zapForMac"><fmt:message
												key="login.link.zap_mac" /></a></td>
								</tr>
							</table>
						</div>
						<br> <a id="showAbout" href="javascript:;"><fmt:message
								key="generic.text.aboutSecShep" /></a>
					</div>
				</div>
			</div>
			<div align="justify">

				<div id="aboutDiv" style="display: none;">
					<h2 class="title">
						<fmt:message key="generic.text.aboutSecShep" />
					</h2>
					<p id="about_shepherd_blurb">
						<fmt:message key="login.text.about_blurb" />
					</p>
					<%= Analytics.sponsorshipMessage(new Locale(Validate.validateLanguage(request.getSession()))) %>
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
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %>
	<% } %>
</body>
</html>
