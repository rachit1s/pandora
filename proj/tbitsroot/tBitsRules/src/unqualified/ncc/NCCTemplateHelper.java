package ncc;

import java.io.File;
import java.util.ArrayList;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;

/**
 * 
 * @author lokesh
 *
 *
 * This class to serve the purpose of providing all the data required by the transmittal template.
 * The required parameters are:
 * I. Master page: 
 * 	1. DTN No.
 *  2. To Address:[{line1:"Wardha Power Company Limited",line2:"8-2-293/82/A/431/A, Jubilee Hills, Road No-22", line3:"Hyderabad - 500 033, India."}]
 *  
 * II. Template Layout:
 * 	1. Subject - Get it from the transmittal-process-table details
 *  2. Kind Attn. : G.P.Rao
 *  
 *  3. Your Reference Details table:
 *  	a. Ref Id.
 *  	b. DTN Number
 *  	c. DTN Date
 *  
 *  4. Drawings/Documents list table:
 *  	a. Sl. No.
 *  	b. Ref
 *  	c. Description
 *  	d. Drawing/Document
 *  	e. Rev
 *  	f. Qty
 *  	g. Cat
 *  	h. Type
 *  	i. Summary(Remark of each document)
 *  
 *  5. Distribution to:
 *  	a. Sl. No.
 *  	b. Name
 *  	c. Organization
 *  	d. email
 *  	e. phoneNo.
 *  	f. Qty???
 *  	g. Remark???
 *  
 *  6. Logger info of the originating firm(Yours Faithfully):
 * 		a. Name
 * 		b. Contact No
 * 		c. E-Mail
 * 
 */
public class NCCTemplateHelper {
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger("ncc");
	
	private String toAddress, dtnNumber, subject, remarks, kindAttentionString, emailBody;
	private ArrayList<String[]> distributionList, drawingsList;
	private String[] loggerInfo = {"", "", ""};
	private String[] approvalCategory;
	private String[] documentTypes;
	private String loggerImagePath, yourReferenceNumber, transmittalDate;
	public NCCTemplateHelper(String toAddress, String dtnNumber,
			String subject, String remarks, String kindAttentionString,
			ArrayList<String[]> drawingsList,
			String[] approvalCategory, String[] documentTypes,
			ArrayList<String[]> distributionList,
			String[] loggerInfo, String emailBody, 
			String yourReferenceNumber, String transmittalDate){//, String reference) {
		super();
		this.setToAddress(toAddress);
		this.dtnNumber = dtnNumber;
		this.subject = subject;
		this.remarks = remarks;
		this.kindAttentionString = kindAttentionString;
		this.drawingsList = drawingsList;
		this.approvalCategory = approvalCategory;
		this.setDocumentTypes(documentTypes);
		this.distributionList = distributionList;		
		this.loggerInfo = loggerInfo;
		//this.reference = reference;
		File imageFile = Configuration.findPath("tbitsreports/" + loggerInfo[3] + ".gif");
		if((imageFile != null) && (imageFile.exists())){
			this.loggerImagePath = 	imageFile.getAbsolutePath();			
		}
		else
			LOG.warn("No image exists with the name: " + loggerInfo[3] + ".gif, hence creating transmittal without the digital signature.");
		
		this.setEmailBody(emailBody);
		this.yourReferenceNumber = yourReferenceNumber;
		this.transmittalDate = transmittalDate;
	}
	public String getDtnNumber() {
		return dtnNumber;
	}
	public void setDtnNumber(String dtnNumber) {
		this.dtnNumber = dtnNumber;
	}
	public String getSubject() {
		return subject;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getKindAttentionString() {
		return kindAttentionString;
	}
	public void setKindAttentionString(String kindAttentionString) {
		this.kindAttentionString = kindAttentionString;
	}
	public void setApprovalCategory(String[] approvalCategory) {
		this.approvalCategory = approvalCategory;
	}
	public String[] getApprovalCategory() {
		return approvalCategory;
	}
	public void setDocumentTypes(String[] documentTypes) {
		this.documentTypes = documentTypes;
	}
	public String[] getDocumentTypes() {
		return documentTypes;
	}
	public ArrayList<String[]> getDistributionList() {
		return distributionList;
	}
	public void setDistributionList(ArrayList<String[]> distributionList) {
		this.distributionList = distributionList;
	}
	public ArrayList<String[]> getDrawingsList() {
		return drawingsList;
	}
	public void setDrawingsList(ArrayList<String[]> drawingsList) {
		this.drawingsList = drawingsList;
	}
	public String[] getLoggerInfo() {
		return loggerInfo;
	}
	public void setLoggerInfo(String[] loggerInfo) {
		this.loggerInfo = loggerInfo;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	public String getToAddress() {
		return toAddress;
	}
	/*public void setReference(String reference) {
		this.reference = reference;
	}
	public String getReference() {
		return reference;
	}*/
	public void setLoggerImagePath(String loggerImagePath) {
		this.loggerImagePath = loggerImagePath;		
	}
	public String getLoggerImagePath() {
		return loggerImagePath;
	}
	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}
	public String getEmailBody() {
		return emailBody;
	}
	public void setYourReferenceNumber(String yourReferenceNumber) {
		this.yourReferenceNumber = yourReferenceNumber;
	}
	public String getYourReferenceNumber() {
		return yourReferenceNumber;
	}
	public void setTransmittalDate(String transmittalDate) {
		this.transmittalDate = transmittalDate;
	}
	public String getTransmittalDate() {
		return transmittalDate;
	}
}

