package mom.com.tbitsGlobal.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public class DraftData implements Serializable{
	private String caption;
	private Map<String, Object> headerModelMap;
	private List<Map<String, Object>> actionMaps;
	private HashMap<String, String> params;
	
	public DraftData() {
		// TODO Auto-generated constructor stub
	}
	
	public DraftData(PrintData data) {
		this();
		this.caption = data.getCaption();
		this.headerModelMap = data.getHeaderModel().getProperties();
		
		this.actionMaps = new ArrayList<Map<String,Object>>();
		for(TbitsTreeRequestData model : data.getActions()){
			actionMaps.add(model.getProperties());
		}
		
		this.params = data.getParams();
	}
	
	public PrintData getPrintData(){
		PrintData printData = new PrintData();
		
		printData.setCaption(caption);
		
		TbitsTreeRequestData headerModel = new TbitsTreeRequestData();
		headerModel.setProperties(headerModelMap);
		printData.setHeaderModel(headerModel);
		
		printData.setActions(new ArrayList<TbitsTreeRequestData>());
		for(Map<String,Object> map : actionMaps){
			TbitsTreeRequestData model = new TbitsTreeRequestData();
			model.setProperties(map);
			printData.getActions().add(model);
		}
		
		printData.setParams(params);
		
		return printData;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Map<String, Object> getHeaderModelMap() {
		return headerModelMap;
	}

	public void setHeaderModelMap(Map<String, Object> headerModelMap) {
		this.headerModelMap = headerModelMap;
	}

	public List<Map<String, Object>> getActionMaps() {
		return actionMaps;
	}

	public void setActionMaps(List<Map<String, Object>> actionMaps) {
		this.actionMaps = actionMaps;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}
	
	
}
