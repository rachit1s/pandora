package com.nattubaba.servlets.examples;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.oreilly.nattubaba.servlet.MultipartRequest;

public class Uploader extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		try {
			// Blindly take it on faith this is a multipart/form-data request
			// Construct a MultipartRequest to help read the information.
			// Pass in the request, a directory to save files to, and the
			// maximum POST size we should attempt to handle.
			// Here we (rudely) write to the current dir and impose 5 Meg limit.
			MultipartRequest multi = new MultipartRequest(req, ".",
					5 * 1024 * 1024);
			out.println("<HTML>");
			out.println("<HEAD><TITLE>UploadTest</TITLE></HEAD>");
			out.println("<BODY>");
			out.println("<H1>UploadTest</H1>");
			// Print the parameters we received
			out.println("<H3>Params:</H3>");
			out.println("<PRE>");
			Enumeration params = multi.getParameterNames();
			while (params.hasMoreElements()) {
				String name = (String) params.nextElement();
				String value = multi.getParameter(name);
				out.println(name + " = " + value);
			}
			out.println("</PRE>");
			// Show which files we received
			out.println("<H3>Files:</H3>");
			out.println("<PRE>");
			Enumeration files = multi.getFileNames();
			while (files.hasMoreElements()) {
				String name = (String) files.nextElement();
				String filename = multi.getFilesystemName(name);
				String type = multi.getContentType(name);
				File f = multi.getFile(name);
				out.println("name: " + name);
				out.println("filename: " + filename);
				out.println("type: " + type);
				if (f != null) {
					out.println("length: " + f.length());
				}
				out.println();
			}
			out.println("</PRE>");
		} catch (Exception e) {
			out.println("<PRE>");
			e.printStackTrace(out);
			out.println("</PRE>");
		}
		out.println("</BODY></HTML>");
	}
}