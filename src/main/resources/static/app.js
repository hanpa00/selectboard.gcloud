/**
 * 
 */
var serverHost = "";//"g-engine-select-20201229.appspot.com";
var serverPort = "";//"443";
var apiKey = ""; //"AIzaSyBZR4KJn8qhpGMsjUFM6IJhuFdrfxkm1cA";
var restBaseUrl = ""; //"https://" + serverHost;
var stompClient = null;
var playerStompClient = null;
var adminSStompClient = null;
var hintShown = false;
var playerActive = false;
var gridArray = null;
var categoryArray = null;
var playerList = [];
var categoryMap;
var DEFAULT_BANNER_LOGO = "Code of Duty Got Talent!";
var activePlayer = null;
var answerPosition = 0;
var categoryChosen = false;
var countTimer;

$(document).ready(function() {
	//DOM manipulation code
	serverHost = $("#server-host p").html();
	serverPort = $("#server-port p").html();
	serverSSL = $("#server-ssl p").html();
	if ((typeof serverPort !== 'undefined') && (serverPort != '')) {
		serverHost = serverHost + ':' + serverPort;
	}
	apiKey = '?key=' + $("#apikey p").html();//'?key=AIzaSyBZR4KJn8qhpGMsjUFM6IJhuFdrfxkm1cA'; 
	if ((typeof serverSSL !== 'undefined') && (serverSSL == 'true')) { 
		restBaseUrl = "https://" + serverHost;
	} else {
		restBaseUrl = "http://" + serverHost;
	}
	console.log('REST API URL: ' + restBaseUrl);
	hideHint();
	createTimer();
	hideTimer();
	categoryMap = new Map();
	populateGridArray();
});

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	if (connected == false) {
		$("#send").prop("disabled", false);
	}
	$("#disconnect").prop("disabled", !connected);
	if (connected) {
		$("#conversation").show();
	}
	else {
		$("#conversation").hide();
	}
	$("#greetings").html("");
}
/*
function connect() {
	var socket = new SockJS('/gs-guide-websocket');
	stompClient = Stomp.over(socket);
	console.log("Creating socket /gs-guide-websocket");
	updateBanner(DEFAULT_BANNER_LOGO, 'brand-glow');
	stompClient.connect({}, function (frame) {
		setConnected(true);
		console.log('Connected: ' + frame);
		stompClient.subscribe('/topic/greetings', function (greeting) {
//			console.log("In /topic/greetings handler");
			console.log("Got from /topic/greetings: " + JSON.parse(greeting.body));
//			showGreeting(JSON.parse(greeting.body).content);
//			showPlayerPosting(JSON.parse(greeting.body).content);
			processMessage(JSON.parse(greeting.body));
		});
	});
}
*/

function connect() {
    var url = restBaseUrl + "/greeting" + apiKey;
	var message = {'name':getCurrentUser()};
	updateBanner(DEFAULT_BANNER_LOGO, 'brand-glow');
	$.get(url, message, function (data, textStatus, jqXHR) {
	    if (typeof data != 'undefined') {
	    	//processMessage(JSON.parse(data))
	    	console.log(data);			
		}	
	});		
}

function updateBanner(text, newClass) {
	$("#banner-text").html(text);
	if (typeof newClass !== 'undefined') {
		$("#banner-text").removeClass().addClass(newClass);
	}
}

function updateBannerCounter(text, newClass, countTime) {	
	$("#banner-text").html(text);
	if (typeof newClass !== 'undefined') {
		$("#banner-text").removeClass().addClass(newClass);
	}
	countTimer = getCountdown(countTime, "#banner-text");
	return countTimer;
}

function getCountdown(startSec, htmlElement) {	
	var count = startSec;
	var msg = $(htmlElement).html();
	var padSize = (startSec + "").length;
	// Update the count down every 1 second
	var x = setInterval(function() {
	  count--;
	  if (count <= 0) {
		  clearInterval(x);
		  console.log('getCountdown stopped counter');
	  } else {
		  if (typeof htmlElement !== 'undefined') {			 
			  $(htmlElement).html(msg + " :" + (count + "").padStart(padSize, "0"));
		  }
	  }
	}, 1000, htmlElement);
	return x;
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
	var url = restBaseUrl + "/getadmin" + apiKey;
	var message = {'name':getCurrentUser(), 'action':'removePlayerAction'};
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});
//	setConnected(false);
	console.log("Disconnected");
}

function getCurrentUser() {
	return $("#name").val();
}

function sendName() {
	//console.log("Sending to /app/hello" + JSON.stringify({'name': getCurrentUser()}));
	//stompClient.send("/app/hello", {}, JSON.stringify({'name': getCurrentUser()}));
	var registrationCountdownValue = 0;
	var url = restBaseUrl + "/getadmin" + apiKey;
	var message = {'name':getCurrentUser(), 'action':'addPlayerAction'};
	$.get(url, message, function (data, textStatus, jqXHR) {
//	    console.log('Response: ' + textStatus);
//	    console.log('Response data: ' + JSON.stringify(data));
//	    console.log('Response data.hintUrl: ' + data.hintUrl);
	    if ((typeof data != 'undefined') && (typeof data.waitTime != 'undefined')) {
			registrationCountdownValue = data.waitTime;
			updateBannerCounter("Hello " + getCurrentUser() + ", Waiting For Others To Join", "brand-glow", registrationCountdownValue);
		}	
	});	
	updateBanner("Hello " + getCurrentUser() + ", Waiting For Others To Join", "brand-glow");
	$("#send").prop("disabled", true);
}

function answerButton() {
//	console.log("Sending to /app/answer" + JSON.stringify({'name': getCurrentUser()}));
//	stompClient.send("/app/answer", {}, JSON.stringify({'name': getCurrentUser()}));
	console.log("Sending for answerTurn: " + JSON.stringify({'name': getCurrentUser()}));
	var url = restBaseUrl + "/getadmin" + apiKey;
	var message = {'name':getCurrentUser(), 'action':'answerTurnAction'};
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});
}

function showGreeting(message) {
	$("#greetings").append("<tr><td>" + message + "</td></tr>");

}

function showPlayerPosting(message) {
	$("#playerPosting").html("<strong>" + message + "</strong>");
}

function hintButton() {
	console.log('Callback: hintButton clicked by player ' + getCurrentUser());
	if (hintShown==false) {
		showHint();
	} else {
		hideHint();
	}

}

function formatTimeLeft(time) {
	// The largest round integer less than or equal to the result of time divided being by 60.
	const minutes = Math.floor(time / 60);

	// Seconds are the remainder of the time divided by 60 (modulus operator)
	let seconds = time % 60;

	// If the value of seconds is less than 10, then display seconds with a leading zero
	if (seconds < 10) {
		seconds = `0${seconds}`;
	}

	// The output in MM:SS format
	return `${minutes}:${seconds}`;
}


function populateGridArray() {

	console.log("populateGridArray");
	var url = restBaseUrl + "/getcategories" + apiKey;
	rows = 5;
	columns = 4;
	var width = 932;
	var height = 510;
	var cellWidthPct = Math.round(100/columns) - 1;
	var cellHeightPct = Math.round(100/rows) - 1;
	var col = 0;
	var currCat = 0;
	gridArray = new Array(rows);
	categoryArray = new Array(rows);
	for (var i=0; i<rows; i++) {
		gridArray[i] = new Array(columns);
		categoryArray[i] = new Array(columns);
	}

	$.get(url, function (data, textStatus, jqXHR) {
		console.log('getCategories Response: ' + textStatus);	    
		if ((typeof data !== 'undefined') && (textStatus == 'success')) {

			console.log('getCategories data: ' + data);	    	
			categories = JSON.parse(data);

			categories.forEach((entry) => {
				var keyValue = Object.keys(JSON.parse(entry));
				var key = keyValue[0];
				var kv = JSON.parse(entry);
				var value = kv[key];
				if (value != currCat) {
					currCat = value;
					col = 0;
					console.log("Reset col to 0");
				}				
				console.log("key: " + key + " - col: " + col + " - value: " + value);
				gridArray[value/100 - 1][col] = entry;
				categoryArray[value/100 - 1][col] = key;
				col++;
			});

			var grid_table = "<div class=\"divTable blueTable\"><div class=\"divTableBody\">";
			for (var row = 0; row < rows; row++) {
				grid_table = grid_table + "<div class=\"divTableRow\">";
				for (var col = 0; col < columns; col++) {
		    		if (typeof categoryMap.get(categoryArray[row][col]) === 'undefined') {
		    			categoryMap.set(categoryArray[row][col], Number.MAX_SAFE_INTEGER);
		    			console.log('getCategories - category remaining was undefined - set to max int');
		    		}
		    		console.log('getCategories - Category ' + categoryArray[row][col] + ' entries remaining: ' + categoryMap.get(categoryArray[row][col]));;
//		    		var cellData = "<div id=\"grid-" + row + "-" + col + "\" onclick='getGridDetails(" + row + ", " + col + ");' ";
		    		var cellData = "<div id=\"grid-" + row + "-" + col + "\" onclick='showGridHint(" + row + ", " + col + ");' ";
					grid_table = grid_table + cellData + " style=\"max-width:" + cellWidthPct + "%;width:" + cellWidthPct + "%\"><div>" + categoryArray[row][col] + "</div><div>" + ((row+1)*100) + "</div></div>";
				};
				grid_table = grid_table + "</div>";
			};
			grid_table = grid_table + "</div></div>";
//			console.log("Grid: " + grid_table);
			$("#container-box").append(grid_table);
			$("#container-box").show();

			for (var row = 0; row < rows; row++) {
				for (var col = 0; col < columns; col++) {
					updateGridCell(row, col);
				}
			}
		} 
	});
}

function getGridDetails(x, y) {
	if (playerActive == true) {
		var gridDetail = gridArray[x][y];
		var gridCategory = categoryArray[x][y];
		console.log('Getting grid details for Category:' + gridDetail + ' row:' + x + ' col:' + y);
		var ans = confirm("Do you want to pick category '" + gridCategory + "'?");
		if (ans == true) {
			console.log('Getting grid details for: ' + gridDetail);
			var url = restBaseUrl + "/getcategoryentry" + apiKey;
			var message = {'category':gridDetail};
			$.get(url, message, function (data, textStatus, jqXHR) {
				console.log('Response: ' + textStatus);
				if ((typeof data !== 'undefined') && (textStatus == 'success')) {
					var categorydata = JSON.stringify(data);
					console.log('activateCategory data: ' + categorydata);
					categoryMap.set(gridCategory, data.remaining);
					if (categoryMap.get(gridCategory) == 0) {
						console.log('activateCategory - Last entry for category: ' + gridCategory);
					}
					var message = {'action':'showHint', 'hintUrl':data.embeddedUrl, 'content': "{\"title\":\"" + data.title + 
							"\",\"points\":\"" + data.points + 
							"\",\"hintUrl\":\"" + data.url + "\"}"};
					showHint(message);
				}
			});	
		}
	}

}



function showGridHint(x, y) {
	if ((playerActive == true) && (categoryChosen == false)) {
		var gridDetail = gridArray[x][y];
		var gridCategory = categoryArray[x][y];
		console.log('Getting grid details for Category:' + gridDetail + ' row:' + x + ' col:' + y);
		categoryChosen = confirm("Do you want to pick category '" + gridCategory + "'?");
		if (categoryChosen == true) {
			console.log('Getting grid details for: ' + gridDetail);
			var url = restBaseUrl + "/getadmin" + apiKey;
			var message = {'action': 'showGridHintAction', 'content': gridDetail, 'name':getCurrentUser()};
			console.log('showHint sending: ' + message);
			$.get(url, message, function (data, textStatus, jqXHR) {
			    console.log('Response: ' + textStatus);
			});			

			if ((typeof data !== 'undefined') && (textStatus == 'success')) {
				var categorydata = JSON.stringify(data);
				console.log('activateCategory data: ' + categorydata);
				categoryMap.set(gridCategory, data.remaining);
				if (categoryMap.get(gridCategory) == 0) {
					console.log('activateCategory - Last entry for category: ' + gridCategory);
				}
//				var message = {'action':'showHint', 'hintUrl':data.embeddedUrl, 'content': "{\"title\":\"" + data.title + 
//						"\",\"points\":\"" + data.points + 
//						"\",\"hintUrl\":\"" + data.url + "\"}"};
				var message = {'action':'showHint', 'hintUrl':data.embeddedUrl, 'content': data.content};
				showHint(message);
			}
		}
	}
}


function updateGridCell(x, y) {

	var gridDetail = gridArray[x][y];
	var gridCategory = categoryArray[x][y];
	console.log('Setting grid remain value for Category:' + gridDetail + ' row:' + x + ' col:' + y);

	var url = restBaseUrl + "/getcategoryremain" + apiKey;
	var message = {'category':gridDetail};
	$.get(url, message, function (data, textStatus, jqXHR) {
		console.log('Response: ' + textStatus);
		var cellGrid = $("#grid-" + x + "-" + y); 
		var addedClasses = "divTableCell dark-grid-" + (x*100+100);
		if ((typeof data !== 'undefined') && (textStatus == 'success')) {
			var remaining = JSON.parse(data).remaining;
			console.log('getcategoryremain data: ' + remaining);
			categoryMap.set(gridCategory, remaining);			
		} else {
			console.log('getcategoryremain data is null, setting to 0');
			categoryMap.set(gridCategory, 0);			
		}
		
		if (parseInt(categoryMap.get(gridCategory)) > 0) {
			cellGrid.addClass("divTableCell grid-" + (x*100+100));
		} else {
			console.log('getcategoryremain is 0, disabling gridcategory ' + gridCategory);
			cellGrid.addClass("divTableCell dark-grid-" + (x*100+100));
			cellGrid.prop('onclick', null).off('click');
		}
	});	
}



//function that clears the grid
function clearGrid(){
//	$(".grid").remove();
};  

//function that prompts the user to select the number of boxes in a new grid
//the function then also creates that new grid
function refreshGrid(){
	var z = prompt("How many boxes per side?");
	clearGrid();
	createGrid(z);
};


function showHintAnswer(jsonObject) {
	var htmlStr = "<p>Hint is shown here</p>";
	var title = "Title";
	var points = "0";
	var hintUrl = "URL";
	var winner = "None";
	var winPoints = "0";

	console.log("Show Answer");
	
	if (typeof jsonObject != 'undefined') {
		htmlStr = "<iframe width=\"420\" height=\"315\" src=\"" + jsonObject.hintUrl +
		"\" frameborder=\"0\" allowfullscreen></iframe>";
		console.log("Hint URL: " + jsonObject.hintUrl);
		var contentStr = JSON.stringify(jsonObject.content);			
		var content = JSON.parse(jsonObject.content);
		console.log('showHint content: ' + contentStr);	
		title = "Title: " + content.title;
		points = "Points: " + content.points;
		hintUrl = content.hintUrl;
		winner = content.winPlayer;
//		console.log('Winner: ' + winner);
		winPoints = content.winPoints;
//		console.log('Winner points: ' + winPoints);
	}
	var bannerTimer = updateBannerCounter("Winner is " + winner + 
			" with " + winPoints + " points ", "brand-slidein", jsonObject.waitTime);

	$("#youtubehint").html(htmlStr);
	$("#title").html(title);
	$("#title").show();
	$("#points").html(points);
	$("#points").show();	
	$("#hintUrl").html(hintUrl);
	$("#hintUrl").attr("href", hintUrl);
	$("#hintUrl").show();
	$("#bg_mask1").show();
	$("#youtubehint").show();
	$("#hintButton").text("Close");

	var hideTimer = setTimeout(function() {
		console.log("Timer is closing hint answer panel");
		clearInterval(bannerTimer);
		$("#title").hide();
		$("#points").hide();	
		$("#bg_mask1").hide();
		$("#youtubehint").hide();
		updateBanner(DEFAULT_BANNER_LOGO, "brand-glow");
	}, (jsonObject.waitTime*1000));
	$( "#hintButton" ).click(function() { 
		console.log("User is closing hint answer panel");
		clearInterval(bannerTimer);
		clearTimeout(hideTimer);
		$("#title").hide();
		$("#points").hide();	
		$("#bg_mask1").hide();
		$("#youtubehint").hide();
		updateBanner(DEFAULT_BANNER_LOGO, "brand-glow");
	});				
}

function showHint(jsonObject) {
	var htmlStr = "<p>Hint is shown here</p>";
	var title = "Title";
	var points = "0";
	var hintUrl = "URL";
	
	$( "#hintButton" ).click(function() { hintButton(); });
	if ((playerActive == true) && (hintShown==false)) {
		console.log("Show hint");		
		if (typeof jsonObject != 'undefined') {
			htmlStr = "<iframe width=\"420\" height=\"315\" src=\"" + jsonObject.hintUrl +
			"\" frameborder=\"0\" allowfullscreen></iframe>";
			console.log("Hint URL: " + jsonObject.hintUrl);
//			var contentStr = JSON.stringify(jsonObject.content);			
			var content = JSON.parse(jsonObject.content);
//			console.log('showHint content: ' + content);	
			title = "Title: " + content.title;
			points = "Points: " + content.points;
			hintUrl = content.hintUrl;
		}
		updateBannerCounter(title + ". Below is A Hint", "brand-slidein", jsonObject.waitTime);
		hintShown=true;
		$("#youtubehint").html(htmlStr);
		$("#title").html(title);
		$("#title").show();
		$("#points").html(points);
		$("#points").show();	
		$("#hintUrl").html(hintUrl);
		$("#hintUrl").attr("href", hintUrl);
		$("#hintUrl").show();
		$("#bg_mask1").show();
		$("#youtubehint").show();
		$("#hintButton").text("Hide Hint");
	}
}



function hideHint() {
	console.log("Hide hint");
	hintShown=false;
	if (typeof countTimer != 'undefined') {
		if (playerActive == true) {
			console.log('hideHint is stopping banner counter');
			clearInterval(countTimer);
		}
	}
	$( "#hintButton" ).click(function() { hintButton(); });
	$("#bg_mask1").hide();
	$("#youtubehint").hide();
	$("#title").hide();
	$("#points").hide();
	$("#hintUrl").hide();
	$("#youtubehint").html("<p>Hint is shown here</p>");
	$("#hintButton").text("Show Hint");
	var url = restBaseUrl + "/getadmin" + apiKey;
	var message = {'action': 'hideGridHintAction', 'name':getCurrentUser()};
	console.log('hideHint sending: ' + message);
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});			
}

function showTimer() {	
	$("#app-timer-overlay").show();
	if (playerActive == true) {
		$("#answerButton-panel").hide();
	} else {
		$("#answerButton-panel").show();
	}
	$("#bg_mask2").show();
}

function hideTimer() {
	$("#bg_mask2").hide();
	$("#app-timer-overlay").hide();
}


function showAnswerPanel(waitTime) {
	if (playerActive != true) {
		updateBanner("Please submit your answer!", "brand-glow", waitTime);
		$("#bg_mask3").show();
		$("#answer-overlay").show();
	} 
}

function hideAnswerPanel() {
	if (playerActive != true) {
		$("#bg_mask3").hide();
		$("#answer-overlay").hide();
	} 
}


function showResultPanel(jsonObject) {
	var winnerName = "None";
	var winnerScore = Number.NEGATIVE_INFINITY;
	var tmp = 0;
	if (playerActive != true) {
		playerList.forEach((value) => {
			console.log("Searching winner = " + value.name);
			if (winnerScore < value.score) {
				console.log("Found winner = " + value.name + " : " + JSON.stringify(value));
				winnerScore = value.score;
				winnerName = value.name;
			}
		});
		updateBanner("Thanks For Playing!", "brand-glow");
		$("#win-player").html("<h1>Winner: " + winnerName + "</h1>");
		$("#win-points").html("<h1>Score: " + winnerScore + "</h1>");
		$("#bg_mask4").show();
		$("#results-overlay").show();
		var applauseAudio = $("#applause")[0];
		applauseAudio.play();
//		setTimeout(function() {
//			console.log("Timer is closing hint answer panel");
//			$("#bg_mask4").hide();
//			$("#results-overlay").hide();
//			updateBanner(DEFAULT_BANNER_LOGO, "brand-glow");
//		}, (jsonObject.waitTime*1000));
	} 
}


function sendAnswer() {
	console.log("Sending to /app/hello" + JSON.stringify({'name': getCurrentUser()}));
	var url = restBaseUrl + "/getadmin" + apiKey;
	var message = {'name':getCurrentUser(), 'action':'submitAnswerAction', 'content':$("#answer").val()};
	$.get(url, message, function (data, textStatus, jqXHR) {
	    console.log('Response: ' + textStatus);
	});
	clearInterval(countTimer);
//	hideAnswerPanel();
}


function updateScore(name) {
	console.log('Updating score for ' + name);
	var message = {'name':name};
	var url = restBaseUrl + "/getplayer" + apiKey;
	$.get(url, message, function (data, textStatus, jqXHR) {
		console.log('getplayer Response: ' + textStatus);
//		console.log('getPlayerList Response data: ' + JSON.parse(data));
		if ((typeof data !== 'undefined') && (textStatus == 'success')) {
//			var player = JSON.parse(data);
			console.log('Player: ' + data);
			$("#score").html("You Score: " + data.score);
		}
	});
}


function showPlayerList(data) {
	var players = Object.keys(JSON.parse(data));
	console.log('Players keys: ' + players);
	playerList = [];
	var name = "";
	$("#playerlist").html("");
	Object.entries(JSON.parse(data)).forEach(([key, value]) => { 
		console.log("Player = " + key + " : " + JSON.stringify(value));
		playerList.push(JSON.parse(JSON.stringify(value)));
		name = value.name;
		if (activePlayer == name) {
			name = "*" + name;
		}
		$("#playerlist").append("<tr><td>" + value.name + "</td><td>" + value.score + "</td></tr>");
		if (value.name == getCurrentUser()) {
			$("#score").html("Your Score: " + value.score);
		}
	});
	console.log('Number of Players entries: ' + playerList.length);
}

function refreshPlayerList() {	
	var name = "";
	$("#playerlist").html("");
	playerList.forEach((value) => {
		console.log("Player = " + value.name + " : " + JSON.stringify(value));
		name = value.name;
		if (activePlayer == name) {
			name = "*" + name;
		}
		$("#playerlist").append("<tr><td>" + name + "</td><td>" + value.score + "</td></tr>");
	});
	console.log('Number of Players entries: ' + playerList.length);
}

function processMessage(jsonObject) {

	console.log('processMessage got message from user '  + jsonObject.userName);
//	if (jsonObject.userName!=null && jsonObject.userName!==getCurrentUser()) {
//	console.log('Message is not for current user ' + getCurrentUser());
//	return;
//	}
	console.log("Message command: " + jsonObject.action);

	switch(jsonObject.action) {
	case "activatePlayerCmd":
		console.log("Activating player " + jsonObject.userName);
		answerPosition = 0;
		activePlayer = jsonObject.userName;
		if (typeof countTimer != 'undefined') {
			console.log('activatePlayerCmd is stopping banner counter');
			clearInterval(countTimer);
		}
		refreshPlayerList();
		if (jsonObject.userName!=getCurrentUser()) {
			console.log('Message is not for current user ' + getCurrentUser());
			playerActive = false;
			updateBanner(jsonObject.userName + " Is Up Next!", "brand-slidein");
			return;
		}
		updateBanner(getCurrentUser() + ", Pick A Category From The Board", "brand-slidein");		
		console.log("Activating player " + jsonObject.userName);
		playerActive = true;
		var applauseAudio = $("#applause")[0];
		applauseAudio.play();
		$("#answerButton-subpanel").removeAttr("style");
		break;
	case "deActivatePlayerCmd":
		console.log("Deactivating players");
		playerActive = false;
		activePlayer = null;
		categoryChosen = false;
		hideAnswerPanel();
		refreshPlayerList();
		updateBanner(DEFAULT_BANNER_LOGO, "brand-glow");
		break;
	case "showHintCmd":
		showHint(jsonObject);
		break;
	case "hideHintCmd":
		hideHint();
		break;
	case "showHintAnswerCmd":
		showHintAnswer(jsonObject);
		break;
	case "startCountdownCmd":
		var time = 0;
		if (jsonObject.content !=null && jsonObject.content > 0) {
			time = jsonObject.waitTime;
		}
		if (playerActive == false) {
			updateBanner("Can You Guess The Tune?", "brand-glow");
		} else {
			updateBanner("Time To Show Your Humming Talent!", "brand-glow");
		}		
		startTimer(time);
		showTimer();
		break;
	case "stopCountdownCmd":
		onTimesUp();
		break;
	case "resetCountdownCmd":
		hideTimer();
		resetTimer();	
		break;
	case "answerSubmissionCmd":		
		console.log("Answer submission position: " + answerPosition);
		if ((answerPosition > 0) && (answerPosition <= 2)) {
			showAnswerPanel(jsonObject.waitTime);
		}		
		break;
	case "hideCountdownCmd":
		hideTimer();
		break;
	case "incrementScoreCmd":
		if (jsonObject.userName===getCurrentUser()) {
			updateScore(jsonObject.userName);
		}
//		else if (playerActive == true) {
//		updateScore(getCurrentUser());
//		}
		break;
	case "decrementScoreCmd":
		if (jsonObject.userName===getCurrentUser()) {
			updateScore(jsonObject.userName);
		}
		break;
	case "updateScoreCmd":
		if (jsonObject.userName===getCurrentUser()) {
			updateScore(jsonObject.userName);
		}
		break;
	case "answerPositionCmd":
		if (jsonObject.userName!=null && jsonObject.userName==getCurrentUser()) {
			answerPosition = jsonObject.content;
			console.log("User " + getCurrentUser() + " has answer position: " + answerPosition);
			if (answerPosition == 1) {
				console.log("User is the first to answer");
				var answerFirstAudio = $("#answerfirst")[0];
				answerFirstAudio.play();
				$("#answerButton-subpanel").css("background-color", "#3e8e41");
				updateBanner(getCurrentUser() + ", Your Answer Will Be First Pick!", "brand-slidein");
			} else if (answerPosition == 2) {
				console.log("User is the second to answer");
				var answerFirstAudio = $("#answerfirst")[0];
				answerFirstAudio.play();
				$("#answerButton-subpanel").css("background-color", "#3e8e41");
				updateBanner(getCurrentUser() + ", Your Answer Will Be Second Pick!", "brand-slidein");
			}
		}
		break;
	case 'gridRefreshCmd':
		console.log("Refresh grid details of: " + jsonObject.content);
		for (var x=0; x<gridArray.length; x++) {
			for (var y=0; y<gridArray[0].length; y++) {
				console.log("Checking against gridArray[" + x + "][" + y + "] = " + gridArray[x][y]);
				if (gridArray[x][y] == jsonObject.content) {
					console.log("Refresh grid x:" + x + " - y:" + y);
					updateGridCell(x, y);
					break;
				}
			}
		}
		break;
	case 'playerListCmd':
		console.log("Setting player list: " + jsonObject.content);
		showPlayerList(jsonObject.content);		
		break;
	case 'refreshListCmd':
		console.log("Refreshing current player list");
		refreshPlayerList();		
		break;
	case 'restartGameCmd':
		console.log("Restarting game...");
		showResultPanel(jsonObject);
		disconnect();
		setTimeout(function() {
			location.reload();
		}, (jsonObject.waitTime*1000));				
		break;
	case 'cannotPlayGameCmd':
		updateBanner("We Need At Least 2 Players!", "brand-slidein");
		console.log("Restarting game...");		
		disconnect();
		setTimeout(function() {
			location.reload();
		}, (jsonObject.waitTime*1000));				
		break;
	default:
		console.log("Command not found");

	}


}


$(function () {
	$("form").on('submit', function (e) {
		e.preventDefault();
	});
	$( "#connect" ).click(function() { connect(); });
	$( "#disconnect" ).click(function() { disconnect(); });
	$( "#send" ).click(function() { sendName(); });
//	$( "#hintButton" ).click(function() { hintButton(); });
	$( "#answerButton" ).click(function() { answerButton(); });
	$( "#sendAnswer" ).click(function() { sendAnswer(); });

});
