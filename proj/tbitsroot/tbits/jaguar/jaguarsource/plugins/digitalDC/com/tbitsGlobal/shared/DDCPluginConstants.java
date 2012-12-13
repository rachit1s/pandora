package digitalDC.com.tbitsGlobal.shared;

import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

public class DDCPluginConstants {

	public static String getDocField(){
		return DocumentFieldNameToBeUsed;
	}



	public static  String DocumentFieldNameToBeUsed ;
	
	

	public static void setDocField(String documentFieldNameToBeUsed) {
		DocumentFieldNameToBeUsed = documentFieldNameToBeUsed;
	}

	public static Integer getDeliverableFieldID() {
		return deliverableFieldID;
	}

	public static void setDeliverableFieldID(Integer deliverableFieldID) {
		DDCPluginConstants.deliverableFieldID = deliverableFieldID;
	}

	public static Integer getTRN_PROCESS_ID() {
		return TRN_PROCESS_ID;
	}

	public static void setTRN_PROCESS_ID(Integer tRNPROCESSID) {
		TRN_PROCESS_ID = tRNPROCESSID;
	}



	public static  Integer deliverableFieldID;
	
	public static  Integer TRN_PROCESS_ID ;
	
	public static final String TITLE = "subject";
	
	public static ArrayList<TbitsModelData>fields;
}
