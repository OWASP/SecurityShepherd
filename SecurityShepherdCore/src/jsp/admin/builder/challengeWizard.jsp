<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
// Challenge Wizard Index Page

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
 
System.out.println("Challenge Wizard Accessed");
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
	System.out.println("DEBUG(Challenge Wizard.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateSession ensures a valid session, and valid role credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateAdminSession(ses) && tokenCookie != null)
{
	// Getting Session Variables
	//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
	Encoder encoder = ESAPI.encoder();
	String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
	String vulnerableApplicationRoot = ExposedServer.getApplicationRoot();
	if(!vulnerableApplicationRoot.isEmpty())
	{
	%>
	<h1 class="title">Challenge Builder</h1>
	<p> 
		You can use this wizard to build custom challenges for players to take.<br/>
		Please input the name of the challenge you want to create, and the challenges database table name and attributes. If you want to include a challenge introduction or cheat sheet, these are option inputs! If you wish to select the attribute of the database table that you want users to use as the <a>look up</a> attribute (the attribute used to filter a result set). By default, this will be the first attribute you create.
		<div id="badData" style="display: none;"></div>
		<div id="theStep">
			<form id="dbForm" action="javascript:;">
			<table>
				<tr>
					<td>Challenge Name</td>
					<td><input type="text" maxlength="64" id="challengeName"/></td>
				</tr>
				<tr>
					<td>Table name</td>
					<td><input type="text" maxlength="20" id="tableName" /></td>
				</tr><tr>
					<td>Attribute Names</td>
					<td>
						<% for(int i = 0; i < 5; i++) { %>
						<input type="text" maxlength="20" id="attribName<%= i+1 %>"/> <input type="radio" name="lookUpAttrib" value="<%= i %>" <% if(i == 0) { %> checked <% } %>/><br/>
						<% } %>
					</td>
				</tr>
				<tr>
					<td>Challenge Introduction</td>
					<td><textarea style="width: 525px; height: 100px;;" id="challengeIntroduction"/></textarea></td>
				</tr>
				<tr>
					<td>Cheat Sheet</td>
					<td><textarea style="width: 525px; height: 100px;" id="challengeCheat"/></textarea></td>
				</tr>
				<tr>
					<td>Form Type</td>
					<td>
						<select id="formType">
							<option value="1">Input Box Look Up</option>
							<option value="2">Drop Down Box Select</option>
						</select>
					</td>
				</tr>
				<tr>
					<td>Vulnerability Type</td>
					<td>
						<select id="theVulType">
							<option value="1">SQL Injection</option>
							<option value="2">Blind SQL Injection</option>
							<option value="4">Reflected Cross Site Scripting</option>
						</select>
					</td>
				</tr>
				<tr>
					<td>Custom Filter</td>
					<td>
						<table><tr><td>Change This</td><td><input type="text" maxlength="20" id="filterToChange"/></td></tr>
						<tr><td>To This</td><td><input type="text" maxlength="20" id="filterToThis"/></td></tr></table>
					</td>
				</tr>
				<tr>
					<td>Populate Schema</td>
					<td>
						<div id="addRowError" style="display: none;"></div>
						<table>
						<tbody>
							<tr><th>Row #</th><th id="headerOne"></th><th id="headerTwo"></th><th id="headerThree"></th><th id="headerFour"></th><th id="headerFive"></th></tr>
							<tr style="display: none;" id="row1"><td>1</td><td><input type='text' id='row1AttribOne' maxlength='64'/></td><td><input type='text' id='row1AttribTwo' maxlength='64'/></td><td><input type='text' id='row1AttribThree' maxlength='64'/></td><td><input type='text' id='row1AttribFour' maxlength='64'/></td><td><input type='text' id='row1AttribFive' maxlength='64'/></td></tr>
							<tr style="display: none;" id="row2"><td>2</td><td><input type='text' id='row2AttribOne' maxlength='64'/></td><td><input type='text' id='row2AttribTwo' maxlength='64'/></td><td><input type='text' id='row2AttribThree' maxlength='64'/></td><td><input type='text' id='row2AttribFour' maxlength='64'/></td><td><input type='text' id='row2AttribFive' maxlength='64'/></td></tr>
							<tr style="display: none;" id="row3"><td>3</td><td><input type='text' id='row3AttribOne' maxlength='64'/></td><td><input type='text' id='row3AttribTwo' maxlength='64'/></td><td><input type='text' id='row3AttribThree' maxlength='64'/></td><td><input type='text' id='row3AttribFour' maxlength='64'/></td><td><input type='text' id='row3AttribFive' maxlength='64'/></td></tr>
							<tr style="display: none;" id="row4"><td>4</td><td><input type='text' id='row4AttribOne' maxlength='64'/></td><td><input type='text' id='row4AttribTwo' maxlength='64'/></td><td><input type='text' id='row4AttribThree' maxlength='64'/></td><td><input type='text' id='row4AttribFour' maxlength='64'/></td><td><input type='text' id='row4AttribFive' maxlength='64'/></td></tr>
							<tr style="display: none;" id="row5"><td>5</td><td><input type='text' id='row5AttribOne' maxlength='64'/></td><td><input type='text' id='row5AttribTwo' maxlength='64'/></td><td><input type='text' id='row5AttribThree' maxlength='64'/></td><td><input type='text' id='row5AttribFour' maxlength='64'/></td><td><input type='text' id='row5AttribFive' maxlength='64'/></td></tr>
						</tbody>
						
						</table>
						<input type="button" id="addRow" value="Add Row"/>
					</td>
				</tr>
				<tr>
					<td colspan="2"><input type="submit" value="Generate Challenge"></td>
				</tr>
			</table>
			</form>
		</div>
		<div id="loadingSign" style="display: none;"><p>Loading...</p></div>
		<br/>
		<br/>
		<div id="resultsDiv"></div>
	</p>
	</div>
	<script>
	var lookUpAttribVal = 0;
	var formTypeVal = 0;
	var rowNumber = 0;
	
	$("#addRow").click(function(){
		$("#addRowError").hide("fast");
		if(rowNumber < 5)
		{
			var addTheRow = "no";
			if(rowNumber != 0)
			{
				var prevOne = $("#row" + rowNumber + "AttribOne").val();
				var prevTwo = $("#row" + rowNumber + "AttribTwo").val();
				var prevThree = $("#row" + rowNumber + "AttribThree").val();
				var prevFour = $("#row" + rowNumber + "AttribFour").val();
				var prevFive =  $("#row" + rowNumber + "AttribFive").val();
				if
				(
					prevOne.length > 0 &&
					prevTwo.length > 0 &&
					prevThree.length > 0 &&
					prevFour.length > 0 &&
					prevFive.length > 0
				)
				{
					addTheRow = "yes"
					rowNumber++;
				}
			}
			else
			{
				rowNumber++;
				addTheRow = "yes"
			}
			if(addTheRow == "yes")
			{
				$("#row" + rowNumber).show("slow");
				//Set focus to the correct input
			}
			else
			{
				$("#addRowError").hide("fast", function(){
					$("#addRowError").html("<font color='red'>You must complete the previous row first.</font>");
					$("#addRowError").show("slow");
				});
			}
		}
		else
		{
			$("#addRowError").hide("fast", function(){
				$("#addRowError").html("<font color='red'>Max Input Rows Reached!</font>");
				$("#addRowError").show("slow");
			});
		}	
	});
	
	$("#attribName1").keyup(function () {
		$("#headerOne").text($(this).val());
	}).keyup();
	
	$("#attribName2").keyup(function () {
		$("#headerTwo").text($(this).val());
	}).keyup();
	
	$("#attribName3").keyup(function () {
		$("#headerThree").text($(this).val());
	}).keyup();
	
	$("#attribName4").keyup(function () {
		$("#headerFour").text($(this).val());
	}).keyup();
	
	$("#attribName5").keyup(function () {
		$("#headerFive").text($(this).val());
	}).keyup();
	
	$("#dbForm").submit(function(){
		var lookUpAttribVal = $("input:radio[name=lookUpAttrib]").val();
		var formTypeVal = $("#formType").val();
		$("#badData").hide("fast");
		var theChallengeName = $("#challengeName").val();
		var theTableName = $("#tableName").val();
		var theAttribNameOne = $("#attribName1").val();
		var theAttribNameTwo = $("#attribName2").val();
		var theAttribNameThree = $("#attribName3").val();
		var theAttribNameFour = $("#attribName4").val();
		var theAttribNameFive = $("#attribName5").val();
		var theIntroduction = $("#challengeIntroduction").val();
		var theCheat = $("#challengeCheat").val();
		var theToFilter = $("#filterToChange").val();
		var theToFilterTo = $("#filterToThis").val();
		var theVul = $("#theVulType").val();
		var row1 = new Array();
			row1[0] = $("#row1AttribOne").val();
			row1[1] = $("#row1AttribTwo").val();
			row1[2] = $("#row1AttribThree").val();
			row1[3] = $("#row1AttribFour").val();
			row1[4] = $("#row1AttribFive").val();
		var row2 = new Array();
			row2[0] = $("#row2AttribOne").val();
			row2[1] = $("#row2AttribTwo").val();
			row2[2] = $("#row2AttribThree").val();
			row2[3] = $("#row2AttribFour").val();
			row2[4] = $("#row2AttribFive").val();
		var row3 = new Array();
			row3[0] = $("#row3AttribOne").val();
			row3[1] = $("#row3AttribTwo").val();
			row3[2] = $("#row3AttribThree").val();
			row3[3] = $("#row3AttribFour").val();
			row3[4] = $("#row3AttribFive").val();
		var row4 = new Array();
			row4[0] = $("#row4AttribOne").val();
			row4[1] = $("#row4AttribTwo").val();
			row4[2] = $("#row4AttribThree").val();
			row4[3] = $("#row4AttribFour").val();
			row4[4] = $("#row4AttribFive").val();
		var row5 = new Array();
			row5[0] = $("#row5AttribOne").val();
			row5[1] = $("#row5AttribTwo").val();
			row5[2] = $("#row5AttribThree").val();
			row5[3] = $("#row5AttribFour").val();
			row5[4] = $("#row5AttribFive").val();
		if(theChallengeName.length > 0 &&
		theTableName.length > 0 &&
		theVul.length > 0 &&
		(
			theAttribNameOne.length > 0 &&
			theAttribNameTwo.length > 0 && 
			theAttribNameThree.length > 0 && 
			theAttribNameFour.length > 0 && 
			theAttribNameFive.length > 0
		))
		{
			$("#theStep").hide("fast");
			$("#loadingSign").show("slow");
			$("#resultsDiv").hide("slow", function(){
				var ajaxCall = $.ajax({
					dataType: "text",
					type: "POST",
					url: "generateChallengeDb",
					data: {
						challengeName: theChallengeName, tableName: theTableName,
						attribName1: theAttribNameOne, attribName2: theAttribNameTwo, attribName3: theAttribNameThree, attribName4: theAttribNameFour, attribName5: theAttribNameFive,
						cheatSheet: theCheat, challengeIntro: theIntroduction,
						formType: formTypeVal, lookUpAttrib: lookUpAttribVal, vulnerabilityType: theVul,
						filterFrom: theToFilter, filterTo: theToFilterTo,
						r1a1: row1[0], r1a2: row1[1], r1a3: row1[2], r1a4: row1[3], r1a5: row1[4],
						r2a1: row2[0], r2a2: row2[1], r2a3: row2[2], r2a4: row2[3], r2a5: row2[4],
						r3a1: row3[0], r3a2: row3[1], r3a3: row3[2], r3a4: row3[3], r3a5: row3[4],
						r4a1: row4[0], r4a2: row4[1], r4a3: row4[2], r4a4: row4[3], r4a5: row4[4],
						r5a1: row5[0], r5a2: row5[1], r5a3: row5[2], r5a4: row5[3], r5a5: row5[4],
						csrfToken: "<%= csrfToken %>"
					},
					async: false
				});
				if(ajaxCall.status == 200)
				{
					$("#theStep").html(ajaxCall.responseText);
				}
				else
				{
					$("#theStep").html("<p> An Error Occured: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
				}
				$("#loadingSign").hide("fast", function(){
					$("#theStep").show("slow");
				});
			});
		}
		else
		{
			var errorMessage = "";
			if(theChallengeName.length == 0)
			{
				errorMessage = "Invalid Challenge Name";
			} 
			else if (theTableName.length == 0)
			{
				errorMessage = "Invalid Table Name";
			}
			else if (theAttribNameOne.length == 0 || 
			theAttribNameTwo.length == 0 || 
			theAttribNameThree.length == 0 || 
			theAttribNameFour.length == 0 || 
			theAttribNameFive.length == 0)
			{
				errorMessage = "You must specify all attibute names";
			}
			else if (theVul.length == 0)
			{
				errorMessage = "You must specify a Vulnerability Type";
			}
			else
			{
				errorMessage = "Invalid Data"
			}
			$("#badData").html("<font color='red'>" + errorMessage + "</font>");
			$("#badData").show("slow");
		}
	});
	</script>
	<%
	}
	else
	{
	//No vulnerable applicaiton root has been set. 
	System.out.println("No Vulnerable Application Root has been set"); %>
	<h1 class="title">Vulnerable Application Root</h1>
	This functionality will not work until you set your vulnerable application server's context. This is available in the vulnerable application servers log file. Search the log file for "Servlet root". Challenge Builder Functionailty will not work if this is not set correctly. This can be modified in the <a>Configuration</a> section of your administrators controls<br/>
	An example application root is as follows;<br/><br/>
	Servlet root = <a>C:\Users\userName\Servers\applicationServers\tomcatExposed\temp\1-ROOT</a>
	<br/>
	<br/>
	<div id="badData" style="display: none;"></div>
	<div id="theStep">
	<form action="javascript:;" id="leForm">
	Vulnerable Application Root <input type="text" id="vAppRoot" style="width: 300px;"/><input type="submit" id="submitButton" value="Set Vulnerable Application Root"/>
	<div id="loadingSign" style="display: none;"><p>Loading...</p></div>
	</form>
	</div>
	<script>
	$("#leForm").submit(function(){
		$("#badData").hide("fast");
		var applicationRoot = $("#vAppRoot").val();
		if(applicationRoot.length > 8)
		{
			$("#submitButton").hide("fast");
			$("#loadingSign").show("slow", function(){
				var ajaxCall = $.ajax({
					dataType: "text",
					type: "POST",
					url: "changeVulnerableAppRoot",
					data: {
						vulnerableApplicationRoot: applicationRoot,
						csrfToken: "<%= csrfToken %>"
					},
					async: false
				});
				$("#theStep").hide("fast", function(){
					if(ajaxCall.status == 200)
					{
						$("#theStep").html(ajaxCall.responseText);
					}
					else
					{
						$("#badData").html("<p> An Error Occured: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
						$("#badData").show("slow");
					}
				});
				$("#loadingSign").hide("fast", function(){
					$("#theStep").show("slow");
				});
			});
		}
		else
		{
			$("#badData").html("<font color='red'>Invalid Application Root. Too Short.</font>");
			$("#badData").show("slow");
		}
	});
	</script>
	<%
	}
}
else
{
response.sendRedirect("login.jsp");
}
}
else
{
response.sendRedirect("login.jsp");
}
%>
