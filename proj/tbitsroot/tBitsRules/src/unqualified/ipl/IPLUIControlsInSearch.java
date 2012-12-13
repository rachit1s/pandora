/**
 * 
 */
package ipl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import transbit.tbits.ExtUI.IExtUIRenderer;
import transbit.tbits.ExtUI.ISearchResultsHeaderSlotFiller;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/**
 * @author lokesh
 *
 */
public class IPLUIControlsInSearch implements ISearchResultsHeaderSlotFiller {

	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IExtUIRenderer#getSequence()
	 
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	 (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IExtUIRenderer#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Hashtable, java.util.ArrayList, transbit.tbits.domain.BusinessArea, int)
	 
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable tagTable,
			ArrayList<String> tagList, BusinessArea ba, int action)
			throws TBitsException {
		
		
	}
	*/
	public String getSearchResultsHeaderHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		
		StringBuffer sb = new StringBuffer();		
		String displayTransmittal = "none";
		
		int systemId = ba.getSystemId();
		if (PropertiesHandler.isTransmittalEnabled() && IPLUtils.isExistsInDCRBAList(systemId)){
			
			displayTransmittal = "inline";
			String options = "";
			
			try {
				ArrayList<IPLTransmittalType> ttList = IPLTransmittalType.lookupTransmittalTypesBySystemId(systemId);
				//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@tt: " + ttList.get(0).getDisplayName());
				
				class KSKTransmittalTypeComparator implements Comparator<IPLTransmittalType>
				{
					public int compare(IPLTransmittalType tt1, IPLTransmittalType tt2) {	
						Integer ttId1 = tt1.getSortOrder();
						Integer ttId2 = tt2.getSortOrder();
						return ttId1.compareTo(ttId2);
					}
				}
				
				Collections.sort(ttList, new KSKTransmittalTypeComparator());
				if ((ttList != null) && (!ttList.isEmpty())){
					for(IPLTransmittalType tt : ttList)
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
				//throw new TBitsException(e);
			}
						
			sb.append("<span style='display:").append(displayTransmittal).append("'>").append(" | ").append("<SELECT id='trnTypeSelect' size='1'")
			.append("style='width: 250px; vertical-align: middle; font-size: 100%; font-weight: normal;").append("name='trnTypeSelect'>")
			.append(options).append("</SELECT>").append("<input type='button' onclick=\"function showTransmittalWindow(){" +
					"var trnType = document.getElementById('trnTypeSelect');" +
					"var trnTypeValue = trnType.options[trnType.selectedIndex].value;" +
					"var reqList =  getValue('requestList');" +
					"var url = '" + WebUtil.getNearestPath(httpRequest, "") +"transmittal?requestType=selection&dcrBA=" + ba.getSystemPrefix()
					+ "&trnType=' + trnTypeValue + '&requestList=' + reqList; showTransmittalPanel(url);}" +
					"showTransmittalWindow()\"");
			sb.append(" value='Create Transmittal...'/>").append("</span>");
		}
		
		//tagTable.put("searchResExtUIList", sb.toString());
		return sb.toString();
	}

	public int getSearchResultsHeaderOrder() {
		// TODO Auto-generated method stub
		return 0;
	}
}
