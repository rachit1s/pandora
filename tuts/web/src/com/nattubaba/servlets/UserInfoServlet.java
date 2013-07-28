package com.nattubaba.servlets;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	{
		// do nothing
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	{
		String myFirstName = req.getParameter("myFirstName");
		String myLastName = req.getParameter("myLastName");
		
		// store this data to your database.
		
		try {
			Writer out = res.getWriter();
			out.write("<html> <title>Your User Info</title> <body>");
			out.write("Your Name : " + myFirstName + "<br/>");
			out.write("Your Last : " + myLastName + "<br/>");
			out.write("</body></html>");
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		RequestDispatcher reqdis = req.getRequestDispatcher("/servlet/userInfo?name=" + myFirstName);
//		try {
//			reqdis.forward(req, res);
//		} catch (ServletException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
