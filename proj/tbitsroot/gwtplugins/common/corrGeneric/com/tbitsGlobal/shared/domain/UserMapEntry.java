package corrGeneric.com.tbitsGlobal.shared.domain;

import java.io.Serializable;


public class UserMapEntry implements Serializable
{
	private UserMapEntry()
	{
		// for serialization
	}
	public UserMapEntry(long id, String userLogin, String sysPrefix, String type1,
			String type2, String type3, String userTypeFieldName,
			String userLoginValue, int strictNess) {
		super();
		if( null == userLogin || null == sysPrefix )
			throw new IllegalArgumentException("userLogin And sysPrefix cannot be null. Their supplied values are respectively : " + userLogin + "," + sysPrefix);
		this.id = id;
		this.userLogin = userLogin.trim();
		this.sysPrefix = sysPrefix.trim();
		this.type1 = ( null == type1 ? null : type1.trim() );
		this.type2 = ( null == type2 ? null : type2.trim() );
		this.type3 = ( null == type3 ? null : type3.trim() );
		this.userTypeFieldName = userTypeFieldName.trim();
		this.userLoginValue = userLoginValue.trim();
		this.strictNess = strictNess;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserMapEntry [id=" + id + ", sysPrefix=" + sysPrefix
				+ ", userLogin=" + userLogin + ", type1=" + type1 + ", type2="
				+ type2 + ", type3=" + type3 + ", userTypeFieldName="
				+ userTypeFieldName + ", userLoginValue=" + userLoginValue
				+ ", strictNess=" + strictNess + "]";
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
		result = prime * result + ((type1 == null) ? 0 : type1.hashCode());
		result = prime * result + ((type2 == null) ? 0 : type2.hashCode());
		result = prime * result + ((type3 == null) ? 0 : type3.hashCode());
		result = prime * result
				+ ((userLogin == null) ? 0 : userLogin.hashCode());
		result = prime * result
				+ ((userLoginValue == null) ? 0 : userLoginValue.hashCode());
		result = prime
				* result
				+ ((userTypeFieldName == null) ? 0 : userTypeFieldName
						.hashCode());
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
		if (!(obj instanceof UserMapEntry)) {
			return false;
		}
		UserMapEntry other = (UserMapEntry) obj;
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
		if (userLoginValue == null) {
			if (other.userLoginValue != null) {
				return false;
			}
		} else if (!userLoginValue.equals(other.userLoginValue)) {
			return false;
		}
		if (userTypeFieldName == null) {
			if (other.userTypeFieldName != null) {
				return false;
			}
		} else if (!userTypeFieldName.equals(other.userTypeFieldName)) {
			return false;
		}
		return true;
	}
	/**
	 * @return the userLogin
	 */
	public String getUserLogin() {
		return userLogin;
	}
	/**
	 * @return the sysPrefix
	 */
	public String getSysPrefix() {
		return sysPrefix;
	}
	/**
	 * @return the type1
	 */
	public String getType1() {
		return type1;
	}
	/**
	 * @return the type2
	 */
	public String getType2() {
		return type2;
	}
	/**
	 * @return the type3
	 */
	public String getType3() {
		return type3;
	}
	/**
	 * @return the userTypeFieldName
	 */
	public String getUserTypeFieldName() {
		return userTypeFieldName;
	}
	/**
	 * @return the userLoginValue
	 */
	public String getUserLoginValue() {
		return userLoginValue;
	}
	
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

	private long id ;
	private String userLogin;
	private String sysPrefix;
	private String type1;
	private String type2;
	private String type3;
	private String userTypeFieldName;
	private String userLoginValue;
	private int strictNess;
	
	/**
	 * @return the strictNess
	 */
	public int getStrictNess() {
		return strictNess;
	}
	/**
	create table corr_user_map
	(
		sys_prefix varchar(32),
		user_login varchar(255),
		user_map_type1 varchar(255),
		user_map_type2 varchar(255),
		user_map_type3 varchar(255),
		user_type_field_name varchar(128),
		user_login_value varchar(255),
		strictness int
	)
*/

	public static final String TableName = "corr_user_map";
	public static final String SysPrefix = "sys_prefix";
	public static final String UserLogin = "user_login";
	public static final String Type1 = "user_map_type1";
	public static final String Type2 = "user_map_type2";
	public static final String Type3 = "user_map_type3";
	public static final String UserTypeFieldName = "user_type_field_name";
	public static final String UserLoginValue = "user_login_value";
	public static final String StrictNess = "strictness";
	public static final String Id = "id" ;
	
	public static final int StrictNess_Strict = 4;
	public static final int StrictNess_AllowExtra = 2;
	public static final int StrictNess_AllowAny = 1;
	public static final int StrictNess_FromThese = 3; // allows more than 0 users. all the users from the given list
}
