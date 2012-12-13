package transbit.tbits.authentication;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import transbit.tbits.webapps.WebUtil;

public class LogOut extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		handleRequest(req, res);
	}
	public void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException
	{
		handleRequest(req, res);
	}
	private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		HttpSession session = req.getSession(false);
		if(session != null)
			session.invalidate();
		req.setAttribute(AuthConstants.ERROR_MESSAGE, "You have successfully logged out!");
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/loginpage");
		dispatcher.forward(req, res);	
	}
}