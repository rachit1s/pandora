package com.nattubaba.servlets.examples;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class FileLocation extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setContentType("text/plain");
		PrintWriter out = res.getWriter();
		if (req.getPathInfo() != null) {
			out.println("The file \"" + req.getPathInfo() + "\"");
			out.println("Is stored at \"" + req.getPathTranslated() + "\"");
		} else {
			out.println("Path info is null, no file to lookup");
		}
	}
}