	package transmittal.com.tbitsGlobal.client.models;
	import java.util.*;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;
	public class AttachmentModel extends TbitsModelData{
	
		public static String REQUEST_ID 	= "requestID";
		public static  String SUBJECT	= "subject";
		public  static String ATTACHMENT_DETAILS="attachmentdetails";
		
		public  String getREQUEST_ID() {
			return (String) this.get(REQUEST_ID);
		}

		public  void setREQUEST_ID(String requestID) {
			this.set(REQUEST_ID, requestID);

		}

		public  String getSUBJECT() {
			return (String) this.get(SUBJECT);
		}

		public  void setSUBJECT(String Subject) {
			this.set(SUBJECT, Subject);
		}
		
		public  HashMap<String,List<Attachmentinfo>> getAttachmentDetails() {
			return ( HashMap<String, List<Attachmentinfo>>)this.get(ATTACHMENT_DETAILS);
		}

		public  void setAttachmentDetails( HashMap<String,List<Attachmentinfo>>  AttachmentDetails) {
			this.set(ATTACHMENT_DETAILS, AttachmentDetails);
		}
		
		
		
	
	}
	

