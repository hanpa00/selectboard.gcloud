package com.phan.game.pojo;

public class Player {

	private final String name;
	private Integer teamId;
	private Integer score;
	private Integer order;
	private Long registrationOrder;
	
	public Player() {
		name = "anonymous";
		teamId = 0;
		score = 0;
		registrationOrder = 0L;
		order = 0;
	}
	
	public Player(String name) {
		this.name = name;
		this.teamId = 0;
		this.score = 0;
		this.order = 0;
		registrationOrder = 0L;
	}
	
	public Player(String name, Integer teamId) {
		this.name = name;
		this.teamId = teamId;
		this.score = 0;
		this.order = 0;
		registrationOrder = 0L;
	}
	
	public Player(String name, Integer teamId, Integer order) {
		this.name = name;
		this.teamId = teamId;
		this.score = 0;
		this.order = order;
		registrationOrder = 0L;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Integer getTeamId() {
		return this.teamId;
	}
	
	public void setTeamId(int id) {
		this.teamId = id;
	}
	
	public Integer getScore() {
		return this.score;
	}
	
	public void setScore(int val) {
		this.score = val;
	}
	
	public Integer getOrder() {
		return this.order;
	}
	
	public void setOrder(int pos) {
		this.order = pos;
	}
	
	public Long getRegistrationOrder() {
		return this.registrationOrder;
	}
	
	public void setRegistrationOrder(Long pos) {
		this.registrationOrder = pos;
	}
}
