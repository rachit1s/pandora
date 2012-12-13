package digitalDC.com.tbitsGlobal.client;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
public class DocNumberFileTuple extends TbitsModelData {
	
	
/*	public static String DOC_NUMBER = "DocNumber";

	public static String REVISION = "Revision";*/
	
	public static String FILE_PATH = "filepath";
	
	
	public static String REQUEST_ID = "requestId";
	
	public static String SUBJECT = "subject";
	
	public static String FILE_NAME_TO_BE_USED = "filenameToBeUsed";
	
	public static String IS_REGEX_CORRECT = "isRegexCorrect";
	
	
	public String getSubject(){
		return (String) this.get(SUBJECT);
	}
	
	public void setSubject(String subject){
		this.set(SUBJECT, subject);
	}
	
	
	
	public Boolean getIs_Regex_Correct(){
		return (Boolean) this.get(IS_REGEX_CORRECT);
	}
	
	public void setIs_Regex_Correct(Boolean regex){
		this.set(IS_REGEX_CORRECT, regex);
	}
	
/*	public String getDOC_NUMBER(){
		return (String) this.get(DOC_NUMBER);
	}
	
	public void setDOC_NUMBER(String docNO){
		this.set(DOC_NUMBER, docNO);
	}
	*/
	public String getFILE_PATH(){
		return (String) this.get(FILE_PATH);
	}
	
	public void setFILE_PATH(String filePath){
		this.set(FILE_PATH, filePath);
	}
	
/*	
	public String getREVISION(){
		return (String) this.get(REVISION);
	}
	
	public void setREVISION(String string){
		this.set(REVISION, string);
	}*/
	
	
	
	public Integer getRequestID(){
		return (Integer) this.get(REQUEST_ID);
	}
	
	public void setRequestID(Integer requestId){
		this.set(REQUEST_ID, requestId);
	}
	
	
	public String getfilenameToBeUsed(){
		return (String) this.get(FILE_NAME_TO_BE_USED);
	}
	
	public void setfilenameToBeUsed(String FilenameToBeUsed){
		this.set(FILE_NAME_TO_BE_USED, FilenameToBeUsed);
	}
	
	
}
