package commons.com.tbitsGlobal.utils.client.domainObjects;

import com.extjs.gxt.ui.client.util.Format;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Field
public class FieldClient extends TbitsModelData {
	// default constructor
	public FieldClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DATA_TYPE_ID = "data_type_id";
	public static String DESCRIPTION = "description";
	public static String DISPLAY_NAME = "display_name";
	public static String FIELD_ID = "field_id";
	public static String IS_ACTIVE = "is_active";
	public static String IS_DEPENDENT = "is_dependent";
	public static String IS_EXTENDED = "is_extended";
	public static String IS_PRIVATE = "is_private";
	public static String NAME = "name";
	public static String PERMISSION = "permission";
	public static String REGEX = "regex";
	public static String ERROR = "error";
	public static String SYSTEM_ID = "system_id";
	public static String TRACKING_OPTION = "tracking_option";
	
	public static String DISPLAY_GROUP	=	"display_group";
	public static String DISPLAY_ORDER	=	"display_order";

	// getter and setter methods for variable myDataTypeId
	public int getDataTypeId() {
		return (Integer) this.get(DATA_TYPE_ID);
	}

	public void setDataTypeId(int myDataTypeId) {
		this.set(DATA_TYPE_ID, myDataTypeId);
	}

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

	// getter and setter methods for variable myDisplayName
	public String getDisplayName() {
		return (String) this.get(DISPLAY_NAME);
		//return Format.htmlEncode((String) this.get(DISPLAY_NAME));
	}

	public void setDisplayName(String myDisplayName) {
		this.set(DISPLAY_NAME, myDisplayName);
	}

	// getter and setter methods for variable myFieldId
	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}

	// getter and setter methods for variable myIsActive
	public boolean getIsActive() {
		return (Boolean) this.get(IS_ACTIVE);
	}

	public void setIsActive(boolean myIsActive) {
		this.set(IS_ACTIVE, myIsActive);
	}

	// getter and setter methods for variable myIsDependent
	public boolean getIsDependent() {
		return (Boolean) this.get(IS_DEPENDENT);
	}

	public void setIsDependent(boolean myIsDependent) {
		this.set(IS_DEPENDENT, myIsDependent);
	}

	// getter and setter methods for variable myIsExtended
	public boolean getIsExtended() {
		return (Boolean) this.get(IS_EXTENDED);
	}

	public void setIsExtended(boolean myIsExtended) {
		this.set(IS_EXTENDED, myIsExtended);
	}

	// getter and setter methods for variable myIsPrivate
	public boolean getIsPrivate() {
		return (Boolean) this.get(IS_PRIVATE);
	}

	public void setIsPrivate(boolean myIsPrivate) {
		this.set(IS_PRIVATE, myIsPrivate);
	}

	// getter and setter methods for variable myName
	public String getName() {
		return (String) this.get(NAME);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	// getter and setter methods for variable myPermission
	public int getPermission() {
		return (Integer) this.get(PERMISSION);
	}

	public void setPermission(int myPermission) {
		this.set(PERMISSION, myPermission);
	}

	// getter and setter methods for variable myRegex
	public String getRegex() {
		return (String) this.get(REGEX);
	}

	public void setRegex(String myRegex) {
		this.set(REGEX, myRegex);
	}
	// getter and setter methods for variable myError
		public String getError() {
			return (String) this.get(ERROR);
		}

		public void setError(String myError) {
			this.set(ERROR, myError);
		}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myTrackingOption
	public int getTrackingOption() {
		return (Integer) this.get(TRACKING_OPTION);
	}

	public void setTrackingOption(int myTrackingOption) {
		this.set(TRACKING_OPTION, myTrackingOption);
	}
	
	public void setDisplayGroup(int myDisplayGroup) {
		this.set(DISPLAY_GROUP, myDisplayGroup);
	}
	
	public int getDisplayGroup() {
		return (Integer)this.get(DISPLAY_GROUP);
	}

	public void setDisplayOrder(int myDisplayOrder) {
		this.set(DISPLAY_ORDER, myDisplayOrder);
	}

	public int getDisplayOrder() {
		return (Integer)this.get(DISPLAY_ORDER);
	}
	
	@Override
	public int compareTo(TbitsModelData o) {
		Object obj = o.get(DISPLAY_ORDER);
		int displayOrder = (Integer) obj;
		if(this.getDisplayOrder() == displayOrder)
			return 0;
		return this.getDisplayOrder() > displayOrder ? 1 : -1;
	}
}