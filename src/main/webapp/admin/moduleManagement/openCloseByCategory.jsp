<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: openCloseByCategory.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(openCloseByCategory.jsp): tokenCookie Error:" + htmlE.toString());
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
//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety

String csrfToken = Encode.forHtmlAttribute(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	<div id="formDiv" class="post">
		<h1 class="title">Open and Close Levels</h1>
		<div class="entry">
			<form id="theForm" action="javascript:;">
				<p>Use this form to open and close levels by entire categories. Levels that are closed will not appear in any level listings.</p>
				<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
				<div id="submitButton" align="center">
					<div>
						<table>
						<tr><td colspan="2">
						<%= Getter.getOpenCloseCategoryMenu(ApplicationRoot) %>
						</td></tr>
						<tr><td>
						<input type="submit" value="Close Categories">
						</td><td>
						<input type="button" id="openCategories" value="Open Categories">
						</td></tr>
						</table>
					</div>
				</div>
			</form>
			<br>
			<div id="resultDiv" style="display:none;" class="informationBox"></div>
			<div id="loadingDiv" style="display:none;" class="menuButton">Loading...</div>
			<div id="badData"></div>
			<script>					
			$("#theForm").submit(function(){
				//Get Data
				var toDo = $("#toDo").val();
				var theCsrfToken = $('#csrfToken').val();
				
				//Show and Hide Stuff
				$("#loadingDiv").show("fast");
				$("#badData").hide("fast");
				$("#resultDiv").hide("fast");
				$("#submitButton").slideUp("fast", function(){
					//The Ajax Operation
					var ajaxCall = $.ajax({
						type: "POST",
						url: "openCloseModuleCategories",
						data: {
							toOpenOrClose: toDo,
							openOrClose: "closed",
							csrfToken: theCsrfToken
						},
						async: false
					});
					$("#loadingDiv").hide("fast", function(){
						if(ajaxCall.status == 200)
						{
							//Now output Result Div and Show
							$("#resultDiv").html(ajaxCall.responseText);
							$("#resultDiv").show("fast");
						}
						else
						{
							$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
							$("#badData").show("slow");
						}
						console.log('Showing Form');
						$("#submitButton").slideDown("slow");
					});
				});
				var theRefreshError = "Could not Refresh Menu";
				//Refresh the Side Menu
				refreshSideMenu(theCsrfToken, theRefreshError);
			});
			
			$("#openCategories").click(function(){
				var toDo = $("#toDo").val();
				var theCsrfToken = $('#csrfToken').val();
				//Show and Hide Stuff
				$("#loadingDiv").show("fast");
				$("#badData").hide("fast");
				$("#resultDiv").hide("fast");
				$("#submitButton").slideUp("fast", function(){
					//The Ajax Operation
					var ajaxCall = $.ajax({
						type: "POST",
						url: "openCloseModuleCategories",
						data: {
							toOpenOrClose: toDo,
							openOrClose: "open",
							csrfToken: theCsrfToken
						},
						async: false
					});
					$("#loadingDiv").hide("fast", function(){
						if(ajaxCall.status == 200)
						{
							//Now output Result Div and Show
							$("#resultDiv").html(ajaxCall.responseText);
							$("#resultDiv").show("fast");
							//Refresh the Side Menu
							refreshSideMenu(theCsrfToken, theRefreshError);
						}
						else
						{
							$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
							$("#badData").show("slow");
						}
						console.log('Showing Form');
						$("#submitButton").slideDown("slow");
						$('html, body').animate({
					        scrollTop: $("#resultDiv").offset().top
					    }, 1000);
					});
				});
				var theRefreshError = "Could not Refresh Menu";
			});
			</script>
			<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
		</div>
	</div>
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