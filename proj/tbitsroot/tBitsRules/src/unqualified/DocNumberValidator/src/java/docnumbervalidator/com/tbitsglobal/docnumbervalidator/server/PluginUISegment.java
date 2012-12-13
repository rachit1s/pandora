package docnumbervalidator.com.tbitsglobal.docnumbervalidator.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.ISearchFooterSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;

public class PluginUISegment implements ISearchFooterSlotFiller {

	public String getSearchFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		StringBuffer sb = new StringBuffer();		
		String display = "none";
		
		String sysPrefix = ba.getSystemPrefix();
		display = "inline";
		String url = "/proxy/" + PluginResourceServlet.URL;
		sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + url + "/resources/css/gxt-all.css\" />");
		sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + url + "/DocNumberValidator.css\" />");
		sb.append("<span style='display:").append(display).append("; float:left;' id='").append("docValidatorButtonHolder").append("'>");
		sb.append("<script type=\"text/javascript\" language=\"javascript\" src=\"" + url + "/docnumbervalidator.nocache.js\" ></script>");
		return sb.toString();
	}

	public int getSearchFooterOrder() {
		return 0;
	}

}
