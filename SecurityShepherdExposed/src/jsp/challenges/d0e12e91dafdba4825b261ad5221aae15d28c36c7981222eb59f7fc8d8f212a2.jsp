<%@ page contentType="text/html; charset=iso-8859-1" language="java" errorPage="" %>
<%

//No Quotes In level Name
String levelName = "SQL Injection Challenge 6";
//Alphanumeric Only
String levelHash = "d0e12e91dafdba4825b261ad5221aae15d28c36c7981222eb59f7fc8d8f212a2";
//Level blurb can be writen here in HTML OR go into the HTML body and write it there. Nobody will update this but you
String levelBlurb = "Download the file and play with it to extract the key for this level!";

/**
 * SQL Injection Challenge 6
 * <br>
 * System escapes single quotes but then interprets \x encoding.
 *  So the encoding can be bypassed by encoding the attack vector in \x encoding
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
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - <%= levelName %></title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />

</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title"><%= levelName %></h2>
			<p> 
				To obtain the result key you must obtain <em>Brendan's</em> answer to his security question. 
				<br />
				<br />
				<h3>Get Your Authentication Number</h3>
				<p>Put in your pin number to get your current authentication number</p>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>Please enter your Pin number: </td>	
					<td><input type="password" id="pinNumber" maxlength="4" autocomplete="off" /></td></tr>
					<tr><td colspan="2">	
						<div id="submitButton">
						<input type="submit" value="Run User Query"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
				
			</p>
		</div>
		<script>
			$("#leForm").submit(function(){
				var thePinNumber = $("#pinNumber").val();
				$("#submitButton").hide("fast");
				$("#loadingSign").show("slow");
				$("#resultsDiv").hide("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "<%= levelHash %>",
						data: {
							pinNumber: thePinNumber
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
