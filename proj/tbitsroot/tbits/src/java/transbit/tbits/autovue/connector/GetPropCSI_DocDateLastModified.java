package transbit.tbits.autovue.connector;
/**
 * Returns the last modification date of a document
 */

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cimmetry.vuelink.defs.DocID;
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
public class GetPropCSI_DocDateLastModified implements DMSGetPropAction<ActionContext> {
	
	/** log4j logger for the GetPropCSI_DocDateLastModified class*/
	private static final Logger m_logger = LogManager.getLogger(GetPropCSI_DocDateLastModified.class);

	/*
	 * Returns last modification date of a document
	 * @see com.cimmetry.vuelink.core.DMSGetPropAction#execute(com.cimmetry.vuelink.context.DMSContext, com.cimmetry.vuelink.session.DMSSession, com.cimmetry.vuelink.query.DMSQuery, com.cimmetry.vuelink.core.DMSArgument[], com.cimmetry.vuelink.core.Property)
	 */
	public DMSProperty execute(ActionContext context, DMSSession session,
			DMSQuery query, DMSArgument[] args, Property property)
			throws VuelinkException {

//		final DocID docID = new FilesysDMSDocID().String2DocID(query.getDocID());
//		m_logger.info("got docID:" + docID);
//		DMSProperty attrs = getAttrs(context.getBackendAPI(),context.getBackendSession(session, query),query, docID);
		DMSProperty retProp = new DMSProperty(Property.CSI_DocDateLastModified,	(new Date())/*attrs.getFirstChildValue("DateLastModified")*/);
		return retProp;
	}
}
