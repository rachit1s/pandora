package nccIDC;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.ISearchResultsHeaderSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;


public class IDCTagFiller implements ISearchResultsHeaderSlotFiller {


    public int getSearchResultsHeaderOrder() {
        // TODO Auto-generated method stub
        return 0;
    }


   public String getSearchResultsHeaderHtml(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, BusinessArea ba, User user) {
    
           String sysPrefix=ba.getSystemPrefix();
           boolean isBA=sysPrefix.equals(IDCConstants.CSEPDI_SYSPREFIX)||
                        sysPrefix.equals(IDCConstants.DCPL_SYSPREFIX)||
                        sysPrefix.equals(IDCConstants.DESEIN_SYSPREFIX)||
                        sysPrefix.equals(IDCConstants.EDTD_SYSPREFIX)||
                        sysPrefix.equals(IDCConstants.KVK_SYSPREFIX)||
                        sysPrefix.equals(IDCConstants.STUP_SYSPREFIX)||
                        sysPrefix.equals(IDCConstants.NCC_SYSPREFIX)||
                        sysPrefix.equals(IDCConstants.VENDOR1_SYSPREFIX);
           if(isBA){
           StringBuffer sb=new StringBuffer();
           sb.append("<input id='idcbutton' type ='button' value ='create IDC' onclick=\"function createIDC()" +       
               "{\n" +             
                "var reqList =  getValue('requestList');\n" +
                "var url = YAHOO.transbit.tbits.contextPath + 'proxy/IDCProxyServlet?dcrBA=" + ba.getSystemPrefix()
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
