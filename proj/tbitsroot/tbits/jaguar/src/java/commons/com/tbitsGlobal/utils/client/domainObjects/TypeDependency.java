package commons.com.tbitsGlobal.utils.client.domainObjects;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class TypeDependency extends TbitsModelData{
	public static String SYS_ID			=	"sys_id";
	public static String SRC_FIELD_ID	=	"src_field_id";
	public static String SRC_TYPE_ID	=	"src_type_id";
	public static String DEST_FIELD_ID	=	"dest_field_id";
	public static String DEST_TYPE_ID	=	"dest_type_id";
	
	public int getSysId(){
		return (Integer)this.get(SYS_ID);
	}
	
	public void setSysId(int sysId){
		this.set(SYS_ID, sysId);
	}
	
	public int getSrcFieldId(){
		return (Integer)this.get(SRC_FIELD_ID);
	}
	
	public void setSrcFieldId(int fieldId){
		this.set(SRC_FIELD_ID, fieldId);
	}
	
	public int getSrcTypeId(){
		return (Integer)this.get(SRC_TYPE_ID);
	}
	
	public void setSrcTypeId(int typeId){
		this.set(SRC_TYPE_ID, typeId);
	}
	
	public int getDestFieldId(){
		return (Integer)this.get(DEST_FIELD_ID);
	}
	
	public void setDestFieldId(int fieldId){
		this.set(DEST_FIELD_ID, fieldId);
	}
	
	public int getDestTypeId(){
		return (Integer)this.get(DEST_TYPE_ID);
	}
	
	public void setDestTypeId(int typeId){
		this.set(DEST_TYPE_ID, typeId);
	}
}
