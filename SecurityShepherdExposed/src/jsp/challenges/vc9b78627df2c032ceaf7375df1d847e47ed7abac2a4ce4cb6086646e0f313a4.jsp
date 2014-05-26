<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
// Insecure Direct Object References

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

ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Insecure Direct Object References Challenge Two Accessed");
String ApplicationRoot = getServletContext().getRealPath("");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Insecure Direct Object References Challenge Two</title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">Insecure Direct Object Reference Challenge Two</h2>
			<p> 
				The result key for this challenge is stored in the private message for a user not listed below
				<br />
				<br />
				<center>
				<form id="leForm" action="javascript:;">
					<table>
					<tr></td>
						<select id='userId' style='width: 300px;' multiple>
							<option value="c81e728d9d4c2f636f067f89cc14862c">Paul Bourke</option>
							<option value="eccbc87e4b5ce2fe28308fd9f2a7baf3">Will Bailey</option>
							<option value="e4da3b7fbbce2345d7772b0674a318d5">Orla Cleary</option>
							<option value="8f14e45fceea167a5a36dedd4bea2543">Ronan Fitzpatrick</option>
							<option value="6512bd43d9caa6e02c990b0a82652dca">Pat McKenana</option>
						</select>
					</td></tr>
					<tr><td>
						<div id="submitButton"><input style='width: 300px;' type="submit" value="Show this profile"/></div>
						<p style="display: none; text-align: center;" id="loadingSign">Loading...</p>
					</td></tr>
					</table>
				</form>
				</center>
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				var optionValue = $("#userId").val();
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "vc9b78627df2c032ceaf7375df1d847e47ed7abac2a4ce4cb6086646e0f313a4",
						data: {
							userId: optionValue
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
			});
		</script>
</body>
</html>
