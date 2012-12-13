package com.tbitsGlobal.jaguar.client.serializables;

import java.io.Serializable;
import java.util.ArrayList;


import commons.com.tbitsGlobal.utils.client.DQLResults;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;

/**
 * {@link Serializable} class to carry requests for a BA.
 * 
 * @author sourabh
 *
 */
public class BARequests implements Serializable{
	private ArrayList<BAField> fields;
	private DQLResults results;
	
	public BARequests() {
		super();
	}

	public BARequests(ArrayList<BAField> fields, DQLResults results){
		this();
		this.fields = fields;
		this.results = results;
	}

	public void setFields(ArrayList<BAField> fields) {
		this.fields = fields;
	}

	public ArrayList<BAField> getFields() {
		return fields;
	}

	public int getRequestCount(){
		if(results != null){
			return results.getRequests().size();
		}
		return 0;
	}
	
	public int getTotalPages(int pageSize){
		return (results.getTotalRecords()%pageSize > 0)?results.getTotalRecords()/pageSize + 1  : results.getTotalRecords()/pageSize;
	}

	public void setResults(DQLResults results) {
		this.results = results;
	}

	public DQLResults getResults() {
		return results;
	}
}
