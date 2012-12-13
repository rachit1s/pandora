package transmittal.com.tbitsGlobal.server.cacheObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

/**
 * POJO for trn_post_trn_field_map table key.
 * defines a single row and associated mapping.
 * This also overrides the hasCode() and equals() methods of superclass to
 * ensure that same values of the fields result in same object being generated
 * @author devashish
 */
public class PostTrnFieldMapObject extends TbitsModelData {

	public static String DTN_BA_ID 	= "dtnBAId";
	public static String TRN_PROCESS_ID = "trnProcessId";
	public static String DTN_SYS_ID	= "dtnSysId";
	public static String DTR_SYS_ID	= "dtrSysId";
	
	public PostTrnFieldMapObject(){
		
	}
	
	//-------------getter/setter for dtnBaId--------------------//
	public Integer getDtnBAId(){
		return (Integer) this.get(DTN_BA_ID);
	}
	
	public void setDtnBAId(Integer dtnBaId){
		this.set(DTN_BA_ID, dtnBaId);
	}
	
	//-------------getter/setter for trnProcessId----------------//
	
	public Integer getTrnProcessId(){
		return (Integer) this.get(TRN_PROCESS_ID);
	}
	
	public void setTrnProcessId(Integer trnProcessId){
		this.set(TRN_PROCESS_ID, trnProcessId);
	}
	
	//------------getter/setter for dtnSysId---------------------//
	
	public Integer getDtnSysId(){
		return (Integer) this.get(DTN_SYS_ID);
	}
	
	public void setDtnSysId(Integer dtnSysId){
		this.set(DTN_SYS_ID, dtnSysId);
	}
	
	//-----------getter /setter for dtrSysId---------------------//
	
	public Integer getDtrSysId(){
		return (Integer) this.get(DTR_SYS_ID);
	}
	
	public void setDtrSysId(Integer dtrSysId){
		this.set(DTR_SYS_ID, dtrSysId);
	}

	
	@Override
	public int hashCode() {
		String dtnBaIdString	  = Integer.toString(this.getDtnBAId());
		String trnProcessIdString = Integer.toString(this.getTrnProcessId());
		String dtnSysIdString	  = Integer.toString(this.getDtnSysId());
		String dtrSysIdString	  = Integer.toString(this.getDtrSysId());
		
		String hashString = dtnBaIdString + "-" + trnProcessIdString + "-" + dtnSysIdString + "-" + dtrSysIdString;
		return hashString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.hashCode() == this.hashCode())
			return true;
		return false;
	}
	
}
