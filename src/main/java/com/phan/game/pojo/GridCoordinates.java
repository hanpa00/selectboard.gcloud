package com.phan.game.pojo;

public class GridCoordinates {

	private final String name;
	private final int row;
	private final int col;
	

	public GridCoordinates() {
		row = 0;
		col = 0;
		name = "anonymous";
	}

	public GridCoordinates(String user, int x, int y) {
		row = x;
		col = y;
		name = user;
	}
	
	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	public String getName() {
		return name;
	}
}
