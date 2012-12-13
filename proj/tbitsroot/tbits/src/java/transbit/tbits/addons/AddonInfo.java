/**
 * 
 */
package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * 
 *         <pre>
 * UPLOADED --(1)---> REGISTERED----(2)---> ACTIVATED---(3)----> DEACTIVATED---------
 *   | 					  |						 ^                    |				|
 *   |                    |                       |___________(10)____|  			|
 *   | 					  |											  |(4) 	        |                                  
 *   |					  |-------------------(7)--------------->UNREGISTERED		|				
 *   | 					  |(6)										  |(5)			|
 *   |----------(9)--------------------------------------------- > UNLOADED<--(8)---|
 * </pre>
 * 
 CREATE TABLE addon_info
           (
				jar_id bigint IDENTITY (1,1) NOT NULL,
				jar_name varchar(255) NOT NULL,
				status int NOT NULL,
				addon_name varchar(255),
				addon_description varchar(3999),
				addon_author varchar(255),
				jar_bytes varbinary(MAX) NOT NULL,
				PRIMARY KEY (jar_id),
				unique(addon_name)
            )
 */
public class AddonInfo {
	public static final int STATUS_UPLOADED = 1;
	public static final int STATUS_REGISTERED = 2;
	public static final int STATUS_ACTIVATED = 3;
	public static final int STATUS_DEACTIVATED = 4;
	public static final int STATUS_UNREGISTERED = 5;
	public static final int STATUS_UNLOADED = 6; // this will never be persisted
													// in the DB. It will always
													// be deleted.

	public static final String[] STATE_NAMES = { "Illegal",
			"Uploaded", "Registered", "Activated",
			"Deactivated", "Unregistered", "Unloaded" };
	private long jarId;
	private String jarName;
	private String addonName;
	private String addonDescription;
	private String addonAuthor;
	private int status;

	/**
	 * @return the jarId
	 */
	public long getJarId() {
		return jarId;
	}

	/**
	 * @param jarId
	 *            the jarId to set
	 */
	public void setJarId(long jarId) {
		this.jarId = jarId;
	}

	/**
	 * @return the jarName
	 */
	public String getJarName() {
		return jarName;
	}

	/**
	 * @param jarName
	 *            the jarName to set
	 */
	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	/**
	 * @return the addonName
	 */
	public String getAddonName() {
		return addonName;
	}

	/**
	 * @param addonName
	 *            the addonName to set
	 */
	public void setAddonName(String addonName) {
		this.addonName = addonName;
	}

	/**
	 * @return the addonDescription
	 */
	public String getAddonDescription() {
		return addonDescription;
	}

	/**
	 * @param addonDescription
	 *            the addonDescription to set
	 */
	public void setAddonDescription(String addonDescription) {
		this.addonDescription = addonDescription;
	}

	/**
	 * @return the addonAuthor
	 */
	public String getAddonAuthor() {
		return addonAuthor;
	}

	/**
	 * @param addonAuthor
	 *            the addonAuthor to set
	 */
	public void setAddonAuthor(String addonAuthor) {
		this.addonAuthor = addonAuthor;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @param jarId
	 * @param jarName
	 * @param addonName
	 * @param addonDescription
	 * @param addonAuthor
	 * @param status
	 */
	public AddonInfo(long jarId, String jarName, int status,
			String addonName, String addonDescription,
			String addonAuthor) {
		super();
		this.jarId = jarId;
		this.jarName = jarName;
		this.addonName = addonName;
		this.addonDescription = addonDescription;
		this.addonAuthor = addonAuthor;
		this.status = status;
	}

	/**
	 * @param jarId
	 * @param jarName
	 * @param status
	 */
	public AddonInfo(long jarId, String jarName, int status) {
		super();
		this.jarId = jarId;
		this.jarName = jarName;
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (jarId ^ (jarId >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddonInfo other = (AddonInfo) obj;
		if (jarId != other.jarId)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AddonInfo [jarId=" + jarId + ", jarName="
				+ jarName + ", addonName=" + addonName
				+ ", status=" + status
				+ ", addonDescription=" + addonDescription
				+ ", addonAuthor=" + addonAuthor + "]";
	}
}
