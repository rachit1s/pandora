/*
 * Created on Jun 15, 2005
 *
 * Default action properties
 */
package transbit.tbits.autovue.connector;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.DocID;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.DMSGetPropAction;
import com.cimmetry.vuelink.propsaction.DMSProperty;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;
/**
 * Implementation of the "GetProperties" DMS action.
 */
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class GetPropDefault
	implements DMSGetPropAction<ActionContext>, DMSDefs
	{

	private static final String[] MIME_TYPES =  {"multipart/form-data",
												 "application/octet-stream",
												 "application/x-gzip"};

	/** log4j logger for GetPropDefault class*/
	private static final Logger m_logger = LogManager.getLogger(GetPropDefault.class);

	/**
	 * Executes the GetProperties DMS action.
     * @param  context [in] context defining the environment in which
     *                      to execute the action
     * @param  session [in] session defining the environment in which
     *                      to process that specific query
     * @param  query   [in] query to handle
     * @param  args    [in] list of extra arguments (can be null)
     * @return DMS query result
     */
	public DMSProperty execute(final ActionContext    context,
                          final DMSSession    session,
                          final DMSQuery      query,
                          final DMSArgument[] args,
                          final Property property
                          ) throws VuelinkException {

//		final DocID docID = new FilesysDMSDocID().String2DocID(query.getDocID());
//
//        m_logger.info("deserialized docID=" + docID);
//
//        // VueLinkID
//        if ("VueLinkID".equals(property.getName())) {
//    	   return new DMSProperty("VueLinkID", "");
//        }else if ("CSI_MIMETypes".equals(property.getName())) {
//        	return new DMSProperty("CSI_MIMETypes", MIME_TYPES);
//    	} 
//
//        DMSProperty  attrs = (DMSProperty) query.getQueryData( "attrs");
//        if (attrs == null && docID != null) {
//        	attrs = context.getBackendAPI().getAttributes(context.getBackendSession(session, query), docID);
//        	query.setQueryData("attrs", attrs);
//        }
        DMSProperty retProp=null;

//        final String propName = property.getName();
//        if (Property.CSI_AllowBrowse.equals(propName)) {
//    		return new DMSProperty(Property.CSI_AllowBrowse,
//    				new Boolean(true));
//    		
//    	} else if (Property.CSI_AllowSearch.equals(propName)) {
//    		return new DMSProperty(Property.CSI_AllowSearch,
//    				new Boolean(true));
//    		
//    	} else if (Property.CSI_UserName.equals(propName)) {
//    		DMSProperty DMSArgs = new DMSProperty("Args",query.getDMSArgsProperties());
//    		return new DMSProperty(Property.CSI_UserName, 
//    				DMSArgs.getFirstChildValue(Property.CSI_UserName));
//    		
//    	} else if (DMSProperty.CSI_Redirected.equals(propName)) {
//    		String redirectURL = ActionContext.getStaticParameter(ActionContext.PARAM_CSI_REMOTE_VUELINK);
//    		boolean redirected = false;
//    		if(redirectURL != null && redirectURL.length()  > 0){
//    			redirected = true;
//    		}
//    			
//    		return  new DMSProperty(DMSProperty.CSI_Redirected,
//        				new Boolean(redirected));
//    	} 
//    		
//    	retProp = (DMSProperty)attrs.getFirstChildWithName(propName);
//    	
//    	if (retProp == null) {
//    		m_logger.error("Unsupported property: " + propName);
//    		throw new VuelinkException(DMSDefs.ERROR_CODE_DMS_GETPROPERTIES,
//    				"Unsupported property: " + propName);
//    	}
/*    		 
        	if (Property.CSI_Version.equals(propName)) {
            		retProp = new DMSProperty(Property.CSI_Version,
            				attrs.getFirstChildWithName("Version"));
        	}else if ("VersionsNumber".equals(propName)) {
                		retProp = new DMSProperty("VersionsNumber",
                				attrs.get("VersionsNumber"));
        	}else if (Property.CSI_DocAuthor.equals(propName)) {
        		retProp = new DMSProperty(Property.CSI_DocAuthor,
        				attrs.get("Author"));
        				
        	else{
        		// check if it matches any DMS side attribute 
        		if (attrs.containsKey(property.getName())){
        			retProp = new DMSProperty(propName,attrs.get(propName));
        			// check if has any picklist associated 
        		} else {  //check if matches any DMS Attributes
        		
        			m_logger.info("Unsupported property: " + propName);
        		}
        	}
        }
*/
    	// check for pick list
//    	retProp =  context.getBackendAPI().replaceWithPickListIfApplies(context.getBackendSession(session, query),retProp);
        return retProp;
	}
}