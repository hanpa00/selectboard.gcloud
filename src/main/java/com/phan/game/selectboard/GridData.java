package com.phan.game.selectboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

//@JsonPropertyOrder({ "title", "URL", "embeddedURL", "category", "author", "points" })
public class GridData {

	private String title;
	private String url;
	private String embeddedUrl;
	private String category;
	private String author;
	private String points;
	@JsonIgnore
	private Boolean played;

	public GridData() {
		this.title = "title";
		this.url = "http://localhost";
		this.embeddedUrl = "http://localhost";
		this.category = "category";
		this.author = "author";
		this.points = "0";
		this.played = false;
		
	}
	
	public GridData(String title, String URL, String embeddedURL, String category,  String author, String points) {
		this.title = title;
		this.url = URL;
		this.embeddedUrl = embeddedURL;
		this.category = category;
		this.author = author;
		this.points = points;
		this.played = false;
	}
	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String URL) {
		url = URL;
	}

	public String getEmbeddedUrl() {
		return embeddedUrl;
	}

	public void setEmbeddedUrl(String embeddedURL) {
		this.embeddedUrl = embeddedURL;
	}
	
	public Boolean getPlayed() {
		return played;
	}

	public void setPlayed(Boolean played) {
		this.played = played;
	}
}
