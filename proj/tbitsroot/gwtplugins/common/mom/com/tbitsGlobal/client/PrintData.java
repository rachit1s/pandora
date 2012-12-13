package mom.com.tbitsGlobal.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public class PrintData implements Serializable{
	private String caption;
	private TbitsTreeRequestData headerModel;
	private List<TbitsTreeRequestData> actions;
	private HashMap<String, String> params;
	public PrintData() {
		super();
	}

	public PrintData(String caption, TbitsTreeRequestData headerModel, List<TbitsTreeRequestData> actions, HashMap<String, String> params){
		super();
		this.caption = caption;
		this.actions = actions;
		this.headerModel = headerModel;
		this.params = params;
	}

	public TbitsTreeRequestData getHeaderModel() {
		return headerModel;
	}

	public void setHeaderModel(TbitsTreeRequestData headerModel) {
		this.headerModel = headerModel;
	}

	public List<TbitsTreeRequestData> getActions() {
		return actions;
	}

	public void setActions(List<TbitsTreeRequestData> actions) {
		this.actions = actions;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}
}
