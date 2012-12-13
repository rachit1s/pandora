package corrGeneric.com.tbitsGlobal.shared.domain;

public class OnBehalfEntry 
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OnBehalfEntry [id=" + id + ", sysPrefix=" + sysPrefix
				+ ", userLogin=" + userLogin + ", type1=" + type1 + ", type2="
				+ type2 + ", type3=" + type3 + ", onBehalfUser=" + onBehalfUser
				+ "]";
	}

	public OnBehalfEntry(long id,String sysPrefix, String userLogin, String type1,
			String type2, String type3, String onBehalfUser) {
		super();
		this.id = id;
		this.sysPrefix = sysPrefix.trim();
		this.userLogin = userLogin.trim();
		this.type1 = (type1 == null ? null : type1.trim() );
		this.type2 = (type2 == null ? null : type2.trim() );
		this.type3 = ( type3 == null ? null : type3.trim() );
		this.onBehalfUser = ( onBehalfUser == null ? null : onBehalfUser.trim() );
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((onBehalfUser == null) ? 0 : onBehalfUser.hashCode());
		result = prime * result
				+ ((sysPrefix == null) ? 0 : sysPrefix.hashCode());
		result = prime * result + ((type1 == null) ? 0 : type1.hashCode());
		result = prime * result + ((type2 == null) ? 0 : type2.hashCode());
		result = prime * result + ((type3 == null) ? 0 : type3.hashCode());
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
		if (!(obj instanceof OnBehalfEntry)) {
			return false;
		}
		OnBehalfEntry other = (OnBehalfEntry) obj;
		if (onBehalfUser == null) {
			if (other.onBehalfUser != null) {
				return false;
			}
		} else if (!onBehalfUser.equals(other.onBehalfUser)) {
			return false;
		}
		if (sysPrefix == null) {
			if (other.sysPrefix != null) {
				return false;
			}
		} else if (!sysPrefix.equals(other.sysPrefix)) {
			return false;
		}
		if (type1 == null) {
			if (other.type1 != null) {
				return false;
			}
		} else if (!type1.equals(other.type1)) {
			return false;
		}
		if (type2 == null) {
			if (other.type2 != null) {
				return false;
			}
		} else if (!type2.equals(other.type2)) {
			return false;
		}
		if (type3 == null) {
			if (other.type3 != null) {
				return false;
			}
		} else if (!type3.equals(other.type3)) {
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

	public String getSysPrefix() {
		return sysPrefix;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public String getType1() {
		return type1;
	}

	public String getType2() {
		return type2;
	}

	public String getType3() {
		return type3;
	}

	public String getOnBehalfUser() {
		return onBehalfUser;
	}

	/**
	 * 
create table corr_onbehalf_map
(
	sys_prefix varchar(32),
	user_login varchar(255),
	onbehalf_type1 varchar(255),
	onbehalf_type2 varchar(255),
	onbehalf_type3 varchar(255),
	onbehalf_of_login varchar(255)
)

	 */
	
	public static final String TableName = "corr_onbehalf_map";
	public static final String SysPrefix = "sys_prefix" ;
	public static final String UserLogin = "user_login" ;
	public static final String Type1 = "onbehalf_type1";
	public static final String Type2 = "onbehalf_type2";
	public static final String Type3 = "onbehalf_type3";
	public static final String OnBehalfLogin = "onbehalf_of_login";
	public static final String Id = "id";
	
	private long id;
	private String sysPrefix;
	/**
	 * can be single user or mailing list
	 */
	private String userLogin;
	private String type1;
	private String type2;
	private String type3;
	
	/**
	 * can be single user of mailing list
	 */
	private String onBehalfUser;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	private void setId(long id) {
		this.id = id;
	}
}
