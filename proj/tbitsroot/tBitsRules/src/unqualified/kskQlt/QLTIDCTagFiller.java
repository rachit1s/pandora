package kskQlt;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.ISearchResultsHeaderSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;
import kskQlt.QltConstants;
import kskQlt.QLTIDCConstants;

public class QLTIDCTagFiller implements ISearchResultsHeaderSlotFiller {


    public int getSearchResultsHeaderOrder() {
        // TODO Auto-generated method stub
        return 0;
    }


   public String getSearchResultsHeaderHtml(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, BusinessArea ba, User user) {
    
           String sysPrefix=ba.getSystemPrefix();
           boolean isBA=sysPrefix.equals(QltConstants.QLT_SYSPREFIX);
           
           if(isBA){
           StringBuffer sb=new StringBuffer();
           sb.append("<input id='idcbutton' type ='button' value ='Submit Trip Report' onclick=\"function createIDC()" +       
               "{\n" +             
                "var reqList =  getValue('requestList');\n" +
                "var url = YAHOO.transbit.tbits.contextPath + 'proxy/QLTIDCProxyServlet?dcrBA=" + ba.getSystemPrefix()
                + "&requestList='" +" + reqList;\n" +
                " window.open(url); \n" +
                "}\n" +
               
                "createIDC();\" />\n" );
           // button / link should open a new window with url as <nearestPath>/proxy/your-servletname?parameter=value
        
     
           return sb.toString();
           }
           else return "";
    }

}
