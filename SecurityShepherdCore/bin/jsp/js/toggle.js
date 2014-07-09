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