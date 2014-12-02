<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%

String levelName = new String("Cross Site Scripting Lesson");

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
 
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " Accessed");
if (request.getSession() != null)
{
	HttpSession ses = request.getSession();
	//Getting CSRF Token from client
	Cookie tokenCookie = null;
	try
	{
		tokenCookie = Validate.getToken(request.getCookies());
	}
	catch(Exception htmlE)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName +".jsp: tokenCookie Error:" + htmlE.toString());
	}
	// validateSession ensures a valid session, and valid role credentials
	// If tokenCookie == null, then the page is not going to continue loading
	if (Validate.validateSession(ses) && tokenCookie != null)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString());
		// Getting Session Variables
		//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
		Encoder encoder = ESAPI.encoder();
		String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Cross Site Scripting Lesson</title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What is Cross Site Scripting?</h2>
			<p> 
				<div id="lessonIntro">
					Cross-Site Scripting, or <a>XSS</a>, issues occur when an application uses <a>untrusted data</a> in a
					web browser without sufficient <a>validation</a> or <a>escaping</a>. If untrusted data contains
					a client side script, the browser will execute the script while it is interpreting the page.
					<br />
					<br />
					Attackers can use XSS attacks to execute scripts in a victim's browser which can hijack user sessions, 
					deface web sites, or redirect the user to malicious sites. Anyone that can send data to the system, 
					including administrators, are possible candidates for performing XSS attacks in an application. 
					<br />
					<br />
					According to OWASP, XSS is the most widespread vulnerability found in web applications today. 
					This is partially due to the variety of <a>attack vectors</a> that are available. The easiest way 
					of showing an XSS attack executing is using a simple <a>alert box</a> as a client side script pay load. 
					To execute a XSS payload, a variety of an attack vectors may be necessary to overcome insufficient escaping 
					or validation. The following are examples of some known attack vectors, that all create the same
					 <a>alert</a> pop up that reads "XSS".
					<br />
					<br />
					&lt;SCRIPT&gt;<a>alert(&#39;XSS&#39;)</a>&lt;/SCRIPT&gt;<br />
					&lt;IMG SRC=&quot;&#x23;&quot; ONERROR=&quot;<a>alert(&#39;XSS&#39;)</a>&quot;/&gt;<br />
					&lt;INPUT TYPE=&quot;BUTTON&quot; ONCLICK=&quot;<a>alert(&#39;XSS&#39;)</a>&quot;/&gt;<br />
					&lt;IFRAME SRC=&quot;javascript:<a>alert(&#39;XSS&#39;)</a>;&quot;&gt;&lt;/IFRAME&gt;<br />
					<br />
					<br />
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				The following search box outputs untrusted data without any validation or escaping. 
				Get an alert box to execute through this function to show that there is an XSS vulnerability present.
				<br />
				<br />				
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						Please enter the <a>Search Term</a> that you want to look up
					</td></tr>
					<tr><td>
						<input style="width: 400px;" id="searchTerm" type="text"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="Get this user"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
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
				var theSearchTerm = $("#searchTerm").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "zf8ed52591579339e590e0726c7b24009f3ac54cdff1b81a65db1688d86efb3a",
						data: {
							searchTerm: theSearchTerm,
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
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>
<%
	}
	else
	{
		response.sendRedirect("../ajaxLogin.jsp");
	}
}
else
{
	response.sendRedirect("../ajaxLogin.jsp");
}
%>
