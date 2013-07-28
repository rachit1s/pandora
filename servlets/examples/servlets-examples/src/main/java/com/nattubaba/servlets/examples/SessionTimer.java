package com.nattubaba.servlets.examples;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SessionTimer extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		// Get the current session object, create one if necessary
		HttpSession session = req.getSession();
		out.println("<HTML><HEAD><TITLE>SessionTimer</TITLE></HEAD>");
		out.println("<BODY><H1>Session Timer</H1>");
		// Display the previous timeout
		out.println("The previous timeout was "
				+ session.getMaxInactiveInterval());
		out.println("<BR>");
		// Set the new timeout
		session.setMaxInactiveInterval(2 * 60 * 60); // two hours
		// Display the new timeout
		out.println("The newly assigned timeout is "
				+ session.getMaxInactiveInterval());

		// Get the current session ID by searching the received cookies.
		String sessionid = null;
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
//				if (cookies[i].getName().equals("sessionid")) {
//					sessionid = cookies[i].getValue();
//					break;
//				}
				printCookie(cookies[i],out);
			}
		}

		out.println("</BODY></HTML>");
	}

	private void printCookie(Cookie cookie, PrintWriter out) {
		out.println();
		out.println("Cookie.name : " + cookie.getName());
	}
}