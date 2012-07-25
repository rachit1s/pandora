package com.nattubaba.servlets;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FirstServlet extends HttpServlet
{
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		String queryString = request.getQueryString();
		String path = request.getPathInfo() ;
		String pname = request.getParameter("name");
		String pLastname = request.getParameter("lastName");
		
		try {
			Writer out = response.getWriter();
			out.write("<html> <title>First Servlet Info</title> <body>");
			out.write("Path Info : " + path + "<br/>");
			out.write("queryString : " + queryString + "<br/>");
			out.write("Your Name : " + pname + "<br/>");
			out.write("Your Last : " + pLastname + "<br/>");
			out.write("</body></html>");
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
