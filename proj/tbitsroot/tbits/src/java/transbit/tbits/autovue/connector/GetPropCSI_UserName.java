package transbit.tbits.autovue.connector;

/**
 * Returns a document name
 */

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.DMSGetPropAction;
import com.cimmetry.vuelink.propsaction.DMSProperty;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class GetPropCSI_UserName implements DMSGetPropAction<ActionContext> {
	
	/** log4j logger for the GetPropCSI_DocName class*/
	private static final Logger m_logger = LogManager.getLogger(GetPropCSI_UserName.class);

	/*
	 * Returns the document name property
	 * @see com.cimmetry.vuelink.core.DMSGetPropAction#execute(com.cimmetry.vuelink.context.DMSContext, com.cimmetry.vuelink.session.DMSSession, com.cimmetry.vuelink.query.DMSQuery, com.cimmetry.vuelink.core.DMSArgument[], com.cimmetry.vuelink.core.Property)
	 */
	public DMSProperty execute(ActionContext context, DMSSession session,
			DMSQuery query, DMSArgument[] args, Property property)
			throws VuelinkException {
		
		final String username = (String) session.getServletRequest().getSession().getAttribute("user");
		/*
                if (username == null || username.trim() == "") {
                    m_logger.info("username is null or empty");
                    m_logger.info("Session ID=" + session.getID());
                    throw new BasicAuthorizationException(
                                    DMSDefs.ERROR_CODE_DMS_AUTHORIZATIONFAILURE,
                                    DMSDefs.DMS_ERROR_MSG_AUTHORIZATION,
                                    "Please enter valid Filesys username/password ",
                                    "",
                                    Base64.encode(DecrypterFactory.getDecrypter().getPublicKey())
                                    );
                }
                */
		m_logger.info("got username:" + username);
		DMSProperty retProp = new DMSProperty(DMSProperty.CSI_UserName, username);
		return retProp;
	}
}
