package corrGeneric.com.tbitsGlobal.shared.domain;



public class ReportNameEntry 
{
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((reportFileName == null) ? 0 : reportFileName.hashCode());
		result = prime * result + reportId;
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
		if (!(obj instanceof ReportNameEntry)) {
			return false;
		}
		ReportNameEntry other = (ReportNameEntry) obj;
		if (reportFileName == null) {
			if (other.reportFileName != null) {
				return false;
			}
		} else if (!reportFileName.equals(other.reportFileName)) {
			return false;
		}
		if (reportId != other.reportId) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReportNameEntry [id=" + id + ", reportId=" + reportId
				+ ", reportFileName=" + reportFileName + "]";
	}
	
	public ReportNameEntry(long id,int reportId, String reportFileName) {
		super();
		this.id = id; 
		this.reportId = reportId;
		this.reportFileName = ( null == reportFileName ? null : reportFileName.trim() );
	}
	/**
	 * @return the reportId
	 */
	public int getReportId() {
		return reportId;
	}
	/**
	 * @return the reportFileName
	 */
	public String getReportFileName() {
		return reportFileName;
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

	private int reportId ;
	private String reportFileName ;
	/**
	 * 
create table corr_report_name_map
(
	report_id int,
	report_file_name varchar(255)
)

	 */

	public static final String TableName = "corr_report_name_map";
	public static final String ReportId = "report_id";
	public static final String ReportFileName = "report_file_name";
	public static final String Id = "id";
}
