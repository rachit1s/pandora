package commons.com.tbitsGlobal.utils.client.bafield;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;


public class ColPrefs extends TbitsModelData{
	private static final long serialVersionUID = 1L;
	
	public static String FIELD_ID		=	"field_id";
	public static String DISPLAY_NAME	=	"display_name";
	public static String NAME			=	"name";
	public static String COLUMN_SIZE	=	"col_size";
	public static String ORDER_BY		= 	"order_by";
	public ColPrefs(){
		super();
	}
	public ColPrefs(BAField field){
		this();
		copyFromBaField(field);
	}
	
	private void  copyFromBaField(BAField field){
		for(String property : field.getPropertyNames()){
			this.set(property, field.get(property));
		}
	}
	
	public void setColSize(int size){
		this.set(COLUMN_SIZE,size);
	}
	public int getColSize(){
		return (Integer)this.get(COLUMN_SIZE);
	}
	
	public void setOrderBy(boolean order){
		this.set(ORDER_BY,order);
	}
	
	public boolean getOrderBy(){
		return (Boolean)this.get(ORDER_BY);
	}
	
	public void setFieldId(int myFieldId) {
		this.set(FIELD_ID, myFieldId);
	}
	
	public void setName(String myName) {
		this.set(NAME, myName);
	}

	public String getName() {
		return (String)this.get(NAME);
	}
	
	public void setDisplayName(String myName) {
		this.set(DISPLAY_NAME, myName);
	}

	public String getDisplayName() {
		return (String)this.get(DISPLAY_NAME);
	}

	public int getFieldId() {
		return (Integer)this.get(FIELD_ID);
	}
	
//	public int compareTo(ColPrefs o) {
//		return this.getName() - o.getName();
//	}
	
//	@Override
//	public int compareTo(ColPrefs o) {
//		return this.getFieldId() - o.getFieldId();
//	}
}
