package com.cjwsjy.rmip.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SessionManager {

	Object getAttribute(HttpServletRequest request, String name);

	void setAttribute(HttpServletRequest request, HttpServletResponse response, String name, Object value);

	String getSessionId(HttpServletRequest request, HttpServletResponse response);

	void logout(HttpServletRequest request, HttpServletResponse response);
	
}
