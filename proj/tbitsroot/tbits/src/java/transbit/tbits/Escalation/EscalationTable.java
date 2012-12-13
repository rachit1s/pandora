/**
 * 
 */
package transbit.tbits.Escalation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.Helper.Messages;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

/**
 * @author Lokesh
 *
 */
public class EscalationTable extends HttpServlet {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_SCHEDULER);
	private static final String VALUE_FOR_NON_SELECTION = "--Any--";
	private int USERS = 4;
	
	public void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		try {
			handleRequest(aRequest, aResponse);
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
	}
	public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException 
	{
		try {
			handleRequest(aRequest, aResponse);
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
	}
	
	private void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, TBitsException, DatabaseException {
		aResponse.setContentType("text/html");
		PrintWriter out = aResponse.getWriter();
		
		User user = WebUtil.validateUser(aRequest);
    	WebConfig userConfig = user.getWebConfigObject();        
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        int aSystemId = ba.getSystemId();
		
        String ecTableAction = aRequest.getParameter("actionType");	System.out.println("$$$$$$actionType: " + ecTableAction);	
        if ((ecTableAction == null) || (ecTableAction.trim().equals(""))){
        	out.println("Please provide the action to be taken on report table (getTable/insert/delete).");
        	return;
        }
        ecTableAction = ecTableAction.trim();

        if (ecTableAction.equals("getTable")){        	
        	ArrayList<?> ecList = EscalationCondition.lookupEscConditionBySysId(aSystemId);
        	if (ecList != null){
        		JSONArray ecArray = getJSONArrayFromList(aSystemId, ecList);
        		out.println(ecArray.toString());
        	}
        }
        else{
        	String severityStr = aRequest.getParameter("severity");
        	if ((severityStr == null) || severityStr.trim().equals(""))
        		out.println("Please provide proper severity option");

        	String span = aRequest.getParameter("span");
        	if((span == null) || span.trim().equals(""))
        		out.println("Please provide proper span");

        	String categoryStr = aRequest.getParameter("category");
        	if ((categoryStr == null) || categoryStr.trim().equals(""))
        		out.println("Please provide proper category option");

        	String statusStr = aRequest.getParameter("status");
        	if((statusStr == null) || statusStr.trim().equals(""))
        		out.println("Please provide proper status opiton");

        	String typeStr = aRequest.getParameter("requestType");
        	if((typeStr == null) || typeStr.trim().equals(""))
        		out.println("Please provide proper type option");
        	
        	int severityId = 0;
			if (severityStr.trim().equals(VALUE_FOR_NON_SELECTION))
        		severityId = 0;
			else{
				Type severityType = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, Field.SEVERITY, severityStr.trim());
				severityId = severityType.getTypeId();       	 
			}
			
			int categoryId = 0;
			if (categoryStr.trim().equals(VALUE_FOR_NON_SELECTION))
				categoryId = 0;
			else{
				Type categoryType = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, Field.CATEGORY, categoryStr.trim());
				categoryId = categoryType.getTypeId();
			}
			
			int statusId = 0; 			
			if (statusStr.trim().equals(VALUE_FOR_NON_SELECTION))
				statusId = 0;
			else{
				Type statusType = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, Field.STATUS, statusStr.trim());
				statusId = statusType.getTypeId();
			}
			
			int reqTypeId = 0;
			if (typeStr.trim().equals(VALUE_FOR_NON_SELECTION))
				reqTypeId = 0;
			else{
				Type requestType = Type.lookupBySystemIdAndFieldNameAndTypeName(aSystemId, Field.REQUEST_TYPE, typeStr.trim());
				reqTypeId = requestType.getTypeId();
			}
        	
			EscalationCondition ec = new EscalationCondition(aSystemId, severityId,Integer.parseInt(span.trim()),
        			categoryId, statusId, reqTypeId);

        	if (ecTableAction.equals("insert")){
        		EscalationCondition.insert(ec);
        		out.println("Finished adding the escalation condition, please close the window.");
        		return;
        	}

        	if (ecTableAction.equals("delete")){
        		EscalationCondition.delete(ec);
        		out.println(true);
        		return;
        	}
        }
	}
	private static JSONArray getJSONArrayFromList(int aSystemId, ArrayList<?> ecList) throws DatabaseException {
		JSONArray ecArray = new JSONArray();
		for (Object ecObj : ecList){
			EscalationCondition ec = (EscalationCondition)ecObj;
			JSONObject jsonObj = new JSONObject();
			
			int tempId = ec.getSeverityId();
			
			if (tempId == 0)
				jsonObj.accumulate("severity_id", VALUE_FOR_NON_SELECTION);
			else
				jsonObj.accumulate("severity_id", Type.lookupBySystemIdAndFieldNameAndTypeId(aSystemId, Field.SEVERITY, tempId).getName());
			
			jsonObj.accumulate("span", ec.getSpan());
			
			tempId = ec.getCategoryId();
			if (tempId == 0)
				jsonObj.accumulate("category_id", VALUE_FOR_NON_SELECTION);
			else
				jsonObj.accumulate("category_id", Type.lookupBySystemIdAndFieldNameAndTypeId(aSystemId, Field.CATEGORY, tempId).getName());
			tempId =ec.getStatusId();
			if (tempId == 0)
				jsonObj.accumulate("status_id", VALUE_FOR_NON_SELECTION);
			else
				jsonObj.accumulate("status_id", Type.lookupBySystemIdAndFieldNameAndTypeId(aSystemId, Field.STATUS, tempId).getName());
			tempId = ec.getTypeId();
			if (tempId == 0)
				jsonObj.accumulate("type_id", VALUE_FOR_NON_SELECTION);
			else
				jsonObj.accumulate("type_id", Type.lookupBySystemIdAndFieldNameAndTypeId(aSystemId, Field.REQUEST_TYPE, tempId).getName());
			ecArray.add(jsonObj);
		}
		return ecArray;
	}
	
	public static void main(String[] args){
		try {
			ArrayList<?> testList = EscalationCondition.lookupEscConditionBySysId(3);
			System.out.println("List: " + getJSONArrayFromList(3, testList));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
