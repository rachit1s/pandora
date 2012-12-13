package transmittal.com.tbitsGlobal.server;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import transbit.tbits.common.Configuration;

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
public class BirtTemplateHelper implements Serializable{
	
	private BirtTemplateHelper()
	{
		// cosntructor for Serializability
	}
	private static final String DTN_IMAGE_DIR = "DTNImageDirectory";
//	private static final String DTN_HEADER_IMAGE = "DTNHeaderImage";
//	private static final String LOGGER_IMAGE_PATH = "loggerImagePath";
//	private static final String SCANNED_SIGNATURES = "tbitsreports";
	private String dtnNumber, kindAttentionString;
	private ArrayList<String[]> distributionList, drawingsList, selectedAttachmentList;
	private HashMap<String,String> loggerInfo = new HashMap<String, String>();
	private String loggerImagePath, yourReferenceNumber, transmittalDate;
	private String toList;
	private String ccList;
	private HashMap<String, String> parametersTable = null;
	private String imageDir = "tbitsreports";
	private HashMap<String, String> kindAttentionInfo;
	
	public BirtTemplateHelper(HashMap<String,String> paramTable, ArrayList<String[]> drawingsList, 
			ArrayList<String[]>  distributionList, ArrayList<String[]> selectedAttachmentList, 
			HashMap<String,String> loggerInfo, HashMap<String,String> kindAttentionInfo)
	{
		this.parametersTable = paramTable;
		this.distributionList = distributionList;		
		this.drawingsList = drawingsList;
		this.kindAttentionInfo = kindAttentionInfo;
		this.setSelectedAttachmentList(selectedAttachmentList);
		
		if (loggerInfo != null)
			this.loggerInfo.putAll(loggerInfo);
		
		String dtnImageFolder = paramTable.get(DTN_IMAGE_DIR);		
		if ((dtnImageFolder != null) && (!dtnImageFolder.trim().equals("")))
			this.imageDir = dtnImageFolder;
		
		String userLoginColumn = TransmittalUtils.userTableColumnNames[1].trim();
		String userLogin = this.loggerInfo.get(userLoginColumn);
		if ((this.loggerInfo != null) && (null != userLogin) && (!userLogin.trim().equals("")))
			this.loggerImagePath = getImagePath(loggerInfo.get(userLoginColumn), this.imageDir);
		
		this.parametersTable.put(TransmittalUtils.LOGGER_IMAGE_PATH, this.loggerImagePath);	
		
		String headerImage = paramTable.get(TransmittalUtils.DTN_HEADER_IMAGE);		
		if ((headerImage != null) && (!headerImage.trim().equals("")))
			this.parametersTable.put(TransmittalUtils.DTN_HEADER_IMAGE, getImagePath(headerImage, this.imageDir));
		else
			this.parametersTable.put(TransmittalUtils.DTN_HEADER_IMAGE, "");
		
		String footerImage = paramTable.get(TransmittalUtils.DTN_FOOTER_IMAGE);		
		if ((footerImage != null) && (!footerImage.trim().equals("")))
			this.parametersTable.put(TransmittalUtils.DTN_FOOTER_IMAGE, getImagePath(footerImage, this.imageDir));
		else
			this.parametersTable.put(TransmittalUtils.DTN_FOOTER_IMAGE, "");
	}
	
	public static String getImagePath(String userLogin, String imageDirName){
		String imagePath = "";
		File file = Configuration.findPath(imageDirName);
		if (file != null){
			if (file.isDirectory()){
				File[] listFiles = file.listFiles();
				if (listFiles != null){
					for(File imageFile : listFiles){
						if (imageFile.isDirectory())
							continue;
						else{
							String fileName = imageFile.getName();
							int lastIndex = fileName.lastIndexOf(".");
							String substring = "";
							if (lastIndex>0) 
								substring = fileName.substring(0, lastIndex);
							else
								substring = fileName;
							if (userLogin.equalsIgnoreCase(substring))
							{								
								return imageFile.getAbsolutePath();
							}
						}
					}
				}
			}
		}	
		
		return imagePath;
	}	
	
	public String getDtnNumber() {
		return dtnNumber;
	}
	public void setDtnNumber(String dtnNumber) {
		this.dtnNumber = dtnNumber;
	}
	
	public String getKindAttentionString() {
		return kindAttentionString;
	}
	public void setKindAttentionString(String kindAttentionString) {
		this.kindAttentionString = kindAttentionString;
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
	public HashMap<String, String> getLoggerInfo() {
		return loggerInfo;
	}
	public void setLoggerInfo(HashMap<String, String> loggerInfo) {
		this.loggerInfo = loggerInfo;
	}
	
	public void setLoggerImagePath(String loggerImagePath) {
		this.loggerImagePath = loggerImagePath;		
	}
	public String getLoggerImagePath() {
		return loggerImagePath;
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
	public void setToList(String toList) {
		this.toList = toList;
	}
	public String getToList() {
		return toList;
	}
	public void setCcList(String ccList) {
		this.ccList = ccList;
	}
	public String getCcList() {
		return ccList;
	}
	public void setParametersTable(HashMap<String, String> parametersTable) {
		this.parametersTable = parametersTable;
	}
	public HashMap<String, String> getParametersTable() {
		return parametersTable;
	}
	
	public void setSelectedAttachmentList(ArrayList<String[]> selectedAttachmentList) {
		this.selectedAttachmentList = selectedAttachmentList;
	}

	public ArrayList<String[]> getSelectedAttachmentList() {
		return selectedAttachmentList;
	}

	public HashMap<String, String> getKindAttentionInfo() {
		return kindAttentionInfo;
	}

	public static void main(String[] argsv){
		System.out.println("%%%%%%%%%%%%%%%%%%%%%Done%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("File: " + getImagePath("root", "tbitsreports"));
		
	}
}

