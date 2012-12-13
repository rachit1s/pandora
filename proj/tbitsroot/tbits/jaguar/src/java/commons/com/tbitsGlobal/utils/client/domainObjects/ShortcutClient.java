package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class ShortcutClient extends TbitsModelData {

	public static String NAME = "name";
	public static String QUERY = "query";
	public static String TEXT = "text";
	public static String VIEW = "view";
	public static String FILTER = "filter";
	public static String IS_BA_SHORTCUT = "isBaShortcut";
	public static String IS_DEFAULT = "isDefault";
	public static String IS_LIST_ALL = "isListAll";
	public static String IS_PUBLIC = "isPublic";

	public ShortcutClient() {
		super();
	}

	public void setFilter(String myFilter) {
		this.set(FILTER, myFilter);
	}

	public String getFilter() {
		return (String) this.get(FILTER);
	}

	public void setIsBAShortcut(boolean myIsBAShortcut) {
		this.set(IS_BA_SHORTCUT, myIsBAShortcut);
	}

	public boolean getIsBAShortcut() {
		return (Boolean) this.get(IS_BA_SHORTCUT);
	}

	public void setIsDefault(boolean myIsDefault) {
		this.set(IS_DEFAULT, myIsDefault);
	}

	public boolean getIsDefault() {
		return (Boolean) this.get(IS_DEFAULT);
	}

	public void setIsListAll(boolean myIsListAll) {
		this.set(IS_LIST_ALL, myIsListAll);
	}

	public boolean getIsListAll() {
		return (Boolean) this.get(IS_LIST_ALL);
	}

	public void setIsPublic(boolean myIsPublic) {
		this.set(IS_PUBLIC, myIsPublic);
	}

	public boolean getIsPublic() {
		return (Boolean) this.get(IS_PUBLIC);
	}

	public void setName(String myName) {
		this.set(NAME, myName);
	}

	public String getName() {
		return (String) this.get(NAME);
	}

	public void setQuery(String myQuery) {
		this.set(QUERY, myQuery);
	}

	public String getQuery() {
		return (String) this.get(QUERY);
	}

	public void setText(String myText) {
		this.set(TEXT, myText);
	}

	public String getText() {
		return (String) this.get(TEXT);
	}

	public void setView(int myView) {
		this.set(VIEW, myView);
	}

	public int getView() {
		return (Integer) this.get(VIEW);
	}
}
