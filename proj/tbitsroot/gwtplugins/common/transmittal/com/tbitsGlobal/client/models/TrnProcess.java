package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class TrnProcess extends TbitsModelData{
	public static String SRC_BA			=	"src_ba";
	public static String PROCESS_ID		=	"process_id";
	public static String NAME			=	"name";
	public static String DESCRIPTION	=	"desc";
	public static String ORDER			=	"order";
	public static String DTN_BA			=	"dtn_ba";
	public static String DTR_BA			=	"dtr_ba";
	public static String SERIAL_KEY		=	"serial_key";
	
	public BusinessAreaClient getSrcBA(){
		return (BusinessAreaClient)this.get(SRC_BA);
	}
	
	public void setSrcBA(BusinessAreaClient srcBA){
		this.set(SRC_BA, srcBA);
	}
	
	public int getProcessId(){
		if(this.getPropertyNames().contains(PROCESS_ID))
			return (Integer)this.get(PROCESS_ID);
		return 0;
	}
	
	public void setProcessId(int processId){
		this.set(PROCESS_ID, processId);
	}
	
	public String getName(){
		return (String)this.get(NAME);
	}
	
	public void setName(String name){
		this.set(NAME, name);
	}
	
	public String getDescription(){
		return (String)this.get(DESCRIPTION);
	}
	
	public void setDescription(String displayName){
		this.set(DESCRIPTION, displayName);
	}
	
	public int getOrder(){
		return (Integer)this.get(ORDER);
	}
	
	public void setOrder(int order){
		this.set(ORDER, order);
	}
	
	public BusinessAreaClient getDTNBA(){
		return (BusinessAreaClient)this.get(DTN_BA);
	}
	
	public void setDTNBA(BusinessAreaClient dtnBA){
		this.set(DTN_BA, dtnBA);
	}
	
	public BusinessAreaClient getDTRBA(){
		return (BusinessAreaClient)this.get(DTR_BA);
	}
	
	public void setDTRBA(BusinessAreaClient dtnBA){
		this.set(DTR_BA, dtnBA);
	}
	
	public String getSerialKey(){
		return (String)this.get(SERIAL_KEY);
	}
	
	public void setSerialKey(String serialKey){
		this.set(SERIAL_KEY, serialKey);
	}
	
	public <T extends TbitsModelData> T clone(T model) {
		model = super.clone(model);
		model.remove(PROCESS_ID);
		return model;
	}
}
