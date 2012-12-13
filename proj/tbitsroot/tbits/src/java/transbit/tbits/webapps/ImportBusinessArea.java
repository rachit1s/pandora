/**
 * 
 */
package transbit.tbits.webapps;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.RoleUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 *
 */
public class ImportBusinessArea extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final TBitsLogger LOG   = TBitsLogger.getLogger(TBitsConstants.PKG_WEBAPPS);
	private static final String WIZARD_HTML = "web/tbits-admin-import-ba.htm";

	/**
	 * 
	 */
	public ImportBusinessArea() {
		// TODO Auto-generated constructor stub
	}
	
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) 
	throws ServletException, IOException {
		handleRequest (aRequest, aResponse);
	}
		
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		handleRequest (aRequest, aResponse);
	}	
	
	/**
	 * @param HttpServletRequest aRequest, HttpServletResponse aResponse
	 */
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		try {
			String baNameList = "";
			String sysPrefixList = "";
			String emailList = "";
			
			User user = WebUtil.validateUser(aRequest);		
			ServletOutputStream out = aResponse.getOutputStream();
			if (RoleUser.isSuperUser(user.getUserId())){				
				aResponse.setContentType("text/html");
				String ba = aRequest.getParameter("ba");
				
				for(BusinessArea tempBA : BusinessArea.getAllBusinessAreas()){
					if (baNameList.equals("")){
						baNameList = tempBA.getName();
						sysPrefixList = tempBA.getSystemPrefix();
						emailList = tempBA.getEmail();
					}
					else{
						baNameList = tempBA.getName() + "," + baNameList;
						sysPrefixList = tempBA.getSystemPrefix() + "," + sysPrefixList;
						emailList = tempBA.getEmail() + "," + emailList;
					}
				}				
				
				DTagReplacer dTag = new DTagReplacer(WIZARD_HTML);
				dTag.replace ("sysPrefix", ba);		
				dTag.replace ("baNameList", baNameList);
				dTag.replace ("sysPrefixList", sysPrefixList);
				dTag.replace ("emailList", emailList);
				dTag.replace ("nearestPath", WebUtil.getNearestPath(aRequest,""));
				out.println(dTag.parse(0));			
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (TBitsException e) {
			e.printStackTrace();
		}
	}	
	
	
}
