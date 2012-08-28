package com.oreilly.nattubaba.servlet;

public class VersionDetector {
	static String servletVersion;
	static String javaVersion;

	public static String getServletVersion() {
		if (servletVersion != null) {
			return servletVersion;
		}
		// javax.servlet.http.HttpSession was introduced in Servlet API 2.0
		// javax.servlet.RequestDispatcher was introduced in Servlet API 2.1
		// javax.servlet.http.HttpServletResponse.SC_EXPECTATION_FAILED was
		// introduced in Servlet API 2.2
		// javax.servlet.Filter is slated to be introduced in Servlet API 2.3
		String ver = null;
		try {
			ver = "1.0";
			Class.forName("javax.servlet.http.HttpSession");
			ver = "2.0";
			Class.forName("javax.servlet.RequestDispatcher");
			ver = "2.1";
			Class.forName("javax.servlet.http.HttpServletResponse")
					.getDeclaredField("SC_EXPECTATION_FAILED");
			ver = "2.2";
			Class.forName("javax.servlet.Filter");
			ver = "2.3";
		} catch (Throwable t) {
		}
		servletVersion = ver;
		return servletVersion;
	}

	public static String getJavaVersion() {
		if (javaVersion != null) {
			return javaVersion;
		}
		// java.lang.Void was introduced in JDK 1.1
		// java.lang.ThreadLocal was introduced in JDK 1.2
		// java.lang.StrictMath was introduced in JDK 1.3
		String ver = null;
		try {
			ver = "1.0";
			Class.forName("java.lang.Void");
			ver = "1.1";
			Class.forName("java.lang.ThreadLocal");
			ver = "1.2";
			Class.forName("java.lang.StrictMath");
			ver = "1.3";
		} catch (Throwable t) {
		}
		javaVersion = ver;
		return javaVersion;
	}
}