package transbit.tbits.autovue.connector;
/**
 * Returns the CSI_Markup property
 */
import java.util.Date;
import java.util.Vector;
import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cimmetry.vuelink.defs.DocID;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.property.GUIElementCombo;
import com.cimmetry.vuelink.property.GUIElementEdit;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.DMSGetPropAction;
import com.cimmetry.vuelink.propsaction.DMSProperty;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSBackendSession;
import com.cimmetry.vuelink.session.DMSSession;
import com.cimmetry.vuelink.util.DMSUtil;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class GetPropCSI_Markups extends GetFilesysProperty implements
		DMSGetPropAction<ActionContext> {
	
	/** log4j logger for GetPropCSI_Markups class*/
	private static final Logger m_logger = LogManager.getLogger(GetPropCSI_Markups.class);

	/*
	 * Builds the markup GUI
	 */
	private DMSProperty[] buildMarkupGui(DMSBackendImp be, DMSBackendSession beSession, DocID docID) {

		DMSProperty guiProps[] = new DMSProperty[3];
		DMSProperty DispOptArr[] = new DMSProperty[7];
		DispOptArr[0] = new DMSProperty("AllowDelete","true");
		DispOptArr[1] = new DMSProperty("ShowPreviousVersions","true");
		DispOptArr[2] = new DMSProperty("AllowNew","true");
		// BUG 59599
		DispOptArr[3] = new DMSProperty("AllowImport","true");
		DispOptArr[4] = new DMSProperty("AllowExport","true");
		DispOptArr[5] = new DMSProperty("AllowNewLayers","false");
		DispOptArr[6] = new DMSProperty("AllowModifyLayers","false");

		guiProps[0] = new DMSProperty(DMSProperty.PROP_GUI_DISPLAYOPTS, DispOptArr);

		// For "Markup Files" dialog
		DMSProperty DispArr[] = new DMSProperty[6];
		DispArr[0] = new DMSProperty(Property.CSI_DocName,"20");
		// BUG 59571 59552
		DispArr[1] = new DMSProperty(Property.CSI_MarkupType,"15");
		DispArr[2] = new DMSProperty(Property.CSI_DocSize,"10");
		DispArr[3] = new DMSProperty(Property.CSI_Version,"10");
		DispArr[4] = new DMSProperty("Read-Only","6");
		DispArr[5] = new DMSProperty(Property.CSI_DocDateLastModified,"15");
		
		guiProps[1] = new DMSProperty(DMSProperty.PROP_GUI_DISPLAY, DispArr);
		
		// For "Save Markup File As" dialog
		Property EditArr[] = new Property[3];
		EditArr[0] = new GUIElementEdit("CSI_DocName", "Name", "",false);
		
		// Types of markups
		String comboVals[] = new String[3];
		comboVals[0] = DMSProperty.CSI_MarkupType_Normal;
		comboVals[1] = DMSProperty.CSI_MarkupType_Master;
		comboVals[2] = DMSProperty.CSI_MarkupType_Consolidated;
		EditArr[1] = new GUIElementCombo(DMSProperty.CSI_MarkupType, "Markup Type", DMSProperty.CSI_MarkupType_Normal, comboVals, false);
		
		// Read-only markup option
		String [] opts =  {"false", "true"};
		EditArr[2] = new GUIElementCombo(DMSProperty.CSI_DocReadOnly, "Read-Only", "false", opts, true);
		
		guiProps[2] = new DMSProperty(DMSProperty.PROP_GUI_EDIT, EditArr);
		
		m_logger.debug("got the markup GUI elements: " + guiProps);
		return guiProps;
	}
	
	/*
	 * Builds the content of the markup property (list of markups that will be displayed on
	 * the markup GUI)
	 */
	private Property[] buildMarkupProperty(DMSBackendImp be, DMSBackendSession beSession, DMSQuery query) throws VuelinkException{

//		final DocID docID = new FilesysDMSDocID().String2DocID(query.getDocID());
		DMSProperty guiProps[] = buildMarkupGui(be, beSession, new TbitsDocID(query.getDocID()));
		//Gets the list of markups from the DMS
		Vector<DocInfo> mrkDocIds = be.dmsListMarkups(beSession, new TbitsDocID(query.getDocID()));
		
		DMSProperty markup[] = new DMSProperty[mrkDocIds.size()+1];
		markup[0] = new DMSProperty(DMSProperty.PROP_GUI ,guiProps);

		for (int i = 0; i < mrkDocIds.size(); i++)
		{
			DMSProperty mrkProp[] = new DMSProperty[9];
			mrkProp[0] = new DMSProperty("CSI_DocID", mrkDocIds.get(i).getDocID().toString());
			mrkProp[1] = new DMSProperty("CSI_DocName", mrkDocIds.get(i).getName());
			
			// BUG 59571
			String mrkType = "normal" ;//mrkDocIds.get(i).getFile().getParentFile().getName().toLowerCase().trim();
			
			boolean bReadOnly = false;
			boolean editable = true;
			// Treat asset markup and workflow markup as master
//			if(!mrkType.equalsIgnoreCase("normal")&& !mrkType.equalsIgnoreCase("master") && !mrkType.equalsIgnoreCase("consolidated")){
//				mrkType = "master";
//				String oevfType = mrkDocIds.get(i).getFile().getParentFile().getParentFile().getName().toLowerCase().trim();
//				if(oevfType.equalsIgnoreCase(Markup.ASSETS)){
//					if(!(Boolean)beSession.getAttribute("EditMode") || 
//							((Boolean)beSession.getAttribute("EditMode") && 
//							 !DMSUtil.isNullOrBlank(docID.getWorkflowID()))){
//						editable = false;
//					}
//				}
//			}
//			if (!editable) { // default asset markup in non-editable mode
//				bReadOnly = true;
//			}
//			else { // non-oevf markup
//				File file = mrkDocIds.get(i).getFile();
//				if (file.canWrite() == false) {
//					m_logger.info(file.getAbsolutePath() + " is not writable.");
//					bReadOnly = true;
//				}
//			}
			
			mrkProp[2] = new DMSProperty(Property.CSI_MarkupType, mrkType);
			mrkProp[3] = new DMSProperty(Property.CSI_DocSize, mrkDocIds.get(i).getFile().length()+"");
				
//			DMSProperty attrs = getAttrs(be, beSession,query, docID);
			mrkProp[4] = new DMSProperty(Property.CSI_Version, "");
			
			mrkProp[5] = new DMSProperty(Property.CSI_DocReadOnly, new Boolean(bReadOnly).toString());  // This is needed for AutoVue Server
			mrkProp[6] = new DMSProperty("Read-Only", new Boolean(bReadOnly).toString());
			
			// CSI_DocDateLastModified and CSI_DocAuthor are mandatory for online and offline function.
			// Here the author for the markup is set as the current logged in user, but this is just a sample. 
			// In real world, the author of the markup should be saved in DMS systems. 
			mrkProp[7] = new DMSProperty(Property.CSI_DocDateLastModified, new Date(mrkDocIds.get(i).getFile().lastModified()).toString());
			mrkProp[8] = new DMSProperty(Property.CSI_DocAuthor,"");
			
			markup[i+1] = new DMSProperty(DMSProperty.PROP_MARKUP,mrkProp);
		}
		m_logger.debug("got the list of markups: " + markup);
		return markup;
	}

	/*
	 * Returns the CSI_Markup property 
	 * @see com.cimmetry.vuelink.core.DMSGetPropAction#execute(com.cimmetry.vuelink.context.DMSContext, com.cimmetry.vuelink.session.DMSSession, com.cimmetry.vuelink.query.DMSQuery, com.cimmetry.vuelink.core.DMSArgument[], com.cimmetry.vuelink.core.Property)
	 */
	public DMSProperty execute(ActionContext context, DMSSession session,
			DMSQuery query, DMSArgument[] args, Property property)
			throws VuelinkException {

		DMSProperty retProp = new DMSProperty(Property.CSI_Markups, buildMarkupProperty(context.getBackendAPI(),
				context.getBackendSession(session, query), query));
		return retProp;
	}
}
