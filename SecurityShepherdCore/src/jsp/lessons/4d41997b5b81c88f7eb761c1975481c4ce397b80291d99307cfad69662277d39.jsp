<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*" errorPage=""%>
<%@page import="java.util.Locale"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:requestEncoding value="UTF-8" />
<fmt:setLocale value="${lang}" />
<fmt:setBundle basename="i18n.lessons.m_content_provider_leakage.4d41997b5b81c88f7eb761c1975481c4ce397b80291d99307cfad69662277d39" />

<%
	//No Quotes In level Name
	String levelName = "Content Provider Leakage Lesson";
	//Alphanumeric Only
	String levelHash = "4d41997b5b81c88f7eb761c1975481c4ce397b80291d99307cfad69662277d39";
	//Level blurb can be written here in HTML OR go into the HTML body and write it there. Nobody will update this but you
	String levelBlurb = "";

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
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));

	/*
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
	 * @author Sean Duggan
	 */
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Security Shepherd - <%=levelName%></title>
<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css"	media="screen" />

</script>
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<div id="contentDiv">
		<h2 class="title"><fmt:message key="title.question.content_provider_leak" /></h2>
		<p>
		<div id="lessonIntro">

			<br /> 	A <a>Content Provider</a> is used by Android to provide access to a structured set of data within a central repository.
					 Content Providers are intended to be accessed by other applications, however with the <a>Android Debug Bridge</a>, they can be accessed by anyone with access to a device.
					 
			<br />
					
			<br />
			In order to query a Content Provider without an App, perform the following adb commands:		<br />
			<br />
			
			<ul>
			<li>adb devices</li>
			<li>adb connect [device IP]</li>
			<li>adb shell content query --uri [Content Provider URI]</li>
			</ul>
			
			<br />
			<input type="button" value="Hide Lesson Introduction" id="hideLesson" />
		</div>
	
		<input type="button" value="Show Lesson Introduction" id="showLesson" style="display: none;" />
		<br />
		The Key can be attained by querying the Content Provider. The URI is : <a>content://com.somewhere.hidden.SecretProvider/data</a>		<br/>
		<br>
		<%= Analytics.getMobileLevelBlurb("CProviderLeakage.apk") %>
		
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
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
		</p>
	</div>
</body>
</html>
<% 
		}
		else
		{
			response.sendRedirect("../loggedOutSheep.html");
		}
	}
	else
	{
		response.sendRedirect("../loggedOutSheep.html");
	}
%>