/**
 * 
 */
package transbit.tbits.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.SysConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.dms.TransmittalTemplate;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/**
 * @author Lokesh
 *
 */
public class AdminTransmittals extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TRANSMITTALS_HTM = "web/tbits-admin-transmittals.htm";
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_ADMIN);
	private static final String EMPTY_STRING = "";
	private int USERS = 4;
	
	private static final String SAVE = "save";
	private static final String LATEST = "latest";
	private static final String DTN = "dtn";
	private static final String NONE = "none";
	private static final String SAVE_BUTTON_VALUE = "Save";
	
	//Transmittal mapping table column names
	public static final String LATEST_BA_SYS_PREFIX = "latest_ba_sys_prefix";
	public static final String TRANSMITTAL_SYS_PREFIX = "transmittal_sys_prefix";
	
	static
	{
		 //urls
        String url = "transmittals";
    	String completeURL = url + ".admin";
    	
        //Create Mapping
		URLRegistry.getInstance().addMapping(AdminProxyServlet.class, url, AdminTransmittals.class);
		
		//Create Menu
		NavMenu nav = NavMenu.getInstance();
		nav.BAMenu.add(new MenuItem("Transmittals Mapping", completeURL, "Configure the transmittals mappings."));
		
	}
	
	/**
     * This method services the HTTP-Get request to this servlet.
     * Basically, it does display of the page ready for user to start filling
     * it and submit.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @exception ServeletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {

      HttpSession session = aRequest.getSession();
       try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();

            return;
        } catch (TBitsException de) {
            session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();

            return;
        }

        return;
    }

	/**
     * The doPost method of the servlet.
     *
     * @param  aRequest          the HttpServlet Request Object
     * @param  aResponse         the HttpServlet Response Object
     *
     * @exception ServeletException
     * @exception IOException
     */
    public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
        
    	HttpSession session = aRequest.getSession();

        try {
            handleRequest(aRequest, aResponse);
        } catch (DatabaseException de) {
        	session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();
            return;
        } catch (TBitsException de) {
        	session.setAttribute("ExceptionObject", de);
            aResponse.sendRedirect(WebUtil.getServletPath(aRequest, "/error"));
            de.printStackTrace();
            return;
        }

        return;
    }

	private void handleRequest(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, DatabaseException, TBitsException, IOException {
		
		aResponse.setContentType("text/html");
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();      
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        
        int userId = user.getUserId();
        int systemId = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
        SysConfig sc = ba.getSysConfigObject();        
        String baList = AdminUtil.getSysIdList(systemId, userId);

        DTagReplacer dTag = new DTagReplacer(TRANSMITTALS_HTM);        
        dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
        dTag.replace("cssFile", WebUtil.getCSSFile(sc.getWebStylesheet(), sysPrefix, false));
        dTag.replace("title", "TBits Admin: " + ba.getDisplayName() + " Escalations");
        dTag.replace("sysPrefix", ba.getSystemPrefix());
        dTag.replace("sys_ids", baList);
        dTag.replace("sys_prefix_list", getSysPrefixList(systemId, userId));        
        dTag.replace("userLogin", user.getUserLogin());
        String sysPrefixes = TransmittalTemplate.getMappedBusinessAreas(systemId);
        dTag.replace("mappingData", getJSONString(sysPrefixes).toString());
        dTag.replace("templateData", TransmittalTemplate.getTemplatesJSONArrayForSysId(systemId).toString());
        out.println(dTag.parse(systemId));
	}
	
	private static JSONArray getJSONString(String sysPrefixes){
		JSONArray jArray = new JSONArray();
		if (sysPrefixes.trim().equals(EMPTY_STRING)){
			JSONObject obj = new JSONObject();
			obj.accumulate(DTN, NONE);
			obj.accumulate(LATEST, NONE);
			obj.accumulate(SAVE, SAVE_BUTTON_VALUE);
			jArray.add(obj);
		}
		else{
			String[] sysPrefix = sysPrefixes.split(",");
			JSONObject obj = new JSONObject();
			obj.accumulate(DTN, sysPrefix[0]);
			obj.accumulate(LATEST, sysPrefix[1]);
			obj.accumulate(SAVE, SAVE_BUTTON_VALUE);
			jArray.add(obj);
		}		
		return jArray;
	}
	
	public static String getSysPrefixList(int aSystemId, int aUserId) throws DatabaseException {
		
		JSONArray sysPrefixList = new JSONArray();
		ArrayList<BusinessArea> adminList = BusinessArea.getAdminBusinessAreas(aUserId);

		BusinessArea.setSortParams(BusinessArea.DISPLAYNAME, 0);
		adminList = BusinessArea.sort(adminList);

		if ((adminList == null) || (adminList.size() == 0)) {
			return sysPrefixList.toString();
		}

		sysPrefixList.add(NONE);
		String sysPrefix   = null;		
		for (BusinessArea ba : adminList) {
			sysPrefix   = ba.getSystemPrefix();
			sysPrefixList.add(sysPrefix);
		}
		return sysPrefixList.toString();
	}
		
	public static void main(String[] args){
		try {
			System.out.println("sysprefixes: " + getJSONString(TransmittalTemplate.getMappedBusinessAreas(6)));;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

