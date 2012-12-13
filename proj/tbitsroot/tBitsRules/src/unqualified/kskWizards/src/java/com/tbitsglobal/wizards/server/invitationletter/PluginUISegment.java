package com.tbitsglobal.wizards.server.invitationletter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.ISearchFooterSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;

import com.tbitsglobal.wizards.server.PluginResourceServlet;

public class PluginUISegment implements ISearchFooterSlotFiller {

	public String getSearchFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		StringBuffer sb = new StringBuffer();		
		String displayTransmittal = "none";
		
		String sysPrefix = ba.getSystemPrefix();
		if (sysPrefix.toUpperCase().equals("ED")){
			
			displayTransmittal = "inline";
			String url = "/proxy/" + PluginResourceServlet.URL;
			sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + url + "/resources/css/gxt-all.css\" />");
			sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + url + "/Wizards.css\" />");
			sb.append("<span style='display:").append(displayTransmittal).append("; float:left;' id='").append("wizardButtonContainer").append("'>");
			sb.append("<script type=\"text/javascript\" language=\"javascript\" src=\"" + url + "/wizards.nocache.js\" ></script>");
		}
		return sb.toString();
	}

	
	public int getSearchFooterOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
