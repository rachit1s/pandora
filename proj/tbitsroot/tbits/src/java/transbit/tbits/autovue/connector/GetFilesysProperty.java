package transbit.tbits.autovue.connector;

/**
 * Rertuns a set of attributes of a document (see documentObj class)
 */

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.DocID;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.propsaction.DMSProperty;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSBackendSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class GetFilesysProperty  implements DMSDefs {
	
	/** log4j logger for the GetFilesysProperty class*/
	private static final Logger m_logger = LogManager.getLogger(GetFilesysProperty.class);
	
	/*
	 * Returns attributes of a document
	 */
//	protected DMSProperty getAttrs(final DMSBackendImp be, DMSBackendSession beSession,
//	    final DMSQuery query, DocID docID) throws VuelinkException {
//
//		DMSProperty attrs = (DMSProperty) query.getQueryData("attrs"); //if anything stored by setQueryData
//	    if (attrs == null) {
//	    	attrs = be.getAttributes(beSession, docID);
//		    m_logger.info("got document attributes " + attrs);
//	    	query.setQueryData("attrs", attrs);
//	    }
//	    return attrs;
//	 }
}
