package corrGeneric.com.tbitsGlobal.shared.objects;

import java.io.Serializable;
import java.util.Collection;

public class OnBehalfClient implements Serializable
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OnBehalfClient [sysPrefix=" + sysPrefix + ", userLogin="
				+ userLogin + ", type1=" + type1 + ", type2=" + type2
				+ ", type3=" + type3 + ", onBehalfUsers=" + onBehalfUsers + "]";
	}

	public OnBehalfClient(String sysPrefix, String userLogin, String type1,
			String type2, String type3, Collection<String> onBehalfUsers) {
		super();
		this.sysPrefix = sysPrefix;
		this.userLogin = userLogin;
		this.type1 = type1;
		this.type2 = type2;
		this.type3 = type3;
		this.onBehalfUsers = onBehalfUsers;
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
	 * @return the onBehalfUsers
	 */
	public Collection<String> getOnBehalfUsers() {
		return onBehalfUsers;
	}

	/**
	 * applicable ba
	 */
	String sysPrefix;
	
	/**
	 * for the user : usually the logged in user
	 */
	String userLogin;
	/**
	 * onBehalf type1 : can be null
	 */
	String type1;
	
	/**
	 * onBehalf type2 : can be null
	 */
	String type2;
	/**
	 * onBehalf type3 : can be null
	 */
	String type3;
	
	/**
	 * dereferenced list of users
	 */
	Collection<String> onBehalfUsers;
	
}
