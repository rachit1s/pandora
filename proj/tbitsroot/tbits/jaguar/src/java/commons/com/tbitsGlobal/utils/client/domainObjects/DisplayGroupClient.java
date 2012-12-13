package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for DisplayGroup
public class DisplayGroupClient extends TbitsModelData {

	public static String ID = "id";
	public static String DISPLAY_NAME = "display_name";
	public static String DISPLAY_ORDER = "display_order";
	public static String IS_ACTIVE = "is_active";
	public static String SYSTEM_ID = "system_id";
	public static String IS_DEFAULT = "is_default";

	// default constructor
	public DisplayGroupClient() {
		super();
	}

	public int getId() {
		return (Integer) this.get(ID);
	}

	public void setId(int id) {
		this.set(ID, id);
	}

	public String getDisplayName() {
		return (String) this.get(DISPLAY_NAME);
	}

	public void setDisplayName(String displayName) {
		this.set(DISPLAY_NAME, displayName);
	}

	public int getDisplayOrder() {
		return (Integer) this.get(DISPLAY_ORDER);
	}

	public void setDisplayOrder(int displayOrder) {
		this.set(DISPLAY_ORDER, displayOrder);
	}

	public boolean getIsActive() {
		return (Boolean) this.get(IS_ACTIVE);
	}

	public void setIsActive(boolean isActive) {
		this.set(IS_ACTIVE, isActive);
	}

	public boolean getIsDefault() {
		return (Boolean) this.get(IS_DEFAULT);
	}

	public void setIsDefault(boolean isDefault) {
		this.set(IS_DEFAULT, isDefault);
	}

	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int systemId) {
		this.set(SYSTEM_ID, systemId);
	}

	@Override
	public int compareTo(TbitsModelData o) {
		Object obj = o.get(DISPLAY_ORDER);
		int displayOrder = (Integer) obj;
		if (this.getDisplayOrder() == displayOrder)
			return 0;
		return this.getDisplayOrder() > displayOrder ? 1 : -1;
	}

}