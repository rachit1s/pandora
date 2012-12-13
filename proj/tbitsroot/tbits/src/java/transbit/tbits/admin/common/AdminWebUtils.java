package transbit.tbits.admin.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.authentication.AuthConstants;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import com.google.gson.Gson;

/**
 * This servlets functions as the static methods of a class. Just that it is on
 * the server. All it does is provides the Utils functions to be used by the
 * clients. This should be mapped to a url such as adminwebutils.* so that it
 * can cater to wide range of functions. All it would do is call a function
 * correspondng to * from this class and return the data in JSON format.
 * 
 * This would supply the data corresponding to the Administration panel layout.
 */
public class AdminWebUtils extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdminWebUtils() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try
		{
		System.out.println("Admin web utils was called.");
		String nextUrlPart = "";
		String pathInfo = request.getServletPath().substring(1);
		String parts[] = pathInfo.split("\\.");
		if (parts.length > 1) {
			nextUrlPart = parts[parts.length - 2];
		}

		if (nextUrlPart != null) {
			if (nextUrlPart.equals("getbalist")) {
				ArrayList<BusinessArea> bas = null;
				ArrayList<TinyBA> tba = new ArrayList<TinyBA>();
				try {
					bas = BusinessArea.getAllBusinessAreas();
					for (BusinessArea ba : bas) {
						tba.add(new TinyBA(ba.getSystemPrefix(), ba
								.getDisplayName()));
					}
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Gson gson = new Gson();
				response.getWriter().print(gson.toJson(tba));
				return;
			} else if (nextUrlPart.equals("getmenu")) {
				response.getWriter().print(NavMenu.getInstance().toJson());
				return;
			} else if (nextUrlPart.equals("getsettings")) {
				Gson gson = new Gson();
				User user = null;
				try {
					user = WebUtil.validateUser(request);

					String userLogin = user.getUserLogin();

					boolean displayLogout = false;
					if (request.getAuthType() == AuthConstants.AUTH_TYPE)
						displayLogout = true;

					String contextPath = request.getContextPath();

					Hashtable<String, String> settings = new Hashtable<String, String>();
					settings.put("userLogin", userLogin);
					settings.put("displayLogout", Boolean
							.toString(displayLogout));
					settings.put("contextPath", contextPath);
					String settingsStr = gson.toJson(settings);
					System.out.println("Settings: " + settingsStr);
					response.getWriter().println(settingsStr);

				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TBitsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			} else
				response.sendError(1);
		} else
			response.sendError(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
