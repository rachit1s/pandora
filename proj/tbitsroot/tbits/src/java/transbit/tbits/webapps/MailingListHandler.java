/**
 * 
 */
package transbit.tbits.webapps;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import transbit.tbits.api.Mapper;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.MailListUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import static transbit.tbits.api.Mapper.ourMailListUserMap;

/**
 * @author Lokesh
 * Handles all the requests from the clients for adding/deleting/modifying mailing lists
 *
 */
public class MailingListHandler extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Logger that logs information/error messages to the Application Log.
    private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);    
    private static final String TBITS_ADD_MAILING_LIST_HTM = "web/tbits-add-mailing-list.htm";
    
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
		
		if (actionType.equals("delete")){
			String name = request.getParameter("name");
			if ((name == null) || name.trim().equals(""))
				out.println("Please select a mailing list to save.");
			else
				name = name.trim(); 	
			delete(name);
			out.print(true);
		}
		else if (actionType.equals("getMailingLists")){
			JSONArray dgJSONArray = getJSONArrayOfMailingLists();
			out.print(dgJSONArray.toString());
		}
		else if (actionType.equals("getAddPage")){
			DTagReplacer dTag = new DTagReplacer(TBITS_ADD_MAILING_LIST_HTM);
			dTag.replace("nearestPath", WebUtil.getNearestPath(request, ""));	
			dTag.replace("userLogin", user.getUserLogin());
			dTag.replace("mailingListName", "");
			dTag.replace("mailingListUsers", "");
			dTag.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
			dTag.replace("disable_mlName", "");
			dTag.replace("saveType", "create");
			out.println(dTag.parse(0));
		}
		else if (actionType.equals("getEditPage")){
			String mailingListName = request.getParameter("name");
			if ((mailingListName == null) || (mailingListName.trim().equals(""))){
				out.println("Please select a mailing list");
				return;
			}
			else
				mailingListName = mailingListName.trim();
			User mlUser = User.lookupAllByUserLogin(mailingListName);
			if (mlUser != null){
				ArrayList<User> arrayList = ourMailListUserMap.get(mlUser.getUserId());
				JSONObject mailListJSONObject = getMailingListJSONObjectByName(mailingListName, arrayList);
				DTagReplacer dTag = new DTagReplacer(TBITS_ADD_MAILING_LIST_HTM);
				dTag.replace("nearestPath", WebUtil.getNearestPath(request, ""));
				dTag.replace("userLogin", user.getUserLogin());
				dTag.replace("mailingListName", mailingListName);
				JSONArray usersArray = mailListJSONObject.getJSONArray("users");
				StringBuffer usersStr = new StringBuffer("");        	
				for (Object loginName : usersArray){
					String tmpStr = (String)loginName;
					if (usersStr.toString().equals(""))
						usersStr.append(tmpStr); 
					else
						usersStr.append(",").append(tmpStr);
				}
				dTag.replace("usersList", ReportUtil.getJSONArrayOfUsers().toString());
				dTag.replace("mailingListUsers", usersStr.toString());
				dTag.replace("disable_mlName", "disabled");
				dTag.replace("saveType", "update");
				out.println(dTag.parse(0));
			}
			else{
				out.println("Please select a mailing list");
				return;
			}
		}
		else{
			String mlName = request.getParameter("mailingListName");
			if ((mlName == null) || mlName.trim().equals("")){
				out.println("Invalid mailing list name: " + mlName + ", could not save.");
				return;
			}
			else
				mlName = mlName.trim();

			String mlUsersList = request.getParameter("mailingListUsers");        	
			if ((mlUsersList == null) || mlUsersList.trim().equals("")){
				out.println("No mailing list users found for mailing list name: " + mlName + ", hence could not save.");
				return;
			}
			else
				mlUsersList = mlUsersList.trim();

			User mlUser = User.lookupByUserLogin(mlName);
			if (mlUser != null){
        		delete(mlName);
        		for (String loginName : mlUsersList.split(",")){
        			loginName = loginName.trim();
        			if (!loginName.equals("")){
        				User tmpUser = User.lookupByUserLogin(loginName);
        				if (tmpUser != null)
        					insert(mlUser.getUserId(), tmpUser.getUserId());
        			}        			
        		} 
        		Mapper.refreshUserMapper();
        		out.println("Successfully saved mailing list. Please close the window.");
        		out.flush() ;
        	}
			else
			{
				out.println("Mailing list with name ( " + mlName +  " ) not found. Please close this dialog. And first create the mailing list on the AllUsers page"  ) ;
				out.flush() ;
			}
        }
	}
	
	public static void insert(int mailListId, int userId) throws DatabaseException{
		Connection aCon = null;
		try {
			aCon = DataSourcePool.getConnection();
			PreparedStatement ps = aCon.prepareStatement("INSERT INTO mail_list_users (mail_list_id,user_id) VALUES (" +  
					+ mailListId + "," + userId + ")");
			ps.execute();
			ps.close();
			
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception occured while insert mailing list:" + mailListId + " and user_id:" + userId);

			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (aCon != null) {
				try {
					aCon.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					// Should this be logged.?
				}
			}
		}
		Mapper.refreshUserMapper();
	}	
	
	public static void delete(String mailingListName) throws DatabaseException{
		Connection aCon = null;
		try {
			
			aCon = DataSourcePool.getConnection();
			User mlUser = User.lookupAllByUserLogin(mailingListName);
			if (mlUser != null){
				PreparedStatement ps = aCon.prepareStatement("DELETE FROM mail_list_users where mail_list_id=" 
						+ mlUser.getUserId());
				ps.execute();
				ps.close();
			}
//			aCon.close();
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception occured while deleting the mailing list: " + mailingListName);

			throw new DatabaseException(message.toString(), sqle);
		} finally {
			if (aCon != null) {
				try {
					aCon.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					// Should this be logged.?
				}
			}
		}
		Mapper.refreshUserMapper();
	}	
	
	private static JSONArray getJSONArrayOfMailingLists() throws DatabaseException{
		JSONArray mailingListMap = new JSONArray();
		Set<Integer> keySet = ourMailListUserMap.keySet();
		for (int key : keySet){
			User mLUser = User.lookupAllByUserId(key);
			if (mLUser != null){
				String mLUserLogin = mLUser.getUserLogin();
				ArrayList<User> usersList = ourMailListUserMap.get(key);
				JSONObject jObj = getMailingListJSONObjectByName(mLUserLogin, usersList);
				mailingListMap.add(jObj);
			}
		}
		return mailingListMap;
	}

	/**
	 * @param mLUser
	 * @param usersList
	 * @param jObj
	 * @return
	 */
	private static JSONObject getMailingListJSONObjectByName(String mLUserLogin,
			ArrayList<User> usersList) {
		JSONObject jObj = null;
		if((usersList != null) && (!usersList.isEmpty())){			
			jObj = new JSONObject();
			jObj.put("name", mLUserLogin);
			JSONArray uArray = new JSONArray();
			for (User user : usersList){
				if (user != null)
					uArray.add(user.getUserLogin());
			}
			jObj.put("users", uArray);
			jObj.put("edit", "Edit");
			//jObj.put("delete", "Delete");				
		}
		return jObj;
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		//DisplayGroupHandler dg = new DisplayGroupHandler();
		//dg.getJSONArrayFromList(DisplayGroup.lookupAll());
	/*	DisplayGroup dg = DisplayGroup.lookupBySystemIdAndDisplayName(19, "test display group");
		if (dg != null){			
			System.out.println("Update values..." + dg.getDisplayName() + ", " + dg.getDisplayOrder());
			dg.setDisplayOrder(17);
			dg.setIsActive(false);
		}
		DisplayGroup.update(dg);*/
		/*System.out.println("uList: " + ourMailListUserMap.get(23));
		JSONObject temp = getMailingListJSONObjectByName("LCService", ourMailListUserMap.get(23));
		String tmpString = temp.getString("users");*/
		//delete("LCService");
		/*insert(21,22);
		System.out.println("Done...." );*/
		System.out.println("Mailing lists: " + getJSONArrayOfMailingLists());
	}

}
