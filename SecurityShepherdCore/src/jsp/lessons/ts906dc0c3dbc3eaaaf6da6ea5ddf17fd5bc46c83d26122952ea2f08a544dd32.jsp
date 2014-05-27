<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Insufficient Transport Layer Protection Lesson Accessed");

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
 */
 
if (request.getSession() != null)
{
HttpSession ses = request.getSession();
Getter get = new Getter();
//Getting CSRF Token from client
Cookie tokenCookie = null;
try
{
	tokenCookie = Validate.getToken(request.getCookies());
}
catch(Exception htmlE)
{
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(Insufficient Transport Layer Protection.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateSession ensures a valid session, and valid role credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateSession(ses) && tokenCookie != null)
{
// Getting Session Variables
String userName = (String) ses.getAttribute("userName");
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Broadcast Triggered by: " + userName);
Broadcaster.sendBroadcast();
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Insufficient Transport Layer Protection</title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What is Insufficient Transport Layer Protection?</h2>
			<p> 
				<div id="lessonIntro">
					Applications that fail to authenticate, encrypt and protect the confidentially and integrity of sensitive network traffic are implementing insufficient transport layer protection. Sufficient protection can be implemented using a secure transport protocol, like <a>HTTPS</a> (Hypertext Transfer Protocol Secure) or <a>sFTP</a> (Secure File Transfer Protocol), that implements SSL protocol to provide encrypted data transfer. Anyone that can monitor the network traffic between a user and the application can perform <a>Man in the Middle</a> (MITM) attacks.
					<br/>
					<br/>
					Imagine a victim is interacting with an application's web server from an Internet cafe. An attacker is sniffing the network traffic in the cafe and sees that the victim is interacting with an application that is using <a>insufficient transport layer protection</a>. This is easily identifiable as the attack can see the victim's packets to the application pass across in <a>plain text</a>. The attacker can hijack the victim's session by simply taking the victim's session cookie out of a request or the attacker can modify the actions that the victim wants to carry out, such as adding a zero at the end of a quantity amount.
					<br/>
					<br/>
					In the same cafe the attacker notices that a user is beginning communication with a service that does use some form of transport layer protection. The application does not have a valid SSL certificate and the user's <a>browser warns them</a> every time they interact with the application that it is not trusted. The users, by habit, ignore the warning and add an exception to their browser. The attacker can take advantage of this trust by intercepting the SSL authentication. Instead of creating an SSL connection with the application, the victim is tricked into creating a secure connection with the attackers machine using their credentials. The attacker then forwards the same request to the application to create a valid and secure connection. To do this, the victim is presented with a forged SSL certificate. The browser warns the user of the untrusted certificate, but the <a>user ignores the warning</a> out of habit.
					<br/>
					<br/>
					User <a>awareness</a> is as important as sufficient transport layer protection when it comes to these types of security risks. 
					<br />
					<br />
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				To mark this lesson as complete you need to find the result key. The result key is been broadcast in plain text on the network.
			</p>
		</div>
		<script>
			$('#hideLesson').click(function(){
				$("#lessonIntro").hide("slow", function(){
					$("#showLesson").show("fast");
				});
			});
			
			$("#showLesson").click(function(){
				$('#showLesson').hide("fast", function(){
					$("#lessonIntro").show("slow");
				});
			});
		</script>
</body>
</html>
<%
}
else
{
response.sendRedirect("login.jsp");
}
}
else
{
response.sendRedirect("login.jsp");
}
%>