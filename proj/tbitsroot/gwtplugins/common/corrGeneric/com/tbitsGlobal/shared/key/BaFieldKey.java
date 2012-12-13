package corrGeneric.com.tbitsGlobal.shared.key;

public class BaFieldKey 
{
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fromSysPrefix == null) ? 0 : fromSysPrefix.hashCode());
		result = prime * result
				+ ((toSysPrefix == null) ? 0 : toSysPrefix.hashCode());
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
		if (!(obj instanceof BaFieldKey)) {
			return false;
		}
		BaFieldKey other = (BaFieldKey) obj;
		if (fromSysPrefix == null) {
			if (other.fromSysPrefix != null) {
				return false;
			}
		} else if (!fromSysPrefix.equals(other.fromSysPrefix)) {
			return false;
		}
		if (toSysPrefix == null) {
			if (other.toSysPrefix != null) {
				return false;
			}
		} else if (!toSysPrefix.equals(other.toSysPrefix)) {
			return false;
		}
		return true;
	}
	public BaFieldKey(String fromSysPrefix, String toSysPrefix) {
		super();
		if( null == fromSysPrefix || null == toSysPrefix)
			throw new IllegalArgumentException( "fromSysPrefix And toSysPrefix cannot be null. Their respective value were : " + fromSysPrefix + "," + toSysPrefix );
		this.fromSysPrefix = fromSysPrefix;
		this.toSysPrefix = toSysPrefix;
	}
	/**
	 * @return the fromSysPrefix
	 */
	public String getFromSysPrefix() {
		return fromSysPrefix;
	}
	/**
	 * @return the toSysPrefix
	 */
	public String getToSysPrefix() {
		return toSysPrefix;
	}
	private String fromSysPrefix;
	private String toSysPrefix;
}
