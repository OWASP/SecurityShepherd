<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  language="java" import="utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>

<%
	//No Quotes In level Name
	String levelName = "What is Mobile Insecure Data Storage?";
	//Alphanumeric Only
	String levelHash = "ecfad0a5d41f59e6bed7325f56576e1dc140393185afca8975fbd6822ebf392f";
	//Translation Stuff
	Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
	ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.m_insecure_data_storage." + levelHash, locale);
	ResourceBundle mobile = ResourceBundle.getBundle("i18n.moduleGenerics.mobileGenericStrings", locale);
	//Used more than once translations
	String translatedLevelName = bundle.getString("title.question.m_insecure_data_storeage");
	
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
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
	<div id="contentDiv">
		<h2 class="title"><%= translatedLevelName %></h2>
		<p> 
			<div id="lessonIntro">
			<%= bundle.getString("paragraph.info.1") %>
			<br>
			<br/>
			<%= bundle.getString("paragraph.info.2") %>
			<br>
			<br/>
			<%= bundle.getString("paragraph.info.3") %>
			<br>
			<br/>
			 <%= bundle.getString("paragraph.info.4") %>
			<br />
			<br>
			<br/>
			<input type="button" value="<%= bundle.getString("button.hideIntro") %>" id="hideLesson"/>
		</div>
		<input type="button" value="<%= bundle.getString("button.showIntro") %>" id="showLesson"  style="display: none;"/>
		<br/>
		<br/>
		<%= bundle.getString("challenge.description") %>
		
		<br/>
		<br/>
		<%= mobile.getString("mobileBlurb.vmLink.1") + " InsecureData.apk " + mobile.getString("mobileBlurb.vmLink.2") %>
		
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
