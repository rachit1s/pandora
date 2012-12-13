/**
 * 
 */
package commons.com.tbitsGlobal.utils.client;

/**
 * @author dheeru
 * 
 */
public enum PermissionPlaceValue {

	ADD(1), CHANGE(2), VIEW(4), SET(64), SEARCH(32), HYPERLINK(128), D_ACTION(
			16), DISPLAY(8), EMAIL_VIEW(8);

	private int value;

	PermissionPlaceValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
