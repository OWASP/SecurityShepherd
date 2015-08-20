<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>

<%
String levelName = "Poor Data Validation";
String levelHash = "4d8d50a458ca5f1f7e2506dd5557ae1f7da21282795d0ed86c55fefe41eb874f";

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.poor_data_validation." + levelHash, locale);
//Used more than once translations
String translatedLevelName = bundle.getString("title.question.poor_data_validation");

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
		<div id="contentDiv">
			<h2 class="title"><%= translatedLevelName %></h2>
			<p> 
				<div id="lessonIntro">
					Poor Data Validation occurs when an application does not validate submitted data correctly or sufficiently. Poor Data Validation application issues are generally low severity, they are more likely to be coupled with other security risks to increase their impact. If all data submitted to an application is validated correctly, security risks are significantly more difficult to exploit. 
					<br />
					<br />
					Attackers can take advantage of poor data validation to perform business logic attacks or cause server errors.
					<br />
					<br />
					When data is submitted to a web application, it should ensure that the data is strongly typed, has correct syntax, is within length boundaries, contains only permitted characters and within range boundaries. The data validation process should ideally be performed on the client side and again on the server side.  
					<br/>
					<br/>
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				To get the result key to this lesson, you must bypass the validation in the following function and submit a negative number.
				<div id="hint" style="display: none;">
					<h2 class="title">Lesson Hint</h2>
					The lesson only validates the number on the client side. Try use your proxy to change the data after it has left the browser.
					<br />
					<br />
				</div>
				<br />
				<br />
				<div id="badData"></div>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>
						Enter a number: 
						</td><td>
						<input type="text" id="numberBox" autocomplete="off"/>
						</td>
					<tr><td colspan="2">
						<div id="submitButton"><input type="submit" value="Submit Number"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
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
					theError = "A number must be submitted";		
				}
				else if (number < 0)
				{
					console.log("Invalid Number Submitted");
					theError = "Invalid Number: Number must be greater than 0";
				}
				else
				{
					$("#badData").hide("fast");
					$("#submitButton").hide("fast");
					$("#loadingSign").show("slow");
					$("#resultsDiv").hide("slow", function(){
						var ajaxCall = $.ajax({
							type: "POST",
							url: "<%= levelHash %>",
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
					$("#badData").html("<p> An Error Occurred: " + theError + "</p>");
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
