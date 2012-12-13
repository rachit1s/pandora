package com.tbitsGlobal.jaguar.client;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.Pattern;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Events.TbitsBaseEvent;
import commons.com.tbitsGlobal.utils.client.Events.TbitsEventRegister;
import commons.com.tbitsGlobal.utils.client.Events.ToViewRequestOtherBA;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.urlManager.HistoryToken;
import commons.com.tbitsGlobal.utils.client.urlManager.TbitsURLManager;

/**
 * 
 * @author sutta
 * 
 * Class that provides Utility functions that are used in Jaguar
 */
public class JaguarUtils implements IFixedFields{
	
	/**
	 * Creates a panel that contains the text provided with embedded smartlinks.
	 * 
	 * Looks for following patterns : 
	 * 1. [sys_prefix#request_id]
	 * 2. [sys_prefix#request_id#action_id]
	 * 
	 * @param desc
	 * @return the panel.
	 */
	public static String hyperSmartLinks(String desc, HashMap<String, ClickableLink> linkMap) {
		String outString = "";
        if ((desc == null) || desc.trim().equals("")) { // return empty container
            return "";
        }

        BusinessAreaClient  ba	= null;
        String        str    	= desc; // to carry string yet to be parsed
        Pattern       p      	= new Pattern("([a-zA-Z0-9_]+)([ ]?)#([ ]?)([0-9]+)(#([0-9]+))?", Pattern.CASE_INSENSITIVE);

        while (p.match(str).length > 0) { // for each match
        	String[]      m	= p.match(str);
            String smartLink     	= m[0];
            final String sysPrefix	= m[1];
            String strRequestId  	= m[4];
            String strActionId   	= ((m[6] != null)
                                    	? m[6] : "");
            int    requestId     	= 0;

            try {
                requestId = Integer.parseInt(strRequestId);
            } catch (NumberFormatException e) { // append the whole match to the string and continue
            	outString += str.substring(0, str.indexOf(smartLink) + smartLink.length());
                str = str.substring(str.indexOf(smartLink) + smartLink.length());

                continue;
            }

            ba = ClientUtils.getBAbySysPrefix(sysPrefix);

            if (ba == null) { // append the whole match to the string and continue
            	outString += str.substring(0, str.indexOf(smartLink) + smartLink.length());
                str = str.substring(str.indexOf(smartLink) + smartLink.length());

                continue;
            }
            
            outString += str.substring(0, str.indexOf(smartLink)) + "&nbsp;";

            // Create the Hyper Link widget
            final int rId = requestId;
            ClickableLink link = new ClickableLink(ba.getSystemPrefix() + "#" + rId + 
            		(((strActionId != null) &&!strActionId.equals("")) ? ("#" + strActionId) : ""),
            		new ClickableLinkListener<ComponentEvent>() {
						@Override
						public void onClick(ComponentEvent e) {
							if(rId > 0){
								if(sysPrefix.equals(ClientUtils.getSysPrefix())){
									TbitsURLManager.getInstance().addToken(new HistoryToken(GlobalConstants.TOKEN_VIEW, rId + "", false));
								}else{
									TbitsBaseEvent event = new ToViewRequestOtherBA(sysPrefix, rId);
									TbitsEventRegister.getInstance().fireEvent(event);
								}
							}
						}});
            
            outString += link.getHtml();
            linkMap.put(link.getClassName(), link);
            
            str = str.substring(str.indexOf(smartLink) + smartLink.length());
        }
        
        // append remaining text
        outString += "&nbsp;" + str;

        return outString;
	}
	
	/**
	 * @return The Base url on which the Application is running on. 
	 * e.g http://symphron.mytbits.com:9000/
	 */
	public static String getAppBaseURL(){
		String url = GWT.getHostPageBaseURL();
		url = url.substring(0, url.indexOf("jaguar/"));
		return url;
	}

	public static TbitsTreeRequestData mapToTbitsTreeRequestData(int requestId, HashMap<String, POJO> map){
		if(map == null)
			return null;
		TbitsTreeRequestData requestData =  new TbitsTreeRequestData();
		for(String f : map.keySet()){
			POJO tempObj = map.get(f);
			if(tempObj != null)
				requestData.set(f, tempObj);
		}
		
		requestData.setRequestId(requestId);
		
		return requestData;
	}

	public static TbitsTreeRequestData findModelInTree(HashMap<Integer, TbitsTreeRequestData> requestTree, int key) {
		TbitsTreeRequestData model = requestTree.get(key);
		if(model != null)
			return model;
		for(TbitsTreeRequestData data : requestTree.values()){
			TbitsTreeRequestData result = data.findByRequestIdInChildren(key);
			if(result != null)
				return result;
		}
		return null;
	}
}
