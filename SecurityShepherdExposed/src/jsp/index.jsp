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
				Your In the wrong place! Get your ass back to the <a>core</a>!
			</div>
		</div>
		<div class="post">
			<h1 class="title">About Security Shepherd</h1>
			<div class="entry">
				It is widely accepted now among researchers and practitioners in computing that there 
				is no application or service on the internet that is immune from security attacks or threats.
				These security threats can result in attacks that diminish customers' trust with an organisation, 
				damage it's reputation, as well as subjecting the organisation to an array of costly law suits. 
				However, a lot of efforts are being done by organisations and users to protect their network and applications,
				such as deploying network layer security to create a secure service. Protections like this and many more that 
				form defence in depth, do not prevent attacks that can be performed at the application layer.
				<br/>
				<br/>
				Web application security risks are a widespread problem and in most cases can be very complicated to solve.
				Some applications can be developed with security in mind, such as strong authentication mechanisms for administrators.
				These mechanisms do not prevent the majority of an experienced hacker's arsenal of exploits from compromising 
				administrator accounts. It is important to find and fix these vulnerabilities before an application is released.
				<br/>
				<br/>
				Security Shepherd is a security aware in depth project. Designed and implemented with the aim of fostering and
				improving security awareness among a varied skill-set demographic. This project enables users to learn or to 
				improve upon existing manual penetration testing skills. This is accomplished through challenge and demonstration 
				techniques. Utilizing the OWASP top ten as a challenge test bed, common security vulnerabilities can be explored 
				and their impact on a system understood. The bi-product of this challenge game is the acquired skill to harden a 
				players own environment from OWASP top ten security risks. <br/>
			</div>
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
