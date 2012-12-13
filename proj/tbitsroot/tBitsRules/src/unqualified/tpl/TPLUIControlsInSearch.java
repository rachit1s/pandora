/**
 * 
 */
package tpl;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IExtUIRenderer;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/**
 * @author lokesh
 *
 */
public class TPLUIControlsInSearch implements IExtUIRenderer {
	
	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IExtUIRenderer#getSequence()
	 */
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IExtUIRenderer#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Hashtable, java.util.ArrayList, transbit.tbits.domain.BusinessArea, int)
	 */
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable tagTable,
			ArrayList<String> tagList, BusinessArea ba, int action)
			throws TBitsException {
		
		StringBuffer sb = new StringBuffer();		
		String displayTransmittal = "none";
		
		String sysPrefix = ba.getSystemPrefix().trim();
		
		if (PropertiesHandler.isTransmittalEnabled() && 
				(sysPrefix.equals("Design") || sysPrefix.equals("tscr"))){
			
			displayTransmittal = "inline";
			String options = "";		
			
			
			/*sb.append("<script type=\"type/javascript\">" +
					"function showTransmittalWindow(){\n" +
					"var reqList =  getValue('requestList');" + 
					"var url = '" + WebUtil.getNearestPath(request, "") + "transmittal?requestType=selection&ba=" + sysPrefix +
					"&requestList=' + reqList;" + "window.open(url,'_blank','left=20, top=20, height=435, width=900,toolbar=yes," +
					" location=yes,status=yes, resize=yes, menubar=yes, scrollbars=yes,copyhistory=yes');}");
			sb.append("</script>");*/
			
			sb.append("<span style='display:").append(displayTransmittal).append("'>").append(" | ")
				.append("<input type='button' " + "onclick=\"javascript:function showTransmittalWindow(){var reqList =  getValue('requestList');var url = '"
						+ WebUtil.getNearestPath(request, "") + "transmittal?requestType=selection&ba=" + sysPrefix 
						+ "&requestList='+ reqList" + ";window.open(url,'_blank','left=20, top=20, height=435, width=900,toolbar=yes," +
								"location=yes,status=yes, resize=yes, menubar=yes, scrollbars=yes,copyhistory=yes');} showTransmittalWindow()\"")
					.append(" value='Create Transmittal...'/>").append("</span>");
						
		}
		
		tagTable.put("searchResExtUIList", sb.toString());	
	}

}
