package com.nattubaba.servlets.examples;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NormalServlet extends HttpServlet
{
	int count = 0;
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	{
		count++ ;
		PrintWriter pw = null;
		try {
			pw = res.getWriter();
			pw.println("the count is : " + count + " for servlet : " + this);
			System.out.println("the count is : " + count + " for servlet : " + this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if( null != pw)
			{
				pw.flush();
				pw.close();
			}
		}
	}
}
