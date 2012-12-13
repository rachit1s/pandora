package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.TypeClient;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

/**
 * Contains correspondence report map variables.
 * POJO for ReportMap
 * @author devashish
 *
 */
public class ReportMapClient extends TbitsModelData {
	
	public static String ID			= "id";
	public static String REPORT_ID	= "reportId";
	public static String SYS_PREFIX = "sysPrefix";
	public static String STATUS		= "status";
	
	public static String REPORT_TYPE1 = GenericParams.ReportType1;
	public static String REPORT_TYPE2 = GenericParams.ReportType2;
	public static String REPORT_TYPE3 = GenericParams.ReportType3;
	public static String REPORT_TYPE4 = GenericParams.ReportType4;
	public static String REPORT_TYPE5 = GenericParams.ReportType5;
	
	public ReportMapClient(){
		super();
	}
	
	//--------------get/set report id-----------------------------//
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
	
	public void setReportId(String id){
		this.set(REPORT_ID, id);
	}
	
	//----------------get/set sysPrefix------------------//
	public String getSysPrefix(){
		return (String) this.get(SYS_PREFIX);
	}
	
	public void setSysPrefix(String sysPrefix){
		this.set(SYS_PREFIX, sysPrefix);
	}
	
	//--------------get/set status type--------------------//
	public String getStatus(){
		return (String) this.get(STATUS);
	}	
	public void setStatus(String status){
		this.set(STATUS, status);
	}
	
	//--------------get/set report type--------------------//
	public TypeClient getType1(){
		return (TypeClient) this.get(REPORT_TYPE1);
	}
	
	public void setType1(TypeClient type1){
		this.set(REPORT_TYPE1, type1);
	}
	
	//--------------get/set report type--------------------//
	public TypeClient getType2(){
		return (TypeClient) this.get(REPORT_TYPE2);
	}
	
	public void setType2(TypeClient type2){
		this.set(REPORT_TYPE2, type2);
	}
	
	//--------------get/set report type--------------------//
	public TypeClient getType3(){
		return (TypeClient) this.get(REPORT_TYPE3);
	}
	
	public void setType3(TypeClient type3){
		this.set(REPORT_TYPE3, type3);
	}
	
	//--------------get/set report type--------------------//
	public TypeClient getType4(){
		return (TypeClient) this.get(REPORT_TYPE4);
	}
	
	public void setType4(TypeClient type4){
		this.set(REPORT_TYPE4, type4);
	}
	
	//--------------get/set report type--------------------//
	public TypeClient getType5(){
		return (TypeClient) this.get(REPORT_TYPE5);
	}
	
	public void setType5(TypeClient type5){
		this.set(REPORT_TYPE5, type5);
	}
	

}
