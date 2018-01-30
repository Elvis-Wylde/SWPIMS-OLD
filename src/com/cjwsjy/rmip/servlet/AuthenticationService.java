package com.cjwsjy.rmip.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.cjwsjy.rmip.json.JsonArray;
import com.cjwsjy.rmip.json.JsonObject;
import com.cjwsjy.rmip.session.HttpSessionManager;
import com.cjwsjy.rmip.util.ResponseErrorUtil;

public class AuthenticationService extends BasicService {

	private static final long serialVersionUID = -5446260298006621958L;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		super.doPost(request, response);
		String method = request.getParameter("method");
		JsonObject userprofile = null;
		if (method.equalsIgnoreCase("login")) {
			try {
				userprofile = login(request);
				HttpSessionManager.getInstance().setAttribute(request, response, HttpSessionManager.AUTH_USER, userprofile);
				write(userprofile, response, 0);
			} catch (IOException ex) {
				error(ResponseErrorUtil.CODE_DB_ERROR, ResponseErrorUtil.MSG_DB_ERROR, "内部错误，请联系管理员", response);
			} catch (Exception ex) {
				if (ex.getMessage().equals("usernotfound")) {
					error(ResponseErrorUtil.CODE_USERNOTFOUND, ResponseErrorUtil.MSG_USERNOTFOUND, null, response);
				} else {
					error(ResponseErrorUtil.CODE_PASSWORDUNMATCH, ResponseErrorUtil.MSG_PASSWORDUNMATCH, null, response);
				}
			}
		} else if (method.equalsIgnoreCase("verify")) {
			userprofile = verify(request);
			if (userprofile != null) {
				write(userprofile, response, 0);
			} else {
				error(ResponseErrorUtil.CODE_SESSION_EXPIRED, ResponseErrorUtil.MSG_SESSION_EXPIRED, null, response);
			}
		}
	}
	
	private JsonObject login(HttpServletRequest request) throws Exception {
		String userid = request.getParameter("userid");
		String password = request.getParameter("password");
		JsonObject obj = ((JsonObject)parseJsonFile("data/JSON/userprofile.json"));
		JsonArray records = obj.getJsonArray("results");
		JsonObject profile = null;
		
		for (int i = 0, len = records.count(); i < len; i++) {
			JsonObject userObj = records.getJsonObject(i);
			if (userObj.getString("userid").equals(userid)) {
				if (userObj.getString("password").equals(password)) {
					profile = userObj;
					break;
				} else {
					throw new Exception("passwordunmatch");
				}
			}
		}
		if (profile == null) {
			throw new Exception("usernotfound");
		}
		return profile;
	}
	
	private JsonObject verify(HttpServletRequest request) {
		return (JsonObject)HttpSessionManager.getInstance().getAttribute(request, HttpSessionManager.AUTH_USER);
	}

}
