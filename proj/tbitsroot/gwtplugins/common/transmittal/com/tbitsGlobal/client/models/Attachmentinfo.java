package transmittal.com.tbitsGlobal.client.models;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;


public class Attachmentinfo extends TbitsModelData{
	
	public static String REQUEST_FILE_ID 	= "requestFileID";
	public  String FILE_NAME	= "FileName";
	
	public  Integer getREQUEST_FILE_ID() {
		return (Integer) this.get(REQUEST_FILE_ID);
	}

	public  void setREQUEST_FILE_ID(int i) {
		this.set(REQUEST_FILE_ID, i);

	}

	public  String getFILE_NAME() {
		return (String) this.get(FILE_NAME);
	}

	public  void setFILE_NAME(String fIELDNAME) {
		this.set(FILE_NAME, fIELDNAME);
	}

	public Attachmentinfo(){}
	

}
