/**
 * 
 */
package transbit.tbits.dms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.ReportUtil;
import transbit.tbits.webapps.WebUtil;

/**
 * @author Lokesh
 *
 */
public class AddTransmittalTemplate extends HttpServlet {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String ADD_TRANSMITTAL_TEMPLATE_HTM = "web/tbits-add-transmittal-template.htm";
	private static final String EMPTY_STRING = "";
	private int USERS = 4;
		
	//Url request parameter constants
	private static final String CC_LIST = "ccList";
	private static final String TO_LIST = "toList";
	private static final String FILE_NAME = "fileName";
	private static final String TEMPLATE_NAME = "templateName";
	private static final String ACTION_TYPE = "actionType";
	private static final String LATEST_SYS_PREFIX = "latestSysPrefix";
	private static final String DTN_SYS_PREFIX = "dtnSysPrefix";
	
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
    
    private void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws IOException, ServletException, DatabaseException, TBitsException{
    	String actionType = aRequest.getParameter(ACTION_TYPE);
		if ((actionType == null) || (actionType.equals(EMPTY_STRING)))
			aResponse.getWriter().println("Specify what action to take(save/delete)");
		else
			actionType = actionType.trim();
		if (actionType.equals("open"))
			handleTemplateAddWindow(aRequest, aResponse);
		if (actionType.equals("edit"))
			handleTemplateEditWindow(aRequest, aResponse);
		else if (actionType.equals("insert"))
			handleInsert(aRequest, aResponse);
		else if (actionType.equals("update"))
			handleUpdate(aRequest, aResponse);
		else if (actionType.equals("delete"))
			handleDeleteTemplate (aRequest, aResponse); 
		else if (actionType.equals("mapTransmittals"))
			handleTransmittalMapping (aRequest, aResponse);
		else if (actionType.equals("deleteMapping"))
			handleDeleteMapping(aRequest, aResponse);
    }


	private void handleUpdate(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, DatabaseException, TBitsException, IOException {
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();      
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
        
        int templateId = -1;
        String templateIdStr = aRequest.getParameter("templateId");
		if((templateIdStr == null) || templateIdStr.trim().equals(EMPTY_STRING)){
			out.println("Could not find the appropriate template. Check if template exists.");
			return;
		}
		else{
			templateIdStr = templateIdStr.trim();
			templateId = Integer.parseInt(templateIdStr);
		}
        
		String templateName = aRequest.getParameter(TEMPLATE_NAME);
		if((templateName == null) || templateName.trim().equals(EMPTY_STRING)){
			out.println("Please specify templateName");
			return;
		}
		else
			templateName = templateName.trim();
		
		String fileName = aRequest.getParameter(FILE_NAME);
		if((fileName == null) || fileName.trim().equals(EMPTY_STRING))
			out.println("Please specify the file name");
		else
			fileName = fileName.trim();
		
		String toList = aRequest.getParameter(TO_LIST);
		toList = (toList == null)? EMPTY_STRING : toList.trim();
		
		String ccList = aRequest.getParameter(CC_LIST);
		ccList = (ccList == null)? EMPTY_STRING : ccList.trim();		
		
		TransmittalTemplate tt = new TransmittalTemplate(ba.getSystemId(), templateId, templateName, fileName,toList,ccList);
		TransmittalTemplate.updateTransmittalTemplate(tt);
		out.println("Updated transmittal template entry. Please close the window.");		
		
	}

	private void handleTemplateEditWindow(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, DatabaseException, TBitsException, FileNotFoundException, IOException{
		aResponse.setContentType("text/html");
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();      
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
                
        int systemId = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
        
        String templateName = aRequest.getParameter("template_name");
        if ((templateName == null) || (templateName.trim().equals(EMPTY_STRING)))
        	out.println("Please provide proper template id.");
        else
        	templateName = templateName.trim();
        
		TransmittalTemplate tt = TransmittalTemplate.lookupBySystemIdAndTemplateName(systemId, templateName);
		if (tt == null)
			out.println("Could not find template with name: " + templateName);
		else{
			DTagReplacer dTag = new DTagReplacer(ADD_TRANSMITTAL_TEMPLATE_HTM);        
			dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
			dTag.replace("title", "TBits Admin: " + " Edit Transmittal Template of " + ba.getDisplayName() );
			dTag.replace("sysPrefix", sysPrefix);
			dTag.replace("userLogin", user.getUserLogin());
			dTag.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
			dTag.replace("templateId", tt.getTemplateId() + EMPTY_STRING);
			dTag.replace("templateName", tt.getTemplateName());
			dTag.replace("fileName", tt.getTemplateFileName());
			dTag.replace("toList", tt.getAssigneeList());
			dTag.replace("ccList", tt.getSubscribersList());
			dTag.replace("saveType", "update");
			out.println(dTag.parse(systemId));
		}		
	}

	private void handleDeleteTemplate(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, DatabaseException, TBitsException, IOException {		
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();      
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
        
        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        
        int templateId = -1;
        String templateIdStr = aRequest.getParameter("template_id");
        if ((templateIdStr == null) || (templateIdStr.trim().equals(EMPTY_STRING)))
        	out.println("Please provide proper template id.");
        else{
        	templateIdStr = templateIdStr.trim();
        	templateId = Integer.parseInt(templateIdStr);
        }
        
        int deleted = TransmittalTemplate.deleteTransmittalTemplate(templateId);
		if (deleted > 0)
			out.println(true);
		else
			out.println(false);
	}

	private void handleTransmittalMapping(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, DatabaseException, IOException, TBitsException {
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();      
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
        
        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        
        String dtnBASysPrefix = aRequest.getParameter(DTN_SYS_PREFIX);
        if ((dtnBASysPrefix == null) || (dtnBASysPrefix.trim().equals(EMPTY_STRING)))
        	out.println("Please select proper DTN BA system prefix");
        else
        	dtnBASysPrefix = dtnBASysPrefix.trim();
        
        String latestBASysPrefix = aRequest.getParameter(LATEST_SYS_PREFIX);
        if ((latestBASysPrefix == null)||(latestBASysPrefix.trim().equals(EMPTY_STRING)))
        	out.println("Please select proper LATEST BA system prefix");
        else
        	latestBASysPrefix = latestBASysPrefix.trim();
        
        int systemId = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();        
        TransmittalTemplate.addTransmittalMapping(systemId, sysPrefix, dtnBASysPrefix, latestBASysPrefix);
        out.println("Saved transmittal mapping");
	}

	private void handleDeleteMapping(HttpServletRequest aRequest,
			HttpServletResponse aResponse) throws ServletException, TBitsException, DatabaseException, IOException {
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();      
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
        
        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        
        int deleted = TransmittalTemplate.removeTransmittalMapping(ba.getSystemId());
		if (deleted > 0)
			out.println(true);
		else
			out.println(false);
	}

	private void handleTemplateAddWindow(HttpServletRequest aRequest,
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
                
        int systemId = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
       /* SysConfig sc = ba.getSysConfigObject();        
        String baList = AdminUtil.getSysIdList(systemId, userId);*/

        DTagReplacer dTag = new DTagReplacer(ADD_TRANSMITTAL_TEMPLATE_HTM);        
        dTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
        dTag.replace("title", "TBits Admin: " + ba.getDisplayName() + " Add Transmittal Template");
        dTag.replace("sysPrefix", sysPrefix);
        dTag.replace("userLogin", user.getUserLogin());
        dTag.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
        dTag.replace("templateId", "-1");
        dTag.replace("templateName", "");
        dTag.replace("fileName", "");
        dTag.replace("toList", "");
        dTag.replace("ccList", "");
        dTag.replace("saveType", "insert");
        out.println(dTag.parse(systemId));
	}
	
	private void handleInsert (HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, DatabaseException, TBitsException{
		
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();      
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);
        
        int templateId = -1;
        String templateIdStr = aRequest.getParameter("templateId");
		if((templateIdStr == null) || templateIdStr.trim().equals(EMPTY_STRING))
			templateId = -1;
		else{
			templateIdStr = templateIdStr.trim();
			templateId = Integer.parseInt(templateIdStr);
		}
        
		String templateName = aRequest.getParameter(TEMPLATE_NAME);
		if((templateName == null) || templateName.trim().equals(EMPTY_STRING))
			out.println("Please specify templateName");
		else
			templateName = templateName.trim();
		
		String fileName = aRequest.getParameter(FILE_NAME);
		if((fileName == null) || fileName.trim().equals(EMPTY_STRING))
			out.println("Please specify the file name");
		else
			fileName = fileName.trim();
		
		String toList = aRequest.getParameter(TO_LIST);
		toList = (toList == null)? EMPTY_STRING : toList.trim();
		
		String ccList = aRequest.getParameter(CC_LIST);
		ccList = (ccList == null)? EMPTY_STRING : ccList.trim();		
		
		TransmittalTemplate tt = new TransmittalTemplate(ba.getSystemId(), templateId, templateName, fileName,toList,ccList);
		TransmittalTemplate.insert(tt);
		out.println("Added transmittal template entry. Please close the window.");		
	}
}
