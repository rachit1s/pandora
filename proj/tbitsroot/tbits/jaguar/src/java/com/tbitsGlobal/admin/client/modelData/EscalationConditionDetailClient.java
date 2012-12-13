package com.tbitsGlobal.admin.client.modelData;


import java.util.ArrayList;



import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class EscalationConditionDetailClient extends TbitsModelData {
	
	public static final long serialVersionUID = 1L;
	public static String ESC_COND_ID = "esc_cond_id";
	public static String DISPLAY_NAME = "dis_nmae";
	public static String DESCRIPTION = "description";
	public static String ESC_HIERARCHY  =  "esc_hierarchy";
	public static String SRC_BA=  "src_ba";
	public static String SRC_USER_FIELD = "src_user_field";
	public static String SRC_DATE_FIELD = "src_date_field";
	public static String DES_USER_FIELD = "des_user_field";
	public static String DES_DATE_FIELD = "des_date_field";
	public static String SPAN=  "span";
	public static String ON_BEHALF_USER = "on_behalf_user";
	public static String DQL = "dql";
	public static String ESC_COND_PARAMETERS = "esc_cond_params";
	public static String IS_ACTIVE = "active";
	
	
	
	
	public EscalationConditionDetailClient()
	{
		super();
	}
	
	
	public Integer getEscCondId(){
		if(this.getPropertyNames().contains(ESC_COND_ID))
			return (Integer)this.get(ESC_COND_ID);
		return 0;
	}
	
	public void setEscCondId(Integer escCondId){
		this.set(ESC_COND_ID, escCondId);
	}
		
	public void setDisName(String disName){
	
		this.set(DISPLAY_NAME,disName);
		
	}
	
	
	public String getDisName()
	{
		return this.get(DISPLAY_NAME);
	}
	
	public void setDescription(String des){
		
		this.set(DESCRIPTION,des);
		
	}
	
	
	public String getDescription()
	{
		return this.get(DESCRIPTION);
	}
	
	public void setEscHierarchy(EscalationHierarchiesClient hierarchy){
		
		this.set(ESC_HIERARCHY,hierarchy);
		
	}
	
	
	public EscalationHierarchiesClient getEscHierarchy()
	{
		return this.get(ESC_HIERARCHY);
	}
	

	public void setSrcBa(String bac){
		
		this.set(SRC_BA,bac);
		
	}
	
	
	public String getSrcBa()
	{
		return this.get(SRC_BA);
	}
	
public void setSrcUserField(String fcu){
		
		this.set(SRC_USER_FIELD,fcu);
		
	}
	
	
	public String  getSrcUserField()
	{
		return this.get(SRC_USER_FIELD);
	}
	
public void setSrcDateField(String fcd){
		
		this.set(SRC_DATE_FIELD,fcd);
		
	}
	
	
	public  String getSrcDateField()
	{
		return this.get(SRC_DATE_FIELD);
	}
	
public void setDesUserField(String fcu){
		
		this.set(DES_USER_FIELD,fcu);
		
	}
	
	
	public String  getDesUserField()
	{
		return this.get(DES_USER_FIELD);
	}
	
public void setDesDateField(String fcd){
		
		this.set(DES_DATE_FIELD,fcd);
		
	}
	
	
	public  String getDesDateField()
	{
		return this.get(DES_DATE_FIELD);
	}
	
	
public void setSpan(String span){
		
		this.set(SPAN,span);
		
	}
	
	
	public String getSpan()
	{
		return this.get(SPAN);
	}
	
public void setOnBehalfUser(String uc){
		
		this.set(ON_BEHALF_USER,uc);
		
	}
	
	
	public String getOnBehalfUser()
	{
		return this.get(ON_BEHALF_USER);
	}
	
public void setDql(String dql){
		
		this.set(DQL,dql);
		
	}
	
	
	public String getDql()
	{
		return this.get(DQL);
	}
	
public void setParams(ArrayList<EscalationConditionParametersClient> params){
		
		this.set(ESC_COND_PARAMETERS,params);
		
	}
	
	
	public ArrayList<EscalationConditionParametersClient> getParams()
	{
		return this.get(ESC_COND_PARAMETERS);
	}
	
public void setIsActive(Boolean isActive){
		
		this.set(IS_ACTIVE,isActive);
		
	}
	
	
	public Boolean getIsActive()
	{
		return this.get(IS_ACTIVE);
	}
	
	
	
	

}
