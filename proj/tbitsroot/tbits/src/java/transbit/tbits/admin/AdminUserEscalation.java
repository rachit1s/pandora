/**
 * 
 */
package transbit.tbits.admin;


import java.io.FileNotFoundException;
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

import transbit.tbits.Escalation.EscalationUtils;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.Messages;
import transbit.tbits.admin.common.AdminProxyServlet;
import transbit.tbits.admin.common.MenuItem;
import transbit.tbits.admin.common.NavMenu;
import transbit.tbits.admin.common.URLRegistry;
import transbit.tbits.common.DTagReplacer;
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
public class AdminUserEscalation extends HttpServlet {	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Logger that logs information/error messages to the Application Log.
	public static final TBitsLogger LOG        = TBitsLogger.getLogger(TBitsConstants.PKG_ADMIN);
	private static final String TBITS_ADMIN_ESCALATION_HIERARCHY_HTM = "web/tbits-admin-escalation-hierarchy.htm";
	private static final String TBITS_ADMIN_ESCALATION_CONDITION_HTM = "web/tbits-admin-escalation-conditions.htm"; 
	private static final int        USERS      = 4;
	private static final String EMPTY_STRING = "";
	private static final String VALUE_FOR_NON_SELECTION = "--Any--";

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
			HttpServletResponse aResponse) throws IOException, DatabaseException, TBitsException, ServletException {
		PrintWriter out = aResponse.getWriter();
		User user = WebUtil.validateUser(aRequest);    	
		WebConfig userConfig = user.getWebConfigObject();

		Hashtable<String, Object> paramTable = AdminUtil.getRequestParams(aRequest, userConfig, USERS);
		BusinessArea ba = (BusinessArea) paramTable.get(Field.BUSINESS_AREA);

		if (ba == null) {
			throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
		}

		//int userId = user.getUserId();
		int systemId  = ba.getSystemId();
		
		String pageType = aRequest.getParameter("pageType");
		if ((pageType == null) || (pageType.trim().equals(""))){ 
			out.println("Please provide proper page type"); 
			return;
		}
		else
			pageType = pageType.trim(); 
		
		if (pageType.equals("hierarchy")){
			String subAction = aRequest.getParameter("sub_action");
			if ((subAction == null) || subAction.trim().equals(""))
				out.print("Please provide proper sub_action to take (either view/save heirarchy)");
			else
				subAction = subAction.trim();
			
			ArrayList<User> activeUsers = User.getActiveUsers();
			User.setSortParams(2, 0);
			activeUsers = User.sort(activeUsers);

			String strUserId = aRequest.getParameter("user_id");
			int selectedUserId = -1;
			if(strUserId == null){
				//throw new TBitsException(Messages.getMessage("INVALID_TRANSBIT_USER"));
				selectedUserId = activeUsers.get(0).getUserId();        	
			}
			else
				selectedUserId = Integer.parseInt(strUserId);        

			User selectedUser = User.lookupByUserId(selectedUserId);

			if(selectedUser == null)
				throw new TBitsException(Messages.getMessage("INVALID_TRANSBIT_USER"));
			else if (subAction.equals("view")){
				out.println(getUserHierarchy(systemId, selectedUserId));
			}
			else if (subAction.equals("save")){
				/*String parentUsers = aRequest.getParameter("parentUsers");
				parentUsers = (parentUsers == null)? "" : parentUsers.trim();*/
				String childUsers = aRequest.getParameter("childUsers");
				childUsers = (childUsers == null) ? "" : childUsers.trim();
				String insertionMsg = insertHeirarchy(ba.getSystemId(), selectedUserId, childUsers);
				out.println(insertionMsg);
			}
			else if (subAction.equals("deleteFromHierarchy")){
				String escUserType = aRequest.getParameter("userType");
				if ((escUserType == null) || (escUserType.trim().equals(""))){
					out.println("Did not delete any user");
					return;
				}
				else
					escUserType = escUserType.trim();
				
				int escUserId = -1;
				String escUserIdStr = aRequest.getParameter("userId");
				if ((escUserIdStr == null) || escUserIdStr.trim().equals("")){
					out.println("No child/parent user found");
					return;
				}
				else{
					escUserIdStr = escUserIdStr.trim();
					escUserId = Integer.parseInt(escUserIdStr);					
				}
				
				User deletionUser = null;
				String deletionUserLogin = aRequest.getParameter("removeUser");
				if ((deletionUserLogin == null) || (deletionUserLogin.trim().equals(""))){
					out.println ("Could not find the child/parent user who has to be deleted from hierarchy.");
					return;
				}
				else{
					deletionUserLogin = deletionUserLogin.trim();
					deletionUser = User.lookupByUserLogin(deletionUserLogin);
					if (deletionUser == null){
						out.println("Could not find user for id: " + escUserId);
						return;
					}
				}
				
				deleteHierarchyUser(systemId, escUserType, escUserId,
						deletionUser);				
			}
		}
		else if (pageType.equals("condition")){
			DTagReplacer escConditionTag = new DTagReplacer(TBITS_ADMIN_ESCALATION_CONDITION_HTM);
			escConditionTag.replace("nearestPath", WebUtil.getNearestPath(aRequest, EMPTY_STRING));
			escConditionTag.replace("userLogin", user.getUserLogin());
			escConditionTag.replace("sysPrefix", ba.getSystemPrefix());
			escConditionTag.replace("display_logout", "none");
			
			String severityDName = Field.lookupBySystemIdAndFieldName(systemId, Field.SEVERITY).getDisplayName();
			escConditionTag.replace("severityName", severityDName);
			ArrayList<Type> tmpType = Type.lookupAllBySystemIdAndFieldName(systemId, Field.SEVERITY);			
			escConditionTag.replace("severity_ids", getTypeHtml(systemId, tmpType));		
			
			String catDisplayName = Field.lookupBySystemIdAndFieldName(systemId, Field.CATEGORY).getDisplayName();
			escConditionTag.replace("categoryName", catDisplayName);
			tmpType = Type.lookupAllBySystemIdAndFieldName(systemId, Field.CATEGORY);
			escConditionTag.replace("category_ids", getTypeHtml(systemId, tmpType));
			
			String statusDName = Field.lookupBySystemIdAndFieldName(systemId, Field.STATUS).getDisplayName();
			escConditionTag.replace("statusName", statusDName);
			tmpType = Type.lookupAllBySystemIdAndFieldName(systemId, Field.STATUS);
			escConditionTag.replace("status_ids",  getTypeHtml(systemId, tmpType));
			
			String reqTypeDName = Field.lookupBySystemIdAndFieldName(systemId, Field.REQUEST_TYPE).getDisplayName();
			escConditionTag.replace("requestTypeName", reqTypeDName);
			tmpType = Type.lookupAllBySystemIdAndFieldName(systemId, Field.REQUEST_TYPE);
			escConditionTag.replace("type_ids", getTypeHtml(systemId, tmpType));
			out.println(escConditionTag.parse(systemId));
			return;
		}
	}

	/**
	 * @param systemId
	 * @param escUserType
	 * @param escUserId
	 * @param deletionUser
	 * @throws DatabaseException
	 */
	private void deleteHierarchyUser(int systemId, String escUserType,
			int escUserId, User deletionUser) throws DatabaseException {
		if (escUserType.equals("parentUser")){
			System.out.println("Deleting parent: " + systemId + "," + deletionUser.getUserId()+ "," + escUserId);
			EscalationUtils.deleteUserHierarchy(systemId, escUserId, deletionUser.getUserId());
		}
		else if (escUserType.equals("childUser")){
			System.out.println("Deleting child: " + systemId + "," + escUserId +"," + deletionUser.getUserId());
			EscalationUtils.deleteUserHierarchy(systemId, deletionUser.getUserId(), escUserId);
		}
	}

	private String insertHeirarchy(int aSystemId, int aUserId,
			String childUsers) throws DatabaseException {
		
		//EscalationUtils.deleteUserHierarchy(aSystemId, aUserId);		
		/*if ((parentUsers != null) || !parentUsers.trim().equals(""))
			usersArray = JSONArray.fromObject(parentUsers);
		
		for (Object userLogin : usersArray){
			User usr = User.lookupAllByUserLogin((String)userLogin);
			System.out.println("usr: " + usr.getUserLogin());
			EscalationUtils.insertUserHierarchy(aSystemId, aUserId, usr.getUserId());
		}		
		usersArray.clear();
		usersArray = null;*/
		JSONArray usersArray = null;
		if ((childUsers != null) && (!childUsers.trim().equals("")))
			usersArray = JSONArray.fromObject(childUsers);
		String insertionMsg = "";
		for (Object userLogin : usersArray){
			System.out.println("user: " + userLogin);
			User usr = User.lookupByUserLogin((String)userLogin);
			System.out.println("After lookup: " + usr.getDisplayName() + ";" + aSystemId  + "," + usr.getUserId() + "," + aUserId);
			insertionMsg = insertionMsg + "\n" +EscalationUtils.insertUserHierarchy(aSystemId, usr.getUserId(), aUserId);			
		}
		return insertionMsg;
	}

	private String getUserHierarchy(int aSystemId, int aUserId) throws FileNotFoundException, IOException, TBitsException{    	
		DTagReplacer tempTag = new DTagReplacer(TBITS_ADMIN_ESCALATION_HIERARCHY_HTM);
		/*ArrayList<String> userList = EscalationUtils.getParentUsers(aSystemId, aUserId);
		tempTag.replace("parent_ids", getUserHTML(userList));*/
		ArrayList<String> userList = EscalationUtils.getChildUsers(aSystemId, aUserId);   	
		tempTag.replace("child_ids", EscalationUtils.getUserHTML(userList));  	
		return tempTag.parse(aSystemId);
	}
	
	private String getTypeHtml (int aSystemId, ArrayList<Type> typeList){		
		StringBuffer sb = new StringBuffer();		
		sb.append("<option value='").append(VALUE_FOR_NON_SELECTION).append("'>").append(VALUE_FOR_NON_SELECTION).append("</option>");
		for (Type t : typeList){
			sb.append("<option value='").append(t.getName()).append("'>").append(t.getDisplayName()).append("</option>");
		}
		return sb.toString();
	}

	/*private String getUserHTML(ArrayList<String> userList){
		StringBuffer sb = new StringBuffer();
		for (String userLogin : userList){
			sb.append("<option value='").append(userLogin).append("'>").append(userLogin).append("</option>").append("\n");
		}
		return sb.toString();
	}*/
	
	public static void main(String[] args) throws DatabaseException{
		
			//System.out.println("Child users: " + EscalationUtils.getChildUsers(6, 50049).toString());
			//System.out.println("Child users insertion: " + EscalationUtils.insertUserHierarchy(6,19,16));
			//AdminUserEscalation aue = new AdminUserEscalation();
			//aue.insertHeirarchy(6, 16, "[\"ritesh\"]");
			ArrayList<String> userList = new ArrayList<String>();
			for (User usr: User.lookupAll()){
				userList.add(usr.getUserLogin());
			}
			System.out.println(EscalationUtils.getUserHTML(userList));
			/*User usr = User.lookupAllByUserId(16);
			System.out.println("Delete users: "); aue.deleteHierarchyUser(6, "parentUser", 50061, usr);*/
			System.out.println("Done...");		
	}
}