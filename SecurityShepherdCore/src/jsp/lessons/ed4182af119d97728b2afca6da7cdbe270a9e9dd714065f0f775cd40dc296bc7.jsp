<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	// CSRF Lesson
ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "CSRF Lesson Accessed");

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
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(index.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateSession ensures a valid session, and valid role credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateSession(ses) && tokenCookie != null)
{
// Getting Session Variables
String hex = (String) ses.getAttribute("userName");
String falseId = new Integer(hex.getBytes().hashCode() + hex.substring(0, hex.length() / 2).hashCode()).toString();
ses.setAttribute("falseId", falseId);
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Cross-Site Request Forgery Lesson</title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What is a Cross-Site Request Forgery?</h2>
			<p> 
				<div id="lessonIntro">
					A Cross-Site Request Forgery, or <a>CSRF</a>, attack forces a user's browser to send a <a>forged HTTP request</a> with the user's session cookie to an application, tricking the user into unknowingly interacting with an application that they are currently logged into. CSRF attacks are possible when the application is not ensuring that a user is enfact interacting with it. The severity of a CSRF attack varies with the functionality of the application the victim is been tricked into interacting with. If the attack is aimed at an administrator, the severity will be a lot higher than that of a guest user. 
					<br />
					<br />
					To prevent CSRF attacks, every request must contain a <a>nonce</a> token (an unpredictable number) to be included with every request. To find CSRF vulnerabilities in applications, this is the token that is tested. If a request does not contain a nonce at all, then it is likely vulnerable to CSRF attacks. If a request does contain a nonce, then there are more steps to include in testing for CSRF. Even though the nonce is in the request, it may not be checked, and may work with a null value. It is possible that the applications nonce management will allow an attack to use their valid nonce in other user requests!
					<br />
					<br />
					HTTP requests can be sent using Javascript. Requests that are sent this way include a "X-Requested-With" header. If this is checked for incoming requests, this can serve as CSRF protection without a nonce value. This header cannot be replicated from a remote domain, due to the <a>Same Origin Policy</a>, preventing an attacker from delivering the attack remotely. It is not advised to use this as a sole CSRF protection model, as the Same Origin Policy does not exist in HTML5.
					<br />
					<br />
					CSRF attacks can be performed on <a>GET</a> and <a>POST</a> HTTP requests. To force a victim to seamlessly submit a request in a GET request, the request (highlighted) can be embedded into an image tag on a web page such as follows;<br />
					&lt;img src=&quot;<a>http://www.secureBank.ie/sendMoney?giveMoneyTo=hacker&amp;giveAmount=1000</a>&quot;/&gt;
					<br />
					<br />
					To force a victim to send a POST request, it requires a little more effort. The easiest way is to create a form that automatically submits using javascript, such as the following example;<br/>
					&lt;form name=&quot;csrfForm&quot; action=&quot;<a>http://www.secureBank.ie/sendMoney</a>&quot; method=&quot;<a>POST</a>&quot;&gt;<br />
					&lt;input type=&quot;hidden&quot; name=&quot;<a>giveMoneyTo</a>&quot; value=&quot;hacker&quot; /&gt;<br />
					&lt;input type=&quot;hidden&quot; name=&quot;<a>giveAmount</a>&quot; value=&quot;1000&quot; /&gt;<br />
					&lt;input type=&quot;submit&quot;/&gt;<br />
					&lt;/form&gt;<br />
					&lt;script&gt;<br />
					document.csrfForm.submit();<br />
					&lt;/script&gt;<br />
					<br />
					<input type="button" value="Hide CSRF Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show CSRF Introduction" id="showLesson"  style="display: none;"/>
				<br />
				<br />
				The function used by an administrator to mark this lesson as complete for a user is done with the following GET request to <a><%= encoder.encodeForHTML(ExposedServer.getSecureUrl()) %></a>, where 'exampleId' is a valid userId;
				<br/>
				<br/>
				<a> GET /root/grantComplete/csrfLesson?userId=exampleId </a>
				<br />
				To complete this lesson, send an administrator a message with a image URL, that will show in an embedded <a>&lt;img&gt;</a> tag that will force them to submit the request described above, replacing the exampleId attribute with your temp userId: <a><%= falseId %></a>
				<br />
				<br />
				<br />
				<h2 class="title">Contact Admin</h2>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						Please enter the <a>URL of the image</a> that you want to send to one of Security Shepherds 24 hour administrators.
					</td></tr>
					<tr><td>
						<input style="width: 400px;" id="messageForAdmin" type="text"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="Send Message"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				var theMessageForAdmin = $("#messageForAdmin").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "ed4182af119d97728b2afca6da7cdbe270a9e9dd714065f0f775cd40dc296bc7",
						data: {
							messageForAdmin: theMessageForAdmin,
							csrfToken: "<%= csrfToken %>"
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv").html(ajaxCall.responseText);
					}
					else
					{
						$("#resultsDiv").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv").show("slow", function(){
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
						});
					});
				});
			});
			
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