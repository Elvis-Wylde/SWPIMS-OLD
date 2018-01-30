package com.cjwsjy.rmip.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HttpSessionManager implements SessionManager {
	// 登录后用户信息
	public static final String AUTH_USER = "auth_user";

	private static HttpSessionManager instance = new HttpSessionManager();

	private HttpSessionManager() {
	}

	public static HttpSessionManager getInstance() {
		return instance;
	}

	public Object getAttribute(HttpServletRequest request, String name) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return session.getAttribute(name);
		} else {
			return null;
		}
	}

	public void setAttribute(HttpServletRequest request, HttpServletResponse response, String name, Object value) {
		HttpSession session = request.getSession();
		session.setAttribute(name, value);
	}

	public String getSessionId(HttpServletRequest request, HttpServletResponse response) {
		return request.getSession().getId();
	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
