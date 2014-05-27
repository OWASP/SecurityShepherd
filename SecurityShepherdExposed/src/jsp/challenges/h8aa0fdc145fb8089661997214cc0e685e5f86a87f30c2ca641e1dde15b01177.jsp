<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.ShepherdExposedLogManager" errorPage="" %>
<%
// Insecure Cryptographic Storage Challenge 2 Accessed

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

String levelName = "Insecure Cryptographic Storage Challenge 2 Accessed";
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
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Insecure Cryptographic Storage Challenge Two</title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">Insecure Cryptographic Storage Challenge Two</h2>
			<p> 
				The result key has been encrypted to ensure that nobody can finish the challenge without knowing the secret key to decrypt it. The following form can be used to check if you have the correct result key.
				<br/>
				<br/>
				<form id="leForm" action="javascript:;">
				<table>
					<tr><td>
						<input type="text" id="resultKeyAttempt" style="width: 300px;"/></><input type="submit" value="Check Result Key"/>
					</td></tr>
				</table>
				</form>
				<br/>
				<br/>
				<div id="resultDiv"></div>
			</p>
		</div>
		<script>			
		$("#leForm").submit(function(){
			//2D encryption
			var input = $("#resultKeyAttempt").val();
			theKey = "kpoisaijdieyjaf";
			var theAlphabet =   "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz";

			// Validate theKey:
			theKey = theKey.toUpperCase();
			var theKeysLength = theKey.length;
			var i;
			var adjustedKey = "";
			for(i = 0; i < theKeysLength; i ++)
			{
				var currentKeyChar = theAlphabet.indexOf(theKey.charAt(i));
				if(currentKeyChar < 0)
					continue;
				adjustedKey += theAlphabet.charAt(currentKeyChar);
			}
			theKey = adjustedKey;
			theKeysLength = theKey.length;

			// Transform input:
			var inputLength = input.length;
			var output = "";
			var theKeysCurrentIndex = 0;
			for(i = 0; i < inputLength; i ++)
			{
				var currentChar = input.charAt(i);
				var currentCharValue = theAlphabet.indexOf(currentChar);
				if(currentCharValue < 0)
				{
					output += currentChar;
					continue;
				}
				var lowercase = currentCharValue >= 26 ? true : false;
				currentCharValue += theAlphabet.indexOf(theKey.charAt(theKeysCurrentIndex));
				currentCharValue += 26;
				if(lowercase)
					currentCharValue = currentCharValue % 26 + 26;
				else
					currentCharValue %= 26;
				output += theAlphabet.charAt(currentCharValue);
				theKeysCurrentIndex =(theKeysCurrentIndex + 1) % theKeysLength;
			}
			
			//Check result for validity
			$("#resultDiv").hide("fast", function(){
				if(output == "DwsDagmwhziArpmogWaSmmckwhMoEsmgmxlivpDttfjbjdxqBwxbKbCwgwgUyam")
					$('#resultDiv').html("<p>Yeah, that's correct</p>");
				else
					$('#resultDiv').html("<p>No, that's not correct</p>");
				$("#resultDiv").show("slow");
			});
			//Output the output variable
			/*
			$("#resultDiv").hide("fast", function(){
					$('#resultDiv').html("Encrypted Output: " + output);
				$("#resultDiv").show("slow");
			});
			*/
		});
		</script>
</body>
</html>
