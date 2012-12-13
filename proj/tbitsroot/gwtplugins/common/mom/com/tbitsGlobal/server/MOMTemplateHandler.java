package mom.com.tbitsGlobal.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;


public class MOMTemplateHandler implements Serializable{
	private Hashtable<String, String> headerTable;
	private ArrayList<Hashtable<String, String>> actionTableList;
	private Hashtable<String, String> configTable;
	private List<String> enclosures;
	
	private BusinessArea currentBA;
	private Field aField;
	private Type aType;
	
	public MOMTemplateHandler(BusinessArea ba, Field aField, Type aType) {
		super();
		
		this.currentBA = ba;
		this.aField = aField;
		this.aType = aType;
	}

	public MOMTemplateHandler(ArrayList<Hashtable<String, String>> actionTableList,
			Hashtable<String, String> configTable,
			Hashtable<String, String> headerTable, List<String> enclosures) {
		super();
		this.actionTableList = actionTableList;
		this.configTable = configTable;
		this.headerTable = headerTable;
		this.enclosures = enclosures;
	}

	public Hashtable<String, String> getHeaderTable() {
		return headerTable;
	}

	public void setHeaderTable(Hashtable<String, String> headerTable) {
		this.headerTable = headerTable;
	}

	public List<Hashtable<String, String>> getActionTableList() {
		return actionTableList;
	}

	public void setActionTableList(ArrayList<Hashtable<String, String>> actionTableList) {
		this.actionTableList = actionTableList;
	}

	public Hashtable<String, String> getConfigTable() {
		return configTable;
	}

	public void setConfigTable(Hashtable<String, String> configTable) {
		this.configTable = configTable;
	}

	public void setEnclosures(List<String> enclosures) {
		this.enclosures = enclosures;
	}

	public List<String> getEnclosures() {
		return enclosures;
	}

	public void setCurrentBA(BusinessArea currentBA) {
		this.currentBA = currentBA;
	}

	public BusinessArea getCurrentBA() {
		return currentBA;
	}

	public void setAField(Field aField) {
		this.aField = aField;
	}

	public Field getAField() {
		return aField;
	}

	public void setAType(Type aType) {
		this.aType = aType;
	}

	public Type getAType() {
		return aType;
	}
}
