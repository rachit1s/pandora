package transbit.tbits.plugin;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TbitsRemoteServiceServlet extends RemoteServiceServlet{
//	private String userLogin;
	private HttpServletRequest request;
	
	public TbitsRemoteServiceServlet() {
		super();
	}
//	
//	public void setUserLogin(String userLogin){
//		this.userLogin = userLogin;
//	}
//	
//	public String getUserLogin(){
//		return userLogin;
//	}
	
	

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		if(request != null)
			return request;
		return this.getThreadLocalRequest();
	}
}
