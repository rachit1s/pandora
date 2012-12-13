package corrGeneric.com.tbitsGlobal.shared.domain;

import java.io.Serializable;


public class PropertyEntry implements Serializable
{
	private PropertyEntry()
	{
		// for serialization
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (!(obj instanceof PropertyEntry)) {
			return false;
		}
		PropertyEntry other = (PropertyEntry) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PropertyEntry [id=" + id + ", name=" + name + ", value="
				+ value + ", description=" + description + "]";
	}

	/**
 create table corr_properties
(
	property_name varchar(400),
	property_value varchar(4000),
	property_description varchar(4000),
	UNIQUE(property_name)
)
	 */
	
	public static final String TableName = "corr_properties";
	public static final String PropertyName = "property_name";
	public static final String PropertyValue = "property_value";
	public static final String PropertyDescription = "property_description";
	public static final String Id = "id";
	
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
	public String getDescription() {
		return description;
	}
	public PropertyEntry(long id,String name, String value, String description) {
		super();
		this.id = id;
		this.name = name.trim();
		this.value = (null == value ? null : value.trim());
		this.description = ( null == description ? null : description.trim());
	}
	
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

	private String name;
	private String value;
	private String description;
}
