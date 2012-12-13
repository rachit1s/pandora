/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.AdminUtil;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

/**
 * @author Lokesh
 * Handles all the requests from the clients for adding/deleting/modifying display groups
 *
 */
public class DisplayGroupHandler extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Logger that logs information/error messages to the Application Log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
    
    private static final String TBITS_ADD_DISPLAY_GROUP_HTM = "web/tbits-add-display-group.htm";

	private static final int USERS = 4;
    
	protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
	throws ServletException, IOException {
		try{
			handleRequest (aRequest, aResponse);
		} catch (DatabaseException e) {
			LOG.severe("",(e));
		} catch (TBitsException e) {
			LOG.severe("",(e));
		}
	}

	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
	throws ServletException, IOException {
		try{
			handleRequest (aRequest, aResponse);
		} catch (DatabaseException e) {
			LOG.severe("",(e));
		} catch (TBitsException e) {
			LOG.severe("",(e));
		}
	}
	
	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws  DatabaseException, TBitsException, IOException, ServletException{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();		
		User user = WebUtil.validateUser(request);
		String actionType = request.getParameter("actionType");
		if ((actionType == null) || (actionType.trim().equals(""))){
			//TODO return properly
			out.print("Please provide proper action type(\"add/edit/delete\")");
			return;
		}
		else
			actionType = actionType.trim();
		
		WebConfig userConfig = user.getWebConfigObject();        
        Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(request, userConfig, USERS);
        BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

        if (ba == null) {
            throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
        }
        
	    int systemId  = ba.getSystemId();
        String sysPrefix = ba.getSystemPrefix();
        
        
       if (actionType.equals("delete")){
        	String name = request.getParameter("name");
        	if ((name == null) || name.trim().equals(""))
        		out.println("Please provide the display group name to delete.");
        	else
        		name = name.trim();
        	
        	DisplayGroup dg = DisplayGroup.lookupBySystemIdAndDisplayName(systemId, name);
        	if (DisplayGroup.delete(dg) != null)
        		out.println(true);
        	else
        		out.println(false);
        }
        else if (actionType.equals("getDisplayGroups")){
        	ArrayList<DisplayGroup> dgList = DisplayGroup.lookupBySystemId(systemId);
        	JSONArray dgJSONArray = getJSONArrayFromList(dgList);
        	out.print(dgJSONArray.toString());
        }
        else if (actionType.equals("getAddPage")){
        	//DTagReplacer dTag = new DTagReplacer(TBITS_ADD_DISPLAY_GROUP_HTM);        	
        	//dTag.replace("nearestPath", WebUtil.getNearestPath(request, ""));	
        	//dTag.replace("sysPrefix", sysPrefix);
        	//dTag.replace("userLogin", user.getUserLogin());
        	JsonObject respObj = new JsonObject();
        	JsonPrimitive jPrim = new JsonPrimitive("create");
        	respObj.add("actionType", jPrim);
        	jPrim = new JsonPrimitive(0);
        	respObj.add("displayGroupId", jPrim);
        	jPrim = new JsonPrimitive("");
        	respObj.add("name", jPrim);
        	respObj.add("displayOrder", jPrim);
        	jPrim = new JsonPrimitive("off");        	
        	respObj.add("isActive", jPrim);
        	out.println(respObj.toString());
        	respObj.add("isDefault", jPrim);
        	out.println(respObj.toString());
        }
        else{
        	String dGIdStr = request.getParameter("displayGroupId");
        	if ((dGIdStr == null) || (dGIdStr.trim().equals(""))){
        		out.println("Please please an appropriate display group id.");
        		return;
        	}
        	else
        		dGIdStr = dGIdStr.trim();
        	int dGId = Integer.parseInt(dGIdStr);
        	
        	String name = request.getParameter("name");
        	if ((name == null) || name.trim().equals("")){
        		out.println("Invalid display group name: " + name + ", could not save.");
        		return;
        	}
        	else
        		name = name.trim();
        	
        	String displayOrderStr = request.getParameter("displayOrder");
        	if ((displayOrderStr == null) || displayOrderStr.trim().equals("")){
        		out.println ("Provide a display order for the display group: " + name);
        		return;
        	}
        	else
        		displayOrderStr = displayOrderStr.trim();        	
        	int displayOrder = Integer.parseInt(displayOrderStr);
        	
        	String isActiveStr = request.getParameter("isActive");
        	
        	String isDefaultStr = request.getParameter("isDeafult");
        	if (isActiveStr == null){
        		isActiveStr = "off";
        	}
        	else
        		isActiveStr = isActiveStr.trim();
        	if(isDefaultStr == null)
        	{ isDefaultStr="off";
        	
        	}
        	else 
        		isDefaultStr =isDefaultStr.trim();
        	
        	if (actionType.equals("create")){
            	boolean isActive = isActiveStr.equals("on")? true : false;
            	boolean isDefault = isDefaultStr.equals("on")? true : false;
            	DisplayGroup tempDG = DisplayGroup.lookupBySystemIdAndDisplayName(systemId, name);
            	if (tempDG == null){
            		DisplayGroup dg = new DisplayGroup(systemId, name, displayOrder,isActive,isDefault);
            		DisplayGroup.insert(dg);
            		out.println("Created new display group: " + name);
            	}
            	else{
            		out.println("Already a display group exists with the name: " + name + ". Hence did not add.");
            	}
            }
            else if (actionType.equals("save")){
            	boolean isActive = Boolean.parseBoolean(isActiveStr);
            	
            	if (dGId == 0){
            		out.println("Please provide an appropriate display group id. {" + dGId + "} is not valid.");
            		return;
            	}
            	else{            	
	            	DisplayGroup dg = DisplayGroup.lookupBySystemIdAndDisplayGroupId(systemId, dGId);
	            	dg.setDisplayName(name);
	            	dg.setDisplayOrder(displayOrder);
	            	dg.setIsActive(isActive);
	            	DisplayGroup tempDG = DisplayGroup.update(dg);
	            	if (tempDG != null)
	            		out.println(true);
	            	else
	            		out.println(false);   
            	}
            }
        }
	}
	
	/**Converts arraylist of display groups to JSON array.
	 * 
	 * @param dgList
	 * @return
	 */
	public JSONArray getJSONArrayFromList(ArrayList<DisplayGroup> dgList) {
		JSONArray dgJSONArray = new JSONArray();		
		dgJSONArray.setExpandElements(true);
		JSONObject dgObj = new JSONObject();
		for (DisplayGroup dg : dgList){
			dgObj.put("id", dg.getId());
			dgObj.put("name", dg.getDisplayName());
			dgObj.put("displayOrder", dg.getDisplayOrder());
			dgObj.put("is_active", dg.getIsActive());
			dgObj.put("is_default",dg.getIsDefault());
			dgObj.put("save", "Save");
			dgJSONArray.add(dgObj);		
		}
		return dgJSONArray;
	}

	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		//DisplayGroupHandler dg = new DisplayGroupHandler();
		//dg.getJSONArrayFromList(DisplayGroup.lookupAll());
		DisplayGroup dg = DisplayGroup.lookupBySystemIdAndDisplayName(19, "test display group");
		if (dg != null){			
			System.out.println("Update values..." + dg.getDisplayName() + ", " + dg.getDisplayOrder());
			dg.setDisplayOrder(17);
			dg.setIsActive(false);
		}
		DisplayGroup.update(dg);
		System.out.println("Done....");
	}

}
