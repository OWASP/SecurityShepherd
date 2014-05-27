<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.ShepherdExposedLogManager" errorPage="" %>
<%
	//No Quotes In level Name
String levelName = "Failure to Restrict URL Access Challenge 2";
//Alphanumeric Only
String levelHash = "278fa30ee727b74b9a2522a5ca3bf993087de5a0ac72adff216002abf79146fa";
//Level blurb can be writen here in HTML OR go into the HTML body and write it there. Nobody will update this but you
String levelBlurb = "Download the file and play with it to extract the key for this level!";
//Logs the IP, Forwarded IP that acceeded this level with the level name in the debug for convience. If you want to log more stuff in the JSP use this as an example
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
 * This level has obfusticated javascript that describes admin funcitons that fail to restrict URL access
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
				<% /* Put Your Blurb Here Instead of the following scriptlet. Not this comment Bren. Jeesh*/ %>
				
				An administrator of the following sub application would have no issue finding the result key to this level. But considering that you are a meer guest, you will not be shown the simple button administrators can click.
				<br/>
				<br/>
				<% /* IF you need a form - Present it like this */ %>
				<form id="leForm" action="javascript:;">
					<table>
					<tr><td>			
						<div id="submitButton">
						<input type="submit" value="Get Guest Info"/></div>
						<p style="display: none;" id="loadingSign">Loading...</p>
						<div style="display: none;" id="hintButton"><input type="button" value="Would you like a hint?" id="theHintButton"/></div>
					</td></tr>
					</table>
				</form>
				
				<div id="resultsDiv"></div>
			</p>
		</div>
		<script>
			eval(function(p,a,c,k,e,d){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--){d[e(c)]=k[c]||e(c)}k=[function(e){return d[e]}];e=function(){return'\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c])}}return p}('$("#A").f(3(){$("#8").5("9");$("#7").4("1");$("#2").5("1",3(){e 0=$.h({i:"j",b:"v",c:{u:"t",},d:g});m(0.6==k){$("#2").a(0.s)}q{$("#2").a("<p> l r n: "+0.6+" "+0.o+"</p>")}$("#2").4("1",3(){$("#7").5("9",3(){$("#8").4("1")})})})});$("#w").f(3(){$("#8").5("9");$("#7").4("1");$("#2").5("1",3(){e 0=$.h({i:"j",b:"x",c:{y:"z",},d:g});m(0.6==k){$("#2").a(0.s)}q{$("#2").a("<p> l r n: "+0.6+" "+0.o+"</p>")}$("#2").4("1",3(){$("#7").5("9",3(){$("#8").4("1")})})})});',37,37,'ajaxCall|slow|resultsDiv|function|show|hide|status|loadingSign|submitButton|fast|html|url|data|async|var|submit|false|ajax|type|POST|200|An|if|Occurred|statusText||else|Error|responseText|ismcoa98sUD8j21dmdoasmcoISOdjh3189|guestData|278fa30ee727b74b9a2522a5ca3bf993087de5a0ac72adff216002abf79146fa|leAdministratorFormOfAwesomeness|278fa30ee727b74b9a2522a5ca3bf993087de5a0ac72adff216002abf79146fahghghmin|adminData|youAreAnAdminOfAwesomenessWoopWoop|leForm'.split('|'),0,{}))
		</script>
</body>
</html>