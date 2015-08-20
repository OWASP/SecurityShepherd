<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>

<%
	//No Quotes In level Name
	String levelName = "Mobile Broken Crypto?";
	//Alphanumeric Only
	String levelHash = "911fa7f4232e096d6a74a0623842c4157e29b9bcc44e8a827be3bb7e58c9a212";
	//Translation Stuff
	Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
	ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.m_broken_crypto." + levelHash, locale);
	//Used more than once translations
	String translatedLevelName = bundle.getString("title.question.mobile_broken_crypto");
	
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
	 * @author Sean Duggan
	 */
	 
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
	
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Security Shepherd - <%= translatedLevelName %></title>
<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css"
	media="screen" />

</script>
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<div id="contentDiv">
		<p>
		<h2 class="title"><%= translatedLevelName %></h2>
			<p> 
				<div id="lessonIntro">
			<br/>
			
			Cryptography is difficult to get right, as a result many Apps use cryptography poorly and become vulnerable to attack. An App can transfer encryption keys insecurely, use a known broken or deprecated cryptographic algorithm or developers can create their own Crypto algorithms.			
			<br/>
			
			Poor key management can be due to hard coded keys, keys stored in directories, transferring the key in an insecure way or using the same key all the time. If the developers of an App make use a of custom, unproven, untested encryption algorithm then it is highly likely that the encrypted data is vulnerable.
			Finally
			<br/>
			
			
			<br/>
			
			<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
			
			
			 <br/> The developers of this App are holding a competition, whoever can crack their secure chat wins. Unfortunately, the developers have misunderstood the definition of Cryptography. Reduce the intercepted messages exchanged to plain text to reveal the key. <br />
				<br/>
			  <%= Analytics.getMobileLevelBlurb("BrokenCrypto.apk") %>

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
			
		</p>
	</div>
	<% if (Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
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
