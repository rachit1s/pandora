package transbit.tbits.autovue.connector;

/**
 * Returns the markup policy specified in DMS.
 */
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.DMSGetPropAction;
import com.cimmetry.vuelink.propsaction.DMSProperty;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;
import com.cimmetry.vuelink.xml.CData;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class GetPropCSI_MarkupPolicy extends GetFilesysProperty implements DMSGetPropAction<ActionContext> {	
	
	/** log4j logger for the GetPropCSI_MarkupPolicy class*/
	private static final Logger m_logger = LogManager.getLogger(GetPropCSI_MarkupPolicy.class);
	
	public DMSProperty execute(ActionContext context, DMSSession session, DMSQuery query,			
			DMSArgument[] args, Property property) throws VuelinkException {		
		DMSProperty policyProp = null;
		
		String policy = getMarkupPolicy();
		if (policy != null) {
			policyProp = new DMSProperty(DMSProperty.CSI_MarkupPolicy , 
										 (String[])null, 
										 new Object[]{new CData(policy)},
										 null);
		}
		
		m_logger.debug("got the markup policy: " + policyProp);
		return policyProp;
	}
	
	private String getMarkupPolicy() {		
		String policyFile = ActionContext.getStaticParameter(ActionContext.PARAM_CSI_MARKUPPOLICY_DEF_LOCATION);
		if (policyFile == null) {
			return null;			
		}
		
		try {
			FileInputStream fis = new FileInputStream(policyFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int bytes = 0;
			while ((bytes = bis.read(buffer)) > 0) {				
				baos.write(buffer, 0, bytes);				
			}
			
			fis.close();
			return baos.toString();
		} catch (IOException ex) {
			m_logger.error("Error on reading markup policy file");
			
		}
		return null;
	}

}
