package com.phan.game.selectboard;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Greeting {

	private static long id = 0;
	private final String userName;
	private String hintUrl;
	private String content;
	private String action;
	private String gridValue;
	private Integer waitTime;
	@JsonIgnore
	private static final AtomicLong counter = new AtomicLong();


	public Greeting() {
		this.content = "Hello there";
		userName = "anonymous";
		hintUrl = "https://localhost";
		action = "none";
		gridValue = "$value";
		id = counter.incrementAndGet();
	}

	public Greeting(String content) {
		this.content = content;
		userName = "anonymous";
		hintUrl = "http://localhost";
		action = "none";
		gridValue = "$value";
		id = counter.incrementAndGet();
	}

	public Greeting(String user, String content) {
		userName = user;
		hintUrl = "http://localhost";
		id = counter.incrementAndGet();
		this.content = content;
		gridValue = "$value";
		action = "none";
	}
	
	public Greeting(String user, String content, String val) {
		userName = user;
		hintUrl = "http://localhost";
		id = counter.incrementAndGet();
		this.content = content;
		gridValue = val;
		action = "none";
	}

	public Greeting(String user, String content, String hint, String action) {
		userName = user;
		hintUrl = hint;
		id = counter.incrementAndGet();
		this.content = content;
		this.action = action;
		gridValue = "$value";
	}
	
	public Greeting(String user, String content, String hint, String action, String val) {
		userName = user;
		hintUrl = hint;
		id = counter.incrementAndGet();
		this.content = content;
		this.action = action;
		gridValue = val;
	}
		
	
	public Integer getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(Integer waitTime) {
		this.waitTime = waitTime;
	}

	public long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getHintUrl() {
		return hintUrl;
	}
	
	
	public String getAction() {
		return action;
	}
	
	public String getGridValue() {
		return gridValue;
	}
		
	
	@JsonIgnore
	public String toJsonString() {
		String jsonGreet = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			jsonGreet = objectMapper.writeValueAsString(this);			 
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonGreet;
	}
}