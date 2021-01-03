
var serverHost = "localhost";
var serverPort = "8080";
var restBaseUrl = "http://" + serverHost + ":" + serverPort;
var playerList = [];
var answercount = 0;
var playscore = 0;
var rewardActivePlayer = true;
const MAX_TRIES = 2;
var categoryMap;
var categories;

$(document).ready(function() {
	createTimer(logPlayerAnswer);
	hideTimer();
	categoryMap = new Map();
	serverHost = $("#server-host p").html();
	serverPort = $("#server-port p").html();
	restBaseUrl = "http://" + serverHost + ":" + serverPort;
	$("#categorySelectList").html("");
});


function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
//        $("#conversation").show();
    }
    else {
//        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    console.log("Creating socket /gs-guide-websocket");
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
        	console.log("In /topic/greetings handler");
        	console.log("Got from /topic/greetings: " + JSON.parse(greeting.body));

        });
    });
}


function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}


function disconnectPlayer() {
    if (playerStompClient !== null) {
    	playerStompClient.disconnect();
    }
//    setConnected(false);
    console.log("Disconnected");
}

function getCurrentUser() {
	return $("#name").val();
}

function sendName() {
	console.log("Sending to /app/hello" + JSON.stringify({'name': $("#name").val()}));
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function getPlayerList() {
	var url = restBaseUrl + "/getplayers";
	console.log('getPlayerList: ' + url);
	$.get(url, function (data, textStatus, jqXHR) {
	    console.log('getPlayerList Response: ' + textStatus);
	    console.log('getPlayerList Response data: ' + JSON.parse(data));
	    if ((typeof data !== 'undefined') && (textStatus == 'success')) {
	    	var players = Object.keys(JSON.parse(data));
	    	console.log('Players keys: ' + players);
	    	$("#playerSelectList").html("");
	    	players.forEach((key, index) => {
	    		$("#playerSelectList").append("<option value=\"" + key + "\">" + key + "</option>");
	    	});	    	
	    	playerList = [];
	    	players = Object.entries(JSON.parse(data)).forEach(([key, value]) => { 
	    		console.log("Player = " + key + " : " + JSON.stringify(value));
	    		playerList.push(JSON.parse(JSON.stringify(value)));
	    	});
	    	console.log('Number of Players entries: ' + playerList.length);
	    } 
	});
}



function playerSelectList() {
	var selected = $("#playerSelectList").val();
	$("#activePlayer").val(selected);
	console.log('playerSelectList: ' + selected);
}

function activatePlayer() {
	var player = $("#activePlayer").val();
	if (empty(player)) {
		console.log("Activate player - please enter a name");
		alert("Activate player - please enter a name");
		return;
	}
	console.log("Activating player " + player);
	var url = restBaseUrl + "/getadminold";
	var message = {'name':player, 'action':'activatePlayerCmd'};
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});
}

function deActivatePlayers() {
	console.log("De-activating players");
	var url = restBaseUrl + "/getadminold";
	var message = {'action':'deActivatePlayerCmd'};
	$("#activePlayer").val("");
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});	
}

function getCategories() {
	console.log("getCategories");
	var url = restBaseUrl + "/getcategories";
	$.get(url, function (data, textStatus, jqXHR) {
	    console.log('getCategories Response: ' + textStatus);	    
	    if ((typeof data !== 'undefined') && (textStatus == 'success')) {
	    	
	    	console.log('getCategories data: ' + data);	    	
	    	categories = JSON.parse(data);
//	    	categories.sort();
	    	$("#categorySelectList").html("");
	    	categories.forEach((entry) => {
	    		var keyValue = Object.keys(JSON.parse(entry));
	    		var key = keyValue[0];
	    		var kv = JSON.parse(entry);
	    		var value = kv[key];
//	    		var remain = categoryMap.get(key);
	    		if (typeof categoryMap.get(key) === 'undefined') {
	    			categoryMap.set(key, Number.MAX_SAFE_INTEGER);
	    			console.log('getCategories - category remaining was undefined - set to max int');
	    		}
	    		console.log('getCategories - Category ' + key + ' entries remaining: ' + categoryMap.get(key));;
	    		if (parseInt(categoryMap.get(key)) > 0) {
	    			console.log('getCategories - Category ' + key + " added to category list");
	    			$("#categorySelectList").append("<option value='" + entry + "'>" + value + " : " + key + "</option>");
	    		} else {
	    			console.log('getCategories - Category ' + key + " excluded from category list");
	    			$("#categorySelectList").append("<option disabled=\"true\" value='" + entry + "'>" + value + " : " + key + "</option>");
	    		}
	    	});	    	
	    } 
	});
}


function activateCategory() {
	var selected = $("#categorySelectList").val();
	console.log('activateCategory: ' + selected);
	
	var url = restBaseUrl + "/getcategoryentry";
	var message = {'category':selected};
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	    if ((typeof data !== 'undefined') && (textStatus == 'success')) {
	    	var categorydata = JSON.stringify(data);
	    	console.log('activateCategory data: ' + categorydata);
	    	$("#titleName").val(data.title);
	    	$("#hintUrl").val(data.embeddedUrl);
	    	$("#playPoints").val(data.points);
	    	$("#otherUrl").val(data.url);
	    	categoryMap.set(selected, data.remaining);
	    	if (categoryMap.get(selected) == 0) {
	    		console.log('activateCategory - Last entry for category: ' + selected);
	    		getCategories();
	    	}
	    }
	});	
	
}

function categorySelectList() {
	var selected = $("#categorySelectList").val();
	console.log('playerSelectList: ' + selected);
}


function logPlayerAnswer(timeLeft) {
	
	var orderedList = [];
	var time = $("#playTime").val();
//	if (timeLeft < Math.abs(time * 0.8)) {
		console.log('logPlayerAnswer: Refreshing player list');
		getPlayerList(); 
//	}
	if (typeof playerList === 'undefined') {
		console.log('logPlayerAnswer: Player list is not defined');
		return;
	}

//	orderedList = playerList;
	orderedList = JSON.parse(JSON.stringify(playerList)); // deep copy of array
	if (orderedList.length == 0) {
		return;
	}
	orderedList.sort((a, b) => (a.order > b.order) ? 1 : -1);
	console.log('logPlayerAnswer: Refreshing answering order list at time = ' + timeLeft);
	$("#answerOrder").html("");
	for (i=0; i<orderedList.length; i++) {
		$("#answerOrder").append("<tr><td>" + orderedList[i].order + 
				"</td><td>" + orderedList[i].name +
				"</td><td>" + orderedList[i].score + 
				"</td><td>" + 
				"<button id=\"" + orderedList[i].name + "_win\" class=\"btn btn-default\" onclick='winhandler(\"" + orderedList[i].name + "\")' type=\"button\">Win</button>" +
				"<button id=\"" + orderedList[i].name + "_loss\" class=\"btn btn-default\" onclick='losshandler(\"" + orderedList[i].name + "\")' type=\"button\">Loss</button>" +
				"</td></tr>");    
	}
	var activeplayer = $("#activePlayer").val();
	if (typeof activeplayer !== 'undefined') {
		$("#" + activeplayer + "_win").prop('disabled', true);
		$("#" + activeplayer + "_loss").prop('disabled', true);
	}
}


function winhandler(name) {
	console.log('winhandler Winner: ' + name);
	var url = restBaseUrl + "/getadminold";
	var points = $("#playPoints").val();
	answercount++;
	if (answercount>MAX_TRIES) {
		console.log('winhandler: Max tries reached... skipping decrementScore');
		return;
	}
	var mypoints1 = Math.round(points/answercount);
	var message1 = {'action':'incrementScore', 'content': "" + mypoints1, 'name': ""+ name};
	console.log('winhandler sending: ' + JSON.stringify(message1));
	$.get(url, message1, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});
	logPlayerAnswer(0);
	if (rewardActivePlayer != true) {
		return;
	}
	var name2 = $("#activePlayer").val();
	var mypoints2 = Math.round(points/(answercount + answercount));
	var message2 = {'action':'incrementScoreCmd', 'content': "" + mypoints2, 'name': ""+ name2};
	console.log('winhandler sending: ' + JSON.stringify(message2));
	$.get(url, message2, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);   
	});
	logPlayerAnswer(0);
}

function losshandler(name) {
	console.log('losshandler Loser: ' + name);
	var points = $("#playPoints").val();
	answercount++;
	if (answercount>MAX_TRIES) {
		console.log('losshandler: Max tries reached... skipping decrementScore');
		return;
	}
	var mypoints = Math.round(points/answercount);
	var url = restBaseUrl + "/getadminold";
//	var message = JSON.stringify("{'action':'decrementScore', 'content':" + playscore + ", 'name': " + name + "}");
	var message = {'action':'decrementScoreCmd', 'content': "" + mypoints, 'name': ""+ name};
	console.log('losshandler sending: ' + JSON.stringify(message));
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});
	logPlayerAnswer(0);
}



function showTimer() {
	$("#timerdiv").show();
	$("#app-timer").show();
}

function hideTimer() {
	$("#timerdiv").hide();
	$("#app-timer").hide();
}

function startCountdown() {
	console.log("startCountdown");
	var time = $("#playTime").val();
	var url = restBaseUrl + "/getadminold";
	var message = {'action':'startCountdownCmd', 'content':""+time};

	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
//	    console.log('Response data: ' + JSON.parse(data));
	});	
	showTimer();
	startTimer(time);
	logPlayerAnswer(0);
}

function stopCountdown() {
	console.log("startCountdown");
	var url = restBaseUrl + "/getadminold";
	var message = {'action':'stopCountdownCmd'};

	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
//	    console.log('Response data: ' + JSON.parse(data));
	});	
	onTimesUp();
	logPlayerAnswer(0);
}


function resetCountdown() {
	console.log("resetCountdown");
	var url = restBaseUrl + "/getadminold";
	var message = {'action':'resetCountdownCmd'};

	$.get(url, message, function (data, textStatus, jqXHR) {
		console.log('Response: ' + textStatus);
//		console.log('Response data: ' + JSON.parse(data));
	});	
	resetTimer();
	answercount = 0;
	logPlayerAnswer(0);
}


function hideCountdown() {
	console.log("hideCountdown");
	var url = restBaseUrl + "/getadminold";
	var message = {'action':'hideCountdownCmd'};

	$.get(url, message, function (data, textStatus, jqXHR) {
		console.log('Response: ' + textStatus);
//		console.log('Response data: ' + JSON.parse(data));
	});	
	hideTimer();
	logPlayerAnswer(0);
}

function showHint() {
	
	var activePlayer = $("#activePlayer").val();
	if (empty(activePlayer)) {
		console.log("Cannot show hint - there is no active player");
		alert("Cannot show hint - there is no active player");
		return;
	}
	console.log("Show hint to active player " + activePlayer);
	var hintUrl = $("#hintUrl").val();
	var title = $("#titleName").val();
	var points = $("#playPoints").val();
	var otherHinUrl = $("#otherUrl").val();
	if (empty(hintUrl)) {
		console.log("Cannot show hint - please provide URL");
		alert("Cannot show hint - please provide URL");
		return;
	}
	var url = restBaseUrl + "/getadminold";
	var message = {'action':'showHintCmd', 'hintUrl':hintUrl, 'content': "{\"title\":\"" + title + 
			"\",\"points\":\"" + points + 
			"\",\"hintUrl\":\"" + otherHinUrl + "\"}"};
	console.log('showHint sending: ' + message);
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
//	    console.log('Response data: ' + JSON.parse(data));
	});
}

function hideHint() {
	console.log("Hide hint from players");
	var url = restBaseUrl + "/getadminold";
	var message = {'action':'hideHintCmd'};
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
//	    console.log('Response data: ' + JSON.parse(data));
	});
}

function empty(str)
{
    if (typeof str === 'undefined' || !str || str.length === 0 || str === "" || !/[^\s]/.test(str) || /^\s*$/.test(str) || str.replace(/\s/g,"") === "")
    {
        return true;
    }
    else
    {
        return false;
    }
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); disconnectPlayer();});
    $( "#send" ).click(function() { sendName(); });
    $( "#activatePlayer" ).click(function() { activatePlayer(); });
    $( "#deActivatePlayers" ).click(function() { deActivatePlayers(); });
    $( "#showHint" ).click(function() { showHint(); });
    $( "#hideHint" ).click(function() { hideHint(); });
    $( "#startCountdownTimer" ).click(function() { startCountdown(); });
    $( "#stopCountdownTimer" ).click(function() { stopCountdown(); });
    $( "#resetCountdownTimer" ).click(function() { resetCountdown(); });
    $( "#hideCountdownTimer" ).click(function() { hideCountdown(); });
    $( "#playerSelectList" ).click(function() { playerSelectList(); });
    $( "#getPlayers" ).click(function() { getPlayerList(); });
    $( "#getCategories" ).click(function() { getCategories(); });
    $( "#activateCategory" ).click(function() { activateCategory(); });
    $( "#categorySelectList" ).click(function() { categorySelectList(); });
});