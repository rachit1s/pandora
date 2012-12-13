package corrGeneric.com.tbitsGlobal.shared.domain;

import java.io.Serializable;


public class BaFieldEntry implements Serializable
{
	private BaFieldEntry()
	{
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fromFieldName == null) ? 0 : fromFieldName.hashCode());
		result = prime * result
				+ ((fromSysPrefix == null) ? 0 : fromSysPrefix.hashCode());
		result = prime * result
				+ ((toFieldName == null) ? 0 : toFieldName.hashCode());
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
		if (!(obj instanceof BaFieldEntry)) {
			return false;
		}
		BaFieldEntry other = (BaFieldEntry) obj;
		if (fromFieldName == null) {
			if (other.fromFieldName != null) {
				return false;
			}
		} else if (!fromFieldName.equals(other.fromFieldName)) {
			return false;
		}
		if (fromSysPrefix == null) {
			if (other.fromSysPrefix != null) {
				return false;
			}
		} else if (!fromSysPrefix.equals(other.fromSysPrefix)) {
			return false;
		}
		if (toFieldName == null) {
			if (other.toFieldName != null) {
				return false;
			}
		} else if (!toFieldName.equals(other.toFieldName)) {
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BaFieldEntry [id=" + id + ", fromSysPrefix=" + fromSysPrefix
				+ ", fromFieldName=" + fromFieldName + ", toSysPrefix="
				+ toSysPrefix + ", toFieldName=" + toFieldName + "]";
	}
	public String getFromSysPrefix() {
		return fromSysPrefix;
	}
	public String getFromFieldName() {
		return fromFieldName;
	}
	public String getToSysPrefix() {
		return toSysPrefix;
	}
	public String getToFieldName() {
		return toFieldName;
	}
	public BaFieldEntry(long id, String sysPrefix, String fieldName,
			String toSysPrefix, String toFieldName) {
		super();
		this.id = id;
		this.fromSysPrefix = sysPrefix.trim();
		this.fromFieldName = fieldName.trim();
		this.toSysPrefix = toSysPrefix.trim();
		this.toFieldName = toFieldName.trim();
	}
	/**
	 * 
create table corr_ba_field_map
(
	from_sys_prefix varchar(32),
	from_field_name varchar(128),
	to_sys_prefix varchar(32),
	to_field_name varchar(128)
)

	 */
	
	public static final String TableName = "corr_ba_field_map" ;
	public static final String Id = "id";
	public static final String FromSysPrefix = "from_sys_prefix";
	public static final String FromFieldName = "from_field_name";
	public static final String ToSysPrefix = "to_sys_prefix";
	public static final String ToFieldName = "to_field_name";
	
	private String fromSysPrefix ;
	private String fromFieldName ;
	private String toSysPrefix ;
	private String toFieldName ;
	private long id ;
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
