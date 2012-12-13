package ddc.com.tbitsglobal.ddc.rest;

import transbit.tbits.api.AttachmentInfo;

import ddc.com.tbitsglobal.ddc.domain.FirmProperty;


public class DTNParameter
{
	private int totalParams = 0 ;
	private int totalValues = 0 ;
	private FirmProperty fp;
	private AttachmentInfo ai;
	private String	dtnLetterIdentificationString     ;
	private String	fromField                   ;
	private String	toField                     ;
	private String	dtnNumber                   ;
	private String	ownerDocumentNumber         ;
	private String	contractorDocumentNumber    ;
	private String	vendorDocumentNumber       ;
	private String	revision                    ;
	private String	submissionOrDecisionCode    ;
	private String	dtnDateTimeInDTNNote        ;
	private String	projectCode                 ;
	private String	submissionCode             ;
	
	/**
	 * @return the totalParams
	 */
	public int getTotalParams() {
		return totalParams;
	}
	/**
	 * @return the totalValues
	 */
	public int getTotalValues() {
		return totalValues;
	}
	/**
	 * @return the fp
	 */
	public FirmProperty getFp() {
		return fp;
	}
	/**
	 * @param fp the fp to set
	 */
	public void setFp(FirmProperty fp) {
		this.fp = fp;
//		see how many parameters are given
		if( null != fp.getDtnLetterIdentificationString() )
			totalParams++ ;
		if( null != fp.getFromFieldAlgoId() )
			totalParams++;
		if( null != fp.getToFieldAlgoId() )
			totalParams++;
		if(null !=fp.getDtnNumberAlgoId() )
			totalParams++;
		if( null != fp.getOwnerDocumentNumberAlgoId() )
			totalParams++;
		if(null != fp.getContractorDocumentNumberAlgoId() )
			totalParams++;
		if(null != fp.getVendorDocumentNumberAlgoId() )
			totalParams++;
		if( null != fp.getRevisionAlgoId() )
			totalParams++;
		if( null != fp.getSubmissionCodeAlgoId() )
			totalParams++;
		if(null != fp.getDtnDateTimeInDTNNoteAlgoId() )
			totalParams++;
		if( null != fp.getProjectCodeAlgoId() )
			totalParams++;
		if(null != fp.getSubmissionCodeAlgoId() )
			totalParams++;
	}
	/**
	 * @return the ai
	 */
	public AttachmentInfo getAi() {
		return ai;
	}
	/**
	 * @param ai the ai to set
	 */
	public void setAi(AttachmentInfo ai) {
		this.ai = ai;
	}
	/**
	 * @return the dtnLetterIdentificationString
	 */
	public String getDtnLetterIdentificationString() {
		return dtnLetterIdentificationString;
	}
	/**
	 * @param dtnLetterIdentificationString the dtnLetterIdentificationString to set
	 */
	public void setDtnLetterIdentificationString(
			String dtnLetterIdentificationString) {
		if( null == dtnLetterIdentificationString || dtnLetterIdentificationString.trim().equals(""))
			return;
		
		this.dtnLetterIdentificationString = dtnLetterIdentificationString;
		totalValues++;
	}
	/**
	 * @return the fromField
	 */
	public String getFromField() {
		return fromField;
	}
	/**
	 * @param fromField the fromField to set
	 */
	public void setFromField(String fromField) {
		if( null == fromField || fromField.trim().equals(""))
			return;

		this.fromField = fromField;
		totalValues++;
	}
	/**
	 * @return the toField
	 */
	public String getToField() {
		return toField;
	}
	/**
	 * @param toField the toField to set
	 */
	public void setToField(String toField) {
		if( null == toField || toField.trim().equals(""))
			return;

		this.toField = toField;
		totalValues++;
	}
	/**
	 * @return the dtnNumber
	 */
	public String getDtnNumber() {
		return dtnNumber;
	}
	/**
	 * @param dtnNumber the dtnNumber to set
	 */
	public void setDtnNumber(String dtnNumber) {
		if( null == dtnNumber || dtnNumber.trim().equals(""))
			return;

		this.dtnNumber = dtnNumber;
		totalValues++;
	}
	/**
	 * @return the ownerDocumentNumber
	 */
	public String getOwnerDocumentNumber() {
		return ownerDocumentNumber;
	}
	/**
	 * @param ownerDocumentNumber the ownerDocumentNumber to set
	 */
	public void setOwnerDocumentNumber(String ownerDocumentNumber) {
		if( null == ownerDocumentNumber || ownerDocumentNumber.trim().equals(""))
			return;

		this.ownerDocumentNumber = ownerDocumentNumber;
		totalValues++;
	}
	/**
	 * @return the contractorDocumentNumber
	 */
	public String getContractorDocumentNumber() {
		return contractorDocumentNumber;
	}
	/**
	 * @param contractorDocumentNumber the contractorDocumentNumber to set
	 */
	public void setContractorDocumentNumber(String contractorDocumentNumber) {
		if( null == contractorDocumentNumber || contractorDocumentNumber.trim().equals(""))
			return;

		this.contractorDocumentNumber = contractorDocumentNumber;
		totalValues++;
	}
	/**
	 * @return the vendorDocumentNumber
	 */
	public String getVendorDocumentNumber() {
		return vendorDocumentNumber;
	}
	/**
	 * @param vendorDocumentNumber the vendorDocumentNumber to set
	 */
	public void setVendorDocumentNumber(String vendorDocumentNumber) {
		if( null == vendorDocumentNumber || vendorDocumentNumber.trim().equals(""))
			return;

		this.vendorDocumentNumber = vendorDocumentNumber;
		totalValues++;
	}
	/**
	 * @return the revision
	 */
	public String getRevision() {
		return revision;
	}
	/**
	 * @param revision the revision to set
	 */
	public void setRevision(String revision) {
		if( null == revision || revision.trim().equals(""))
			return;

		this.revision = revision;
		totalValues++;
	}
	/**
	 * @return the submissionOrDecisionCode
	 */
	public String getSubmissionOrDecisionCode() {
		return submissionOrDecisionCode;
	}
	/**
	 * @param submissionOrDecisionCode the submissionOrDecisionCode to set
	 */
	public void setSubmissionOrDecisionCode(String submissionOrDecisionCode) {
		if( null == submissionOrDecisionCode || submissionOrDecisionCode.trim().equals(""))
			return;

		this.submissionOrDecisionCode = submissionOrDecisionCode;
		totalValues++;
	}
	/**
	 * @return the dtnDateTimeInDTNNote
	 */
	public String getDtnDateTimeInDTNNote() {
		return dtnDateTimeInDTNNote;
	}
	/**
	 * @param dtnDateTimeInDTNNote the dtnDateTimeInDTNNote to set
	 */
	public void setDtnDateTimeInDTNNote(String dtnDateTimeInDTNNote) {
		if( null == dtnDateTimeInDTNNote || dtnDateTimeInDTNNote.trim().equals(""))
			return;
		this.dtnDateTimeInDTNNote = dtnDateTimeInDTNNote;
		totalValues++;
	}
	/**
	 * @return the projectCode
	 */
	public String getProjectCode() {
		return projectCode;
	}
	/**
	 * @param projectCode the projectCode to set
	 */
	public void setProjectCode(String projectCode) {
		if( null == projectCode || projectCode.trim().equals(""))
			return;
		this.projectCode = projectCode;
		totalValues++;
	}
	/**
	 * @return the submissionCode
	 */
	public String getSubmissionCode() {
		return submissionCode;
	}
	/**
	 * @param submissionCode the submissionCode to set
	 */
	public void setSubmissionCode(String submissionCode) {
		if( null == submissionCode || submissionCode.trim().equals(""))
			return;
		this.submissionCode = submissionCode;
		totalValues++;
	}
}
