package transbit.tbits.domain;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.exception.TBitsException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Lokesh
 *
 */
public class BAMenu implements Serializable {	
	
	private int myMenuId;
	private String myMenuCaption;
	private int myParentMenuId;
	
	private ArrayList<Integer> subMenuIds;
	private ArrayList<Integer> baIds;

	//Menu button for sysPrefixes
    private static final String MENU_BUTTON_SUBMENU = "submenu";
	private static final String MENU_BUTTON_ITEMDATA = "itemdata";
	private static final String HTML_ID_PROPERTY = "id";
	private static final String HTML_VALUE_PROPERTY = "value";
	private static final String HTML_TEXT_PROPERTY = "text";
	
	//Menu table column names
	private static final String PARENT_MENU_ID = "parent_menu_id";
	private static final String MENU_CAPTION = "menu_caption";
	private static final String MENU_ID = "menu_id";
	private static final String SYS_ID = "sys_id";
	
	public BAMenu(){
		
	}
	
	/**
	 * 
	 * @param menuId
	 * @param menuCaption
	 * @param parentMenuId
	 */
	public BAMenu(int menuId, String menuCaption, int parentMenuId){
		myMenuId = menuId;
		myMenuCaption = menuCaption;
		setParentMenuId(parentMenuId);
	}
	
	public BAMenu(int myMenuId, String myMenuCaption, int myParentMenuId, ArrayList<Integer> subMenuIds, ArrayList<Integer> baIds) {
		this(myMenuId, myMenuCaption, myParentMenuId);
		
		this.setParentMenuId(myParentMenuId);
		this.subMenuIds = subMenuIds;
	}
	
	public int getMenuId(){
		return myMenuId;
	}
	
	public String getMenuCaption(){
		return myMenuCaption;
	}

	public int getParentMenuId(){
		return myParentMenuId;
	}
	
	public void setBaIds(ArrayList<Integer> baIds) {
		this.baIds = baIds;
	}

	public ArrayList<Integer> getBaIds() {
		return baIds;
	}

	public void setSubMenuIds(ArrayList<Integer> subMenuIds) {
		this.subMenuIds = subMenuIds;
	}

	public ArrayList<Integer> getSubMenuIds() {
		return subMenuIds;
	}
	
	@Override
	public String toString() {
		return "[" + myMenuId + ", " + myMenuCaption + ", " + getParentMenuId() + "]";
	}
	
	/**
	 * Returns menu object for a given menu id.
	 * @param aBAMenuId
	 * @return
	 * @throws DatabaseException
	 */
	public static BAMenu lookupBAMenuByMenuId(int aBAMenuId) throws DatabaseException{
		BAMenu baMenu = null;
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * from ba_menu_table WHERE menu_id=?");
			ps.setInt(1, aBAMenuId);
			ResultSet rs = ps.executeQuery();
			if (rs!=null)
				while (rs.next()){
					String menuCaption = rs.getString(MENU_CAPTION);
					int parentMenuId = rs.getInt(PARENT_MENU_ID);
					baMenu = new BAMenu(aBAMenuId, menuCaption, parentMenuId);
				}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving BAMenu for: " +
					+ aBAMenuId, sqle);
		}finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}		
		return baMenu;
	}
	
	/**
	 * Returns an ArrayList of all existing BAMenus
	 * @return
	 * @throws DatabaseException 
	 */
	public static ArrayList<BAMenu> getAllBAMenus() throws DatabaseException{
		ArrayList<BAMenu> baMenuList = new ArrayList<BAMenu>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_table");
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					BAMenu baMenu = new BAMenu(rs.getInt(MENU_ID), rs.getString(MENU_CAPTION), 
													rs.getInt(PARENT_MENU_ID));
					if (baMenu != null)
						baMenuList.add(baMenu);
				}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving all BAMenus", sqle);
		} finally {
			if(conn != null)
				try {
					conn.close();
				}catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return baMenuList;
	}
	
	public static boolean delete(int menuId) throws TBitsException, DatabaseException{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_table where parent_menu_id = ?");
			ps.setInt(1, menuId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				if(rs.next()){
					throw new TBitsException("Submenu to this menu exists");
				}
			}
			rs.close();
			ps.close();
			
			ps = conn.prepareStatement("DELETE FROM ba_menu_table where menu_id = ?");
			ps.setInt(1, menuId);
			ps.execute();
			ps.close();
			
			ps = conn.prepareStatement("DELETE FROM ba_menu_mapping where menu_id = ?");
			ps.setInt(1, menuId);
			ps.execute();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving all BAMenus", sqle);
		} finally {
			if(conn != null)
				try {
					conn.close();
				}catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return true;
	}
	
	public static BAMenu insert(BAMenu menu) throws TBitsException, DatabaseException{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_table where menu_id = ?");
			ps.setInt(1, menu.getMenuId());
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				if(rs.next()){
					throw new TBitsException("Menu already exists");
				}
			}
			rs.close();
			ps.close();
			
			int maxId = 0;
			conn = DataSourcePool.getConnection();
			ps = conn.prepareStatement("SELECT max(menu_id) FROM ba_menu_table");
			rs = ps.executeQuery();
			if (rs != null){
				if(rs.next()){
					maxId = rs.getInt(1);
				}
			}
			rs.close();
			ps.close();
			
			ps = conn.prepareStatement("INSERT INTO ba_menu_table(menu_id, menu_caption, parent_menu_id) " +
					"VALUES(?,?,?)");
			ps.setInt(1, maxId + 1);
			ps.setString(2, menu.getMenuCaption());
			ps.setInt(3, menu.getParentMenuId());
			ps.execute();
			ps.close();
			
			return new BAMenu(maxId + 1, menu.getMenuCaption(), menu.getParentMenuId());
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while inserting BAMenu", sqle);
		} finally {
			if(conn != null)
				try {
					conn.close();
				}catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public static BAMenu update(BAMenu menu) throws TBitsException, DatabaseException{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_table where menu_id = ?");
			ps.setInt(1, menu.getMenuId());
			ResultSet rs = ps.executeQuery();
			if (rs == null || !rs.next()){
				throw new TBitsException("Menu does not exists");
			}
			rs.close();
			ps.close();
			
			ps = conn.prepareStatement("UPDATE ba_menu_table SET menu_caption = ? , parent_menu_id = ? " +
					"where menu_id = ?");
			ps.setString(1, menu.getMenuCaption());
			ps.setInt(2, menu.getParentMenuId());
			ps.setInt(3, menu.getMenuId());
			ps.execute();
			ps.close();
			
			return new BAMenu(menu.getMenuId(), menu.getMenuCaption(), menu.getParentMenuId());
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while updating BAMenu", sqle);
		} finally {
			if(conn != null)
				try {
					conn.close();
				}catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public static boolean updateMapping(int menuId, int sysId) throws DatabaseException{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			PreparedStatement ps = conn.prepareStatement("DELETE FROM ba_menu_mapping where sys_id = ?");
			ps.setInt(1, sysId);
			ps.execute();
			ps.close();
			
			ps = conn.prepareStatement("INSERT INTO ba_menu_mapping(menu_id, sys_id) VALUES(?,?)");
			ps.setInt(1, menuId);
			ps.setInt(2, sysId);
			ps.execute();
			ps.close();
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while updating BAMenu Mapping", sqle);
		} finally {
			if(conn != null)
				try {
					conn.close();
				}catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return true;
	}
	
	public static List<Integer> getMapping(int menuId) throws DatabaseException{
		List<Integer> sysIds = new ArrayList<Integer>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_mapping where menu_id = ?");
			ps.setInt(1, menuId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					int sysId = rs.getInt("sys_id");
					sysIds.add(sysId);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving all BAMenus", sqle);
		} finally {
			if(conn != null)
				try {
					conn.close();
				}catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return sysIds;
	}
	
	/**
	 * Returns a list of all BAMenus in JSON format
	 * @return
	 * @throws DatabaseException
	 */
	public static JsonArray getJsonArrayOfAllBAMenus() throws DatabaseException{
		JsonArray baMenuArray = new JsonArray();
		ArrayList<BAMenu> baMenuList =getAllBAMenus();
		if (baMenuList != null)
			for (BAMenu baMenu : baMenuList){
				JsonPrimitive pCaption = new JsonPrimitive(baMenu.getMenuCaption());
				baMenuArray.add(pCaption);	
			}
		return baMenuArray;
	}
	
	/**
	 * Returns a list of BAMenu ids for a given BAMenuId.
	 * @param aBAMenuId
	 * @return
	 * @throws DatabaseException
	 */
	public static ArrayList<BAMenu> getChildBAMenus(int aBAMenuId) throws DatabaseException{
		
		ArrayList<BAMenu> baMenuList = new ArrayList<BAMenu>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_table where parent_menu_id=?");
			ps.setInt(1, aBAMenuId);
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					int menuId = rs.getInt(MENU_ID);
					String menuCaption = rs.getString(MENU_CAPTION);					
					BAMenu baMenu = new BAMenu(menuId, menuCaption, aBAMenuId);
					baMenuList.add(baMenu);
				}						
			}
			rs.close();
			ps.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving child " +
					"menu items for menu: " + aBAMenuId, sqle);
		}finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}		
		return baMenuList;
	}
	
	/**
	 * Returns a list of Business Areas related to a BAMenu.
	 * @param aBAMenuId
	 * @return
	 * @throws DatabaseException 
	 */
	public static ArrayList<BusinessArea> getChildBusinessAreas(int aBAMenuId) throws DatabaseException{
		
		ArrayList<BusinessArea> baList = new ArrayList<BusinessArea>();
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_mapping WHERE menu_id=?");
			ps.setInt(1, aBAMenuId);
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				while(rs.next()){
					int tempSysId = rs.getInt("sys_id");
					BusinessArea tempBA = BusinessArea.lookupBySystemId(tempSysId);
					if (tempBA != null)
						baList.add(tempBA);
				}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving business areas " +
					"menu: " + aBAMenuId, sqle);
		}finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return baList;
	}
	
	/**
	 * Returns BAMenu for a given system id.
	 * @param aSystemId
	 * @return
	 * @throws DatabaseException
	 */	 
	public static BAMenu lookupBAMenuBySystemId(int aSystemId) throws DatabaseException{
		
		BAMenu baMenu = null;
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_mapping WHERE sys_id=?");
			ps.setInt(1, aSystemId);			
			ResultSet rs = ps.executeQuery();
			if (rs != null)
				if(rs.next()){
					int tempMenuId = rs.getInt(MENU_ID);
					baMenu = BAMenu.lookupBAMenuByMenuId(tempMenuId);
				}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving " +
					"menu for system id: " + aSystemId, sqle);
		}finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

		return baMenu;
	}	
	
	/**
	 * Returns All BA Menus
	 * @return
	 * @throws DatabaseException
	 */	 
	public static HashMap<Integer, BAMenu> getBAMenuMap() throws DatabaseException{
		Connection conn = null;
		
		HashMap<Integer, BAMenu> menuMap = new HashMap<Integer, BAMenu>();
		
		try {
			conn = DataSourcePool.getConnection();
			
			HashMap<Integer,ArrayList<Integer>> menuBAMap = new HashMap<Integer, ArrayList<Integer>>();
			
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM ba_menu_mapping");
			
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					int tempMenuId = rs.getInt(MENU_ID);
					if(!menuBAMap.containsKey(tempMenuId)){
						ArrayList<Integer> arr = new ArrayList<Integer>();
						menuBAMap.put(tempMenuId, arr);
					}
					ArrayList<Integer> arr = menuBAMap.get(tempMenuId);
					int sysId = rs.getInt(SYS_ID);
					arr.add(sysId);
				}
			}
			rs.close();
			ps.close();
			
			PreparedStatement ps1 = conn.prepareStatement("SELECT * from ba_menu_table");
			ResultSet rs1 = ps1.executeQuery();
			if (rs1 != null){
				while(rs1.next()){
					int tempMenuId = rs1.getInt(MENU_ID);
					String menuCaption = rs1.getString(MENU_CAPTION);
					int parentMenuId = rs1.getInt(PARENT_MENU_ID);
					
					BAMenu menu = new BAMenu(tempMenuId, menuCaption, parentMenuId);
					if(menuBAMap.containsKey(tempMenuId))
						menu.setBaIds(menuBAMap.get(tempMenuId));
					
					menuMap.put(tempMenuId, menu);
				}
			}
			rs1.close();
			ps1.close();
			
			for(int menuId : menuMap.keySet()){
				BAMenu menu = menuMap.get(menuId);
				int parentMenuId = menu.getParentMenuId();
				if(menuMap.containsKey(parentMenuId)){
					BAMenu parentMenu = menuMap.get(parentMenuId);
					if(parentMenu.getSubMenuIds() == null)
						parentMenu.setSubMenuIds(new ArrayList<Integer>());
					parentMenu.getSubMenuIds().add(menu.getMenuId());
				}
			}
		}catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Error occurred while retrieving while building BA Menu", sqle);
		}finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

		return menuMap;
	}
	
	/**
	 * Returns 1 if the BAMenu is a child menu; returns 0 if not a child and returns -1 if the BAMenu is not
	 * found for the given id.
	 * @param aBAMenuId
	 * @return
	 * @throws DatabaseException
	 */
	public static boolean isChildBAMenu(int aBAMenuId) throws DatabaseException{
		
		boolean isChild = false;
		BAMenu baMenu = lookupBAMenuByMenuId(aBAMenuId);
		if (baMenu != null) 
			if (baMenu.getParentMenuId() > 0)
				isChild = false;
			else
				isChild = true;		
		return isChild;
	}
	
	/**
	 * Returns JsonArray for a given
	 * @param baList
	 * @return
	 * @throws DatabaseException 
	 */
	public static JsonArray getBAMenuJsonArray(ArrayList<BusinessArea> baList) throws DatabaseException{
		
		JsonArray baJsonArray = new JsonArray();
		ArrayList<BAMenu> baMenuList = new ArrayList<BAMenu>();
		Hashtable<BAMenu, ArrayList<BAMenu>> baMenuMap = new Hashtable<BAMenu, ArrayList<BAMenu>>();	
		ArrayList<BusinessArea> independentBAList = new ArrayList<BusinessArea>();
				
		//Get all the applicable BAMenus for the business areas related to the user.
		for (BusinessArea tBA : baList){
			// Retrieves the parent menu for the current BA "tBA".
			BAMenu baMenu = lookupBAMenuBySystemId(tBA.getSystemId());
			if (baMenu != null){ 
				baMenuList.add(baMenu);				
				if (baMenu.getParentMenuId()>0){
					//Get the parent menu for the menu fetched.
					BAMenu parentBAMenu = BAMenu.lookupBAMenuByMenuId(baMenu.getParentMenuId());
					if (parentBAMenu != null)
						baMenuList.add(parentBAMenu);
				}
				//Create unique objects list
				Set<BAMenu> tSet = new HashSet<BAMenu>(baMenuList);
				baMenuList.clear();
				baMenuList.addAll(tSet);				
			}
			else{
				independentBAList.add(tBA);
			}
		}
		
		//Map parent-child menu items
		if ((baMenuList != null) && (!baMenuList.isEmpty())){
			for (BAMenu tBAMenu : baMenuList){
				int parentMenuId = tBAMenu.getMenuId();
				ArrayList<BAMenu> childMenuList = getChildBAMenus(parentMenuId);
				if (childMenuList != null)
					baMenuMap.put(tBAMenu, childMenuList);
			}
		}
		
		//Get the whole BAMenu tree
		Set<BAMenu> keySet = baMenuMap.keySet();
		for (BAMenu parentBAMenu : keySet){
			if (parentBAMenu.getParentMenuId() == 0){
				JsonObject topNode = getTopNode(baMenuMap, parentBAMenu, baList);
				if ((topNode != null) && (!topNode.isJsonNull()))
					baJsonArray.add(topNode);
			}
		}
		
		//Insert Business areas which are independent of any project/group.
		for(BusinessArea tBA : independentBAList){
        	if (tBA != null){
        		JsonObject tObj = getBANode(tBA);
        		baJsonArray.add(tObj);
        	}
        }       
		
		return baJsonArray;
	}
	
	
	/**
	 * 
	 * @param baMenuMap
	 * @param pBAMenu
	 * @param userBAList 
	 * @return
	 * @throws DatabaseException 
	 */
	
	private static JsonObject getTopNode(
			Hashtable<BAMenu, ArrayList<BAMenu>> baMenuMap,
			BAMenu pBAMenu,  ArrayList<BusinessArea> userBAList) throws DatabaseException {
		
		JsonObject topNode = new JsonObject();
		String menuCaption = pBAMenu.getMenuCaption();
		topNode.addProperty(HTML_TEXT_PROPERTY, menuCaption);
		
		ArrayList<BAMenu> tmpChildList = baMenuMap.get(pBAMenu);
		if (tmpChildList != null){
			JsonObject childNode = new JsonObject();
			childNode.addProperty(HTML_ID_PROPERTY, menuCaption + pBAMenu.getMenuId());
			JsonArray nLvlNodes = new JsonArray();
			for (BAMenu cBAMenu : tmpChildList){        				
				ArrayList<BAMenu> nextLevelList = baMenuMap.get(cBAMenu);
				//If there are more child items, continue exploring
				if (nextLevelList != null){
					nLvlNodes.add(getTopNode(baMenuMap, cBAMenu, userBAList));
				}
			}
			//If reached the leaf nodes, add them to the tree.
			ArrayList<BusinessArea> leafLvlBAs = getChildBusinessAreas(pBAMenu.getMenuId());
			if ((leafLvlBAs != null) && (!leafLvlBAs.isEmpty()))
				for (BusinessArea tBA : leafLvlBAs){
					if (userBAList.contains(tBA)){
						JsonObject leafNode = getBANode(tBA); 
						nLvlNodes.add(leafNode);
					}
				}
			childNode.add(MENU_BUTTON_ITEMDATA, nLvlNodes);
			topNode.add(MENU_BUTTON_SUBMENU, childNode);
		}
		return topNode;
	}
	
	public static boolean isChildBA(int aSystemId) throws DatabaseException{
		boolean isChild = false;
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM related_business_areas where related_sys_id=" + aSystemId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null) && rs.next())
				isChild = true;
			ps.close();
			
			/**
			 * the connection object was getting closed 2 times first in the
			 * try block and the again in the finally block 
			 * fixed the bug by commeting the connection.close in line below
			 */
			//connection.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new DatabaseException("Database error occurred while checking if the BA is not a parent BA or not.", sqle);
		}finally {
			if(connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return isChild;		
	}
    
	/**
	 * @param tBA
	 * @return
	 */
	private static JsonObject getBANode(BusinessArea tBA) {
		JsonObject tObj = new JsonObject();
		String systemPrefix = tBA.getSystemPrefix();
		tObj.addProperty(HTML_TEXT_PROPERTY, tBA.getDisplayName() + " [" + systemPrefix + "]");
		tObj.addProperty(HTML_VALUE_PROPERTY, systemPrefix);
		return tObj;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {	
		return myMenuId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		BAMenu other = (BAMenu) obj;
		if (myMenuId != other.myMenuId)
			return false;
		return true;
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException {
		//System.out.println("Children : " + BAMenu.getChildMenus(2) + ", " + BAMenu.lookupByMenuId(2));
		/*ArrayList<BAMenu> lst = new ArrayList<BAMenu>();
		lst.add(BAMenu.lookupByMenuId(2));
		BAMenu tmpMenu = BAMenu.lookupByMenuId(3);System.out.println("Before: " + lst.toString());
		if (!lst.contains(tmpMenu))
			lst.add(tmpMenu);
		System.out.println("After: " + lst.toString());*/
		User u = User.lookupByUserLogin("kp");
		ArrayList<BusinessArea> baList = BusinessArea.getBusinessAreasByUserId(u.getUserId());
		for (BusinessArea ba : baList){
			System.out.println("BA for " + u.getDisplayName()+ ": " + ba.getDisplayName());	
		}
		System.out.println("Tree:\n" + getBAMenuJsonArray(baList));
	}

	public void setMenuId(int myMenuId) {
		this.myMenuId = myMenuId;
	}

	public void setMenuCaption(String myMenuCaption) {
		this.myMenuCaption = myMenuCaption;
	}

	public void setParentMenuId(int myParentMenuId) {
		this.myParentMenuId = myParentMenuId;
	}
}
