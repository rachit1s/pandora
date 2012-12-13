package transbit.tbits.autovue.connector;

import java.util.Hashtable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import com.cimmetry.vuelink.authentication.Authorization;
import com.cimmetry.vuelink.authentication.AuthorizationException;
import com.cimmetry.vuelink.context.GenericContext;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class ActionContext extends GenericContext {

   	// log4j Logger for ISDKContext class
    private static final Logger m_logger = LogManager.getLogger(ActionContext.class);

    public ActionContext() {
        super();   
        // TODO     
    }

    static public String getStaticParameter(String paramName) {
		if(m_initParameters.containsKey(paramName)) {
			return (String) m_initParameters.get(paramName);
		}
		return null;
	}
	/**
	 * Gets the DMS backend API
	 *
	 * @return the DMS backend object
	 * @exception VuelinkException if an exception occurs
	 */
	public DMSBackendImp getBackendAPI() throws VuelinkException {
		if (m_backend == null) {
			throw new VuelinkException(DMS_ERROR_CODE_ERROR,
			   "Backend API not registered");
		}
		
		return (DMSBackendImp)m_backend;	
	}

	/**
	 * Finds the backend session object corresponding the the {@link com.cimmetry.vuelink.session.DMSSession}.
	 * Creates a new backend session if an existing session cannot be found
	 * 
	 * @param session
	 *          The DMS session to find a Backend session for. In simple cases
	 *         	there will be a 1-1 mapping between DMSSessions and
	 *          BackendSessions, but in more complex scenarios, a single
	 *          BackendSession object could span multiple DMSsessions.
	 * @return 	the appropriate Backend Session object.
	 * 
	 * @see 	com.cimmetry.vuelink.session.DMSSession
	 */
	public DMSBackendSessionImp getBackendSession(DMSSession session)throws AuthorizationException {
		m_logger.info("DMSSessionID : " +session.getID());
		
		// Get Backend session from DMSSession if it has been put there before
		if (session.getAttribute("BackendSession") != null) {
			return (DMSBackendSessionImp)session.getAttribute("BackendSession");
		}

		// No Backend session exists, establish new connection
		Hashtable<String,Object> connectInfo = new Hashtable<String,Object>();
		// TODO Set connecting information to login to DMS
		connectInfo.put("user", "" /*session.getServletRequest().getSession().getAttribute("user")*/);
		
		// Connect to the backend
		DMSBackendSessionImp backendSession = (DMSBackendSessionImp)m_backend.connect(connectInfo);
		
		// Save the BackendSession in DMSSession
		session.setAttribute("BackendSession",backendSession); 
		return backendSession;
	}

	// Similar to the above API with additional information available to login to DMS
	/**
	 * Finds the backend session object corresponding the the {@link com.cimmetry.vuelink.session.DMSSession}.
	 * Creates a new backend session if an existing session cannot be found
	 * 
	 * @param session
	 *          The DMS session to find a Backend session for. In simple cases
	 *         	there will be a 1-1 mapping between DMSSessions and
	 *          BackendSessions, but in more complex scenarios, a single
	 *          BackendSession object could span multiple DMSsessions.
	 *        query
	 *          DMSQuery object passed in request , it is useful when Authorization block is needed
	 *          to create the backend session at the first time
	 * @return 	the appropriate Backend Session object.
	 * 
	 * @see 	com.cimmetry.vuelink.session.DMSSession 
	 */
	public DMSBackendSessionImp getBackendSession(
			DMSSession session, 
			DMSQuery query
			) throws AuthorizationException {
		m_logger.debug("DMSSessionID : " +session.getID());
		
		// Get BackendSession from DMSSession if it has been put there before
		if (session.getAttribute("BackendSession") != null) {
			return (DMSBackendSessionImp)session.getAttribute("BackendSession");
		}	

		m_logger.debug("Creating backendSession for DMSSessionID : " +session.getID());		
		// No backend session exists yet. Establish new connection to DMS and create new backend session.
		Hashtable<String,Object> connectInfo = new Hashtable<String,Object>();
		String user = (String) session.getServletRequest().getSession().getAttribute("user");
		connectInfo.put("user", "");
		// Connect to the backend DMS and return the BackendSession
		DMSBackendSessionImp backendSession = (DMSBackendSessionImp)m_backend.connect(connectInfo);
		
		// Put the BackendSession to DMSSession
		session.setAttribute("BackendSession",backendSession); 
		// TODO Put authorization information to DMSSession, for example, username, if needed.
		
		return backendSession;		
	} 
}