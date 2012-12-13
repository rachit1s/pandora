package corrGeneric.com.tbitsGlobal.shared.key;

public class OnBehalfKey 
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OnBehalfKey [sysPrefix=" + sysPrefix + ", userLogin="
				+ userLogin + "]";
	}

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
		if (!(obj instanceof OnBehalfKey)) {
			return false;
		}
		OnBehalfKey other = (OnBehalfKey) obj;
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
	
	public OnBehalfKey(String sysPrefix, String userLogin) {
		super();
		if( null == sysPrefix )
			throw new IllegalArgumentException("SysPrefix cannot be null.");
		if( null == userLogin )
			throw new IllegalArgumentException("UserLogin cannot be null.");
		
		this.sysPrefix = sysPrefix;
		this.userLogin = userLogin;
	}
	public String getSysPrefix() {
		return sysPrefix;
	}
	public String getUserLogin() {
		return userLogin;
	}
	
//	public boolean equals(Object obj)
//	{
//		if( null == obj || !(obj instanceof OnBehalfKey) )
//			return false ;
//	
//		if( obj == this )
//			return true; 
//		
//		OnBehalfKey obk = (OnBehalfKey) obj;
//		
//		if(obk.getSysPrefix().equals(this.getSysPrefix()) && obk.getUserLogin().equals(this.getUserLogin()))
//			return true ;
//		
//		return false;
//	}
//
//	public int hashCode()
//	{
//		int hashcode = HashCodeUtil.SEED ;
//		hashcode = HashCodeUtil.hash(hashcode, this.getSysPrefix());
//		hashcode = HashCodeUtil.hash(hashcode, this.getUserLogin());
//		
//		return hashcode;
//	}
	
	private String sysPrefix;
	private String userLogin;
}
