package corrGeneric.com.tbitsGlobal.shared.domain;


public class ReportParamEntry 
{

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReportParamEntry [id=" + id + ", reportId=" + reportId
				+ ", paramName=" + paramName + ", paramType=" + paramType
				+ ", paramValueType=" + paramValueType + ", paramValue="
				+ paramValue + "]";
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

	private int reportId;
	private String paramName;
	private String paramType;
	private String paramValueType;
	private String paramValue;
	/**
	 * 
create table corr_report_params_map
(
	report_id varchar(255) not null,
	param_type varchar(32) not null,
	param_name varchar(511) not null,
	param_value_type varchar(32) not null,
	param_value varchar(1024) not null
)
	 */
	/**
	 * @return the reportId
	 */
	public int getReportId() {
		return reportId;
	}
	/**
	 * @return the paramName
	 */
	public String getParamName() {
		return paramName;
	}
	/**
	 * @return the paramType
	 */
	public String getParamType() {
		return paramType;
	}
	/**
	 * @return the paramValueType
	 */
	public String getParamValueType() {
		return paramValueType;
	}
	public ReportParamEntry(long id ,int reportId, String paramType, String paramName,
			String paramValueType, String paramValue) {
		super();
		
		if( null == paramName || null == paramValue) 
			throw new IllegalArgumentException("The arguments : " + ParamName + " and " + ParamValue + " cannot be null. Their supplied values are respectively : " + paramName + "," + paramValue);
		this.id = id ;
		this.reportId = reportId;
		this.paramType = ( null == paramType ? null : paramType.trim() );
		this.paramName = ( null == paramName ? null : paramName.trim() );
		this.paramValueType = ( null == paramValueType ? null : paramValueType.trim() );
		this.paramValue = ( null == paramValue ? null : paramValue.trim() );
	}

	/**
	 * @return the paramValue
	 */
	public String getParamValue() {
		return paramValue;
	}
	
	public static final String TableName = "corr_report_params_map";
	public static final String ReportId = "report_id";
	public static final String ParamName = "param_name";
	public static final String ParamType = "param_type";
	public static final String ParamValueType = "param_value_type";
	public static final String ParamValue = "param_value";
	public static final String Id = "id";

}
