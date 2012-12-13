package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.HashMap;

import com.extjs.gxt.ui.client.util.Format;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Report
public class ReportClient extends TbitsModelData {
	
	private HashMap<String, String> params;
	
	// default constructor
	public ReportClient() {
		super();
	}

	// Static Strings defining keys for corresponding variable
	public static String REPORT_ID = "report_id";
	public static String REPORT_NAME = "report_name";
	public static String DESCRIPTION = "description";
	public static String FILE_NAME = "file_name";
	public static String IS_PRIVATE = "is_private";
	public static String IS_ENABLED = "is_enabled";
	public static String GROUP = "group";

	// getter and setter methods for variable myReportId
	public int getReportId() {
		return (Integer) this.get(REPORT_ID);
	}

	public void setReportId(int myReportId) {
		this.set(REPORT_ID, myReportId);
	}

	// getter and setter methods for variable myReportName
	public String getReportName() {
		return Format.htmlEncode((String) this.get(REPORT_NAME));
	}

	public void setReportName(String myReportName) {
		this.set(REPORT_NAME, myReportName);
	}

	// getter and setter methods for variable myDescription
	public String getDescription() {
		return (String) this.get(DESCRIPTION);
	}

	public void setDescription(String myDescription) {
		this.set(DESCRIPTION, myDescription);
	}

	// getter and setter methods for variable myFileName
	public String getFileName() {
		return (String) this.get(FILE_NAME);
	}

	public void setFileName(String myFileName) {
		this.set(FILE_NAME, myFileName);
	}

	// getter and setter methods for variable isPrivate
	public boolean getIsPrivate() {
		return (Boolean) this.get(IS_PRIVATE);
	}

	public void setIsPrivate(boolean isPrivate) {
		this.set(IS_PRIVATE, isPrivate);
	}

	// getter and setter methods for variable isEnabled
	public boolean getIsEnabled() {
		return (Boolean) this.get(IS_ENABLED);
	}

	public void setIsEnabled(boolean isEnabled) {
		this.set(IS_ENABLED, isEnabled);
	}

	// getter and setter methods for variable myGroup
	public String getGroup() {
		return (String) this.get(GROUP);
	}

	public void setGroup(String myGroup) {
		this.set(GROUP, myGroup);
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	public HashMap<String, String> getParams() {
		return params;
	}
	
	public String getParamQuery(){
		String query = "";
		if(params != null){
			for(String name : params.keySet()){
				String value = params.get(name);
				if(value != null){
					query = query + "&" + Format.htmlEncode(name) + "=" + Format.htmlEncode(value);
				}
			}
		}
		return query;
	}

}