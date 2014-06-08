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

$("#createNewAdminLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/userManagement/createNewAdmin.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#createNewClassLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/userManagement/createNewClass.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#addPlayersLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/userManagement/addPlayers.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#assignPlayersLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/userManagement/assignPlayers.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#setDefaultClassForRegistrationLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/userManagement/setDefaultClassForRegistration.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#upgradePlayersLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/userManagement/upgradePlayers.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#enableCheatsLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/cheatManagement/enableCheats.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#disableCheatsLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/cheatManagement/disableCheats.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#createCheatsLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/cheatManagement/createCheat.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#openFloorModuleLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/openFloor.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#incrementalModulesLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/incrementalModules.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#feedbackLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/feedback.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#setModuleStatusLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/setStatus.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#openCloseByCategory").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/openCloseByCategory.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});


$("#setVulnerableRootLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/config/setVulnerableRoot.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#registrationLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/userManagement/updateRegistration.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#progressLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/classProgress.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#scoreboardLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/scoreboard.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#stopHereLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/stopHere.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#disableBlockLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/moduleManagement/removeModuleBlock.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#setCoreHostAddressLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/config/setCoreHostAddress.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#setExposedHostAddressLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/config/setHostAddress.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#setCoreDatabaseLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/config/setCoreDatabase.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#setExposedDatabaseLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/config/setExposedDatabase.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#enableFeedbackLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/config/enableFeedback.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});

$("#disableFeedbackLink").click(function(){
	$("#contentDiv").hide("fast", function(){
		$("#contentDiv").load("admin/config/disableFeedback.jsp", function(response, status, xhr) {
		  if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		  $("#contentDiv").show("fast");
		});
	});	
});