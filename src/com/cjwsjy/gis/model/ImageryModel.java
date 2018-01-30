package com.cjwsjy.gis.model;

import java.util.Random;

public class ImageryModel {
	
	// public static final String GOOGLE_SERVICE = "http://www.google.cn/maps/vt?lyrs=s@167&gl=cn&x={x}&y={y}&z={z}";
	// public static String GOOGLE_SERVICE = "http://khm{$serverpart}.google.com/kh/v=699&x={x}&y={y}&z={z}&s=Galileo";
	protected static Random rand = new Random();
	
	public static String getSourceURL(TileModel tile) {
		String dataSet = tile.getDataSet().toLowerCase();
		if (dataSet.indexOf("google") >= 0) {
			return getGoogleURL(tile);
		} else if (dataSet.indexOf("tianditudom") >= 0) {
			return getTiandituDOM(tile);
		} else if (dataSet.indexOf("tianditu4326dom") >= 0) {
			return getTiandituDOM4326(tile);
		} else if (dataSet.indexOf("tianditucva") >= 0) {
			return getTiandituCva(tile);
		} else if (dataSet.indexOf("tianditucia") >= 0) {
			return getTiandituCia(tile);
		} else if (dataSet.indexOf("tianditu4326cia") >= 0) {
			return getTiandituCia4326(tile);
		} else if (dataSet.indexOf("tiandituwat") >= 0) {
			return getTiandituWat(tile);
		} else if (dataSet.indexOf("bingaerial") >= 0) {
			return getBingAerialURL(tile);
		} else if (dataSet.indexOf("mapboxstreets") >= 0) {
			return getMapboxStreets(tile);
		}
		return null;
	}
	
	public static String getContentType(TileModel tile) {
		String dataSet = tile.getDataSet().toLowerCase();
		if (dataSet.indexOf("tianditucva") >= 0 || dataSet.indexOf("tianditucia") >= 0 || dataSet.indexOf("tiandituwat") >= 0 
				|| dataSet.indexOf("tianditu4326cia") >= 0) {
			return "image/png";
		} else {
			return "image/jpeg";
		}
	}
	
	public static String getGoogleURL(TileModel tile) {
		// String service = "http://khm{$serverpart}.google.com/kh/v=699&x={$x}&y={$y}&z={$z}&s=Galileo";
		String service = "http://mt{$serverpart}.google.cn/vt/lyrs=s&hl=zh-CN&x={$x}&y={$y}&z={$z}";
		return handlService(tile, service, 4);
	}
	
	public static String getBingAerialURL(TileModel tile) {
		String service = "http://ecn.t{$serverpart}.tiles.virtualearth.net/tiles/a{$quadkey}.jpeg?g=5296";
		String quadKey = convertQuadkey(tile);
		int i = rand.nextInt(4);
		return service.replace("{$serverpart}", i + "").replace("{$quadkey}", quadKey);
	}
	
	public static String getMapboxStreets(TileModel tile) {
		String token = "pk.eyJ1IjoiYW5hbHl0aWNhbGdyYXBoaWNzIiwiYSI6ImNpd204Zm4wejAwNzYyeW5uNjYyZmFwdWEifQ.7i-VIZZWX8pd1bTfxIVj9g";
		String service = "https://api.mapbox.com/v4/mapbox.streets/{$z}/{$x}/{$y}.png?access_token=" + token; 
		return handlService(tile, service, 1);
	}
	
	// Tianditu DOM
	public static String getTiandituDOM(TileModel tile) {
		String service = "http://t{$serverpart}.tianditu.com/DataServer?T=img_w&x={$x}&y={$y}&l={$z}";
		return handlService(tile, service, 8);
	}
	
	// Tianditu 水系
	public static String getTiandituWat(TileModel tile) {
		String service = "http://t{$serverpart}.tianditu.com/DataServer?T=wat_w&x={$x}&y={$y}&l={$z}";
		return handlService(tile, service, 8);
	}
	
	// Tianditu 路网
	public static String getTiandituCia(TileModel tile) {
		String service = "http://t{$serverpart}.tianditu.com/DataServer?T=cia_w&x={$x}&y={$y}&l={$z}";
		return handlService(tile, service, 8);
	}
	
	// Tianditu 注记
	public static String getTiandituCva(TileModel tile) {
		String service = "http://t{$serverpart}.tianditu.com/DataServer?T=cva_w&x={$x}&y={$y}&l={$z}";
		return handlService(tile, service, 8);
	}
	
	// Tianditu DOM 4326
	public static String getTiandituDOM4326(TileModel tile) {
		String service = "http://t{$serverpart}.tianditu.com/DataServer?T=img_c&x={$x}&y={$y}&l={$z}";
		return handlService(tile, service, 8);
	}
	
	// Tianditu 路网 4326
	public static String getTiandituCia4326(TileModel tile) {
		String service = "http://t{$serverpart}.tianditu.com/DataServer?T=cia_c&x={$x}&y={$y}&l={$z}";
		return handlService(tile, service, 8);
	}
	
	protected static String handlService(TileModel tile, String service, int serverpart) {
		int i = rand.nextInt(serverpart);
		String url = "";
		url = service.replace("{$serverpart}", i + "").replace("{$x}", tile.getCol() + "").replace("{$y}", tile.getRow() + "").replace("{$z}", tile.getLevel() + ""); 
		return url;
	}
	
	protected static String convertQuadkey(TileModel tile) {
		String quadKey = "";
		int level = tile.getLevel() - 1;
		int y = tile.getRow();
		int x = tile.getCol();
		for (int i = level; i >= 0; --i) {
			int bitmask = 1 << i;
			int digit = 0;
			
			if ((x & bitmask) != 0) {
				digit |= 1;
			}
			if ((y & bitmask) != 0) {
				digit |= 2;
			}
			quadKey += digit;
		}
		return quadKey;
	}
}
