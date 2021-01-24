package com.phan.game.pojo;

public class CategoryEntry {

	private String title;
	private String url;
	private String embeddedUrl;
	private String category;
	private String author;
	private Integer points;
	private String hint1;
	private String hint2;
	private Boolean played;
	private Integer remaining;
	
	public CategoryEntry() {
		this.title = "title";
		this.url = "http://localhost";
		this.embeddedUrl = "http://localhost";
		this.category = "category";
		this.author = "author";
		this.points = 0;
		this.hint1 = "hint1";
		this.hint2 = "hint2";
		this.played = false;
		this.remaining = 0;
	}
	
	public CategoryEntry(String title, String URL, String embeddedURL, String category,  String author, Integer points) {
		this.title = title;
		this.url = URL;
		this.embeddedUrl = embeddedURL;
		this.category = category;
		this.author = author;
		this.points = points;
		this.hint1 = "";
		this.hint2 = "";
		this.played = false;
		this.remaining = 0;
	}
	
	public CategoryEntry(GridData data) {
		this.title = data.getTitle();
		this.url = data.getUrl();
		this.embeddedUrl = data.getEmbeddedUrl();
		this.category = data.getCategory();
		this.author = data.getAuthor();
		this.points = Integer.parseInt(data.getPoints());
		this.hint1 = "";
		this.hint2 = "";
		this.played = false;
		this.remaining = 0;
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
	
	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
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
	
	public String getHint1() {
		return hint1;
	}

	public void setHint1(String hint1) {
		this.hint1 = hint1;
	}

	public String getHint2() {
		return hint2;
	}

	public void setHint2(String hint2) {
		this.hint2 = hint2;
	}

	public Boolean getPlayed() {
		return played;
	}

	public void setPlayed(Boolean played) {
		this.played = played;
	}

	public Integer getRemaining() {
		return remaining;
	}

	public void setRemaining(Integer remaining) {
		this.remaining = remaining;
	}

}
