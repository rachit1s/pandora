package kskorg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static kskorg.ORGConstants.*;

import transbit.tbits.ExtUI.ISearchResultsHeaderSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;


public class CVSlotFiller implements ISearchResultsHeaderSlotFiller {

	@Override
	public String getSearchResultsHeaderHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		String sysPrefix=ba.getSystemPrefix();
        boolean isBA=sysPrefix.equals(CV_sysprefix);
                     
        
        if(isBA){
        StringBuffer sb=new StringBuffer();
        sb.append("<input id='idcbutton' type ='button' value ='Send For ShortListing' onclick=\"function createIDC()" +       
            "{\n" +             
             "var reqList =  getValue('requestList');\n" +
             "var url = YAHOO.transbit.tbits.contextPath + 'proxy/IDProxyServlet?dcrBA=" + ba.getSystemPrefix()
             + "&requestList='" +" + reqList;\n" +
             " window.open(url); \n" +
             "}\n" +
            
             "createIDC();\" />\n" );
        // button / link should open a new window with url as <nearestPath>/proxy/your-servletname?parameter=value
     
  
        return sb.toString();
        }
        else return "";
	}

	@Override
	public int getSearchResultsHeaderOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
