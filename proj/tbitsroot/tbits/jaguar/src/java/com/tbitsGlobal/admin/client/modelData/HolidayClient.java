package com.tbitsGlobal.admin.client.modelData;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class HolidayClient extends TbitsModelData{

	public static final String OFFICE = "office";
	public static final String DATE = "date";
	public static final String ZONE = "zone";
	public static final String DESCRIPTION = "description";
	
	public HolidayClient() {
		super();
	}
	
	public void setOffice(String office){
		this.set(OFFICE, office);
	}
	
	public String getOffice(){
		return (String)this.get(OFFICE);
	}
	
	public void setDate(String date){
		this.set(DATE, date);
	}
	
	public String getDate(){
		return (String)this.get(DATE);
	}
	
	public void setZone(String zone){
		this.set(ZONE, zone);
	}
	
	public String getZone(){
		return (String)this.get(ZONE);
	}
	
	public void setDescription(String desc){
		this.set(DESCRIPTION, desc);
	}
	
	public String getDescription(){
		return (String)this.get(DESCRIPTION);
	}
}
