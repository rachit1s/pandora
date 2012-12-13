package commons.com.tbitsGlobal.utils.client.domainObjects;

import com.extjs.gxt.ui.client.util.Format;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Type
public class TypeClient extends TbitsModelData {
	
	public static int ourSortField;
	public static int ourSortOrder;

	// default constructor
	public TypeClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DESCRIPTION = "description";
	public static String DISPLAY_NAME = "display_name";
	public static String FIELD_ID = "field_id";
	public static String IS_ACTIVE = "is_active";
	public static String IS_CHECKED = "is_checked";
	public static String IS_DEFAULT = "is_default";
	public static String IS_FINAL = "is_final";
	public static String IS_PRIVATE = "is_private";
	public static String NAME = "name";
	public static String ORDERING = "ordering";
	public static String SYSTEM_ID = "system_id";
	public static String TYPE_ID = "type_id";

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

	// getter and setter methods for variable myDisplayName
	public String getDisplayName() {
		//return Format.htmlEncode((String) this.get(DISPLAY_NAME));
		return (String) this.get(DISPLAY_NAME);
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

	// getter and setter methods for variable myIsChecked
	public boolean getIsChecked() {
		return (Boolean) this.get(IS_CHECKED);
	}

	public void setIsChecked(boolean myIsChecked) {
		this.set(IS_CHECKED, myIsChecked);
	}

	// getter and setter methods for variable myIsDefault
	public boolean getIsDefault() {
		return (Boolean) this.get(IS_DEFAULT);
	}

	public void setIsDefault(boolean myIsDefault) {
		this.set(IS_DEFAULT, myIsDefault);
	}

	// getter and setter methods for variable myIsFinal
	public boolean getIsFinal() {
		return (Boolean) this.get(IS_FINAL);
	}

	public void setIsFinal(boolean myIsFinal) {
		this.set(IS_FINAL, myIsFinal);
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

	// getter and setter methods for variable myOrdering
	public int getOrdering() {
		return (Integer) this.get(ORDERING);
	}

	public void setOrdering(int myOrdering) {
		this.set(ORDERING, myOrdering);
	}

	// getter and setter methods for variable mySystemId
	public int getSystemId() {
		return (Integer) this.get(SYSTEM_ID);
	}

	public void setSystemId(int mySystemId) {
		this.set(SYSTEM_ID, mySystemId);
	}

	// getter and setter methods for variable myTypeId
	public int getTypeId() {
		return (Integer) this.get(TYPE_ID);
	}

	public void setTypeId(int myTypeId) {
		this.set(TYPE_ID, myTypeId);
	}
	
	@Override
	public int compareTo(TbitsModelData o) {
		Object obj = o.get(ORDERING);
		int ordering = (Integer) obj;
		if(this.getOrdering() == ordering)
			return 0;
		return this.getOrdering() > ordering ? 1 : -1;
	}

}