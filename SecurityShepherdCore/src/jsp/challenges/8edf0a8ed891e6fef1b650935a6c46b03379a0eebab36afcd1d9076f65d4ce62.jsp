<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*" errorPage="" %>
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
//No Quotes In level Name
String levelName = "SQL Injection Challenge 5";
//Alphanumeric Only
String levelHash = "8edf0a8ed891e6fef1b650935a6c46b03379a0eebab36afcd1d9076f65d4ce62";

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.challenges.injection." + levelHash, locale);
//Used more than once translations
String i18nLevelName = bundle.getString("challenge.challengeName");

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
	<title>Security Shepherd - <%= i18nLevelName %></title>
	<link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen" />
	
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= i18nLevelName %></h2>
			<p> 
				<%= bundle.getString("challenge.description") %>
				<br/>
				<br/>
				<h3><%= bundle.getString("challenge.description.h3") %></h3>
				<%= bundle.getString("challenge.description.p1") %> 
				<%= bundle.getString("challenge.description.p2") %>
				<br />
				<br />
				<form id="leForm" action="javascript:;">
					<table>
					<!-- Header -->
					<tr>
						<th><%= bundle.getString("challenge.form.picture") %></th>
						<th><%= bundle.getString("challenge.form.cost") %></th>
						<th><%= bundle.getString("challenge.form.quantity") %></th>
					</tr>
					<!-- Rage Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/rage.png"/></td>
						<td><%= bundle.getString("challenge.form.45") %></td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfRage" autocomplete="off"/>
					</tr>
					<!-- NotBad Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/notbad.jpeg"/></td>
						<td><%= bundle.getString("challenge.form.15") %></td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfNotBad" autocomplete="off"/>
					</tr>
					<!-- Troll Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/troll.png"/></td>
						<td><%= bundle.getString("challenge.form.3000") %></td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfTroll" autocomplete="off"/>
					</tr>
					<!-- MeGusta Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/megusta.png"/></td>
						<td><%= bundle.getString("challenge.form.30") %></td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfMegusta" autocomplete="off"/>
					</tr>
					</table>
					<%= bundle.getString("challenge.form.pleaseSelect") %>
					<table>
					<tr><td><%= bundle.getString("challenge.form.couponCode") %>:</td><td><input type="text" id="couponCode" autocomplete="off"/></td></tr>
					<tr><td colspons = 2>			
						<div id="submitButton">
						<input type="submit" value="<%= bundle.getString("challenge.form.button.value") %>"/></div>
						<p style="display: none;" id="loadingSign"><%= bundle.getString("sign.loading") %>...</p>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				var theMegustaAmount = $("#numberOfMegusta").val();
				var theTrollAmount = $("#numberOfTroll").val();
				var theRageAmount = $("#numberOfRage").val();
				var theNotBadAmount = $("#numberOfNotBad").val();
				var theCouponCode = $("#couponCode").val();
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							megustaAmount: theMegustaAmount, 
							trollAmount: theTrollAmount,
							rageAmount: theRageAmount, 
							notBadAmount: theNotBadAmount,
							couponCode: theCouponCode
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$("#resultsDiv").html(ajaxCall.responseText);
					}
					else
					{
						$("#resultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					}
					$("#resultsDiv").show("slow", function(){
						$("#loadingSign").hide("fast", function(){
							$("#submitButton").show("slow");
						});
					});
				});
			});
		</script>
		<script src="<%= levelHash %>/couponCheck.js"></script>
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