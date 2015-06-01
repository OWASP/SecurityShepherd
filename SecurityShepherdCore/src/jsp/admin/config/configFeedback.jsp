<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: configFeedback.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(configFeedback.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
// Token is now validated when accessing admin pages to stop attackers causing other users to tigger logs of access attempts
Object tokenParmeter = request.getParameter("csrfToken");
if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
{
	//Logging Username
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
	// Getting Session Variables
	//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
	Encoder encoder = ESAPI.encoder();
	String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
	String ApplicationRoot = getServletContext().getRealPath("");
%>
	<h1 class="title">Configure Feedback</h1>
	<p>You can configure Shepherd to force users to submit a feedback form before the module is marked as complete. This is used both to facilitate project improvements based on feedback submitted and for system administrators to collect "Reports of Understanding" from their students.
	If you would like to submit the collected feedback to the Security Shepherd Project Development Team, please follow the steps <a href="https://github.com/markdenihan/owaspSecurityShepherd/wiki/How-to-Submit-Shepherd-DB-Stored-User-Feedback">found here</a>.</p>
	
	<!-- Enable Feedback Section -->
	<div id="enableFeedback">
		<h2 class="title">Enable Feedback</h2>
		<p>Enable feedback to force users to submit feedback on each module before they can complete them</p>
		<a href="javascript:;" style="text-decoration: none;" id="enableFeedback" title="Enable Feedback">
			<div class="menuButton">Enable Feedback</div>
		</a>
		<br>
	</div>
	<div id="enableLoadingDiv" style="display:none;" class="menuButton">Loading...</div>
	<div id="enableResultDiv" class="informationBox" style="display:none;"></div>
	<div id="enableBadData" style="display:none;"></div>
	
	<!-- Disable feedback Section -->
	<div id="disableFeedback">
		<h2 class="title">Disable Feedback</h2>
		<p>Disable feedback to allow users to complete modules without having to submit a feedback form</p>
		<a href="javascript:;" style="text-decoration: none;" id="disableFeedback" title="Disable Feedback">
			<div class="menuButton">Disable Feedback</div>
		</a>
		<br>
	</div>
	<div id="disableLoadingDiv" style="display:none;" class="menuButton">Loading...</div>
	<div id="disableResultDiv" class="informationBox" style="display:none;"></div>
	<div id="disableBadData" style="display:none;"></div>
	<script>
	var theCsrfToken = "<%= csrfToken %>";
	$("#enableFeedback").click(function(){
		$("#enableLoadingDiv").show("fast");
		$("#enableBadData").hide("fast");
		$("#enableResultDiv").hide("fast");
		//The Ajax Operation
		$("#enableFeedback").slideUp("fast", function(){
			var ajaxCall = $.ajax({
				type: "POST",
				url: "enableFeedback",
				data: {
					csrfToken: theCsrfToken
				},
				async: false
			});
			$("#enableLoadingDiv").hide("fast", function(){
				if(ajaxCall.status == 200)
				{
					$("#enableResultDiv").html(ajaxCall.responseText);
					$("#enableResultDiv").show("fast");
				}
				else
				{
					$("#enableBadData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
					$("#enableBadData").show("slow");
				}
				$("#enableFeedback").slideDown("slow");
				$('html, body').animate({
			        scrollTop: $("#enableResultDiv").offset().top
			    }, 1000);
			});
		});
	});
	
	$("#disableFeedback").click(function(){
		$("#disableLoadingDiv").show("fast");
		$("#disableBadData").hide("fast");
		$("#disableResultDiv").hide("fast");
		//The Ajax Operation
		$("#disableFeedback").slideUp("fast", function(){
			var ajaxCall = $.ajax({
				type: "POST",
				url: "disableFeedback",
				data: {
					csrfToken: theCsrfToken
				},
				async: false
			});
			$("#disableLoadingDiv").hide("fast", function(){
				if(ajaxCall.status == 200)
				{
					$("#disableResultDiv").html(ajaxCall.responseText);
					$("#disableResultDiv").show("fast");
				}
				else
				{
					$("#disableBadData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
					$("#disableBadData").show("slow");
				}
				$("#disableFeedback").slideDown("slow");
				$('html, body').animate({
			        scrollTop: $("#disableResultDiv").offset().top
			    }, 1000);
			});
		});
	});
	</script>
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
	<%
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
%>