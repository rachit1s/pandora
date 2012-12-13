package corrGeneric.com.tbitsGlobal.shared.domain;

import java.io.Serializable;


public class FieldNameEntry implements Serializable
{
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FieldNameEntry [id=" + id + ", corrFieldName=" + corrFieldName
				+ ", baFieldName=" + baFieldName + ", sysPrefix=" + sysPrefix
				+ "]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((baFieldName == null) ? 0 : baFieldName.hashCode());
		result = prime * result
				+ ((corrFieldName == null) ? 0 : corrFieldName.hashCode());
		result = prime * result
				+ ((sysPrefix == null) ? 0 : sysPrefix.hashCode());
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
		if (!(obj instanceof FieldNameEntry)) {
			return false;
		}
		FieldNameEntry other = (FieldNameEntry) obj;
		if (baFieldName == null) {
			if (other.baFieldName != null) {
				return false;
			}
		} else if (!baFieldName.equals(other.baFieldName)) {
			return false;
		}
		if (corrFieldName == null) {
			if (other.corrFieldName != null) {
				return false;
			}
		} else if (!corrFieldName.equals(other.corrFieldName)) {
			return false;
		}
		if (sysPrefix == null) {
			if (other.sysPrefix != null) {
				return false;
			}
		} else if (!sysPrefix.equals(other.sysPrefix)) {
			return false;
		}
		return true;
	}
	private FieldNameEntry()
	{
		// for serialization
	}
	public FieldNameEntry(long id,String corrFieldName, String sysPrefix,
			String baFieldName) {
		super();
		this.id = id;
		this.corrFieldName = corrFieldName.trim();
		this.sysPrefix = sysPrefix.trim();
		this.baFieldName = baFieldName.trim();
	}
	/**
	create table  corr_field_name_map
	(
		corr_field_name varchar(128),
		sys_prefix varchar(32),
		field_name varchar(128)	
	)
		 */

		
	public String getCorrFieldName() {
		return corrFieldName;
	}
	public String getSysPrefix() {
		return sysPrefix;
	}
	public String getBaFieldName() {
		return baFieldName;
	}
	
	public static final String TableName = "corr_field_name_map";
	public static final String Id = "id";
	public static final String CorrFieldName = "corr_field_name";
	public static final String SysPrefix = "sys_prefix";
	public static final String FieldName = "field_name";
	
	private String corrFieldName;
	private String sysPrefix;
	private String baFieldName;
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
}
