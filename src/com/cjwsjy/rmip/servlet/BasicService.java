package com.cjwsjy.rmip.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.cjwsjy.rmip.json.Json;
import com.cjwsjy.rmip.json.JsonObject;
import com.cjwsjy.rmip.json.JsonTextParser;
import com.cjwsjy.rmip.session.HttpSessionManager;
import com.cjwsjy.rmip.util.ResponseErrorUtil;

public class BasicService extends HttpServlet {

	private static final long serialVersionUID = 1607447827175732861L;
	
	protected static final String CONTEXT_TYPE = "application/json";
	protected static String ENCODING = "UTF-8";
	
	protected JsonObject profile = null;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType(CONTEXT_TYPE);
		response.setCharacterEncoding(ENCODING);
		response.setHeader("Access-Control-Allow-Origin","*");
		response.setContentType("text/html; charset=UTF-8");
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType(CONTEXT_TYPE);
		response.setCharacterEncoding(ENCODING);
	}
	
	protected boolean verifyUserStatus(HttpServletRequest request, HttpServletResponse response) {
		this.profile = (JsonObject)HttpSessionManager.getInstance().getAttribute(request, HttpSessionManager.AUTH_USER);
		if (this.profile == null) {
			error(ResponseErrorUtil.CODE_SESSION_EXPIRED, ResponseErrorUtil.MSG_SESSION_EXPIRED, null, response);
			return false;
		}
		return true;
	}
	
	protected Json parseJsonFile(String filePath) throws IOException {
		String webRootPath = getServletContext().getRealPath("/");
		String fileRealPath = webRootPath + filePath;
		InputStreamReader reader = new InputStreamReader(new FileInputStream(fileRealPath),"UTF-8");
		JsonTextParser parser = new JsonTextParser(reader);
		Json json = parser.parse();
		return json;
	}
	
	protected void write(Json result, HttpServletResponse response, int count) {
		JsonObject outContents = new JsonObject();
		outContents.add("success", true);
		outContents.add("result", result);
		outContents.add("totalCount", count <= 0 ? result.count() : count);
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		outContents.outputToWriter(writer, false);
		writer.flush();
		writer.close();
	}
	
	protected void error(int errorCode, String errorMsg, String extendMsg, HttpServletResponse response) {
		ResponseErrorUtil errorRes = new ResponseErrorUtil(errorCode, errorMsg, extendMsg);
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		errorRes.getErrorResponse().outputToWriter(writer, false);
		writer.flush();
		writer.close();
	}
	
	protected void success(HttpServletResponse response) {
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JsonObject successObj = new JsonObject();
		successObj.add("success", true);
		successObj.add("msg", "信息提交成功！");
		successObj.outputToWriter(writer, false);
		writer.flush();
		writer.close();
	}

}
