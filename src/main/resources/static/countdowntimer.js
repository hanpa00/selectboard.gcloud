
const FULL_DASH_ARRAY = 283;
const WARNING_THRESHOLD = 20;
const ALERT_THRESHOLD = 10;

const COLOR_CODES = {
  info: {
    color: "green"
  },
  warning: {
    color: "orange",
    threshold: WARNING_THRESHOLD
  },
  alert: {
    color: "red",
    threshold: ALERT_THRESHOLD
  }
};

var TIME_LIMIT = 60;
var timePassed = 0;
var timeLeft = TIME_LIMIT;
var timerInterval = null;
var remainingPathColor = COLOR_CODES.info.color;
var callback = null;

//document.getElementById("app-timer").innerHTML = `

function registerCallback(func, arg) {
	if (typeof func == 'function') {
		callback = func(arg);
	}
}


function createTimer(func) {
	console.log("Creating timer");
	timePassed = 0;
	timeLeft = TIME_LIMIT;
	timerInterval = null;
	remainingPathColor = COLOR_CODES.info.color
	
	if (typeof func === 'function') {
		callback = func;
	}
	
$("#app-timer").html(
		"<div class=\"base-timer\">" +
		"  <svg class=\"base-timer__svg\" viewBox=\"0 0 100 100\" xmlns=\"http://www.w3.org/2000/svg\">" +
		"    <g class=\"base-timer__circle\">" +
		"      <circle class=\"base-timer__path-elapsed\" cx=\"50\" cy=\"50\" r=\"45\"></circle>" +
		"      <path" +
		"        id=\"base-timer-path-remaining\"" +
		"        stroke-dasharray=\"283\"" +
		"        class=\"base-timer__path-remaining ${remainingPathColor}\"" +
		"        d=\"" +
		"          M 50, 50" +
		"          m -45, 0" +
		"          a 45,45 0 1,0 90,0" +
		"          a 45,45 0 1,0 -90,0" +
		"        \"" +
		"      ></path>" +
		"    </g>" +
		"  </svg>" +
		"  <span id=\"base-timer-label\" class=\"base-timer__label\">${formatTime(timeLeft)}</span>" +
		"</div>");
}

function destroyTimer() {
	console.log("Destroying timer");
	$("app-timer").html("");
}
//startTimer();

function onTimesUp() {
  clearInterval(timerInterval);
}

function resetTimer() {
	clearInterval(timerInterval);
	destroyTimer();
	createTimer();
}

function startTimer(time) {
	if ((typeof time != 'undefined') && (time > 0)) {
		TIME_LIMIT = time;
	}
	console.log("Start timer");
	timerInterval = setInterval(() => {
		timePassed = timePassed += 1;
		timeLeft = TIME_LIMIT - timePassed;
		document.getElementById("base-timer-label").innerHTML = formatTime(
				timeLeft
		);
		setCircleDasharray();
		setRemainingPathColor(timeLeft);
		if (typeof callback == 'function') {
			callback(timeLeft);
		}

		if (timeLeft === 0) {
			onTimesUp();
		}
	}, 1000);
}

function formatTime(time) {
  const minutes = Math.floor(time / 60);
  let seconds = time % 60;

  if (seconds < 10) {
    seconds = `0${seconds}`;
  }

  return `${minutes}:${seconds}`;
}

function setRemainingPathColor(timeLeft) {
  const { alert, warning, info } = COLOR_CODES;
  if (timeLeft <= alert.threshold) {
    document.getElementById("base-timer-path-remaining").classList.remove(warning.color);
    document.getElementById("base-timer-path-remaining").classList.add(alert.color);
  } else if (timeLeft <= warning.threshold) {
    document.getElementById("base-timer-path-remaining").classList.remove(info.color);
    document.getElementById("base-timer-path-remaining").classList.add(warning.color);
  } else {
	  document.getElementById("base-timer-path-remaining").classList.add(info.color);
  }
}

function calculateTimeFraction() {
  const rawTimeFraction = timeLeft / TIME_LIMIT;
  return rawTimeFraction - (1 / TIME_LIMIT) * (1 - rawTimeFraction);
}

function setCircleDasharray() {
  const circleDasharray = `${(calculateTimeFraction() * FULL_DASH_ARRAY).toFixed(0)} 283`;
  document.getElementById("base-timer-path-remaining").setAttribute("stroke-dasharray", circleDasharray);
}