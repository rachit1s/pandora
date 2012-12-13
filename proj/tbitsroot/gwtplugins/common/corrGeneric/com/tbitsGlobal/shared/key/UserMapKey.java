package corrGeneric.com.tbitsGlobal.shared.key;

public class UserMapKey 
{
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sysPrefix == null) ? 0 : sysPrefix.hashCode());
		result = prime * result
				+ ((userLogin == null) ? 0 : userLogin.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UserMapKey)) {
			return false;
		}
		UserMapKey other = (UserMapKey) obj;
		if (sysPrefix == null) {
			if (other.sysPrefix != null) {
				return false;
			}
		} else if (!sysPrefix.equals(other.sysPrefix)) {
			return false;
		}
		if (userLogin == null) {
			if (other.userLogin != null) {
				return false;
			}
		} else if (!userLogin.equals(other.userLogin)) {
			return false;
		}
		return true;
	}
	public UserMapKey(String sysPrefix, String userLogin) {
		super();
		if( null == sysPrefix || null == userLogin )
			throw new IllegalArgumentException("sysPrefix and userLogin cannot be null. There respective values are : " + sysPrefix + "," + userLogin);

		this.sysPrefix = sysPrefix;
		this.userLogin = userLogin;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserMapKey [sysPrefix=" + sysPrefix + ", userLogin="
				+ userLogin + "]";
	}
	/**
	 * @return the sysPrefix
	 */
	public String getSysPrefix() {
		return sysPrefix;
	}
	/**
	 * @return the userLogin
	 */
	public String getUserLogin() {
		return userLogin;
	}
	
	private String sysPrefix;
	private String userLogin;
	
}
