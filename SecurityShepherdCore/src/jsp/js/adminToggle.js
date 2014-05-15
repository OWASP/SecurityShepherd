$("#adminList").click(function () {
	$("#theAdminList").toggle("slow");
	$("#theLessonList").hide("fast");
	$("#theChallengeList").hide("fast");
	$("#theUncompletedList").hide("fast");
	$("#theCompletedList").hide("fast");
});

$("#cheatSheetManagementList").click(function () {
	$("#theCheatSheetManagementList").toggle("slow");
	$("#theModuleManagementList").hide("fast");
	$("#theUserManagementList").hide("fast");
	$("#theConfigurationList").hide("fast");
});

$("#moduleManagementList").click(function () {
	$("#theModuleManagementList").toggle("slow");
	$("#theCheatSheetManagementList").hide("fast");
	$("#theUserManagementList").hide("fast");
	$("#theConfigurationList").hide("fast");
});


$("#userManagementList").click(function () {
	$("#theUserManagementList").toggle("slow");
	$("#theCheatSheetManagementList").hide("fast");
	$("#theModuleManagementList").hide("fast");
	$("#theConfigurationList").hide("fast");
});

$("#configurationList").click(function () {
	$("#theConfigurationList").toggle("slow");
	$("#theCheatSheetManagementList").hide("fast");
	$("#theModuleManagementList").hide("fast");
	$("#theUserManagementList").hide("fast");
});

$("#lessonManagementList").click(function () {
	$("#theLessonManagmentList").toggle("slow");
	$("#theChallengeManagementList").hide("fast");
});

$("#challengeManagementList").click(function () {
	$("#theChallengeManagementList").toggle("slow");
	$("#theLessonManagmentList").hide("fast");
});