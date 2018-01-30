package com.cjwsjy.gis.model;

import javax.servlet.http.HttpServletRequest;

public class TileModel {
	private String dataSet = null;
	private int level = -1;
	private int row = -1;
	private int col = -1;
	
	public TileModel(HttpServletRequest request) {
		this.handleParams(request);
	}
	
	public TileModel(String dateSet, int level, int row, int col) {
		setDataSet(dateSet);
		setLevel(level);
		setRow(row);
		setCol(col);
	}
	
	public String getDataSet() {
		return dataSet;
	}

	public void setDataSet(String dateSet) {
		this.dataSet = dateSet;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	private void handleParams(HttpServletRequest request) throws NullPointerException, NumberFormatException {
		this.dataSet = request.getParameter("T");
		this.level = Integer.parseInt(request.getParameter("L"));
		this.row = Integer.parseInt(request.getParameter("Y"));
		this.col = Integer.parseInt(request.getParameter("X"));
	}
}
