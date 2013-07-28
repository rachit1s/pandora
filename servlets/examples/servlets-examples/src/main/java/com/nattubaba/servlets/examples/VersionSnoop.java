package com.nattubaba.servlets.examples;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.oreilly.nattubaba.servlet.VersionDetector;

public class VersionSnoop extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setContentType("text/plain");
		PrintWriter out = res.getWriter();
		out.println("Servlet Version: " + VersionDetector.getServletVersion());
		out.println("Java Version: " + VersionDetector.getJavaVersion());
	}
}