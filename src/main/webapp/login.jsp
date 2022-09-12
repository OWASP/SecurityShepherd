<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java"
	import="utils.*, dbProcs.*, java.sql.Connection, org.owasp.encoder.Encode"%>
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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
			"Sombody (" + ses.getAttribute("lang") + ") Conntected to login.jsp ...");

	if (request.getSession() != null) {
		if (ses.getAttribute("loginFailed") == null && ses.getAttribute("registrationSuccess") == null
				&& ses.getAttribute("registerError") == null) {
			ses.invalidate();
			ses = request.getSession(true);
			String language = request.getParameter("lang");
			if (language != null) {
				ses.setAttribute("lang", language);
			}

		}
	}

	String url = (request.getRequestURL()).toString();
	if (url.contains("login.jsp")) {
		url = url.substring(0, url.lastIndexOf("/") + 1);
	} else {
		response.sendRedirect("login.jsp");
	}

	String registrationSuccess = new String();
	String loginFailed = new String();
	String registerError = new String();

	if (ses.getAttribute("loginFailed") != null) {
		loginFailed = ses.getAttribute("loginFailed").toString();
		ses.removeAttribute("loginFailed");
	}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Elastic CTF - Powered by OWASP Security Shepherd - Login</title>

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
		</div>
		<!-- end header -->
		<!-- start page -->
		<div id="page">
			<!-- start content -->
			<div id="content"
				style="margin-right: auto; margin-left: auto; width: 40%; max-width: 320px;">
				<div class="post">
					<h1 class="title" id="login_title">
						Get Started
					</h1>
					<p>
						1) Download and install
						<a href="https://portswigger.net/burp/communitydownload" target="_blank">Burp Suite</a>
					</p>
					<%
					if (ScoreboardStatus.isPublicScoreboard()) {
						%>

					<p id="scoreboard_link">
						<fmt:message key="login.text.scoreboard" />
					</p>
					<%
							}
						if (!loginFailed.isEmpty()) {
					%>
					<p>
						<strong><font color="red"><%=loginFailed%></font></strong>
					</p>
					<%
						}
						if (LoginMethod.isLogin()) {
					%>
					<p id="login-info">

						<%
							if (OpenRegistration.isEnabled()) {
						%>
					
					<p id="register_info">
						2) <a href="register.jsp">Register</a> an account on the CTF platform.
					</p>
					<%
							}
					%>
					<p id="register_info">
						3) Login Below
					</p>
					<form name="loginForm" method="POST" action="login">
						<table>
							<tr>
								<td><p>
										<fmt:message key="generic.text.username" />
										:</td>
								<td><input type="text" name="login" value=""
									autocomplete="OFF" autofocus />
									</p></td>
							</tr>
							<tr>
								<td><p>
										<fmt:message key="generic.text.password" />
										:&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
								<td><input type="password" name="pwd" autocomplete="OFF" /><br /></td>
							</tr>
							<tr>
								<td colspan="2" align="center"><fmt:message
										key="generic.text.submit" var="buttonValue" /> <input
									type="submit" name="submit" value="${buttonValue}" /></td>
							</tr>
						</table>
					</form>
					<%
						} else if (LoginMethod.isSaml()) {
					%>
					<a id="login-saml" rel="nofollow" data-method="post"
						href="saml/dologin"><span> Login via SAML </span> </a>
					<%
						}
					%>
					<br /> <br />
					<div align="center">
					<br> <a id="showAbout" href="javascript:;"><fmt:message
							key="generic.text.aboutSecShep" /></a>
					</div>
				</div>
			</div>
			<div>
				<h2 class="title">CTF Rules</h2>
				<ul>
					<li><b>With great power comes great responsibility</b> - The levels within the CTF are for
						educational purposes only. Do not use the skills and techniques that you learn from this
						platform elsewhere without authorization. Always keep it ethical and legal.</li>
					<li><b>Scope</b> - don’t attempt to attack the platform's mechanisms which include, but not limited to
						<ul>
							<li>The login / logout mechanisms </li>
							<li>The registration page</li>
							<li>The CTF key submission</li>
							<li>The scoreboard</li>
							<li>The admin console</li>
						</ul>
						<span style="visibility: hidden; display: none">However, if you stumble across something report it and we'll award points</span>
					</li>
					<li><b>Exploiting</b> - automated tools are forbidden <span  style="visibility: hidden; display: none">where the platform will deduct points for multiple
						wrongly submitted result keys</span></li>
					<li style="visibility: hidden; display: none"><b>Collaborate</b> - If you're super helpful to your fellow Elasticians and they nominate you there's
					bonus points up for grabs</li>
					<li><b>They're out there</b> - post on the #capture-the-flag channel, reach out to a colleague or use
					Google to understand a concept, don't just look up the answer</li>
					<li><b>Have fun!</b> - these are fun challenges that range from beginner to advanced difficulty and if
						you learn something along the way then that’s a bonus!</li>
				</ul>
				<br /><br />
			</div>
			<div align="justify">

				<div id="aboutDiv" style="display: none;">
					<h2 class="title">
						<fmt:message key="generic.text.aboutSecShep" />
					</h2>
					<p id="about_shepherd_blurb">
						<fmt:message key="login.text.about_blurb" />
					</p>
					<%=Analytics.sponsorshipMessage(new Locale(Validate.validateLanguage(request.getSession())))%>
				</div>
			</div>
			<!-- end content -->
			<!-- start sidebar -->
			<!-- end sidebar -->
		</div>
	</div>
	<!-- end page -->
	<script>
		jQuery.fn.center = function() {
			this.css("position", "absolute");
			this.css("left", (($(window).width() - this.outerWidth()) / 2)
					+ $(window).scrollLeft() + "px");
			return this;
		}

		$("#tools").click(function() {
			$("#toolsTable").show("slow");
		});

		$("#showAbout").click(function() {
			$("#aboutDiv").show("slow");
		});
	</script>
	<%
		if (Analytics.googleAnalyticsOn) {
	%><%=Analytics.googleAnalyticsScript%>
	<%
		}
	%>
</body>
</html>
