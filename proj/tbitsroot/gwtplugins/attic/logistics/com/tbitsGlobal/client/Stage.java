package logistics.com.tbitsGlobal.client;

import java.io.Serializable;

public class Stage implements Serializable{
	private int stageId;
	private String sourceSysPrefix;
	private String preSysPrefix;
	private StageParams params;
	
	public Stage() {
	}
	
	public Stage(int stageId) {
		super();
		
		this.stageId = stageId;
	}
	
	public String getSourceSysPrefix() {
		return sourceSysPrefix;
	}
	
	public void setSourceSysPrefix(String sourceSysPrefix) {
		this.sourceSysPrefix = sourceSysPrefix;
	}
	
	public String getPreSysPrefix() {
		return preSysPrefix;
	}
	
	public void setPreSysPrefix(String preSysPrefix) {
		this.preSysPrefix = preSysPrefix;
	}
	
	public StageParams getParams() {
		return params;
	}
	
	public void setParams(StageParams params) {
		this.params = params;
	}

	public int getStageId() {
		return stageId;
	}
}
