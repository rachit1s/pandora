package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for JobNotifier
public class JobNotifierClient extends TbitsModelData {

	private static int ourSortField;
	private static int ourSortOrder;

	// default constructor
	public JobNotifierClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String JOB_ID = "job_id";
	public static String USER_ID = "user_id";

	// getter and setter methods for variable myJobId
	public int getJobId() {
		return (Integer) this.get(JOB_ID);
	}

	public void setJobId(int myJobId) {
		this.set(JOB_ID, myJobId);
	}

	// getter and setter methods for variable myUserId
	public int getUserId() {
		return (Integer) this.get(USER_ID);
	}

	public void setUserId(int myUserId) {
		this.set(USER_ID, myUserId);
	}

}