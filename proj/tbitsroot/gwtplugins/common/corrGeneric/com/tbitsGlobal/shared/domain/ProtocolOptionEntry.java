package corrGeneric.com.tbitsGlobal.shared.domain;

import java.io.Serializable;


public class ProtocolOptionEntry implements Serializable
{
	private ProtocolOptionEntry()
	{
		// for serialization
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProtocolOptionEntry [id=" + id + ", sysPrefix=" + sysPrefix
				+ ", name=" + name + ", value=" + value + ", description="
				+ description + "]";
	}
	public ProtocolOptionEntry(ProtocolOptionEntry poe)
	{
		this.id = poe.id;
		this.name = poe.name;
		this.sysPrefix = poe.sysPrefix;
		this.value = poe.value;
		this.description = poe.description;
	}
	
	public ProtocolOptionEntry(long id,String sysPrefix, String name, String value,
			String description) {
		super();
		this.id = id; 
		this.sysPrefix = sysPrefix.trim();
		this.name = name.trim();
		this.value = ( null == value ? null : value.trim() );
		this.description = ( null == description ? null : description.trim() );
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((sysPrefix == null) ? 0 : sysPrefix.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof ProtocolOptionEntry)) {
			return false;
		}
		ProtocolOptionEntry other = (ProtocolOptionEntry) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (sysPrefix == null) {
			if (other.sysPrefix != null) {
				return false;
			}
		} else if (!sysPrefix.equals(other.sysPrefix)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
	/**
	 * @return the sysPrefix
	 */
	public String getSysPrefix() {
		return sysPrefix;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	private String sysPrefix;
	private String name;
	private String value;
	private String description;
	private long id;
	
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
	/**
	  create table corr_protocol_options
	  (
	  		sys_prefix varchar(32),
	  		option_name varchar(255),
	  		option_value varchar(4000),
	  		option_description varchar(4000)
	  )
	 */
	
	public static final String TableName = "corr_protocol_options";
	public static final String SysPrefix = "sys_prefix";
	public static final String OptionName = "option_name";
	public static final String OptionValue = "option_value";
	public static final String OptionDescription = "option_description";
	public static final String Id = "id";
}
