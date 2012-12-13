package commons.com.tbitsGlobal.utils.client.bafield;

import commons.com.tbitsGlobal.utils.client.domainObjects.FieldClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;

/**
 * 
 * @author sourabh
 * 
 * Class to carry a field and its permissions
 */
public class BAField extends FieldClient{

	private static final long serialVersionUID = 1L;

	private Double temp_double = 0.0;
	
	private boolean canView;
	private boolean canAdd;
	private boolean canUpdate;
	private int userPerm;
	
//	public static String CAN_VIEW		=	"can_view";
//	public static String CAN_ADD		=	"can_add";
//	public static String CAN_UPDATE		=	"can_update";
	public static String CAN_SEARCH		=	"can_search";
	public static String DISPLAY_SIZE	=	"display_size";
	public static String IS_SET			=	"is_set";
	
	public BAField(){
//		this.setCanAdd(false);
//		this.setCanUpdate(false);
		setDisplaySize(0.33);
	}
	
	public void setDisplaySize(double myDisplaySize) {
		this.set(DISPLAY_SIZE, myDisplaySize);
	}
	
	public double getDisplaySize() {
		return (Double)this.get(DISPLAY_SIZE);
	}
	
	public void setCanSearch(boolean canSearch) {
		this.set(CAN_SEARCH, canSearch);
	}

	public boolean isCanSearch() {
		return (Boolean)this.get(CAN_SEARCH);
	}
	
	public void setSetEnabled(boolean isSet){
		this.set(IS_SET, isSet);
	}
	
	public boolean isSetEnabled(){
		return (Boolean)this.get(IS_SET);
	}
	
	/**
	 * @return True if view field control is on
	 */
	public boolean isCanView() {
		return canView;
	}

	public void setCanView(boolean canView) {
		this.canView = canView;
	}

	/**
	 * @return True if add field control is on
	 */
	public boolean isCanAdd() {
		return canAdd;
	}

	public void setCanAdd(boolean canAdd) {
		this.canAdd = canAdd;
	}

	/**
	 * @return True if update field control is on
	 */
	public boolean isCanUpdate() {
		return canUpdate;
	}

	public void setCanUpdate(boolean canUpdate) {
		this.canUpdate = canUpdate;
	}

	public int getUserPerm() {
		return userPerm;
	}

	public void setUserPerm(int userPerm) {
		this.userPerm = userPerm;
	}
	
	/**
	 * @return True if view field control is on and the user has view permissions
	 */
	public boolean isCanViewInBA() {
		return canView && ((userPerm & PermissionClient.VIEW) != 0);
	}

	/**
	 * @return True if add field control is on and the user has add permissions
	 */
	public boolean isCanAddInBA() {
		return canAdd && ((userPerm & PermissionClient.ADD) != 0);
	}

	/**
	 * @return True if update field control is on and the user has update permissions
	 */
	public boolean isCanUpdateInBA() {
		return canUpdate && ((userPerm & PermissionClient.CHANGE) != 0);
	}

	public int compareTo(BAField arg0) {
		return this.getDisplayName().compareTo(arg0.getDisplayName());
	}
}
