package transbit.tbits.admin.common;

public class TinyBA {
	
	public TinyBA(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}
	public TinyBA(String name, String displayName, boolean isSelected) {
		this.name = name;
		this.displayName = displayName;
		this.isSelected = isSelected;
	}
	private String name;
	private String displayName;
	private boolean isSelected = false;
	void setName(String name) {
		this.name = name;
	}
	String getName() {
		return name;
	}
	void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	String getDisplayName() {
		return displayName;
	}
	private void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	private boolean isSelected() {
		return isSelected;
	}
	
}
