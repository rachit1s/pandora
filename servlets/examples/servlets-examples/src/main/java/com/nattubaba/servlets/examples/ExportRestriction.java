package com.nattubaba.servlets.examples;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class ExportRestriction extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		// ...Some introductory HTML...
		// Get the client's hostname
		String remoteHost = req.getRemoteHost();
		System.out.println("Your are from : " + remoteHost);
		// See if the client is allowed
		if (!isHostAllowed(remoteHost)) {
			out.println("Access <BLINK>denied</BLINK> <br/> " + "Your are from : " + remoteHost);
		} else {
			out.println("Access granted <br/>" + "Your are from : " + remoteHost);
			// Display download links, etc...
		}
	}

	// Disallow hosts ending with .cu, .ir, .iq, .kp, .ly, .sy, and .sd.
	private boolean isHostAllowed(String host) {
		return (!host.endsWith(".cu") && !host.endsWith(".ir")
				&& !host.endsWith(".iq") && !host.endsWith(".kp")
				&& !host.endsWith(".ly") && !host.endsWith(".sy") && !host
					.endsWith(".sd"));
	}
}