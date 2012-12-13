package transbit.tbits.autovue.connector;

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
 * Returns the CSI_Renditions property
 * 
 */
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class GetPropCSI_Renditions implements
		DMSGetPropAction<ActionContext> {
		
	/** log4j logger for GetPropCSI_Renditions class*/
	private static final Logger m_logger = LogManager.getLogger(GetPropCSI_Renditions.class);
	
	/*
	 * Returns the CSI_Renditions property
	 * @see com.cimmetry.vuelink.core.DMSGetPropAction#execute(com.cimmetry.vuelink.context.DMSContext, com.cimmetry.vuelink.session.DMSSession, com.cimmetry.vuelink.query.DMSQuery, com.cimmetry.vuelink.core.DMSArgument[], com.cimmetry.vuelink.core.Property)
	 */
	public DMSProperty execute(ActionContext context, DMSSession session,
			DMSQuery query, DMSArgument[] args, Property property)
			throws VuelinkException {
		//DMSProperty(DMSProperty.CSI_Renditions, new String[] {"PCRS_TIF","CSI_META"}, null)
//		final FilesysDMSDocID docID = new FilesysDMSDocID().String2DocID(query.getDocID());
//		
//		
//		String sValidateMeta = context.getInitParameter("ValidateStreamingFile"); 
		
    	String sRendition = "PCVC_PDF"; // "PCRS_TIF;PCRS_GP4;PCRS_EPS;PCRS_PCL;PCRS_PCX;PCVC_PDF;PCRS_RLC;PCRS_BMP;CSI_META";

   		String[] aRenditionList = sRendition.split(";");
    	
   		
//   		DMSProperty[] rendition = null;
   		
//    	if (sValidateMeta != null && sValidateMeta.equalsIgnoreCase("false")) { //no streaming file validation
//			m_logger.debug("No StreamingFile Validation: ValidateStreamingFile option is set to false in web.xml");
//    	} else {
//    		rendition = buildRenditionProperty(context.getBackendAPI(),
//    											context.getBackendSession(session, query), 
//    											docID);
//		}
    	return new DMSProperty(DMSProperty.CSI_Renditions, aRenditionList , null);
	}
	
	
	/*
	 * Builds the rendition docID property
	 */
//	private DMSProperty[] buildRenditionProperty(FilesysDMSBackend be, DMSBackendSession beSession, DocID docID)
//	  throws VuelinkException{
//		
//		FilesysDMSDocID rendDocIds = (FilesysDMSDocID)be.getMetaRendition(beSession, docID);
//		
//		if (rendDocIds == null) return null; 
//		// 
//		DMSProperty[] metaRend = new DMSProperty[1];
//		metaRend[0] = new DMSProperty(DMSProperty.CSI_DocID, rendDocIds.DocID2String());	
//		m_logger.debug("got the docID: " + metaRend);
//		return metaRend;
//	}
}