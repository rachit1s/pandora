/**
 * 
 */
package ksk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.ISearchFooterSlotFiller;
import transbit.tbits.ExtUI.ISearchResultsHeaderSlotFiller;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import transbit.tbits.webapps.WebUtil;

/**
 * @author lokesh
 *
 */
public class TransmittalLink implements ISearchResultsHeaderSlotFiller, ISearchFooterSlotFiller {

	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IExtUIRenderer#process(javax.servlet.http.HttpServletRequest, 
	 * 													javax.servlet.http.HttpServletResponse, 
	 * 													java.util.Hashtable, java.util.ArrayList, 
	 * 													transbit.tbits.domain.BusinessArea, int)
	 */
	public String getSearchResultsHeaderHtml(HttpServletRequest request,
			HttpServletResponse response, BusinessArea ba, User user){
		
		StringBuffer sb = new StringBuffer();		
		String displayTransmittal = "none";
		
		int systemId = ba.getSystemId();
		if (PropertiesHandler.isTransmittalEnabled() && KSKUtils.isExistsInDCRBAList(systemId)){
			
			displayTransmittal = "inline";
			String options = "";
			
			try {
				ArrayList<KSKTransmittalType> ttList = KSKTransmittalType.lookupTransmittalTypesBySystemId(systemId);
				
				class KSKTransmittalTypeComparator implements Comparator<KSKTransmittalType>
				{
					public int compare(KSKTransmittalType tt1, KSKTransmittalType tt2) {	
						Integer ttId1 = tt1.getSortOrder();
						Integer ttId2 = tt2.getSortOrder();
						return ttId1.compareTo(ttId2);
					}
				}
				
				Collections.sort(ttList, new KSKTransmittalTypeComparator());
				if ((ttList != null) && (!ttList.isEmpty())){
					for(KSKTransmittalType tt : ttList)
						if (tt != null){
							String dName = "";
							if ((tt.getDisplayName() == null) || tt.getDisplayName().trim().equals(""))
								dName = tt.getName();
							else{
								dName = tt.getDisplayName();				
								options = options + "<option value='" + tt.getName() + "'> " + dName + "</option>";
							}
						}
						else
							continue;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
						
			sb.append("<span style='display:").append(displayTransmittal).append("'>").append(" | ").append("<SELECT id='trnTypeSelect' size='1'")
			.append("style='width: 250px; vertical-align: middle; font-size: 100%; font-weight: normal;").append("name='trnTypeSelect'>")
			.append(options).append("</SELECT>").append("<input type='button' onclick=\"" +
					"function showTransmittalWindow(){" +
					"var trnType = document.getElementById('trnTypeSelect');" +
					"var trnTypeValue = trnType.options[trnType.selectedIndex].value;" +
					"var reqList =  getValue('requestList');" +
					"var url = '" + WebUtil.getNearestPath(request, "") +"transmittal?requestType=selection&dcrBA=" + ba.getSystemPrefix() +
					"&trnType=' + trnTypeValue + '&requestList=' + reqList; showTransmittalPanel(url);" +
					"}" +
					"showTransmittalWindow()\"");
			sb.append(" value='Create Transmittal...'/>").append("</span>");
			
			
			
			//append("<input type='button' onclick='javascript:showTransmittalWindow()'")
			//.append(" value='Create Transmittal...'/>").append("</span>");
		}
		return sb.toString();
	}

	public int getSearchResultsHeaderOrder() {
		return 0;
	}

	public String getSearchFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		String launchFunction = "function showTransmittalWindow(){" +
		"	var trnType = document.getElementById('trnTypeSelect');" +
		"	var trnTypeValue = 'TransmitToWPCL';" +
		"	if(trnType != null){" +
		"		trnTypeValue = trnType.options[trnType.selectedIndex].value;" +
		"	}" +
		"	var reqList =  bulkUpdateIdList;" +
		"	var url = '" + WebUtil.getNearestPath(httpRequest, "") +"transmittal?requestType=selection&dcrBA=" + ba.getSystemPrefix() +
		"	&trnType=' + trnTypeValue + '&requestList=' + reqList; showTransmittalPanel(url);" +
		"}";
		return "<script> " +
				"afterBulkUpdate[afterBulkUpdate.length] = " + launchFunction + ";" +
				"</script>";
	}

	public int getSearchFooterOrder() {
		return 0;
	}
}
