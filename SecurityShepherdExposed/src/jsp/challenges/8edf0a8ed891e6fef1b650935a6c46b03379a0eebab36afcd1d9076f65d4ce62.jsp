<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.ShepherdExposedLogManager" errorPage="" %>
<%

//No Quotes In level Name
String levelName = "SQL Injection Challenge 5";
//Alphanumeric Only
String levelHash = "8edf0a8ed891e6fef1b650935a6c46b03379a0eebab36afcd1d9076f65d4ce62";
//Level blurb can be writen here in HTML OR go into the HTML body and write it there. Nobody will update this but you
String levelBlurb = "Not used - See Below";

try
{
	if (request.getSession() != null)
	{
		HttpSession ses = request.getSession();
		String userName = (String) ses.getAttribute("decyrptedUserName");
		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + userName);
	}
}
catch (Exception e)
{
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed");
	ShepherdExposedLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not recover username: " + e.toString());
}
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
				If you can buy trolls for free you'll receive the key for this level!
				<br/>
				<br/>
				<h3>Super Meme Shopping</h3>
				Hey customers: Due to a shipping mistake we are completely over stocked in rage Memes. 
				Use the coupon code <a>PleaseTakeARage</a> or <a>RageMemeForFree</a> to get yours for free!!!.
				<br />
				<br />
				<form id="leForm" action="javascript:;">
					<table>
					<!-- Header -->
					<tr>
						<th>Picture</th>
						<th>Cost</th>
						<th>Quantity</th>
					</tr>
					<!-- Rage Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/rage.png"/></td>
						<td>$45</td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfRage" autocomplete="off"/>
					</tr>
					<!-- NotBad Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/notbad.jpeg"/></td>
						<td>$15</td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfNotBad" autocomplete="off"/>
					</tr>
					<!-- Troll Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/troll.png"/></td>
						<td>$3000</td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfTroll" autocomplete="off"/>
					</tr>
					<!-- MeGusta Row -->
					<tr>
						<td><img width="50px" height="50px" src="<%= levelHash %>/megusta.png"/></td>
						<td>$30</td>
						<td><input type="text" style="width: 20px" value="0" id="numberOfMegusta" autocomplete="off"/>
					</tr>
					</table>
					Please select how many things you would like to buy and click submit
					<table>
					<tr><td>Coupon Code:</td><td><input type="text" id="couponCode" autocomplete="off"/></td></tr>
					<tr><td colspons = 2>			
						<div id="submitButton">
						<input type="submit" value="Place Order"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
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
		<script src="<%= levelHash %>/couponCheck.js"></script>
</body>
</html>