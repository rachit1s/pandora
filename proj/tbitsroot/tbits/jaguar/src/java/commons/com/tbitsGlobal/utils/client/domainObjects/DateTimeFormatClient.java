package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for DateTimeFormat
public class DateTimeFormatClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public DateTimeFormatClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String FORMAT = "format";
	public static String FORMAT_ID = "format_id";

	// getter and setter methods for variable myFormat
	public String getFormat() {
		return (String) this.get(FORMAT);
	}

	public void setFormat(String myFormat) {
		this.set(FORMAT, myFormat);
	}

	// getter and setter methods for variable myFormatId
	public int getFormatId() {
		return (Integer) this.get(FORMAT_ID);
	}

	public void setFormatId(int myFormatId) {
		this.set(FORMAT_ID, myFormatId);
	}

}