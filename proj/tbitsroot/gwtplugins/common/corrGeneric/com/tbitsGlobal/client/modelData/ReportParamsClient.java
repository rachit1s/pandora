package corrGeneric.com.tbitsGlobal.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * POJO for report params map
 * @author devashish
 *
 */
public class ReportParamsClient extends TbitsModelData {

	public static String ID = "id";
	public static String REPORT_ID = "reportId";
	public static String PARAM_TYPE = "paramType";
	public static String PARAM_NAME = "paramName";
	public static String PARAM_VALUE_TYPE = "paramValueType";
	public static String PARAM_VALUE	= "paramValue";
	
	//------------constructor------------------//
	public ReportParamsClient(){
		super();
	}
	
	//------------get/set id--------------//
	public String getId(){
		return (String) this.get(ID);
	}
	
	public void setId(String id){
		this.set(ID, id);
	}
	
	//------------get/set report id --------//
	public String getReportId(){
		return (String) this.get(REPORT_ID);
	}
	
	public void setReportId(String reportId){
		this.set(REPORT_ID, reportId);
	}
	
	//------------get/set param type--------//
	public String getParamType(){
		return (String) this.get(PARAM_TYPE);
	}
	
	public void setParamType(String paramType){
		this.set(PARAM_TYPE, paramType);
	}
	
	//------------get/set param name--------//
	public String getParamName(){
		return (String) this.get(PARAM_NAME);
	}
	
	public void setParamName(String paramName){
		this.set(PARAM_NAME, paramName);
	}
	
	//------------get/set param value type--//
	public String getParamValueType(){
		return (String) this.get(PARAM_VALUE_TYPE);
	}
	
	public void setParamValueType(String paramValueType){
		this.set(PARAM_VALUE_TYPE, paramValueType);
	}
	
	//------------set/set param value --------//
	public String getParamValue(){
		return (String) this.get(PARAM_VALUE);
	}
	
	public void setParamValue(String paramValue){
		this.set(PARAM_VALUE, paramValue);
	}
}
