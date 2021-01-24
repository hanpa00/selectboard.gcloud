package com.phan.game.selectboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;
import org.springframework.ui.Model;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.TopicManagementResponse;
import com.phan.game.fcm.PushNotificationService;
import com.phan.game.message.Greeting;
import com.phan.game.message.HelloMessage;
import com.phan.game.pojo.CategoryEntry;
import com.phan.game.pojo.Player;

import com.phan.game.message.PushNotificationRequest;
import com.phan.game.message.PushNotificationResponse;


@Controller
public class GreetingController {

	@Value("${server.host:localhost}")
	private String serverHost;
	
	@Value("${server.port:8080}")
	private String serverPort;
	
	@Value("${apiKey:}")
	private String apiKey;
	
	@Value("${apiHttps:false}")
	private boolean sslEnabled;
	
//	private static final String template = "Hello, %s!";
	private static final String template = "%s";
	public static HashMap<String, Long> threadMap = new HashMap<String, Long>(); 	
	private static SimpMessagingTemplate templateMsg; // needed by ad-hoc stomp client message
	private static HashMap<String, Integer> playerScore = new HashMap<String, Integer>();
	private static HashMap<Integer, Integer> teamScore = new HashMap<Integer, Integer>();
	private static HashMap<String, Integer> playerTeamMap = new HashMap<String, Integer>();
	private static HashMap<String, Player> playerList = new HashMap<String, Player>();
	private static ArrayList<Player> answerList = new ArrayList<Player>();
	final AtomicLong counter = new AtomicLong();
	final AtomicLong playerOrder = new AtomicLong();
	private static HashMap<String, Long> playerRegistrationOrder = new HashMap<>();		
	private static PushNotificationService pushNotificationService;
	private static String topic = "selectboardtopic1";
	private static String registrationToken;// = "eG5FS8h_rxpL3D9qF-KxMM:APA91bFrKBdBKudbUMYYc8werB4TLXqkQMRdXR4N2feL0360QxRc_gXtbyPzilzTIzhoz_uxczrv4L05-IwKF7RiHPW68EXL6Uh0Vt-x1-9skSrkC76bmYFs2LRLywYFpSgMVGsjwWQy";
//	@Autowired
//	public GreetingController(SimpMessagingTemplate template) {
//		System.out.println("GreetingController: Message Template = " + template.getDefaultDestination());
//		templateMsg = template;
//		//pushNotificationService = (PushNotificationService)CacheData.pushNotificationService;
//	}
	
	@Autowired
	public GreetingController(PushNotificationService pushService) {
		
		//List<String> registrationTokens = Arrays.asList(registrationToken);
				
		System.out.println("GreetingController: pushService = " + pushService.toString());
		pushNotificationService = pushService;
//		try {
//			TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
//					registrationTokens, topic);
//			// See the TopicManagementResponse reference documentation
//			// for the contents of response.
//			System.out.println(response.getSuccessCount() + " tokens were subscribed successfully");
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage());
//		}

	}


    @PostMapping("/notification/topic")
    public static ResponseEntity sendNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationWithoutData(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }

    @PostMapping("/notification/token")
    public static ResponseEntity sendTokenNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }

    @PostMapping("/notification/data")
    public static ResponseEntity sendDataNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotification(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }

//    @GetMapping("/notification")
//    public static ResponseEntity sendSampleNotification() {
//        pushNotificationService.sendSamplePushNotification();
//        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
//    }
    
	public static void postGreetingsTopic(Greeting greet) {
		long id = Thread.currentThread().getId();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonGreet = objectMapper.writeValueAsString(greet);
			System.out.println("ThreadID: " + id + " - postGreetingsTopic: " + jsonGreet);
			PushNotificationRequest request = new PushNotificationRequest();
			request.setMessage(jsonGreet);
			request.setTitle(greet.getAction());
			request.setTopic(topic);
			request.setToken(registrationToken);
			System.out.println("ThreadID: " + id + " - request: " + request.toString());
			//sendDataNotification(request);
			sendTokenNotification(request);
			//templateMsg.convertAndSend("/topic/greetings", jsonGreet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@GetMapping("/greeting", produces="application/json")
	// Returs a JSON object
	@RequestMapping(path="/greeting", method=RequestMethod.GET, produces="application/json")
	public @ResponseBody Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name,
			@RequestParam(value = "token", defaultValue = "N/A") String token) {
		long id = Thread.currentThread().getId();
		System.out.println("ThreadID: " + id + " greeting - token=" + token);
		registrationToken = token;
		return new Greeting(String.format(template, name));
	}
	
	// Returns server side updated html page
	@GetMapping("/")
	public String index(Model model) {

		
		String host = CacheData.getInputArgMap().get("--server.host");
		if ((host == null) || (host.isBlank())) {
			host = serverHost;
		}
		String port = CacheData.getInputArgMap().get("--server.port");
		if ((port == null) || (port.isBlank())) {
			port = serverPort;
		}
		String key = CacheData.getInputArgMap().get("--apikey");
		if ((key == null) || (key.isBlank())) {
			key = apiKey;
		}
		
		model.addAttribute("serverssl", sslEnabled);			
		model.addAttribute("serverhost", host);
		model.addAttribute("serverport", port);
		model.addAttribute("apikey", key);
		System.out.println("Thymeleaf data: " + model.toString());

		return "index";
	}


	// handles the message from STOMP client and sends message to /topic/greetings
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public @ResponseBody Greeting greetings(HelloMessage message) throws Exception {
		long id = Thread.currentThread().getId();
		System.out.println("ThreadID: " + id + " greetings " + HtmlUtils.htmlEscape(message.getName()));
//		if (playerScore.get(message.getName()) == null) {
//			playerScore.put(message.getName(), 0);
//			Player pl = new Player(message.getName());
//			playerList.put(pl.getName(), pl);
//		}
		return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()));
	}


	// handles the message from STOMP client and sends message to /topic/greetings
	@RequestMapping(path="/register", method=RequestMethod.GET)
	public void register(HelloMessage message) throws Exception {
		long id = Thread.currentThread().getId();
		System.out.println("ThreadID: " + id + " greetings " + HtmlUtils.htmlEscape(message.getName()));
//		Thread.sleep(1000); // simulated delay
		if (playerScore.get(message.getName()) == null) {
			playerScore.put(message.getName(), 0);
			
		}
		if (playerList.get(message.getName()) == null) {
			Long regOrder = playerOrder.incrementAndGet();
			Player pl = new Player(message.getName());
			pl.setRegistrationOrder(regOrder);
			playerList.put(pl.getName(), pl);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonPlayerList = "{}";
		try {
			jsonPlayerList = objectMapper.writeValueAsString(playerList);
			System.out.println("ThreadID: " + id + " getplayers:\n" + jsonPlayerList);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		postGreetingsTopic(new Greeting("anonymous", jsonPlayerList, "http://local", "playerList"));
		return;
	}

	

	// handles the message from STOMP client and sends message to /topic/greetings
//	@MessageMapping("/grid")
//	@SendTo("/topic/greetings")
//	public @ResponseBody Greeting getGridDetails(GridCoordinates message) throws Exception {
//		long id = Thread.currentThread().getId();
//		System.out.println("ThreadID: " + id + " - coordinates - user=" + HtmlUtils.htmlEscape(message.getName()) + 
//				" x=" + message.getRow() + " y=" + message.getCol());
//		//			Thread.sleep(1000); // simulated delay
//		return new Greeting(HtmlUtils.htmlEscape(message.getName()), "Let it go", "https://www.youtube.com/embed/QnN6glKaWdE", "gridDetails", "$100");
//	}

	
	// Returns a JSON object and sends a message to /topic/greeting via websocket
	@RequestMapping(path="/getadminold", method=RequestMethod.GET)
	public @ResponseBody Greeting getAdminold(@RequestParam(value = "name", defaultValue = "anonymous") String name,
			@RequestParam(value = "hintUrl", defaultValue = "http://localhost") String hintUrl,
			@RequestParam(value = "content", defaultValue = "hello") String content,
			@RequestParam(value = "action", defaultValue = "none") String action) {
		
		Greeting greet = GameManager.getAdminold(name, content, hintUrl, action);
		postGreetingsTopic(greet);
		return greet;
	}
	
	// Returns a JSON object and sends a message to /topic/greeting via websocket
	@RequestMapping(path="/getadmin", method=RequestMethod.GET)
	public @ResponseBody Greeting getAdmin(@RequestParam(value = "name", defaultValue = "anonymous") String name,
			@RequestParam(value = "hintUrl", defaultValue = "http://localhost") String hintUrl,
			@RequestParam(value = "content", defaultValue = "hello") String content,
			@RequestParam(value = "action", defaultValue = "none") String action) {
		long id = Thread.currentThread().getId();
//		String greeting = String.format(template, name);
		System.out.println("ThreadID: " + id + " getadmin message from user: " + name + " action: " + action);
		
		Greeting greet = GameManager.processStateHandler(name, GameManager.StateAction.valueOf(action), content, hintUrl);
//		if (greet != null) {
//			postGreetingsTopic(greet);
//		}
		return greet;
	}
	
//	@RequestMapping(path="/postadmin", method=RequestMethod.POST)
//	public void activatePlayer(@RequestBody Player player) {
//		long id = Thread.currentThread().getId();
//		long time = GregorianCalendar.getInstance().getTimeInMillis();
//		String text = "[" + time + "]:" + player.getName();
//		System.out.println("ThreadID: " + id + " postadmin " + text);
//		ObjectMapper objectMapper = new ObjectMapper();
//		Greeting greet = new Greeting(player.getName(), null, null, "activatePlayer");
//		String jsonGreet = "";
//		try {
//			jsonGreet = objectMapper.writeValueAsString(greet);
//			this.templateMsg.convertAndSend("/topic/greetings", jsonGreet);
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
	// Returns a map in a JSON object
	@RequestMapping(path="/getplayers", method=RequestMethod.GET)
	public ResponseEntity<Object> getPlayers() {
		String jsonPlayerList = GameManager.getPlayers();
		return new ResponseEntity(jsonPlayerList, HttpStatus.OK);
	}
	
	
	// Returns a map in a JSON object of categories
	@RequestMapping(path="/getcategories", method=RequestMethod.GET)
	public ResponseEntity<Object> getCategories() {
		String jsonCategoryList = GameManager.getCategories();
		return new ResponseEntity(jsonCategoryList, HttpStatus.OK);
	}
	

	// Returns a map in a JSON object
	@RequestMapping(path="/getplayer", method=RequestMethod.GET)
	public @ResponseBody Player getPlayer(@RequestParam(value = "name", defaultValue = "anonymous") String name) {
		Player pl = GameManager.getPlayer(name);
		return pl;
	}
	

	// Returns a map in a JSON object
	@RequestMapping(path="/getcategoryentry", method=RequestMethod.GET)
	public @ResponseBody CategoryEntry getCategoryEntry(@RequestParam(value = "category", defaultValue = "none") String category) {
		CategoryEntry catEntry = GameManager.getCategoryEntry(category);
		
		if ((catEntry == null) || (catEntry.getRemaining() <= 0)) {
			Greeting greet = new Greeting("anonymous", category, "http://local", "gridRefresh");
			postGreetingsTopic(greet);
		}
		return catEntry;
	}
	
	

	// Returns a map in a JSON object
	@RequestMapping(path="/getcategoryremain", method=RequestMethod.GET)
	public @ResponseBody String getCategoryRemain(@RequestParam(value = "category", defaultValue = "none") String category) {
		String result = GameManager.getCategoryRemain(category);
		return result;
	}


	// handles the message from STOMP client and sends message to /topic/greetings
//	@GetMapping("/answer")
	@MessageMapping("/answer")
	@SendTo("/topic/greetings")
	public @ResponseBody Greeting readyToAnswer(HelloMessage message) throws Exception {
		Greeting greet = GameManager.readyToAnswer(message);
		return greet;
	}
	

	// Loads the saludos.html page. This uses Thymeleaf
	@GetMapping("/saludos")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		
		String serverhost = CacheData.getInputArgMap().get("--server.host");
		String serverport = CacheData.getInputArgMap().get("--server.port");
		model.addAttribute("name", name);
		model.addAttribute("serverhost", serverhost);
		model.addAttribute("serverport", serverport);
		return "saludos";
	}


}