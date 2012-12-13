/**
 * 
 */
package pyramid;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.IPreRenderer;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author transbit
 *
 */
public class PreFillIDC implements IPreRenderer {
	static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Hashtable)
	 */
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable<String, Object> tagTable, ArrayList<String> tagList)
			throws TBitsException {
		
		HttpSession session = request.getSession();
		Request parentRequest = null;
		final String documentNo = "DocumentNo";
		final String actPercentage = "ActivityPercentage";
		final String numSheets = "NoofSheets";
		final String paperSize = "PaperSize";
		final String docType = "DocType";
		final String revision = "Revision";	
		
		//String sysPrefix = "DCR343, DCR326,VDCR326,DCR345";
		boolean isRuleApplicable = false;
		
		System.out.println("Executing prefilled");
		try{			
			String uri = request.getRequestURI();
			String[] keys = uri.split("/");
			isRuleApplicable = PyramidUtils.isExistsInCommons(keys[2]);
			if (keys[1].equals("add-subrequest") && isRuleApplicable){
				int requestId = -1;
				requestId = Integer.parseInt(keys[3]);
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(keys[2]);
				if (ba != null){					
					parentRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
					String parentDocNo = parentRequest.get(documentNo);
					String parentActPer = parentRequest.get(actPercentage);
					String parentNumSheets = parentRequest.get(numSheets);
					String parentPSize = parentRequest.get(paperSize);
					String parentDocType = parentRequest.get(docType);
					String parentRev = parentRequest.get(revision);
					
					StringBuffer exFieldsBuffer = new StringBuffer();
					exFieldsBuffer.append("<script type=\"text/javascript\"> \n").append("var drwNo=").append("\"").append(parentDocNo).append("\";")
					.append("\n var docElem = document.getElementById (\"").append(documentNo).append("\");\n docElem.value =drwNo; \n")
					.append("var actPer=").append("\"").append(parentActPer).append("\";").append("\n var docElem = document.getElementById (\"")
					.append(actPercentage).append("\");\n docElem.value =actPer; \n").append("var pageNum=").append("\"").append(parentNumSheets).append("\";")
					.append("\n var docElem = document.getElementById (\"").append(numSheets).append("\");\n docElem.value =pageNum; \n")
					.append("setSelect(\"").append(paperSize).append("\",").append("\"").append(parentPSize).append("\");")
					.append("setSelect(\"").append(docType).append("\",").append("\"").append(parentDocType).append("\");")
					.append("setSelect(\"").append(revision).append("\",").append("\"").append(parentRev).append("\");")
					.append("</script>");
					
					tagTable.put("prefillData", exFieldsBuffer.toString().trim());					
				}
			}
		} catch (DatabaseException e) {
			LOG.severe("Error occurred while retrieving parent request for IDC");
			session.setAttribute("ExceptionObject", e);
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
