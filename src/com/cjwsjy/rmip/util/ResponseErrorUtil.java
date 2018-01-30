package com.cjwsjy.rmip.util;

import com.cjwsjy.rmip.json.JsonObject;

public class ResponseErrorUtil {
	public static int CODE_UNKONWN_ERROR = 0;
	public static int CODE_DB_ERROR = 1000;
	public static int CODE_PARAM_ERROR = 2000;
	public static int CODE_SESSION_EXPIRED = 3000;
	public static int CODE_PERMISSION_DENIED = 4000;
	public static int CODE_USERNOTFOUND = 7001;
	public static int CODE_PASSWORDUNMATCH = 7002;

	public static String MSG_UNKONWN_ERROR = "未知错误";
	public static String MSG_DB_ERROR = "数据库错误";
	public static String MSG_PARAM_ERROR = "参数解析错误";
	public static String MSG_SESSION_EXPIRED = "当前会话已过期，请刷新页面后重新登录";
	public static String MSG_PERMISSION_DENIED = "用户权限不足";
	public static String MSG_USERNOTFOUND = "用户不存在";
	public static String MSG_PASSWORDUNMATCH = "密码错误";

	private int code = ResponseErrorUtil.CODE_UNKONWN_ERROR;
	private String message = ResponseErrorUtil.MSG_UNKONWN_ERROR;

	public ResponseErrorUtil() {
		this(-1, null, null);
	}
	
	public ResponseErrorUtil(int code) {
		this(code, null, null);
	}

	public ResponseErrorUtil(int code, String message) {
		this(code, message, null);
	}

	public ResponseErrorUtil(int code, String message, String extendMessage) {
		this.setCode(code);
		this.setMessage(message, extendMessage);
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code < 0 ? ResponseErrorUtil.CODE_UNKONWN_ERROR : code;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.setMessage(message, null);
	}

	public void setMessage(String message, String extendMessage) {
		message = (message == null || message.trim().length() < 1) ? MSG_UNKONWN_ERROR : message;
		extendMessage = (extendMessage == null || extendMessage.trim().length() < 1) ? "" : "<br/>" + extendMessage;
		this.message = message + extendMessage;
	}

	public JsonObject getErrorResponse() {
		JsonObject errorObj = new JsonObject();
		JsonObject errorContent = new JsonObject();
		errorContent.add("code", this.code);
		errorContent.add("message", this.message);
		
		errorObj.add("success", false);
		errorObj.add("error", errorContent);
		return errorObj;
		
	}
}
