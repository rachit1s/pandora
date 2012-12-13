package logistics.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class StageParams extends TbitsModelData{
	public static final String PRE_STAGE_COMPONENT_NAME = "pre_stage_component_name";
	
	/**
	 * @return The name of the component from previous stage
	 */
	public String getPreStageComponentName(){
		return (String)this.get(PRE_STAGE_COMPONENT_NAME);
	}
	
	public void setPreStageComponentName(String componentName){
		this.set(PRE_STAGE_COMPONENT_NAME, componentName);
	}
}
