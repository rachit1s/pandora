package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * POJO for report name map
 * @author devashish
 *
 */
public class ReportNameClient extends TbitsModelData {

	public static String ID		= "id";
	public static String REPORT_ID = "reportId";
	public static String REPORT_FILE_NAME = "reportFileName";
	
	//--------------constructor------------------------//
	public ReportNameClient(){
		super();
	}
	
	//--------------get/set id-----------------------------//
	public String getId(){
		return (String) this.get(ID);
	}
	
	public void setId(String id){
		this.set(ID, id);
	}
	
	//--------------get/set report id-----------------------------//
	public String getReportId(){
		return (String) this.get(REPORT_ID);
	}
	
	public void setReportId(String reportId){
		this.set(REPORT_ID, reportId);
	}
	
	//--------------get/set report filename-----------------------------//
	public String getReportFileName(){
		return (String) this.get(REPORT_FILE_NAME);
	}
	
	public void setReportFileName(String reportFileName){
		this.set(REPORT_FILE_NAME, reportFileName);
	}	
}
