package com.phan.game.selectboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameManager {

	public enum State {
		SIGN_UP, GAME_START, ACTIVE_PLAYER, TURN_START, TURN_END, UPDATE_SCORE, INACTIVE_PLAYER, SHOW_ANSWER, GAME_END, GAME_RESET;
	};

	public enum StateAction {
		addPlayerAction, removePlayerAction, activatePlayerAction, needPlayersAction, deActivatePlayerAction, showGridHintAction, hideGridHintAction, 
		startCountdownAction, stopCountdownAction, resetCountdownAction, hideCountdownAction, gridRefreshAction, answerTurnAction, 
		submitAnswerAction, showAnswerAction, hideAnswerAction, startGameAction, endGameAction, resetGameAction, updateScoreAction, newGameAction
	};

	static class StateNode {
		private State currentState;
		private HashMap<StateAction, StateNode> nextStateNode = new HashMap<>();

		StateNode(State state) {
			currentState = state;
		}

		public State getState() {
			return currentState;
		}

		public void addNeighbour(StateAction key, StateNode value) {
			nextStateNode.put(key, value);
		}

		public StateNode getNext(StateAction key) {
			return nextStateNode.get(key);
		}

		public HashMap<StateAction, StateNode> getNeighbours() {
			return nextStateNode;
		}
	}

	final static AtomicLong counter = new AtomicLong();
	//	private static HashMap<String, Integer> playerScore = new HashMap<String, Integer>();
	//	private static HashMap<Integer, Integer> teamScore = new HashMap<Integer, Integer>();
	private static HashMap<String, Integer> playerTeamMap = new HashMap<String, Integer>();
	private static HashMap<String, Player> playerList = new HashMap<String, Player>();
	private static ArrayList<Player> answerList = new ArrayList<Player>();
	final static AtomicLong playerOrder = new AtomicLong();
	private static ArrayList<Player> playerRegistrationOrder = new ArrayList<>();

	private static HashMap<State, StateNode> stateNodes = new HashMap<>();
	private static StateNode root;
	private static StateNode current;
	private static StateNode previous;
	private static Player currentActivePlayer;
	private static Timer hintViewTimer = new Timer(true);
	private static Timer playerSignupTimer = new Timer(true);
	private static Timer answerTimer = new Timer(true);
	private static Timer pressAnswerButtonTimer = new Timer(true);
	private static Timer answerValidateTimer = new Timer(true);
	private static Timer waitTimer = new Timer(true);
	private static Timer showAnswerTimer = new Timer(true);
	private static Timer countDownTimer = new Timer(true);
	private static int SIGNUP_WAIT = CacheData.getIntegerValue("--SIGNUP_WAIT", 30) * 1000;
	private static int HINT_VIEW_WAIT = CacheData.getIntegerValue("--HINT_VIEW_WAIT", 60) * 1000;
	private static int ANSWER_WAIT = CacheData.getIntegerValue("--ANSWER_WAIT", 60) * 1000;
	private static int ANSWER_SUBMIT_WAIT = CacheData.getIntegerValue("--ANSWER_SUBMIT_WAIT", 60) * 1000;
	private static int ANSWER_VALIDATE_WAIT = CacheData.getIntegerValue("--ANSWER_VALIDATE_WAIT", 60) * 1000;
	private static int ANSWER_SHOW_WAIT = CacheData.getIntegerValue("--ANSWER_SHOW_WAIT", 10) * 1000;
	private static int WAIT_TIME = CacheData.getIntegerValue("--ANSWER_SHOW_WAIT", 10) * 1000;
	private static HashMap<String, String> answersReceived = new HashMap<>();
	private static HashMap<String, String> answerButtonPressed = new HashMap<>();
	private static ArrayList<CategoryEntry> hintHistory = new ArrayList<>();
	private static Integer numberTurns = 0;
	private static Integer MAX_ROUNDS = CacheData.getIntegerValue("--MAX_ROUNDS", 2);
	private static Integer COUNTDOWN_60SEC = 60;
	static Integer countDownValue = COUNTDOWN_60SEC;
	private static ArrayList<Player> turnWinner = new ArrayList<>();
	

	public static void buildStateGraph() {
		System.out.println("Building state graph");
		StateNode newNode = new StateNode(State.SIGN_UP);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.GAME_START);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.ACTIVE_PLAYER);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.INACTIVE_PLAYER);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.TURN_START);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.TURN_END);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.TURN_START);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.UPDATE_SCORE);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.SHOW_ANSWER);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.GAME_END);
		stateNodes.put(newNode.getState(), newNode);
		newNode = new StateNode(State.GAME_RESET);
		stateNodes.put(newNode.getState(), newNode);
		root = stateNodes.get(State.SIGN_UP);
		current = root;
		previous = null;
		stateNodes.get(State.SIGN_UP).addNeighbour(StateAction.addPlayerAction, stateNodes.get(State.SIGN_UP));
		stateNodes.get(State.SIGN_UP).addNeighbour(StateAction.removePlayerAction, stateNodes.get(State.SIGN_UP));
		stateNodes.get(State.SIGN_UP).addNeighbour(StateAction.startGameAction, stateNodes.get(State.GAME_START));
		stateNodes.get(State.GAME_START).addNeighbour(StateAction.needPlayersAction, stateNodes.get(State.SIGN_UP));
		stateNodes.get(State.GAME_START).addNeighbour(StateAction.activatePlayerAction, stateNodes.get(State.ACTIVE_PLAYER));
		stateNodes.get(State.ACTIVE_PLAYER).addNeighbour(StateAction.showGridHintAction, stateNodes.get(State.ACTIVE_PLAYER));
		stateNodes.get(State.ACTIVE_PLAYER).addNeighbour(StateAction.hideGridHintAction, stateNodes.get(State.ACTIVE_PLAYER));
//		stateNodes.get(State.ACTIVE_PLAYER).addNeighbour(StateAction.gridRefreshAction, stateNodes.get(State.ACTIVE_PLAYER));
		stateNodes.get(State.ACTIVE_PLAYER).addNeighbour(StateAction.startCountdownAction, stateNodes.get(State.TURN_START));
		stateNodes.get(State.TURN_START).addNeighbour(StateAction.answerTurnAction, stateNodes.get(State.TURN_START));
		stateNodes.get(State.TURN_START).addNeighbour(StateAction.stopCountdownAction, stateNodes.get(State.TURN_END));
		stateNodes.get(State.TURN_END).addNeighbour(StateAction.resetCountdownAction, stateNodes.get(State.TURN_END));
		stateNodes.get(State.TURN_END).addNeighbour(StateAction.submitAnswerAction, stateNodes.get(State.TURN_END));
		stateNodes.get(State.TURN_END).addNeighbour(StateAction.updateScoreAction, stateNodes.get(State.UPDATE_SCORE));
		stateNodes.get(State.UPDATE_SCORE).addNeighbour(StateAction.deActivatePlayerAction,stateNodes.get(State.INACTIVE_PLAYER));
//		stateNodes.get(State.INACTIVE_PLAYER).addNeighbour(StateAction.gridRefreshAction, stateNodes.get(State.INACTIVE_PLAYER));
		stateNodes.get(State.INACTIVE_PLAYER).addNeighbour(StateAction.showAnswerAction, stateNodes.get(State.SHOW_ANSWER));
		stateNodes.get(State.SHOW_ANSWER).addNeighbour(StateAction.hideAnswerAction, stateNodes.get(State.SHOW_ANSWER));
		stateNodes.get(State.SHOW_ANSWER).addNeighbour(StateAction.startGameAction, stateNodes.get(State.GAME_START));
		stateNodes.get(State.SHOW_ANSWER).addNeighbour(StateAction.endGameAction, stateNodes.get(State.GAME_END));
		stateNodes.get(State.GAME_END).addNeighbour(StateAction.resetGameAction, stateNodes.get(State.GAME_RESET));
		stateNodes.get(State.GAME_RESET).addNeighbour(StateAction.addPlayerAction, stateNodes.get(State.SIGN_UP));
	}

	public StateNode getNext(StateAction action) {
		StateNode node = current.getNext(action);
		previous = current;
		current = node;
		return node;
	}

	
	public static void postActionCommand2Client(ArrayList<Greeting> greetings, Integer postWaitTimeSec) {
		if ((greetings == null) || (greetings.size() == 0)) {
			return;
		}
		ArrayList<Greeting> greets = new ArrayList<Greeting>();
		greets.addAll(greetings);
		Runnable threadCmd = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				long id = Thread.currentThread().getId();				
				for (Greeting greet : greets) {
					System.out.println("ThreadID: " + id + " - " + "Spawning thread to send command to client: " + greet.toJsonString());
					GreetingController.postGreetingsTopic(greet);
					try {
						Thread.sleep(100);
					} catch(Exception ex) {}
				}
			}
		};

		if ((postWaitTimeSec != null) && (postWaitTimeSec > 0)) {
			Timer waitTimer = new Timer(true);
			try {
				waitTimer.schedule(new TimerTask() { // timer to cap time for user to press button before submission
					@Override
					public void run() {						
						threadCmd.run();
					}			
				}, postWaitTimeSec * 1000);
			} catch (Exception ex) {							
			}
		} else {
			threadCmd.run();
		}
	}
	
	public static void issueNextInputAction(String name, StateAction action, String content, String hintUrl) {
		Runnable threadCmd = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				long id = Thread.currentThread().getId();
				System.out.println("ThreadID: " + id + " - " + "Spawning new thread command for action: " + action);
				processStateHandler(name, action, content, hintUrl);
			}
		};
		threadCmd.run();
	}
	
	public static Greeting processStateHandler(String name, StateAction action, String content, String hintUrl) {

		long id = Thread.currentThread().getId();
		StateAction inputAction = action;
		Greeting greet = null;
		ArrayList<Greeting> greetings = new ArrayList<Greeting>();
		String clientCommand = "";
		StateAction nextAction = null;
//		String sendNextActionCmd = null;
		Integer waitTime = 0;
		Integer postWaitTime = 0;
		ObjectMapper objectMapper = new ObjectMapper();		
		
		System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() ### Start - current state = " + current.getState() + " - input action = " + action);
		
		switch (current.getState()) {
			case SIGN_UP:								
				String jsonPlayerList = "{}";				
				if (StateAction.startGameAction.equals(inputAction)) {					
					clientCommand = "playerListCmd";
					playerOrder.set(0L);
					playerSignupTimer.cancel();
					playerSignupTimer.purge();
					playerSignupTimer = new Timer(true);
					Player[] tmpArray = new Player[playerList.size()*2];					
					for (String plName: playerList.keySet()) {
						Player pl = playerList.get(plName);						
						tmpArray[pl.getRegistrationOrder().intValue()] = pl;
					}
					playerRegistrationOrder.clear();
					int index = 0;
					for (int i=0; i<tmpArray.length; i++) {
						if (tmpArray[i] == null) {
							continue;
						}
						tmpArray[i].setRegistrationOrder(new Long(index++));
						playerRegistrationOrder.add(tmpArray[i]);
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() Normalizing player registration index order: Player[" + 
								tmpArray[i].getRegistrationOrder() + "] = " + tmpArray[i].getName());
					}					
					nextAction = StateAction.activatePlayerAction;	
					if (playerList.size() < 2) {
						nextAction = StateAction.needPlayersAction;
					}
				}
				else if (StateAction.addPlayerAction.equals(inputAction)) {
					clientCommand = "playerListCmd";										
					if (playerList.size() == 0) {
						countDownValue = SIGNUP_WAIT/1000;
						try {
							System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() Player signup countdown created: " + new Date());
							countDownTimer = new Timer(true);
							countDownTimer.scheduleAtFixedRate(new TimerTask() {
								
								@Override
								public void run() {
									long id = Thread.currentThread().getId();
									System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() Player signup countdown " + countDownValue + " triggered: " + new Date());
									countDownValue--;
									if (countDownValue <= 0) {
										System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() Player reset : countdown = " + countDownValue);
										countDownTimer.cancel();
										countDownTimer.purge();
//										countDownTimer = new Timer(true);
										countDownValue = SIGNUP_WAIT/1000;
									}
								}				
							}, 0, 1000);
						} catch (Exception ex) {						
						}

						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() Player Signup Timer created: " + new Date());
						playerSignupTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								long id = Thread.currentThread().getId();
								System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() startGameAction: " + SIGNUP_WAIT/1000 + "sec - Player Signup Timer triggered: " + new Date());
								issueNextInputAction("anonymous", StateAction.startGameAction, "", "http://local");
							}				
						}, SIGNUP_WAIT);
					}
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() checking player: " + name);
					if (playerList.get(name) == null) {
						Long regOrder = playerOrder.incrementAndGet();
						Player pl = new Player(name);
						pl.setRegistrationOrder(regOrder);
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() adding player: " + pl.getName());
						playerList.put(pl.getName(), pl);
					}
				}
				else if (StateAction.removePlayerAction.equals(inputAction)) {
					clientCommand = "playerListCmd";
					if (playerList.get(name) == null) {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() removing player: " + name);
						playerList.remove(name);
					}
				}
				else {
					break;
				}				
				try {
					jsonPlayerList = objectMapper.writeValueAsString(playerList);
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() client cmd = " + clientCommand + " - players:\n" + jsonPlayerList);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				greet = new Greeting("anonymous", jsonPlayerList, hintUrl, clientCommand);	
				if (countDownValue > 0) {
					greet.setWaitTime(countDownValue);
				}
				greetings.add(greet);
				break;
			case GAME_START:								
				if (StateAction.activatePlayerAction.equals(inputAction)) {					
					
					if (currentActivePlayer == null) {
						int index = 0;
						while (currentActivePlayer == null) {						
							currentActivePlayer = playerRegistrationOrder.get(index++);
						}
					} else {
						int index = ((int) (currentActivePlayer.getRegistrationOrder()+1)) % playerRegistrationOrder.size();
						currentActivePlayer = playerRegistrationOrder.get(index++);
						while (currentActivePlayer == null) {
							currentActivePlayer = playerRegistrationOrder.get(index++);
						}
					}
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - activating player " + currentActivePlayer.getName());
					clientCommand = "activatePlayerCmd";
					greet = new Greeting(currentActivePlayer.getName(), "", "http://local", clientCommand);
					greetings.add(greet);
				}				
				else if (StateAction.needPlayersAction.equals(inputAction)) {					
					clientCommand = "cannotPlayGameCmd";
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() Not enough players signed up");
					greet = new Greeting("anonymous", "", "http://local", clientCommand);
					greet.setWaitTime(20);					
					greetings.add(greet);
					numberTurns = 0;
					answersReceived = new HashMap<>();
					answerButtonPressed = new HashMap<>();
					hintHistory = new ArrayList<>();
					playerList = new HashMap<String, Player>();
					turnWinner = new ArrayList<>();
					root = stateNodes.get(State.SIGN_UP);
					current = root;
					previous = null;
					nextAction = null;
				}
				break;
			case ACTIVE_PLAYER:
				answerButtonPressed = new HashMap<>();
				String newMessage = "{}";
				String newHintUrl = "http://local";
				if ((StateAction.showGridHintAction.equals(inputAction)) && (currentActivePlayer != null)) {
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - showing hint for " + content); 					
					List<GridData> catList = CacheData.getCategoryMap().get(content);
					GridData entry = null;
					int remaining = catList.size();
					for (GridData tmp : catList) {
						if (!tmp.getPlayed()) {
							tmp.setPlayed(true);
							entry = tmp;
							break;
						}
					}
					for (GridData tmp : catList) {
						if (tmp.getPlayed()) {
							remaining--;
						}
					}
					CategoryEntry catEntry = null;
					if (entry != null) {
						catEntry = new CategoryEntry(entry);
						catEntry.setRemaining(remaining);
						hintHistory.add(catEntry);
						numberTurns++;
						System.out.println("ThreadID: " + id + " - " + "============ Turn number: " + numberTurns + " ===============");						
						newMessage = "{\"title\":\"" + catEntry.getTitle() + 
								"\",\"points\":\"" + catEntry.getPoints() +
								"\",\"hintUrl\":\"" + catEntry.getUrl() + 
								"\"}";
						newHintUrl = catEntry.getEmbeddedUrl();
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - hint: " + newMessage); 
					} else {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() getCategoryEntry can't find avail entries for cat=" + content);
					}					
					if ((catEntry == null) || (catEntry.getRemaining() <= 0)) {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - #### No more content for " + content);
						greet = new Greeting("anonymous", "", "http://local", "gridRefreshCmd");					
						greetings.add(greet);								
					}					
					greet = new Greeting(currentActivePlayer.getName(), newMessage, newHintUrl, "showHintCmd");
					greet.setWaitTime(HINT_VIEW_WAIT/1000);
					greetings.add(greet);
					hintViewTimer = new Timer(true);
					hintViewTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							long id = Thread.currentThread().getId();
							System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() hideGridHintAction: " + HINT_VIEW_WAIT/1000 + " - Player Hide Hint Timer started: " + new Date());
							issueNextInputAction("anonymous", StateAction.hideGridHintAction, "", "http://local");
						}				
					}, HINT_VIEW_WAIT);
				} 
				else if (StateAction.hideGridHintAction.equals(inputAction)) {
					greet = new Greeting(currentActivePlayer.getName(), newMessage, newHintUrl, "hideHintCmd");
					greetings.add(greet);
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() hideGridHintAction received- terminating hintViewTimer timer");
					hintViewTimer.cancel();
					hintViewTimer.purge();
//					hintViewTimer = new Timer(true);
					nextAction = StateAction.startCountdownAction;
				}
//				else if (StateAction.gridRefreshAction.equals(inputAction)) {
//					clientCommand = "gridRefreshCmd";
//				}
				else if (StateAction.startCountdownAction.equals(inputAction)) {					
					greet = new Greeting(currentActivePlayer.getName(), newMessage, newHintUrl, "startCountdownCmd");
					greet.setWaitTime(ANSWER_WAIT/1000);
					greetings.add(greet);
					answerTimer = new Timer(true);
					answerTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							long id = Thread.currentThread().getId();
							System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() stopCountdownAction: " + ANSWER_WAIT/1000 + " - Player Start Countdown Timer started: " + new Date());
							issueNextInputAction("anonymous", StateAction.stopCountdownAction, "", "http://local");
						}			
					}, ANSWER_WAIT);
				}
												
				break;
			case TURN_START:				
				if (StateAction.answerTurnAction.equals(inputAction)) {	// answer button pressed	
					Player pl = playerList.get(name);
					Long playerPosition = 0L;
					if (pl != null) {
						playerPosition = counter.incrementAndGet();
						if ((pl.getOrder() == 0) || (pl.getOrder() > playerPosition)) {
							pl.setOrder(playerPosition.intValue());							
							if (pl.getOrder() <= 2) {
								System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - button pressed by " + name);
								answerButtonPressed.put(name, pl.getOrder() + "");	// add placeholder answer since user pressed button
							}
						}
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - readyToAnswer - user=" + 
								pl.getName() + " pos=" + pl.getOrder());
						greet = new Greeting(pl.getName(), "" + pl.getOrder(), "" + pl.getOrder(), "answerPositionCmd");
						greetings.add(greet);
					}										
					if ((counter.get() >= 2) || (counter.get() >= 1 && playerList.size() <= 2)) {
						nextAction = StateAction.stopCountdownAction;						
						greet = new Greeting("anonymous", "", "http://local", "stopCountdownCmd");					
						greetings.add(greet);	
					}
				}
				else if (StateAction.stopCountdownAction.equals(inputAction)) {
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - stopCountdownAction received - terminating answerTimer timer"); 
					answerTimer.cancel();
					answerTimer.purge();
//					answerTimer = new Timer(true);
					counter.set(0L);
					greet = new Greeting("anonymous", "", "http://local", "resetCountdownCmd");
					greetings.add(greet);
					nextAction = StateAction.resetCountdownAction;
				}
				break;
			case TURN_END:
				if (StateAction.resetCountdownAction.equals(inputAction)) {							
					if (answerButtonPressed.size() > 0) { 				
						greet = new Greeting("anonymous", "", "http://local", "answerSubmissionCmd");			
						greet.setWaitTime(ANSWER_SUBMIT_WAIT/1000);
						greetings.add(greet);
						pressAnswerButtonTimer = new Timer(true);
						try {
							pressAnswerButtonTimer.schedule(new TimerTask() { // timer to cap time for user to press button before submission
								@Override
								public void run() {
									long id = Thread.currentThread().getId();
									System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() submitAnswerAction (" + ANSWER_SUBMIT_WAIT/1000 + ")- Wait Timer triggered: " + new Date());
									issueNextInputAction("", StateAction.submitAnswerAction, "", "http://local");
								}			
							}, ANSWER_SUBMIT_WAIT);
						} catch (Exception ex) {							
						}
					} else {
						nextAction = StateAction.updateScoreAction; // no user pressed the button, skip submission and validation
					}
				}
				else if (StateAction.submitAnswerAction.equals(inputAction)) {		
					if (name.length() > 0) {
						answersReceived.put(name, content);	// collect answers from players that pressed the answer button										
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - ### Got answer from " + name + ": " + content);					
					}
					if ((answersReceived.size() >= 2) || (answersReceived.size() >= 1 && playerList.size() <= 2)) {	
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - ### Got " + answersReceived.size() + " answer(s) with " + playerList.size() + " player(s) - terminating answerSubmitTimer timer");
						pressAnswerButtonTimer.cancel();
						pressAnswerButtonTimer.purge();
					}
					answerValidateTimer = new Timer(true);
					try {						
						answerValidateTimer.schedule(new TimerTask() { // timer to cap time for user to submit the answer before validation
							@Override
							public void run() {
								long id = Thread.currentThread().getId();
								System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() updateScoreAction: " + ANSWER_VALIDATE_WAIT + "sec - Submit Answer Countdown Timer triggered: " + new Date());
								issueNextInputAction("anonymous", StateAction.updateScoreAction, "", "http://local");
							}			
						}, ANSWER_VALIDATE_WAIT);
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Submit Answer Countdown Timer created: " + new Date());
					} catch (Exception ex) {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Error creating Answer Countdown Timer");
					}
					if (answersReceived.size() == answerButtonPressed.size()) {			
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Number of answers received: " + answersReceived.size() + " - terminating answerValidateTimer timer");
						answerValidateTimer.cancel();
						answerValidateTimer.purge();
//						answerValidateTimer = new Timer(true);
						nextAction = StateAction.updateScoreAction;
					}
				}
				else if (StateAction.updateScoreAction.equals(inputAction)) {		
					if (answerButtonPressed.size() == 0) { 
						nextAction = StateAction.deActivatePlayerAction;	
						break;
					}
					for (String plName : answerButtonPressed.keySet()) {
						if (!answersReceived.containsKey(plName)) {
							answersReceived.put(plName, "---"); // add dummy answer for those that pressed the answer button
						}
					}
					Integer [] plScore = new Integer[2];
					HashMap<Integer, Player> posPlayer = new HashMap<>();
					plScore[0] = null;
					plScore[1] = null;
					String thisTitle = hintHistory.get(hintHistory.size()-1).getTitle();
					Integer thisPoints = hintHistory.get(hintHistory.size()-1).getPoints();
					
					for (String plName : answersReceived.keySet()) {
						if (answersReceived.get(plName) != null) { // if user pressed the button then compute answer whether submitted or not
							int percentage = (int)stringCompare(thisTitle, answersReceived.get(plName));
							plScore[(int)(playerList.get(plName).getOrder()-1)] = percentage;
							posPlayer.put(playerList.get(plName).getOrder()-1, playerList.get(plName));
						}
					}					
					for (Player pl : playerList.values()) {
						pl.setOrder(0); // reset player answering order
					}
					int activePlayerPoints = 0;
					int player1Points = 0;
					int player2Points = 0;
					if ((plScore[0] != null) && (plScore[0] > 65)) {
						player1Points = thisPoints;
						activePlayerPoints = thisPoints / 2;						
					} else {
						player1Points = -thisPoints;			
						activePlayerPoints = 0;
						if ((plScore[1] != null) && (plScore[1] > 65)) {
							player2Points = thisPoints / 2;
							activePlayerPoints = thisPoints / 4;
						} else {
							player2Points = -thisPoints / 2;
						}
					}
					
					int winPoints = 0;
					Player winner = null;
					if (player1Points > 0) {
						winner = new Player(posPlayer.get(0).getName());
						winner.setScore(player1Points);
					} else if (player2Points > 0) {
						winner = new Player(posPlayer.get(1).getName());
						winner.setScore(player2Points);
					} else {
						winner = new Player("None");
						winner.setScore(0);
					}
					turnWinner.add(winner);
					
					if (posPlayer.get(0) != null) {
						posPlayer.get(0).setScore(posPlayer.get(0).getScore() + player1Points);						
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Scores: " + posPlayer.get(0).getName() +
								"=" + posPlayer.get(0).getScore());
					}
					if (posPlayer.get(1) != null) {
						posPlayer.get(1).setScore(posPlayer.get(1).getScore() + player2Points);					
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Scores: " + posPlayer.get(1).getName() +
								"=" + posPlayer.get(1).getScore());
					}
					currentActivePlayer.setScore(currentActivePlayer.getScore() + activePlayerPoints);
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Scores: " + currentActivePlayer.getName() + "=" + currentActivePlayer.getScore());
					jsonPlayerList = "{}";
					try {
						jsonPlayerList = objectMapper.writeValueAsString(playerList);
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() client cmd = " + clientCommand + " - players:\n" + jsonPlayerList);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
					greet = new Greeting("anonymous", jsonPlayerList, "http://local", "playerListCmd");		
					greetings.add(greet);
					nextAction = StateAction.deActivatePlayerAction;					
				}
				break;
			case UPDATE_SCORE:
				if (StateAction.deActivatePlayerAction.equals(inputAction)) {		
					greet = new Greeting("anonymous", "", "http://local", "deActivatePlayerCmd");
					greetings.add(greet);
					nextAction = StateAction.showAnswerAction;					
					answersReceived.clear();					
				} 				
				break;
			case INACTIVE_PLAYER:				
				if (StateAction.showAnswerAction.equals(inputAction)) {		
					CategoryEntry catEntry = hintHistory.get(hintHistory.size()-1);
					String hintMessage = "{\"title\":\"" + catEntry.getTitle() + 
							"\",\"points\":\"" + catEntry.getPoints() +
							"\",\"hintUrl\":\"" + catEntry.getUrl() + 
							"\",\"winPlayer\":\"" + turnWinner.get(turnWinner.size()-1).getName() +
							"\",\"winPoints\":\"" + turnWinner.get(turnWinner.size()-1).getScore() +
							"\"}";
					String url = catEntry.getEmbeddedUrl();
					greet = new Greeting("anonymous", hintMessage, url, "showHintAnswerCmd");
					greet.setWaitTime(ANSWER_SHOW_WAIT/1000);
					greetings.add(greet);					
					showAnswerTimer = new Timer(true);
					try {
						showAnswerTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								long id = Thread.currentThread().getId();
								System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() hideAnswerAction: " + ANSWER_SHOW_WAIT/1000 + " - Wait Timer triggered: " + new Date());
								issueNextInputAction("anonymous", StateAction.hideAnswerAction, "", "http://local");
							}			
						}, ANSWER_SHOW_WAIT);
					} catch (Exception ex) {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Error creating Show Answer Countdown Timer");
					}
				}			
				else {					
				}
				break;			
			case SHOW_ANSWER:
				if (StateAction.hideAnswerAction.equals(inputAction)) {		
					if (numberTurns >= MAX_ROUNDS * playerList.size()) {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - ### Reached max number of rounds");
						nextAction = StateAction.endGameAction;
					} else {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - ### Starting next turn");
						nextAction = StateAction.startGameAction;
					}
				}				
				else if (StateAction.startGameAction.equals(inputAction)) {		
					
//					clientCommand = "gridRefreshCmd";
//					greet = new Greeting("anonymous", "", "http://local", clientCommand);		
					waitTimer = new Timer(true);
					try {
						waitTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								long id = Thread.currentThread().getId();
								System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() activatePlayerAction: " + WAIT_TIME/1000 + " - Wait Timer triggered: " + new Date());
								issueNextInputAction("anonymous", StateAction.activatePlayerAction, "", "http://local");
							}			
						}, WAIT_TIME);
					} catch (Exception ex) {
						System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - Error creating Wait Countdown Timer");
					}
				}
				else if (StateAction.endGameAction.equals(inputAction)) {							
					nextAction = StateAction.resetGameAction;
					System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - ### Ending game");
				}
				break;
			case GAME_END:
				if (StateAction.resetGameAction.equals(inputAction)) {							
					nextAction = StateAction.resetGameAction;
					greet = new Greeting("anonymous", "", "http://local", "restartGameCmd");
					greet.setWaitTime(20);
					greetings.add(greet);
				}
				break;
			case GAME_RESET:
				if (StateAction.resetGameAction.equals(inputAction)) {					
					numberTurns = 0;
					answersReceived = new HashMap<>();
					answerButtonPressed = new HashMap<>();
					hintHistory = new ArrayList<>();
					playerList = new HashMap<String, Player>();
					root = stateNodes.get(State.SIGN_UP);
					current = root;
					previous = null;
//					nextAction = "addPlayerAction";
				}
				break;
			default:

			}
			
		synchronized(GameManager.class) {
			StateNode nextNode = GameManager.current.getNext(inputAction);
			if ((nextNode != null) && (nextNode != GameManager.current)) {									
				GameManager.previous = GameManager.current;
				GameManager.current = nextNode;
				System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - ### Next State: " + current.getState());
			} else {
				System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - ### No State Change from: " + current.getState());
			}
			if (greetings.size() > 0) {
				postActionCommand2Client(greetings, postWaitTime);				
			}
			if (nextAction != null) {
				System.out.println("ThreadID: " + id + " - " + "GameManager::processStateHandler() - issuing state action: " + nextAction);
				issueNextInputAction(name, nextAction, content, hintUrl);
			}

		}
		return greet;
	}


	public static float stringCompare(String str1, String str2) {
		String s1 = str1.toLowerCase();
		String s2 = str2.toLowerCase();
		String [] s1Key = s1.split(" ");
		String [] s2Key = s2.split(" ");
		int keyMatches = 0;
		int totalKeys = 1;
		StringBuffer s1Buf = new StringBuffer();
		StringBuffer s2Buf = new StringBuffer();
		for (int i=0; i<s1Key.length; i++) {
			if (s1Key[i].length() <= 2) {
				continue;
			}
			s1Buf.append(s1Key[i]);
		}
		for (int i=0; i<s2Key.length; i++) {
			if (s2Key[i].length() <= 2) {
				continue;
			}
			s2Buf.append(s2Key[i]);
		}
		System.out.println("s1: " + s1Buf.toString());
		System.out.println("s2: " + s2Buf.toString());
		if ((s1Buf.toString().length() == 0) || (s2Buf.toString().length() == 0)) {
			return 0;
		}
			
		if (s1Buf.toString().compareToIgnoreCase(s2Buf.toString()) == 0) {
			System.out.println("s2 == s2 : 100%");
			return 100;
		}
		String regExp1 = ".*" + s1Buf.toString() + ".*";
		if (Pattern.matches(regExp1, s2Buf.toString())) {
			System.out.println("regExS1 ~ s2 : 90%");
			return 90;
		}
		String regExp2 = ".*" + s2Buf.toString() + ".*";
		if (Pattern.matches(regExp2, s1Buf.toString())) {
			System.out.println("s1 ~ regExS2 : 90%");
			return 90;
		}
		for (int i=0; i<s2Key.length; i++) {
			if (s2Key[i].length() <= 2) {
				continue;
			}
			if (s2Key[i].matches("the|for|from|this|that|these|those|are|on|in|to|at")) {
				continue;
			}
			String regExp = ".*" + s2Key[i] + ".*";
			totalKeys++;
			if (Pattern.matches(regExp, s1)) {
				keyMatches++;
			}
		}
		System.out.println("s1 ~ regExS2 matches:" + keyMatches);
		return (keyMatches/totalKeys)*100;
	}

	public static String getPlayers() {
		long id = Thread.currentThread().getId();
		System.out.println("ThreadID: " + id + " getplayers - player list: " + playerList.size());
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonPlayerList = "{}";
		try {
			jsonPlayerList = objectMapper.writeValueAsString(playerList);
			System.out.println("ThreadID: " + id + " getplayers:\n" + jsonPlayerList);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonPlayerList;
	}
	
	
	public static String getCategories() {
		long id = Thread.currentThread().getId();
		Set<String> keySet = CacheData.getCategoryMap().keySet();
		ArrayList<String> catList = new ArrayList<String>(keySet);
		Collections.sort(catList, new Comparator<String>() {
			
			@Override
			public int compare(String s1, String s2) {
//				System.out.println("Comparing s1:" + s1 + " and s2:" + s2);
				String[] stok1 = s1.substring(1, s1.length()-1).split(":");
				String[] stok2 = s2.substring(1, s2.length()-1).split(":");
				int val1 = Integer.parseInt(stok1[1]);
				int val2 = Integer.parseInt(stok2[1]);
				if (val1 == val2) {
					return (stok1[0].compareToIgnoreCase(stok2[0]));
				} 
				return (Integer.compare(val1, val2));
			}
		});
		System.out.println("ThreadID: " + id + " getCategories - category list: " + catList.size());
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonCategoryList = "{}";
		try {
			jsonCategoryList = objectMapper.writeValueAsString(catList);
			System.out.println("ThreadID: " + id + " getCategories:\n" + jsonCategoryList);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonCategoryList;
	}

	public static Player getPlayer(String name) {
		long id = Thread.currentThread().getId();
		System.out.println("ThreadID: " + id + " getplayer info for player: " + name);
		Player pl = playerList.get(name);
		if (pl == null) {
			System.out.println("ThreadID: " + id + " getplayer can't find: " + name);
		}
		return pl;
	}
	
	

	public static CategoryEntry getCategoryEntry(String category) {
		long id = Thread.currentThread().getId();
		System.out.println("ThreadID: " + id + " getCategoryEntry entry list for cat=" + category); //  getCategoryEntry entry list for cat={"90s":100}
		List<GridData> catList = CacheData.getCategoryMap().get(category);
		GridData entry = null;
		int remaining = catList.size();
		for (GridData tmp : catList) {
			if (!tmp.getPlayed()) {
				tmp.setPlayed(true);
				entry = tmp;
				break;
			}
		}
		for (GridData tmp : catList) {
			if (tmp.getPlayed()) {
				remaining--;
			}
		}
		CategoryEntry catEntry = null;
		if (entry != null) {
			catEntry = new CategoryEntry(entry);
			catEntry.setRemaining(remaining);
		} else {
			System.out.println("ThreadID: " + id + " getCategoryEntry can't find avail entries for cat=" + category);
		}
		
		return catEntry;
	}
	
	
	public static String getCategoryRemain(String category) {
		long id = Thread.currentThread().getId();		
		List<GridData> catList = CacheData.getCategoryMap().get(category);
		int remaining = catList.size();
		for (GridData tmp : catList) {
			if (tmp.getPlayed()) {
				remaining--;
			}
		}		
		System.out.println("ThreadID: " + id + " getCategoryEntry entry list for cat=" + category + " remain=" + remaining);
		return new String("{\"remaining\":" + remaining + "}");
	}


	public static Greeting readyToAnswer(HelloMessage message) throws Exception {
		long id = Thread.currentThread().getId();
		Player pl = playerList.get(message.getName());
		Long playerPosition = 0L;
		if (pl != null) {
			playerPosition = counter.incrementAndGet();
			if ((pl.getOrder() == 0) || (pl.getOrder() > playerPosition)) {
				pl.setOrder(playerPosition.intValue());
			}
		}
		System.out.println("ThreadID: " + id + " - readyToAnswer - user=" + 
				HtmlUtils.htmlEscape(message.getName()) + " pos=" + pl.getOrder());
		return new Greeting(HtmlUtils.htmlEscape(message.getName()), "" + pl.getOrder(), "" + pl.getOrder(), "answerPosition");
	}
	
	
	public static Greeting getAdminold(String name,
			String hintUrl,
			String content,
			String action) {
		long id = Thread.currentThread().getId();
		System.out.println("ThreadID: " + id + " getadmin send message to user: " + name + " action: " + action);
		
		if ("resetCountdown".equalsIgnoreCase(action)) {
			counter.set(0L);
			for (Player pl : playerList.values()) {
				pl.setOrder(0);
			}
		} else if ("incrementScore".equalsIgnoreCase(action)) {
			Player pl = playerList.get(name);
			if (pl != null) {
				int newScore = Integer.parseInt(content) + pl.getScore();
				pl.setScore(newScore);
			}
		} else if ("decrementScore".equalsIgnoreCase(action)) {
			Player pl = playerList.get(name);
			if (pl != null) {
				int newScore = pl.getScore() - Integer.parseInt(content);
				pl.setScore(newScore);
			}
		}
		Greeting greet = new Greeting(name, content, hintUrl, action);
		
		return greet;
	}
	
}
