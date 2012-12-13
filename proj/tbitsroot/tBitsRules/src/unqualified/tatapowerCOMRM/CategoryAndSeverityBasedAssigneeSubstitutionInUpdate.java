/**
 * 
 */
package tatapowerCOMRM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import transbit.tbits.ExtUI.IAddRequestFooterSlotFiller;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class CategoryAndSeverityBasedAssigneeSubstitutionInUpdate implements
		IAddRequestFooterSlotFiller {
	
	public static final String COMMINSIONING_REVIEW_ASSIGNEES_MAP = "comminsioning_review_assignees_map";
	public static final String MSG_CANNOT_PREFILL = "Cannot prefill the form, please continuing filling manually.";
	public static final String ADD_REQUEST_PREFILL_JS = "addRequestPrefill.js";
	public static final String COMRM = "COMRM";
	public static final TBitsLogger LOG = TBitsLogger.getLogger("tatapowerCOMRM");
	
	public String process(HttpServletRequest request,
			HttpServletResponse response, BusinessArea ba, User user  ) {		
		if( (null == ba) || (null == ba.getSystemPrefix()) ) 
			return "" ;
		
		if( ! ba.getSystemPrefix().trim().equalsIgnoreCase(COMRM) ) // this is Correspondence business area
			return "" ;
				
		URL fileURL = getClass().getResource(ADD_REQUEST_PREFILL_JS) ;
		if( null == fileURL )
		{
			LOG.error( "File not found = " + ADD_REQUEST_PREFILL_JS) ;
			String sher = TataPowerComRmUtils.showError(MSG_CANNOT_PREFILL) ;

			return sher;
		}		
		String filePath = fileURL.getFile() ;
		
		DTagReplacer dtagreplacer = null ;
		File myFile = new File(filePath) ;
		try {
			 dtagreplacer = new DTagReplacer( myFile ) ;
		} catch (FileNotFoundException e1) {
			LOG.error("DTagReplacer Exception : File not found" ) ;
			e1.printStackTrace();
			String sher = TataPowerComRmUtils.showError(MSG_CANNOT_PREFILL) ;
			return sher;
		} catch (IOException e1) {
			LOG.error("DTagReplacer Exception" ) ;
			e1.printStackTrace();
			String sher = TataPowerComRmUtils.showError(MSG_CANNOT_PREFILL) ;
			return sher;
		}
				
		Hashtable<String,String> params = new Hashtable<String,String>() ;		
		//Get corresponding assignees.	
		try
		{
			String assigneeMapValue = TataPowerComRmUtils.getAssigneeMap();
			params.put("assigneeMap_value", assigneeMapValue) ;
						
			for( Enumeration<String> keys = params.keys() ; keys.hasMoreElements() ; )
			{
				String key = keys.nextElement() ;
				String value = params.get(key) ;
				dtagreplacer.replace(key, value) ;
			}
			String filedata1 = dtagreplacer.parse() ;
			return filedata1;
		}		
		catch( Exception e ) 
		{
			LOG.error("Exception while filling the prefill javascript") ;
			String sher = TataPowerComRmUtils.showError(MSG_CANNOT_PREFILL) ;
			return sher;
		}
	}	
	
	

	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IAddRequestFooterSlotFiller#getAddRequestFooterHtml(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.User)
	 */
	public String getAddRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		return process(httpRequest, httpResponse, ba, user);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IAddRequestFooterSlotFiller#getAddRequestFooterSlotFillerOrder()
	 */
	public double getAddRequestFooterSlotFillerOrder() {
		// TODO Auto-generated method stub
		return 0;
	}
		
}
