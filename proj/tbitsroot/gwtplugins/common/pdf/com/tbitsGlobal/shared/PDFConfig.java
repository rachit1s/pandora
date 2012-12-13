/**
 * 
 */
package pdf.com.tbitsGlobal.shared;

import java.io.Serializable;

/**
 * @author Lokesh
 *
 */
public class PDFConfig implements Serializable{
	
	public PDFConfig(){}
	
	public PDFConfig(int id, int systemId, String reportTemplateName){
		this.id = id;
		this.systemId = systemId;
		this.reportTemplateName = reportTemplateName;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSystemId() {
		return systemId;
	}
	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}
	public String getReportTemplateName() {
		return reportTemplateName;
	}
	public void setReportTemplateName(String reportTemplateName) {
		this.reportTemplateName = reportTemplateName;
	}
	int id;
	int systemId;
	String reportTemplateName;
	
}
