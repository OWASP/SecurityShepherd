$("#lessonList").click(function () {
	$("#theLessonList").toggle("slow");
	$("#theChallengeList").hide("fast");
	$("#theAdminList").hide("fast");
});   

$("#challengeList").click(function () {
	$("#theChallengeList").toggle("slow");
	$("#theLessonList").hide("fast");
	$("#theAdminList").hide("fast");
});

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

$(".successAlert").click(function(){
	alert("successAlert click");
	$(this).hide("slide", { direction: "left" }, 1000);
});

$(".errorAlert").click(function(){
	$(this).hide("slide", { direction: "left" }, 1000);
});

$(".challengeHeader").click(function(){
	$(".challengeList").hide("fast");
	$(this).parent().find(".challengeList").show("slow");
});