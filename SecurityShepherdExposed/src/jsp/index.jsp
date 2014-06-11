<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage="" %>

<%
try
{
	if (request.getSession() != null)
	{
		HttpSession ses = request.getSession();
		String userName = (String) ses.getAttribute("decyrptedUserName");
		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Exposed Index.jsp has been accessed by " + userName);
	}
}
catch (Exception e)
{
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Exposed Index.jsp has been accessed");
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not recover username: " + e.toString());
}
/**
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

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Security Shepherd</title>

<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<script type="text/javascript" src="js/jquery.js"></script>
<div id="wrapper">
<!-- start header -->
<div id="header">
	<h1>Security Shepherd</h1>
	<p>Exposed Application Server</p>
</div>
<!-- end header -->
<!-- start page -->
<div id="page">
	<!-- start content -->
	<div id="content">
		<div class="post">
			<div class="entry">
				Your In the wrong place for levels! It's good news that you can visit this page but you will have to get back to the core application to play the game!
			</div>
		</div>
		<div class="post">
			<h1 class="title">About Security Shepherd</h1>
			<div class="entry">
				Web application security risks are a widespread problem and in most cases can be very complicated to solve.
				Some applications can be developed with security in mind, such as strong authentication mechanisms for administrators.
				These mechanisms do not prevent the majority of an experienced hacker's arsenal of exploits from compromising 
				administrator accounts. It is important to find and fix these vulnerabilities before an application is released.
				<br/>
				<br/>
				The OWASP Security Shepherd project has been designed and implemented with the aim of fostering and improving application security
				 awareness among a varied skill-set demographic.This project enables users to learn or to 
				 improve upon existing manual penetration testing skills. This is accomplished through 
				 lesson and challenge techniques. A lesson provides a user with a lot of help in completing 
				 that module, where a challenge puts what the user learned in the lesson to use.
				<br/>
				<br/>
				The OWASP Security Shepherd project covers the OWASP Top Ten web app risks and also covers the OWASP Top Ten Mobile risks as well. 
				Using these risks as a challenge test bed, common security vulnerabilities can be explored and their 
				impact on a system understood. Many of these levels include insufficient mitigations and protections to these risks, 
				such as blacklist filters, atrocious encoding schemes, barbaric security mechanisms and 
				poor security configuration. The modules have been crafted to provide not only a challenge for a 
				security novice, but security professionals as well.
			<br/>
			<h1 class="title">Project Sponsors</h1>
			<p>
			The OWASP Security Shepherd project would like to acknowledge and thank the generous support of our sponsors. 
			Please be certain to visit their stall at the <a href="http://bit.ly/AppSecEu2014">OWASP AppSec EU 2014</a> 
			conference as well as follow them on <a href="http://bit.ly/bccRiskAdvisory">twitter</a>.
			<br/><br/>
			<a href="http://bit.ly/BccRiskAdvisorySite"><img src="css/images/bccRiskAdvisoryLogo.jpg" alt="BCC Risk Advisory"/></a>
			<a href="http://bit.ly/EdgeScan"><img src="css/images/edgescanLogo.jpg" alt="EdgeScan" /></a>
			</p>
			</div>
			<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
		</div>
	</div>
	<!-- end content -->
	<!-- start sidebar -->
	<!-- end sidebar -->
</div>
</div>
<!-- end page -->
<div id="footer">
	<p>Mark Denihan, Security Shepherd - 2011</p>
</div>
<script src="js/toggle.js"></script>
</body>
</html>
