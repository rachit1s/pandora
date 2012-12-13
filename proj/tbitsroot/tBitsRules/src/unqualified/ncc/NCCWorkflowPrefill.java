/**
 * 
 */
package ncc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IAddRequestFooterSlotFiller;
import transbit.tbits.ExtUI.IUpdateRequestFooterSlotFiller;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class NCCWorkflowPrefill implements IAddRequestFooterSlotFiller, IUpdateRequestFooterSlotFiller{

	private static final String NCC_WORKFLOW_JS = "ncc_workflow_type.js";
	
	private static final String TRANSBIT_TBITS_TRANSMITTAL_WORKFLOW_PREFILL_BA_LIST =
									"transbit.tbits.transmittal.ncc.workflowPrefillBAList";
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger("ncc");
	
	static final String MSG_CANNOT_PREFILL = "Cannot pre-fill form. Please select workflow manually.";

	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IAddRequestFooterSlotFiller#getAddRequestFooterHtml(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.User)
	 */
	
	public String getAddRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		
		return getWorkflowPrefillHtml(ba);
	}

	/**
	 * @param ba
	 * @return
	 */
	private String getWorkflowPrefillHtml(BusinessArea ba) {		
		
		String baList = PropertiesHandler.getProperty(TRANSBIT_TBITS_TRANSMITTAL_WORKFLOW_PREFILL_BA_LIST);
		if((baList == null) || (!TransmittalUtils.isExistsInString(baList, ba.getSystemPrefix())))
			return "";
		
		URL fileURL = getClass().getResource(NCC_WORKFLOW_JS) ;
		if( null == fileURL )
		{
			LOG.error( "File not found = " + NCC_WORKFLOW_JS ) ;
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		}
		
		String filePath = fileURL.getFile() ;
		if( filePath.equals(""))
		{
			LOG.error( "File not found = " + NCC_WORKFLOW_JS ) ;
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		}		
	
//		 test if DtagReplacer can find my file 
		DTagReplacer dtagreplacer = null ;
		File myFile = new File(filePath) ;
		try {
			 dtagreplacer = new DTagReplacer( myFile ) ;
		} catch (FileNotFoundException e1) {
			LOG.error("DTagReplacer Exception : file not found" ) ;
			e1.printStackTrace();
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		} catch (IOException e1) {
			LOG.error("DTagReplacer Exception" ) ;
			e1.printStackTrace();
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		}
		
		String filedata = dtagreplacer.parse() ;
		return filedata;
	}
	
	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IAddRequestFooterSlotFiller#getAddRequestFooterSlotFillerOrder()
	 */
	
	public double getAddRequestFooterSlotFillerOrder() {
		return 1;
	}

	
	public String getUpdateRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request oldRequest, User user) {
		return getWorkflowPrefillHtml(ba);
	}

	
	public double getUpdateRequestFooterSlotFillerOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
