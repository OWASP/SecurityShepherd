<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>

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

	final String LEVEL_NAME = "Insecure Direct Object References Lesson";
	final String LEVEL_HASH = "fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100";

	//Translation Stuff
	Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
	ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.insecure_direct_object_ref." + LEVEL_HASH, locale);
	//Used more than once translations
	String translatedLevelName = bundle.getString("title.quesetion.insecure_direct_object_ref");

	ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
	String owaspMoreInfo = 	generic.getString("module.generic.owasp.more.info");
	String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
	String owaspUrlAttack = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.attack.insecureDirectObjectReferences");

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
					<%= bundle.getString("paragraph.info1") %>
					<br />
					<br />
					<%= bundle.getString("paragraph.info2") %>
					<br />
					<br />
					<%= bundle.getString("paragraph.info3") %>
					<br/>
					<br/>
					<%= owaspMoreInfo %> <a href="<%= owaspUrlAttack %>" target="_blank"> <%= owaspGuideTo %> Insecure Direct Object References </a>
					<br/>
					<br/>
					<input type="button" value="<%= bundle.getString("button.hideIntro") %>" id="hideLesson"/>
				</div>

				<input type="button" value="<%= bundle.getString("button.showIntro") %>" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				<%= bundle.getString("challenge.description") %>
				<br />
				<br />
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<div id="submitButton"><input type="submit" value="<%= bundle.getString("challenge.refresh") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("sign.loading") %>...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="<%= bundle.getString("sign.hint") %>?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>

				<div id="resultsDiv">
				<h2 class='title'><%= bundle.getString("challenge.userTitle") %>: <%= bundle.getString("challenge.guest") %></h2>
				<table>
					<tr><th><%= bundle.getString("challenge.age") %>:</th><td>22</td></tr>
					<tr><th><%= bundle.getString("challenge.address") %>:</th><td>54 Kevin Street, Dublin</td></tr>
					<tr><th><%= bundle.getString("challenge.email") %>:</th><td>guestAccount@securityShepherd.com</td></tr>
					<tr><th><%= bundle.getString("challenge.message") %>:</th><td><%= bundle.getString("challenge.noMessage") %></td></tr>
				</table>
				</div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= LEVEL_HASH %>",
						data: {
							username: "guest"
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv").html(ajaxCall.responseText);
					}
					else
					{
						$("#resultsDiv").html("<p> <%= bundle.getString("generic.error") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
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
		response.sendRedirect("../loggedOutSheep.html");
	}
}
else
{
	response.sendRedirect("../loggedOutSheep.html");
}
%>
