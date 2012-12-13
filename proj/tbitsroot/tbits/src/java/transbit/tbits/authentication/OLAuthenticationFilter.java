/**
 * 
 */
package transbit.tbits.authentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.common.DatabaseException;

/**
 * @author Lokesh
 *
 */
public class OLAuthenticationFilter implements Filter {
	FilterConfig filterConfig = null;
	/* 
	 * Sets filterConfig to null 
	 */

	public void destroy() {
		this.filterConfig = null;
	}

	/* 
	 * Checks for username and password, if successful continues to fetch data
	 */

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		//HttpSession session = req.getSession(true);		
		
		//OutputStreamWriter out = new OutputStreamWriter (res.getOutputStream());		
		
		String username = req.getParameter("usr");
		if ((username == null) || (username.trim().equals("")))
		{
			res.getWriter().print("false");
			return;
		}			
		else
			username = username.trim();

		String password = req.getParameter("pwd");
		if ((password == null) || (password.trim() == "")){
			res.getWriter().print("false");
			return;
		}
		else
			password = password.trim();	

		try {
			if(!AuthUtils.validateUser(username, password)){
				res.getWriter().print("false");
				return;
			}
			else
			{
				filterChain.doFilter(req, res);
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

}
