package corrGeneric.com.tbitsGlobal.shared.domain;


public class ReportEntry 
{
	/**
	 * 
create table corr_report_map
(
	sys_prefix varchar(32),
	report_type1 varchar(255),
	report_type2 varchar(255),
	report_type3 varchar(255),
	report_type4 varchar(255),
	report_type5 varchar(255),
	report_id int
)
	 */
	
	public static final String TableName = "corr_report_map";
	public static final String SysPrefix = "sys_prefix";
	public static final String Type1 = "report_type1";
	public static final String Type2 = "report_type2";
	public static final String Type3 = "report_type3";
	public static final String Type4 = "report_type4";
	public static final String Type5 = "report_type5";
	public static final String ReportId = "report_id";
	public static final String Id = "id";
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReportEntry [sysPrefix=" + sysPrefix + ", type1=" + type1
				+ ", type2=" + type2 + ", type3=" + type3 + ", type4=" + type4
				+ ", type5=" + type5 + ", reportId=" + reportId + "]";
	}
	public ReportEntry(long id,String sysPrefix, String type1, String type2, String type3,
		String type4, String type5, int reportId) {
		super();
		if( null == sysPrefix )
			throw new IllegalArgumentException("sysPrefix cannot be null. The value suppplied was : " + sysPrefix);
		this.id = id ;
		this.sysPrefix = sysPrefix.trim();
		this.type1 = ( null == type1 ? null : type1.trim() );
		this.type2 = ( null == type2 ? null : type2.trim() );
		this.type3 = ( null == type3 ? null : type3.trim() );
		this.type4 = ( null == type4 ? null : type4.trim() );
		this.type5 = ( null == type5 ? null : type5.trim() );
		this.reportId = reportId;
	}
	/**
	 * @return the type4
	 */
	public String getType4() {
		return type4;
	}
	/**
	 * @return the type5
	 */
	public String getType5() {
		return type5;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + reportId;
		result = prime * result
				+ ((sysPrefix == null) ? 0 : sysPrefix.hashCode());
		result = prime * result + ((type1 == null) ? 0 : type1.hashCode());
		result = prime * result + ((type2 == null) ? 0 : type2.hashCode());
		result = prime * result + ((type3 == null) ? 0 : type3.hashCode());
		result = prime * result + ((type4 == null) ? 0 : type4.hashCode());
		result = prime * result + ((type5 == null) ? 0 : type5.hashCode());
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
		if (!(obj instanceof ReportEntry)) {
			return false;
		}
		ReportEntry other = (ReportEntry) obj;
		if (reportId != other.reportId) {
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
		if (type4 == null) {
			if (other.type4 != null) {
				return false;
			}
		} else if (!type4.equals(other.type4)) {
			return false;
		}
		if (type5 == null) {
			if (other.type5 != null) {
				return false;
			}
		} else if (!type5.equals(other.type5)) {
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
	 * @return the reportId
	 */
	public int getReportId() {
		return reportId;
	}
	
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

	private String sysPrefix ;
	private String type1;
	private String type2;
	private String type3;
	private String type4;
	private String type5;
	private int reportId;
}
