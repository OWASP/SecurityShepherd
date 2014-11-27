<%@ page import="org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, utils.ShepherdLogManager" %>
<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage="" %>
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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Parameter = " + parameter);
	Cookie cookie = new Cookie("JSESSIONID3", parameter);
    ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Cookie Value = " + cookie.getValue());
    response.addCookie(cookie);
}
catch(Exception e)
{
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Parameter Decription Fail: " + parameter);
	parameter = "";
}
if (request.getSession() != null)
{
	HttpSession ses = request.getSession();
	ses.setAttribute("decyrptedUserName", Hash.decryptUserName(parameter));	
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Ready to Go?</title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">		
			<% if(parameter.isEmpty()) { %>
			<h2 class="title">You are not ready!</h2>
			<p>
				Refresh the home page! If this error persists; Log out and back in! If this error continues to persist, please contact an Administrator!
			</p>
			<% } else { %>
			<h2 class="title">You have entered the game!</h2>
			<p> 
				Now that you can see this, you're good to go! Get cracking on lessons and challenges! 
				<br/><br/>
				Remember, the levels you are playing are sub applications. Keep the game play in these applications! 
				Stay away from your session ID's! You'll just log yourself out of you change them!
				<br/><br/>
				If you havn't already configured a web proxy, you better! It makes things much easier!
			</p>
			<% } %>
			<h2 class="title">Project Sponsors</h2>
			<p>
			The OWASP Security Shepherd project would like to acknowledge and thank the generous support of our sponsors. 
			Please be certain to visit their stall at the <a href="http://bit.ly/AppSecEu2014">OWASP AppSec EU 2014</a> 
			conference as well as follow them on <a href="http://bit.ly/bccRiskAdvisory">twitter</a>.
			<br/><br/>
			<a href="http://bit.ly/BccRiskAdvisorySite"><img src="css/images/bccRiskAdvisorySmallLogo.jpg" alt="BCC Risk Advisory"/></a>
			<a href="http://bit.ly/EdgeScan"><img src="css/images/edgescanSmallLogo.jpg" alt="EdgeScan" /></a>
			<br/><br/>
			The OWASP Security Shepherd Project would also like to thank Dr. Anthony Keane and the ITB Research Lab for hosting http://owasp.securityShepherd.eu!  
			</p>
			<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
		</div>
</body>
</html>
