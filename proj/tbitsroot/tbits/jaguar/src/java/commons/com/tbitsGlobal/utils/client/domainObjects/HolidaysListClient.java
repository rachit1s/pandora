package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for HolidaysList
public class HolidaysListClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public HolidaysListClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String DESCRIPTION = "description";
	// public static String HOLIDAY_DATE = "holiday_date";
	public static String OFFICE = "office";
	public static String OFFICE_ZONE = "office_zone";

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

	// getter and setter methods for variable myHolidayDate
	// public Date getHolidayDate (){
	// return (Date) this.get(HOLIDAY_DATE);
	// }
	// public void setHolidayDate(Date myHolidayDate) {
	// this.set(HOLIDAY_DATE, myHolidayDate);
	// }

	// getter and setter methods for variable myOffice
	public String getOffice() {
		return (String) this.get(OFFICE);
	}

	public void setOffice(String myOffice) {
		this.set(OFFICE, myOffice);
	}

	// getter and setter methods for variable myOfficeZone
	public String getOfficeZone() {
		return (String) this.get(OFFICE_ZONE);
	}

	public void setOfficeZone(String myOfficeZone) {
		this.set(OFFICE_ZONE, myOfficeZone);
	}

}