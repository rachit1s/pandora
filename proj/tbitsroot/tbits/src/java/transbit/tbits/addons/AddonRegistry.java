///**
// * 
// */
//package transbit.tbits.addons;
//
///**
// * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
// * STATES : INITIALIZED --> ACTIVATED --> DEACTIVATED
// * 				|				|					|
// * 				---------------->-> UNINITIALIZED <--
// *  UNINITIALIZED : means that the entry is removed.
// */
//public class AddonRegistry 
//{
//	private long addonId;
//	private long jarId;
//	private int isActive;
//	private String addonName;
//	private String addonDescription;
//	private String addonAuthor;
//	
//	/**
//	 * @param addonId
//	 * @param jarId
//	 * @param isActive
//	 * @param addonName
//	 * @param addonDescription
//	 * @param addonAuthor
//	 */
//	public AddonRegistry(long addonId, long jarId, int isActive,
//			String addonName, String addonDescription, String addonAuthor) {
//		super();
//		this.addonId = addonId;
//		this.jarId = jarId;
//		this.isActive = isActive;
//		this.addonName = addonName;
//		this.addonDescription = addonDescription;
//		this.addonAuthor = addonAuthor;
//	}
//
//
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + (int) (addonId ^ (addonId >>> 32));
//		return result;
//	}
//
//
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		AddonRegistry other = (AddonRegistry) obj;
//		if (addonId != other.addonId)
//			return false;
//		return true;
//	}
//
//
//
//	/**
//	 * @return the addonId
//	 */
//	public long getAddonId() {
//		return addonId;
//	}
//
//
//
//	/**
//	 * @param addonId the addonId to set
//	 */
//	public void setAddonId(long addonId) {
//		this.addonId = addonId;
//	}
//
//
//
//	/**
//	 * @return the jarId
//	 */
//	public long getJarId() {
//		return jarId;
//	}
//
//
//
//	/**
//	 * @param jarId the jarId to set
//	 */
//	public void setJarId(long jarId) {
//		this.jarId = jarId;
//	}
//
//
//
//	/**
//	 * @return the isActive
//	 */
//	public int getIsActive() {
//		return isActive;
//	}
//
//
//
//	/**
//	 * @param isActive the isActive to set
//	 */
//	public void setIsActive(int isActive) {
//		this.isActive = isActive;
//	}
//
//
//
//	/**
//	 * @return the addonName
//	 */
//	public String getAddonName() {
//		return addonName;
//	}
//
//
//
//	/**
//	 * @param addonName the addonName to set
//	 */
//	public void setAddonName(String addonName) {
//		this.addonName = addonName;
//	}
//
//
//
//	/**
//	 * @return the addonDescription
//	 */
//	public String getAddonDescription() {
//		return addonDescription;
//	}
//
//
//
//	/**
//	 * @param addonDescription the addonDescription to set
//	 */
//	public void setAddonDescription(String addonDescription) {
//		this.addonDescription = addonDescription;
//	}
//
//
//
//	/**
//	 * @return the addonAuthor
//	 */
//	public String getAddonAuthor() {
//		return addonAuthor;
//	}
//
//
//
//	/**
//	 * @param addonAuthor the addonAuthor to set
//	 */
//	public void setAddonAuthor(String addonAuthor) {
//		this.addonAuthor = addonAuthor;
//	}
//
//
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return "AddonRegistry [addonId=" + addonId + ", jarId=" + jarId
//				+ ", isActive=" + isActive + ", addonName=" + addonName
//				+ ", addonDescription=" + addonDescription + ", addonAuthor="
//				+ addonAuthor + "]";
//	}
//	
//	
//}
