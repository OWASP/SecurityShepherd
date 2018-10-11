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

	final String LEVEL_NAME = "Poor Data Validation";
	final String LEVEL_HASH = "4d8d50a458ca5f1f7e2506dd5557ae1f7da21282795d0ed86c55fefe41eb874f";

	//Translation Stuff
	Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
	ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.poor_data_validation." + LEVEL_HASH, locale);
	//Used more than once translations
	String translatedLevelName = bundle.getString("title.question.poor_data_validation");

	ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
	String owaspMoreInfo = 	generic.getString("module.generic.owasp.more.info");
	String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
	String owaspUrlAttack = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.otg.buslogic.001");

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
        <br/>
        <br/>
        <%= owaspMoreInfo %> <a href="<%= owaspUrlAttack %>" target="_blank"> <%= owaspGuideTo %> Poor Data Validation </a>
        <br/>
        <br/>
        <input type="button" value="<%= bundle.getString("button.hideIntro") %>" id="hideLesson"/>
    </div>

        <input type="button" value="<%= bundle.getString("button.showIntro") %>" id="showLesson"  style="display: none;"/>
        <br/>
        <br/>
        <%= bundle.getString("challenge.description")%>
        <div id="hint" style="display: none;">
            <h2 class="title"><%= bundle.getString("challenge.hintHeader") %></h2>
            <%= bundle.getString("challenge.hint") %>
            <br />
            <br />
				</div>
				<br />
				<br />
				<div id="badData"></div>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						<%= bundle.getString("challenge.enterNumber") %>: 
						</td><td>
						<input type="text" id="numberBox" autocomplete="off"/>
						</td>
					<tr><td colspan="2">
						<div id="submitButton"><input type="submit" value="<%= bundle.getString("button.submitNumber") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("button.loading") %>...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="<%= bundle.getString("button.hint") %>?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			var counter=0;
			$("#leForm").submit(function(){
				counter = counter + 1;
				console.log("Counter: " + counter);
				var number = $("#numberBox").val();
				var theError = "";
				if(number.length == 0)
				{
					console.log("No Number Submitted");
					theError = "<%= bundle.getString("error.noNumber") %>";		
				}
				else if (number < 0)
				{
					console.log("Invalid Number Submitted");
					theError = "<%= bundle.getString("error.badNumber") %>";
				}
				else
				{
					$("#badData").hide("fast");
					$("#submitButton").hide("fast");
					$("#loadingSign").show("slow");
					$("#resultsDiv").hide("slow", function(){
						var ajaxCall = $.ajax({
							type: "POST",
							url: "<%= LEVEL_HASH %>",
							data: {
								userdata: number
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
				} //End of If Else
				if (counter == 3)
				{
					console.log("Showing Hint Button");
					$("#hintButton").show("slow");
				}
				if (theError.length > 0)
				{
					$("#badData").html("<p> <%= bundle.getString("error.occured") %>:" + theError + "</p>");
					$("#badData").show("slow");
				}
			});
			
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
