package transbit.tbits.admin.common;

public class MenuItem {
	public MenuItem(String caption, String url, String description,
			boolean isSelected) {
		this.caption = caption;
		this.url = url;
		this.description = description;
		this.isSelected = isSelected;
	}
	public MenuItem(String caption, String url, String description) {
		this.caption = caption;
		this.url = url;
		this.description = description;
		this.isSelected = isSelected;
	}
	String caption;
	String url;
	String description;
	boolean isSelected;
}
