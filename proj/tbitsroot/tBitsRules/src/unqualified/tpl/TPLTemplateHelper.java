/**
 * 
 */
package tpl;

import java.util.ArrayList;

/**
 * @author lokesh
 *
 */
public class TPLTemplateHelper {
	private ArrayList<String[]> drawingsList;
	private ArrayList<String> approvalCategory;
	private String transmittalRefNumber;
	private String toAddress;
	private String subject;
	private String kindAttentionString;
	private String copyForward;
	private String[] loggerInfo;

	public void setDrawingsList(ArrayList<String[]> drawingsList) {
		this.drawingsList = drawingsList;
	}
	
	public ArrayList<String[]> getDrawingsList() {
		return drawingsList;
	}

	public void setApprovalCategory(ArrayList<String> approvalCategory) {
		this.approvalCategory = approvalCategory;
	}

	public ArrayList<String> getApprovalCategory() {
		return approvalCategory;
	}

	public void setTransmittalRefNumber(String transmittalRefNumber) {
		this.transmittalRefNumber = transmittalRefNumber;
	}

	public String getTransmittalRefNumber() {
		return transmittalRefNumber;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setKindAttentionString(String kindAttentionString) {
		this.kindAttentionString = kindAttentionString;
	}

	public String getKindAttentionString() {
		return kindAttentionString;
	}

	public void setCopyForward(String copyForward) {
		this.copyForward = copyForward;
	}

	public String getCopyForward() {
		return copyForward;
	}

	public void setLoggerInfo(String[] loggerInfo) {
		this.loggerInfo = loggerInfo;
	}

	public String[] getLoggerInfo() {
		return loggerInfo;
	}
}
