<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="dbProcs.Getter, utils.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: scoreboard.jsp *************************");

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
 	//Getting CSRF Token from client
 	Cookie tokenCookie = null;
 	try
 	{
 		tokenCookie = Validate.getToken(request.getCookies());
 	}
 	catch(Exception htmlE)
 	{
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(scoreboard.jsp): tokenCookie Error:" + htmlE.toString());
 	}
 	// validateSession ensures a valid session, and valid role credentials
 	// Also, if tokenCookie != null, then the page is good to continue loading
 	if (Validate.validateSession(ses) && tokenCookie != null)
 	{
 		//Log User Name
 		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString());
 		// Getting Session Variables
 		//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
 		Encoder encoder = ESAPI.encoder();
 		String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
		%>
		<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
		<title>OWASP Security Shepherd - Scoreboard</title>
		
		<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
		</head>
		<body>
		<script type="text/javascript" src="js/jquery.js"></script>
		<script type="text/javascript" src="js/tinysort.js"></script>
		<div id="wrapper">
		<!-- start header -->
		<div id="header">
			<h1>Scoreboard</h1>
			<p>The OWASP Security Shepherd Project</p>
		</div>
		<!-- end header -->
		<!-- start page -->
		<div id="page">
			<!-- start content -->
				<div id="badData"></div>
				<ul id="leaderboard" class="leaderboard"></ul>
			</div>
			<!-- end content -->
			<!-- start sidebar -->
			<!-- end sidebar -->
		</div>
		</div>
		<!-- end page -->
		<script>			
			//Scoreboard based on http://mightystuff.net/dynamic-leaderboard
			function poll() {
				$.ajax({
					type: "POST",
					url: 'scoreboard', // needs to return a JSON array of items having the following properties: id, score, username
					dataType: 'json',
					data: {
						csrfToken: "<%= csrfToken %>"
					},
					success: function(o) {
						for(i=0;i<o.length;i++) {
							if ($('#userbar-'+ o[i].id).length == 0) {
								// this id doesn't exist, so add it to our list.
								var newUser = '<li class="scoreLine"><div id="userbar-'+ o[i].id + '" class="scoreBar" title="' + o[i].username + ' with ' + o[i].score + ' points" style="width: ' + o[i].scale + '\u0025;">' +
										'<div id="userplace-'+ o[i].id + '" class="place"><h3 style="display:none;" id="user-' + o[i].id + '">' + o[i].order + '</h3>' + getGetOrdinal(o[i].place) + ': </div>' 
										+ '<div class="scoreName" >'+ o[i].username + ' </div><div class="scoreNumber" id="userscore-'+ o[i].id + '">' + o[i].score + '</div></div></li>';
								$("#leaderboard").append(newUser);
							} else {
								// this id does exist
								//update user elements in the list item.
								$('#userbar-'+ o[i].id).prop('title', o[i].username + ' with ' + o[i].score + ' points');
								$('#userscore-'+o[i].id).html(o[i].score);
								$('#userplace-'+o[i].id).html('<h3 style="display:none;" id="user-' + o[i].id + '">' + o[i].order + '</h3>' + getGetOrdinal(o[i].place) + ': ');
								
								$('#userbar-'+ o[i].id).animate({
							        width: o[i].scale+"%"
							    }, 300 );
							}
						}
						sort();
					},
				});	

				// play it again, sam (10 secs)
				t=setTimeout("poll()",10000);
			}
			
			//Algorithm from http://tinysort.sjeiti.com/
			function sort() {
				var $Ul = $('ul#leaderboard');
				$Ul.css({position:'relative',height:$Ul.height(),display:'block'});
				var iLnH;
				var $Li = $('ul#leaderboard>li');
				$Li.each(function(i,el){
					var iY = $(el).position().top;
					$.data(el,'h',iY);
					if (i===1) iLnH = iY;
				});

				$Li.tsort('h3:eq(0)',{order:'asc'}).each(function(i,el){
					var $El = $(el);
					var iFr = $.data(el,'h');
					var iTo = i*iLnH;
					$El.css({position:'absolute',top:iFr}).animate({top:iTo},500);
				});
			}
			
			function fixBoard(){
				$("#page").width($(window).width()*0.8);
				var container = $(window);
				var content = $('#page');
				content.css("left", (container.width()-content.width())/2);
			}
			
			fixBoard();
			
			$(window).resize(function() 
			{
				fixBoard();
			});
			
			function getGetOrdinal(n) {
			   var s=["th","st","nd","rd"],
				   v=n%100;
			   return n+(s[(v-20)%10]||s[v]||s[0]);
			}
			//Kick off Scoreboard
			poll();
		</script>
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
		</body>
	</html>
	<%
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
