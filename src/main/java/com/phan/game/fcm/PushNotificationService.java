package com.phan.game.fcm;


import com.google.api.client.util.Value;
import com.phan.game.message.PushNotificationRequest;

import org.springframework.beans.factory.annotation.Autowired;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PushNotificationService {

	@Value("#{${app.notifications.defaults}}")
	private Map<String, String> defaults;

//	private Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
	private FCMService fcmService;
	
	private String registrationToken;
	
	public void setToken(String token) {
		registrationToken = token;
	}

	@Autowired
	public PushNotificationService(FCMService fcmService) {
		this.fcmService = fcmService;
	}

//	@Scheduled(initialDelay = 60000, fixedDelay = 60000)
//	public void sendSamplePushNotification() {
//		try {
//			fcmService.sendMessageWithoutData(getHeartbeatPushNotificationRequest());
//		} catch (InterruptedException | ExecutionException e) {
//			System.out.println(e.getMessage());
//		}
//	}

	public void sendPushNotification(PushNotificationRequest request) {
		try {
			fcmService.sendMessage(buildPayloadData(request), request);
		} catch (InterruptedException | ExecutionException e) {
			System.out.println(e.getMessage());
		}
	}

	public void sendPushNotificationWithoutData(PushNotificationRequest request) {
		try {
			fcmService.sendMessageWithoutData(request);
		} catch (InterruptedException | ExecutionException e) {
			System.out.println(e.getMessage());
		}
	}


	public void sendPushNotificationToToken(PushNotificationRequest request) {
		try {
			fcmService.sendMessageToToken(request);
		} catch (InterruptedException | ExecutionException e) {
			System.out.println(e.getMessage());
		}
	}


	private Map<String, String> getSamplePayloadData() {
		Map<String, String> pushData = new HashMap<>();
		pushData.put("messageId", defaults.get("payloadMessageId"));
		pushData.put("text", defaults.get("payloadData") + " " + LocalDateTime.now());
		return pushData;
	}
	
	private Map<String, String> buildPayloadData(PushNotificationRequest request) {
		Map<String, String> pushData = new HashMap<>();
		pushData.put("messageId", request.getTitle() + ":" + LocalDateTime.now());
		pushData.put("text", request.getMessage());
		return pushData;
	}


	private PushNotificationRequest getHeartbeatPushNotificationRequest() {
		PushNotificationRequest request = new PushNotificationRequest("heartbeat", 
				"time:" + LocalDateTime.now(),
				"selectboardtopic1");		
		request.setToken(registrationToken);

		return request;
	}

	

}
