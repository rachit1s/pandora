package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for JobDefinition
public class JobDefinitionClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public JobDefinitionClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String CRITERIA = "criteria";
	public static String DATE = "date";
	public static String DAY = "day";
	public static String HOUR = "hour";
	public static String JOB_ID = "job_id";
	public static String MINUTE = "minute";
	public static String MONTH = "month";
	public static String USER_ID = "user_id";

	// getter and setter methods for variable myCriteria
	public String getCriteria() {
		return (String) this.get(CRITERIA);
	}

	public void setCriteria(String myCriteria) {
		this.set(CRITERIA, myCriteria);
	}

	// getter and setter methods for variable myDate
	public int getDate() {
		return (Integer) this.get(DATE);
	}

	public void setDate(int myDate) {
		this.set(DATE, myDate);
	}

	// getter and setter methods for variable myDay
	public int getDay() {
		return (Integer) this.get(DAY);
	}

	public void setDay(int myDay) {
		this.set(DAY, myDay);
	}

	// getter and setter methods for variable myHour
	public int getHour() {
		return (Integer) this.get(HOUR);
	}

	public void setHour(int myHour) {
		this.set(HOUR, myHour);
	}

	// getter and setter methods for variable myJobId
	public int getJobId() {
		return (Integer) this.get(JOB_ID);
	}

	public void setJobId(int myJobId) {
		this.set(JOB_ID, myJobId);
	}

	// getter and setter methods for variable myMinute
	public int getMinute() {
		return (Integer) this.get(MINUTE);
	}

	public void setMinute(int myMinute) {
		this.set(MINUTE, myMinute);
	}

	// getter and setter methods for variable myMonth
	public int getMonth() {
		return (Integer) this.get(MONTH);
	}

	public void setMonth(int myMonth) {
		this.set(MONTH, myMonth);
	}

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}

}