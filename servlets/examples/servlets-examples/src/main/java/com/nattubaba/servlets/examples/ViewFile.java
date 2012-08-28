package com.nattubaba.servlets.examples;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.oreilly.servlet.ServletUtils;


public class ViewFile extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		// Use a ServletOutputStream because we may pass binary information
		ServletOutputStream out = res.getOutputStream();
		// Get the file to view
		String file = req.getPathTranslated();
		// No file, nothing to view
		if (file == null) {
			out.println("No file to view : " + req.getQueryString());
			return;
		}
		// Get and set the type of the file
		String contentType = getServletContext().getMimeType(file);
		res.setContentType(contentType);
		// Return the file
		try {
			ServletUtils.returnFile(file, out);
		} catch (FileNotFoundException e) {
			out.println("File not found");
		} catch (IOException e) {
			out.println("Problem sending file: " + e.getMessage());
		}
	}
}