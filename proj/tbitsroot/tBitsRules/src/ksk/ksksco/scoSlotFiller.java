package ksksco;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.ISearchResultsHeaderSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import  static ksksco.scoConstants.*;

public class scoSlotFiller implements ISearchResultsHeaderSlotFiller {

	
	public String getSearchResultsHeaderHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		
		
		 String sysPrefix=ba.getSystemPrefix();
         boolean isBA=sysPrefix.equals(SCO_sysprefix);
         if(isBA){
         StringBuffer sb=new StringBuffer();
         sb.append("<input id='idcbutton' type ='button' value ='create CORR' onclick=\"function createIDC()" +       
             "{\n" +             
              "var reqList =  getValue('requestList');\n" +
              "var url = YAHOO.transbit.tbits.contextPath + 'proxy/scoProxyServlet?dcrBA=" + ba.getSystemPrefix()
              + "&requestList='" +" + reqList;\n" +
              " window.open(url); \n" +
              "}\n" +
             
              "createIDC();\" />\n" );
         // button / link should open a new window with url as <nearestPath>/proxy/your-servletname?parameter=value
      
   
         return sb.toString();
         }
         else return "";
  
	
	}

	
	public int getSearchResultsHeaderOrder() {
				return 0;
	}

}
