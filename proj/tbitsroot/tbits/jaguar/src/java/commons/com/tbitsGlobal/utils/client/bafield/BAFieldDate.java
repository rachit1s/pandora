package commons.com.tbitsGlobal.utils.client.bafield;

/**
 * 
 * @author sourabh
 * 
 * Class for date fields
 */
public class BAFieldDate extends BAField {
	private static final long serialVersionUID = 1L;
	
	private String dateFormat;
	private long defaultValue;

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDefaultValue(long defaultValue) {
		this.defaultValue = defaultValue;
	}

	public long getDefaultValue() {
		return defaultValue;
	}
}
