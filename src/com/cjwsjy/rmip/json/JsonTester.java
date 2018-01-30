package com.cjwsjy.rmip.json;

public class JsonTester {
	public static void main(String[] args) {
		// String json = "{items: [{a: '123'}, {b: '234'}, {c: '345'}]}";
		String json = "{a: 'http://abc.jklajd.net:8080/sadfpijerfklnzxcv', b: 'ewzdf<br/<br/><br/>>ds', c: 'sajdfj:234$%#$-|'}";
		JsonObject jsonObj = (JsonObject)Json.parseJsonText(json);
		
		System.out.println("a: " + jsonObj.getString("a"));
		System.out.println("b: " + jsonObj.getString("b"));
		System.out.println("c: " + jsonObj.getString("c"));
	}
}
