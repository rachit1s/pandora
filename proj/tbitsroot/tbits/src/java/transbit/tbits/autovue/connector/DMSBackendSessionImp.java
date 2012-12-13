package transbit.tbits.autovue.connector;

import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.cimmetry.vuelink.session.DMSBackendSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class DMSBackendSessionImp implements DMSBackendSession
{
	private static long id_count = 0 ;
	HashMap<String,Object> map = new HashMap<String,Object>();
	private long id = 0 ;
	String user = null;
	
	public DMSBackendSessionImp(String user)
	{
		id = id_count++;
		this.user = user;
	}
	
	public static synchronized DMSBackendSession newInstance(String user)
	{
		return new DMSBackendSessionImp(user);
	}
	
	@Override
	public Object getAttribute(String arg0) {
		return map.get(arg0);
	}

	@Override
	public String getID() {
		return id+"";
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return null;
	}

	@Override
	public void removeAttribute(String arg0) {
		map.remove(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		map.put(arg0,arg1);
	}
}
