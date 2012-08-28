package com.nattubaba.servlets.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import com.oreilly.servlet.ServletUtils;

public class GetFile extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		try
		{
		PrintWriter out = resp.getWriter();
		resp.getWriter();
		
		req.getReader();
		req.getReader();
		
		String contextName = req.getParameter("contextName");
		String resourceName = req.getParameter("resourceName");

		ServletContext context = getServletContext();
		if (null != contextName && !"".equals(contextName))
			context = getServletContext().getContext(contextName);

		out.println("queryString : " + req.getQueryString() );
		out.println("pathInfo : " + req.getPathInfo());
		out.println("URL : " + req.getRequestURL().toString() );
		out.println("URI : " + req.getRequestURI() );
		out.println("HttpUtils URL : " + HttpUtils.getRequestURL(req));
		out.println("scheme : " + req.getScheme());
		out.println("protocol : " + req.getProtocol());
		out.println("method : " + req.getMethod());
		
		out.println("Request Headers:");
		out.println();
		Enumeration names = req.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			Enumeration values = req.getHeaders(name); // support multiple
														// values
			if (values != null) {
				while (values.hasMoreElements()) {
					String value = (String) values.nextElement();
					out.println(name + ": " + value);
				}
			}
		}
		
		if (null == context) {
			out.println("The mentioned context was not found.");
			out.flush();
			out.close();
			return;
		}

		URL url = context.getResource(resourceName);
		if (url != null) {
			returnURL(url, out);
			return;
		} else {
			out.println("The mentioned resource could not be converted to URI.");
			out.flush();
			out.close();
			return;
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	// Sends URL contents to a PrintWriter
	public static void returnURL(URL url, PrintWriter out) throws IOException {
		// Determine the URL's content encoding
		URLConnection con = url.openConnection();
		con.connect();
		int contentLength = con.getContentLength(); // not all support
		String contentType = con.getContentType(); // not all support
		long expiration = con.getExpiration(); // not all support
		long lastModified = con.getLastModified(); // not all support
		String encoding = con.getContentEncoding();
		System.out.println("contentLength : " + contentLength + "\ncontentType : " + contentType + "\nexpiration : " + expiration + "\nlastModified "+lastModified);
		
		// Construct a Reader appropriate for that encoding
		BufferedReader in = null;
		if (encoding == null) {
			in = new BufferedReader(new InputStreamReader(url.openStream()));
		} else {
			in = new BufferedReader(new InputStreamReader(url.openStream(),
					encoding));
		}
		char[] buf = new char[4 * 1024]; // 4Kchar buffer
		int charsRead;
		while ((charsRead = in.read(buf)) != -1) {
			out.write(buf, 0, charsRead);
		}
	}

}
