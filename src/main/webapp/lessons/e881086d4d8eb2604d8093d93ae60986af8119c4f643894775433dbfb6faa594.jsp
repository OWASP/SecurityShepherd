<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*" errorPage=""%>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%@ page import="dbProcs.FileInputProperties" %>

<%
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

	final String LEVEL_NAME =  "SQL Injection Lesson";
	final String LEVEL_HASH = "e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594";

	//Translation Stuff
	Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
	ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.sql_injection." + LEVEL_HASH, locale);
	//Used more than once translations
	String translatedLevelName = bundle.getString("title.question.sql_injection");

	ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
	String owaspMoreInfo = 	generic.getString("module.generic.owasp.more.info");
	String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
	String owaspUrlAttack = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.attack.sqli");

	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), LEVEL_NAME + " Accessed");
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
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), LEVEL_NAME +".jsp: tokenCookie Error:" + htmlE.toString());
		}
		// validateSession ensures a valid session, and valid role credentials
		// If tokenCookie == null, then the page is not going to continue loading
		if (Validate.validateSession(ses) && tokenCookie != null)
		{
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), LEVEL_NAME + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));

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
					<br />
					<br />
					<%= bundle.getString("paragraph.info.2") %>
					<br />
					<br />
					<%= bundle.getString("paragraph.info.3") %>
					<br />
					<br />
					<%= bundle.getString("paragraph.info.4") %>
					<br/>
					<br/>
					<%= owaspMoreInfo %> <a href="<%= owaspUrlAttack %>" target="_blank"> <%= owaspGuideTo %> SQL Injection </a>
					<br/>
					<br/>
					<input type="button" value="<%= bundle.getString("button.hideIntro") %>" id="hideLesson"/>
				</div>
				<input type="button" value="<%= bundle.getString("button.showIntro") %>" id="showLesson"  style="display: none;"/>
				<br/>
				
				<%= bundle.getString("challenge.description") %>
				<br />
				<br />
				<div id="hint" style="display: none;">
					<h2 class="title"><%= bundle.getString("hint.title") %></h2>
					<%= bundle.getString("lesson.hint.1") %>
					<br />
					<br />
					<div>SELECT * FROM tb_users WHERE username ='<a id="userContent"></a>';</div>
					<br />
					<br />
				</div>
				
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<%= bundle.getString("enter.user.name") %>
					</td></tr>
					<tr><td>
						<input style="width: 400px;" id="aUserName" type="text"/>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input type="submit" value="<%= bundle.getString("get.user.button") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("word.info.loading") %></p>
						<div style="display: none;" id="hintButton"><input type="button" value="<%= bundle.getString("sign.hint") %>" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			var counter = 0;
			$("#leForm").submit(function(){
				counter = counter + 1;
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#userContent").text($("#aUserName").val());
				var theName = $("#aUserName").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594",
						data: {
							aUserName: theName
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv").html(ajaxCall.responseText);
					}
					else
					{
						$("#resultsDiv").html("<p> <%= bundle.getString("generic.error") %> " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv").show("slow", function(){
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
							if (counter == 3)
							{
								$("#hintButton").show("slow");
							}
						});
					});
				});
			});
			
			$("#aUserName").keyup(function () {
				$("#userContent").text($(this).val());
			}).keyup();
			
			$("#theHintButton").click(function() {
				$("#hintButton").hide("fast", function(){
					$("#hint").show("fast");
				});
			});;
			
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
		response.sendRedirect("../loggedOutSheep.html");
	}
}
else
{
	response.sendRedirect("../loggedOutSheep.html");
}
%>
