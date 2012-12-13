package transmittal.com.tbitsGlobal.server.cacheObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * POJO for trn_src_target_field_map table key.
 * defines a single row and associated mapping
 * @author devashish
 */
public class SrcTargetFieldMapObject extends TbitsModelData{
	
	public static String TRN_PROCESS_ID = "trnProcessId";
	public static String SRC_SYS_ID		= "srcSysId";
	public static String SRC_FIELD_ID	= "srcFieldId";
	public static String TARGET_SYS_ID	= "targetSysId";
	public static String TARGET_FIELD_ID = "targetFieldId";
	
	public SrcTargetFieldMapObject(){
		super();
	}
	
	//----------getter/setter for process id----------------//
	public Integer getTrnProcessId(){
		return (Integer) this.get(TRN_PROCESS_ID);
	}
	
	public void setTrnProcessId(Integer processId){
		this.set(TRN_PROCESS_ID, processId);
	}
	
	//---------getter/setter for src sys id----------------//
	public Integer getSrcSysId(){
		return (Integer) this.get(SRC_SYS_ID);
	}
	
	public void setSrcSysId(Integer srcSysId){
		this.set(SRC_SYS_ID, srcSysId);
	}
	
	//--------getter/setter for src field id---------------//
	public Integer getSrcFieldId(){
		return (Integer) this.get(SRC_FIELD_ID);
	}
	
	public void setSrcFieldId(Integer srcFieldId){
		this.set(SRC_FIELD_ID, srcFieldId);
	}
	
	//--------getter/setter for target sys id-------------//
	public Integer getTargetSysId(){
		return (Integer) this.get(TARGET_SYS_ID);
	}
	
	public void setTargetSysId(Integer targetSysId){
		this.set(TARGET_SYS_ID, targetSysId);
	}
	
	//--------getter/setter for target field id-----------//
	public Integer getTargetFieldId(){
		return (Integer) this.get(TARGET_FIELD_ID);
	}
	
	public void setTargetFieldId(Integer targetFieldId){
		this.set(TARGET_FIELD_ID, targetFieldId);
	}

	@Override
	public int hashCode() {
		String trnProcessIdString = Integer.toString(this.getTrnProcessId());
		String srcSysIdString	  = Integer.toString(this.getSrcSysId());
		String targetSysIdString  = Integer.toString(this.getTargetSysId());
		
		String hashString = trnProcessIdString + "-" + srcSysIdString + "-" + targetSysIdString;
		return hashString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.hashCode() == this.hashCode())
			return true;
		return false;
	}
	
	
}
