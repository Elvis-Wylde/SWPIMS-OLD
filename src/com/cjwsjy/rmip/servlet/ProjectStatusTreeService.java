package com.cjwsjy.rmip.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.cjwsjy.rmip.json.Json;
import com.cjwsjy.rmip.json.JsonArray;
import com.cjwsjy.rmip.json.JsonException;
import com.cjwsjy.rmip.json.JsonObject;

public class ProjectStatusTreeService extends BasicService {

	private static final long serialVersionUID = -5440346796940741231L;
	private static DecimalFormat DF = new DecimalFormat("#.00");
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		super.doGet(request, response);
		response.setHeader("Access-Control-Allow-Origin","*");
		
		String basin = request.getParameter("basin");
		try {
			outputJson();
			// write(getProjectStatusTree(basin), response, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void outputJson() throws JsonException, IOException {
		/*
		JsonObject obj = ((JsonObject)parseJsonFile("data/JSON/projects.json"));
		JsonArray projects = obj.getJsonArray("results");
		JsonObject project = null;
		
		JsonObject results = new JsonObject();
		JsonObject root = new JsonObject();
		root.add("id", "510000");
		root.add("expanded", true);
		root.add("text", "四川省");
		
		root.add("children", new JsonArray());
		
		// JsonArray basinArray = new JsonArray();
		for (int i = 0, len = projects.count(); i < len; i++) {
			project = projects.getJsonObject(i);
			String basin = project.getString("basin");
			JsonArray basinArray = root.getJsonArray("children");
			JsonObject basinObj = getJsonObjectById(basinArray, basin);
			if (basinObj == null) {
				basinObj = new JsonObject();
				basinObj.add("id", basin);
				basinObj.add("text", getBasinName(basin));
				basinObj.add("type", "basin");
				basinObj.add("children", new JsonArray());
				root.getJsonArray("children").append(basinObj);
			}
			JsonObject pitem = new JsonObject();
			pitem.add("id", project.getString("id"));
			pitem.add("text", project.getString("name") + project.getString("ptype"));
			pitem.add("type", "project");
			pitem.add("leaf", true);
			basinObj.getJsonArray("children").append(pitem);
		}
		results.add("root", root);
		System.out.println(results.generateJsonText(false));
		*/
		
		JsonObject obj = ((JsonObject)parseJsonFile("data/JSON/projectcounty.json"));
		JsonArray records = obj.getJsonArray("results");
		JsonObject record = null;
		
		for (int i = 0, len = records.count(); i < len; i++) {
			record = records.getJsonObject(i);
			String countyid = record.getString("id");
			String projid = record.getString("projectid");
			record.add("countyid", countyid);
			record.set("id", projid + "-" + countyid);
			/*
			record = records.getJsonObject(i);
			double count = record.getDouble("scrksum");
			double min = count / 2;
			double diff = count - min;
			int planCount = (int)Math.round(Math.random() * diff + min);
			record.add("scrkplan", planCount);
			
			min = (double)(planCount / 2);
			diff = planCount - min;
			int completeCount = (int)Math.round(Math.random() * diff + min);
			record.add("scrkcomplete", completeCount);
			*/
		}
		
		System.out.println(obj.generateJsonText());
		
		/*
		JsonObject obj = ((JsonObject)parseJsonFile("data/JSON/projects.json"));
		JsonArray projects = obj.getJsonArray("results");
		JsonObject project = null;
		
		for (int i = 0, len = projects.count(); i < len; i++) {
			project = projects.getJsonObject(i);
			//project.set("GCZTZ", format(project.getDouble("GCZTZ") / 10000));
			//project.set("ZJRL", format(project.getDouble("ZJRL")));
			//project.set("GSZJ", format(project.getDouble("GSZJ")));
			//if (project.getString("statusID") == null) {
			//	project.add("statusID", getStatusId(project.getString("YMGZJD")));
			//}
			if (project.getString("YMBCTZ") == null) {
				project.add("YMBCTZ", format(project.getDouble("GSZJ") + project.getDouble("TZZJ")));
			}
		}
		
		System.out.println(obj.generateJsonText());
		*/
	}
	
	private JsonArray getProjectStatusTree(String basinId) throws IOException {
		JsonArray projects = ((JsonObject)parseJsonFile("data/JSON/projects.json")).getJsonArray("results");
		JsonArray results = new JsonArray();
		JsonObject project = null;
		for (int i = 0, len = projects.count(); i < len; i++) {
			project = projects.getJsonObject(i);
			String basin = project.getString("basin");
			if (basinId != null && !basinId.isEmpty() && !basinId.equals(basin)) {
				continue;
			}
			String pstatus = project.getString("YMGZJD");
			JsonObject parent = getStatusNode(results, pstatus);
			
			parent.set("PCOUNT", parent.getLong("PCOUNT") + 1);
			parent.set("cities", parent.getLong("cities") + project.get("cities").count());
			parent.set("counties", parent.getLong("counties") + project.get("counties").count());
			parent.set("GCZTZ", format(parent.getDouble("GCZTZ") + project.getDouble("GCZTZ") / 10000));
			parent.set("ZJRL", format(parent.getDouble("ZJRL") + project.getDouble("ZJRL")));
			parent.set("GHAZRK", parent.getLong("GHAZRK") + project.getLong("GHAZRK"));
			parent.set("GHBQRK", parent.getLong("GHBQRK") + project.getLong("GHBQRK"));
			parent.set("GSZJ", format(parent.getDouble("GSZJ") + project.getDouble("GSZJ")));
			
			project.set("GCZTZ", format(project.getDouble("GCZTZ") / 10000));
			project.add("leaf", true);
			
			parent.getJsonArray("children").append(project);
		}
		
		return results;
	}

	private JsonObject getStatusNode(JsonArray results, String status) {
		JsonObject obj = null;
		for (int i = 0, len = results.count(); i < len; i++) {
			obj = results.getJsonObject(i);
			if (obj.getString("name").equals(status)) {
				break;
			}
			obj = null;
		}
		if (obj == null) {
			obj = new JsonObject();
			obj.add("id", getStatusId(status));
			obj.add("name", status);
			obj.add("PCOUNT", 0);
			obj.add("cities", 0);
			obj.add("counties", 0);
			obj.add("GCZTZ", 0);
			obj.add("ZJRL", 0);
			obj.add("GHAZRK", 0);
			obj.add("GHBQRK", 0);
			obj.add("GSZJ", 0);
			obj.add("expanded", true);
			obj.add("children", new JsonArray());
			
			results.append(obj);
		}
		return obj;
	}
	
	private String getStatusId(String status) {
		switch (status) {
			case "前期阶段":
			default:
				return "A";
			case "实施阶段":
				return "B";
			case "收尾阶段":
				return "C";
			case "完成阶段":
				return "D";
		}
	}
	
	protected void write(Json result, HttpServletResponse response, int count) {
		JsonObject outContents = new JsonObject();
		outContents.add("children", result);
		PrintWriter writer = null;
		try {
			outputJson();
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		outContents.outputToWriter(writer, false);
		writer.flush();
		writer.close();
	}
	
	private double format(double value) {
		return Double.parseDouble(DF.format(value));
	}
	
	private String getBasinName(String id) {
		switch (id) {
			case "603":
			default:
				return "雅砻江流域";
			case "604":
				return "金沙江流域";
			case "212":
				return "大渡河流域";
			case "999":
				return "其他流域";
		}
	}
	
	private JsonObject getJsonObjectById(JsonArray array, String id) {
		for (int i = 0, len = array.count(); i < len; i++) {
			JsonObject item = (JsonObject)array.get(i);
			if (item.getString("id").equals(id)) {
				return item;
			}
		}
		return null;
	}
}
