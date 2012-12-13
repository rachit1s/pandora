package admin.com.tbitsglobal.admin.client.trn.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.domainObjects.BusinessAreaClient;

public class TrnProcessParam extends TbitsModelData{
	public static String SRC_BA			=	"src_ba";
	public static String PROCESS_ID		=	"process_id";
	public static String NAME			=	"name";
	public static String VALUE			=	"value";
	
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
	
	public String getValue(){
		return (String)this.get(VALUE);
	}
	
	public void setValue(String value){
		this.set(VALUE, value);
	}
}
