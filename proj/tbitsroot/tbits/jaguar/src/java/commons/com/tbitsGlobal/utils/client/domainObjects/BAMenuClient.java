package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for BAMenu
public class BAMenuClient extends TbitsModelData {

	// default constructor
	public BAMenuClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String MENU_ID = "menu_id";
	public static String MENU_CAPTION = "menu_caption";
	public static String PARENT_MENU_ID = "parent_menu_id";
	public static String SUB_MENU_IDS = "sub_menu_ids";
	public static String BA_IDS = "ba_ids";
	
	private ArrayList<BAMenuClient> subMenu;
	private ArrayList<BusinessAreaClient> baList;

	// getter and setter methods for variable myMenuId
	public int getMenuId() {
		return (Integer) this.get(MENU_ID);
	}

	public void setMenuId(int myMenuId) {
		this.set(MENU_ID, myMenuId);
	}

	// getter and setter methods for variable myMenuCaption
	public String getMenuCaption() {
		return (String) this.get(MENU_CAPTION);
	}

	public void setMenuCaption(String myMenuCaption) {
		this.set(MENU_CAPTION, myMenuCaption);
	}

	// getter and setter methods for variable myParentMenuId
	public int getParentMenuId() {
		return (Integer) this.get(PARENT_MENU_ID);
	}

	public void setParentMenuId(int myParentMenuId) {
		this.set(PARENT_MENU_ID, myParentMenuId);
	}

	// getter and setter methods for variable subMenuIds
	public ArrayList<Integer> getSubMenuIds() {
		return (ArrayList<Integer>) this.get(SUB_MENU_IDS);
	}

	public void setSubMenuIds(ArrayList<Integer> subMenuIds) {
		this.set(SUB_MENU_IDS, subMenuIds);
	}

	// getter and setter methods for variable baIds
	public ArrayList<Integer> getBaIds() {
		return (ArrayList<Integer>) this.get(BA_IDS);
	}

	public void setBaIds(ArrayList<Integer> baIds) {
		this.set(BA_IDS, baIds);
	}
	
	public ArrayList<BAMenuClient> getSubMenu() {
		return subMenu;
	}

	public void setSubMenu(ArrayList<BAMenuClient> subMenu) {
		this.subMenu = subMenu;
	}

	public ArrayList<BusinessAreaClient> getBaList() {
		return baList;
	}

	public void setBaList(ArrayList<BusinessAreaClient> baIds) {
		this.baList = baIds;
	}

}