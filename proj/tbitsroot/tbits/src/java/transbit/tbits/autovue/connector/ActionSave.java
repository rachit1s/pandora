package transbit.tbits.autovue.connector;

/*
 * Created on Jun 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import transbit.tbits.api.IAutovueOnRendition;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.plugin.PluginManager;

import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.DocID;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.io.Base64Decoder;
import com.cimmetry.vuelink.io.Base64DecoderInputStream;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.DMSAction;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSBackendSession;
import com.cimmetry.vuelink.session.DMSSession;

/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class ActionSave
	implements DMSAction<ActionContext>, DMSDefs {

	public static TBitsLogger logger = TBitsLogger.getLogger("transbit.tbits.autovue.connector");
	public final String TYPE_TEXT   = "text/plain";
	public final String TYPE_STREAM = "application/octet-stream";

    private static final Logger m_logger = LogManager.getLogger(ActionSave.class);

    /**
     * Executes the Save DMS action.
     * @param  context context defining the environment in which
     *                 to execute the action
     * @param  session session defining the environment in which
     *                 to process that specific query
     * @param  query   query to handle
     * @param  args    list of extra arguments (can be null)
     * @return DMS query result
     */
	public Object execute(final ActionContext    context,
                          final DMSSession    session,
                          final DMSQuery      query,
                          final DMSArgument[] args
                          ) throws VuelinkException {
        /* Sanity checks */
        if (!"save".equalsIgnoreCase(query.getActionName())) {
            throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
                                       "Invalid action name within query: " +
                                       query.getActionName());
        }

        final Property[] props = query.getDMSArgsProperties();

        DMSBackendSession beSession = context.getBackendSession(session,query);

        // REDIRECT SUPPORT start based on whether web.xml defines Redirect_VL_URL or not?
//        String ticket = (String)session.getAttribute("Ticket");
//		if (ticket == null && args == null) { //use ticket to update user/pass
//
//			//if there is any redirect use it
//	    	try {
//	            String redirectURL = ActionContext.getStaticParameter(ActionContext.PARAM_CSI_REMOTE_VUELINK);
//
//		    	//redirect save if URL is provided
//		    	if (!DMSUtil.isNullOrBlank(redirectURL)) {
//		    		String receipt = getReceipt(props);
//
//		        	if (receipt != null && receipt.length()!= 0) {
//		        		return new DMSProperty(Property.CSI_DocID, receipt);
//		        	}
//		        	String username = (String)session.getAttribute("username");
//		    		String password = (String)session.getAttribute("password");
//
//		    		if (username != null && username.length() > 0 && password != null ) {
//		    			ticket = username.trim()+ "&" + password.trim();
//		  		    }
//
//		    		m_logger.debug("Ticket: " + ticket);
//		    		return DMSUtil.constructRedirectURL(query, redirectURL, ticket);
//		    	}
//	    	}catch (Exception e) {
//	    		m_logger.error("redirecting save faild " + e.toString());
//
//	    	}
//		}
		//REDIRECT SUPPORT finish

        /* Now the real work... */
        /* Get file name */
		if (args == null || args.length != 1) {
            throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
                                       "Expected one argument to Save DMS request, got " +
                                       args.length);
        }
        final DMSArgument fileArg = args[0];
		String type = fileArg.getType();
		if ( type != null ) {
			// Just in case, we have extra spaces before or after
			type.trim();
		}
        if (type == null ||
			(!TYPE_TEXT.equals(fileArg.getType()) &&
			 !TYPE_STREAM.equals(fileArg.getType()) ) ) {
            throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
                                       "Expected \""+TYPE_TEXT+"\" or \""+TYPE_STREAM+"\" argument type, got " +
                                       fileArg.getType());
        }

        String sUploadFile = fileArg.getName();
        if (!"file".equals(sUploadFile) && !"xml".equals(sUploadFile)) {
            throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
                                       "Expected \"file\" or \"xml\" argument, got " +
                                       sUploadFile);
        }

        m_logger.info("sUploadFile = " + sUploadFile);

		// There are two cases here - for a "Save" operation (at least for
		// markups),
		// We'll recieve the DocID of the markup as the property CSI_DocID, and
		// the name of the
		// markup as the property "name".
		//
		// For a "Save-As" (again for markups at least), We'll receive
		// CSI_BaseDocID containing the base document to save the markup to
		// and CSI_DocName will contain the name of the markup, and
		// CSI_MarkupType will contain the type. (which we don't use,
		// currently.)

        boolean bCompressed = false;
//        boolean bSaveChat = false;      // "True" if saving chat content for a meeting
//		boolean bReadOnly = false; /* true if it is a read-only markup */
		String  rendType  = null;
		DocID   baseID = null;  /* if this is non-null after parsing the properties, we're doing a Save-As */
		DocID   saveID = null;  /* if this is non-null after parsing the properties, we're doing a Save */
		String  docName = null; /* this will contain the value of 'name' for Save, or 'CSI_DocName' for Save-As */
//		String  markType = null ; /* not null if the mark type is specified */
//
//
        if (props != null) {
            for (int i = 0; i < props.length; i++) {
				//System.out.println("["+i+"] = "+props[i]);
				final Property prop = props[i];
				final String   name = prop.getName();
				if ( Property.CSI_Compression.equals(name) ) {
					try {
						bCompressed = "true".equalsIgnoreCase(prop.getValue());
					} catch (Exception ex) {
						bCompressed = false;
					}
				} 
        else if ( Property.CSI_BaseDocID.equals(name) ) {
					baseID = new TbitsDocID().String2DocID(prop.getValue()); //DMSUtil.getDocID(prop.getValue());
				} 
//					else if ( Property.CSI_MarkupType.equals(name) ) {
//					markType = prop.getValue();
//				}
//				else if ( Property.PROP_DOC_READONLY.equals(name) ) {
//					try {
//						bReadOnly = prop.getValue().equalsIgnoreCase("true");
//					} catch (Exception ex) {
//						bReadOnly = false;
//					}
//				}
				else if (Property.CSI_DocID.equals(name)) {
					saveID = new TbitsDocID().String2DocID(prop.getValue()); //DMSUtil.getDocID(prop.getValue());
				}
					else if ( Property.CSI_DocName.equals(name)) {
					docName = prop.getValue();
				} else if ( "name".equals(name)) {
					docName = prop.getValue();
				} 
				else if ( Property.CSI_RenditionType.equals(name) ){
					rendType = prop.getValue();
				}
//					else if (Property.CSI_ClbDocType.equals(name)) {
//			        if (prop.getValue() != null && prop.getValue().equalsIgnoreCase("chat")) {
//			        	bSaveChat = true;
//			        }
//				}
//
//
            }
        }
//
//        if (!bSaveChat) {
//        	// BUG 59500
			if (baseID == null && saveID == null) {
				throw new VuelinkException(DMS_ERROR_CODE_ERROR, "No document ID is provided for saving action.");
			}
//
//	        /* Establish the filename to use for the new file */
//
//
	 		if (docName == null) {  // if no name
	 			if (baseID != null) {
	 				docName =  ((TbitsDocID) baseID).getName();
	 			} else {
	 				docName =  ((TbitsDocID) saveID).getName();
	 			}
	 		}
//        }

		/** Upload the file */
		DocID newDocID = null;
		try {
			InputStream fIn = null;
			if ("file".equals(sUploadFile) &&
				fileArg.get() instanceof File ) {
				final File file = (File) fileArg.get();

				fIn = new FileInputStream(file);
				if ( TYPE_TEXT.equals(fileArg.getType()) ) {
					System.out.println("decoding input stream");
					fIn = new Base64DecoderInputStream(fIn);
				}
			} else if ( "xml".equals(sUploadFile) &&
						fileArg.get() instanceof String ) {
				try
				{
					fIn = new Base64Decoder(new StringReader((String) fileArg.get()));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			} else {
				throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
										   "Expected File or String argument, got " +
										   fileArg.get());
			}
			if ( bCompressed ) {
				//System.out.println("Decompressing stream");
				fIn = new GZIPInputStream(fIn);
			}
			DMSBackendImp be = context.getBackendAPI();

			// Save chat
//			if (bSaveChat) {
//	            String clbData = query.getClbSessionData();
//	            String clbSessionID = clbData;
//	            docName = context.getInitParameter("RootDir") + File.separator + "Meeting" + File.separator + "chat_" + clbSessionID + ".txt";
//	            m_logger.debug(" for session " + clbSessionID + " to: " + docName);
//	            return be.saveChat(beSession, docName, fIn);
//			}
//			else
			if(rendType != null) {  // saving rendtion (new or existing)
				// saving StreamingFile can be enabled/disabled by this parameter in web.xml
				String flag =  context.getInitParameter("StreamingFileCheckin");
				if (flag != null && flag.equalsIgnoreCase("false") && rendType.equalsIgnoreCase(Property.CSI_META)) {
				m_logger.debug("No StreamingFile Checkin: StreamingFileCheckin option is set to false in vuelink properties");
				return null;
				}
//				newDocID = be.saveRendition(beSession,baseID, saveID, docName, rendType, fIn);
//				String did = saveFileAndUpdate(query,fIn);
				String did = null ;
				try {
					did = executePlugins(context, session, query, args, fIn);
				} catch (Exception e) {
					e.printStackTrace();
					throw new VuelinkException(DMS_ERROR_CODE_ERROR,"PluginExecutionException", e);
				}
				newDocID = new TbitsDocID(did);
			} 
			else {// saving markup (new or existing)
				newDocID = be.saveMarkup(beSession,baseID, saveID, docName, fIn);
			}

		}catch (FileNotFoundException ex) {
	        m_logger.info("FileNotFoundException " + ex);
            //throw new VuelinkException(DMS_ERROR_CODE_ERROR,
                                       //DMS_ERROR_MSG_SETFILECONTENT,
									  // ex);
			throw new VuelinkException(DMS_ERROR_CODE_ERROR,
										ex.getMessage(),
										ex);
		}catch (IOException ex) {
			m_logger.info("IOException " + ex);
            throw new VuelinkException(DMS_ERROR_CODE_ERROR,
                                       ex.getMessage(),
									   ex);
		} 
		return newDocID;
	}

	
	private String executePlugins(final ActionContext    context,
            final DMSSession    session,
            final DMSQuery      query,
            final DMSArgument[] args, InputStream fIn) throws Exception {
		ArrayList<Class> pluginClasses = PluginManager.getInstance().findPluginsByInterface(IAutovueOnRendition.class.getName());
		ArrayList<IAutovueOnRendition> plugins = new ArrayList<IAutovueOnRendition>(); 
		String output = null; 
		
		if( null != plugins )
		{
			for( Class klass : pluginClasses )
			{
				Object object = null;;
				try {
					object = klass.newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				IAutovueOnRendition aor = (IAutovueOnRendition) object ;
				if( null != aor )
					plugins.add(aor);
			}
			
			Collections.sort(plugins, new Comparator<IAutovueOnRendition>(){

				@Override
				public int compare(IAutovueOnRendition plug1,
						IAutovueOnRendition plug2) 
				{
					if (plug1.getOrder() - plug2.getOrder() > 0 )
						return 1 ;
					else if(plug1.getOrder() - plug2.getOrder() < 0 )
						return -1 ;
					
					return 0; 
				}
			}
			);
			
			for(IAutovueOnRendition plugin : plugins )
			{
				logger.info("Executing plugin Name : " + plugin.getName() );
				logger.info("\t plugin Desciption : " + plugin.getDescription() );
				output = (String) plugin.execute(context, session, query, args, fIn);
			}
		}
		
		return output;
	}
	
	private String getReceipt(Property [] props){

		String receipt = null;
		if (props != null) {
            for (Property p: props) {
				if ( "Receipt".equalsIgnoreCase(p.getName()) ) {
					receipt = p.getValue();
					break;
				}
            }
        }
		return receipt;
	}
}
