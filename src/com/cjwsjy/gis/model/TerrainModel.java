package com.cjwsjy.gis.model;

import java.util.Random;

public class TerrainModel {

	protected static Random rand = new Random();
	
	public static String getSourceURL(TileModel tile) {
		String dataSet = tile.getDataSet().toLowerCase();
		if (dataSet.indexOf("stk_terrain") >= 0) {
			return getSTKSourceURL(tile);
		} else if (dataSet.indexOf("Artic") >= 0) {
			return getArticSourceURL(tile);
		}
		return null;
	}
	
	private static String getSTKSourceURL(TileModel tile) {
		String service = "https://assets{$serverpart}.agi.com/stk-terrain/world/{$z}/{$x}/{$y}.terrain?v=1.31376.0";
		// String service = "http://assets.agi.com/stk-terrain/world/{$z}/{$x}/{$y}.terrain?v=1.31376.0";
		String[] serverpart = {"01", "02", "03"};
		int i = rand.nextInt(serverpart.length);
		String url = "";
		url = service.replace("{$serverpart}", serverpart[i]).replace("{$x}", tile.getCol() + "").replace("{$y}", tile.getRow() + "").replace("{$z}", tile.getLevel() + "");
		return url;
	}
	
	private static String getArticSourceURL(TileModel tile) {
		String service = "http://http://assets.agi.com/stk-terrain/v1/tilesets/ArticDEM/tiles/{$z}/{$x}/{$y}.terrain?v=1.2491.0";
		String url = "";
		url = service.replace("{$x}", tile.getCol() + "").replace("{$y}", tile.getRow() + "").replace("{$z}", tile.getLevel() + "");
		return url;
	}
}
