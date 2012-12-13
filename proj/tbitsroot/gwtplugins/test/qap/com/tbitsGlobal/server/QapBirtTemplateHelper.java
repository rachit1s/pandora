package qap.com.tbitsGlobal.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class QapBirtTemplateHelper implements Serializable {

	private ArrayList<String[]> arr = null;
	private HashMap<String, String>hm =null;
	private ArrayList<String[]>arr1 =null;
	
	public QapBirtTemplateHelper(HashMap<String, String>hm,ArrayList<String[]> arr1) {
//		this.arr = arr;
		this.hm=hm;
		this.arr1=arr1;
		
	}

//	public ArrayList<String[]>  getParam() {
//		return arr;
//	}
	
	public HashMap<String, String> getParametersTable() {
		return hm;
	}
	

	public ArrayList<String[]>  getParam1() {
		return arr1;
	}
	
}
