package com.tbitsglobal.ddc.domain;


public class FirmProperty 
{
	// non null
	private long id;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FirmProperty other = (FirmProperty) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
	/**
	 * @param id
	 * @param emailPattern
	 * @param docControlUserLogin
	 * @param expectedDTNFileName
	 * @param fromFieldAlgoId
	 * @param toFieldAlgoId
	 * @param dtnNumberAlgoId
	 * @param ownerDocumentNumberAlgoId
	 * @param contractorDocumentNumberAlgoId
	 * @param vendorDocumentNumberAlgoId
	 * @param revisionAlgoId
	 * @param submissionOrDecisionCodeAlgoId
	 * @param dtnDateTimeInDTNNoteAlgoId
	 * @param projectCodeAlgoId
	 * @param submissionCodeAlgoId
	 * @param expectedTypeOfTransaction
	 * @param primaryRecordSearchFieldID
	 * @param ownerDocumentNumberFieldID
	 * @param contractorDocumentNumberFieldID
	 * @param vendorDocumentNumberFieldID
	 * @param revisionFieldID
	 * @param receivedFileUpdateFieldID
	 * @param decisionFieldID
	 * @param dtnDateTimeAsPerTBits
	 * @param dtnProcessId
	 * @param dtnKeywordsId
	 */
	public FirmProperty(long id, String emailPattern,
			String docControlUserLogin, String expectedDTNFileName,
			String fromFieldAlgoId, String toFieldAlgoId,
			String dtnNumberAlgoId, String ownerDocumentNumberAlgoId,
			String contractorDocumentNumberAlgoId,
			String vendorDocumentNumberAlgoId, String revisionAlgoId,
			String submissionOrDecisionCodeAlgoId,
			String dtnDateTimeInDTNNoteAlgoId, String projectCodeAlgoId,
			String submissionCodeAlgoId, String expectedTypeOfTransaction,
			String primaryRecordSearchFieldID,
			String ownerDocumentNumberFieldID,
			String contractorDocumentNumberFieldID,
			String vendorDocumentNumberFieldID, String revisionFieldID,
			String receivedFileUpdateFieldID, String decisionFieldID,
			String dtnDateTimeAsPerTBits, Long dtnProcessId,
			Long dtnKeywordsId) {
		super();
		this.id = id;
		this.emailPattern = emailPattern;
		this.docControlUserLogin = docControlUserLogin;
		this.expectedDTNFileName = expectedDTNFileName;
		this.fromFieldAlgoId = fromFieldAlgoId;
		this.toFieldAlgoId = toFieldAlgoId;
		this.dtnNumberAlgoId = dtnNumberAlgoId;
		this.ownerDocumentNumberAlgoId = ownerDocumentNumberAlgoId;
		this.contractorDocumentNumberAlgoId = contractorDocumentNumberAlgoId;
		this.vendorDocumentNumberAlgoId = vendorDocumentNumberAlgoId;
		this.revisionAlgoId = revisionAlgoId;
		this.submissionOrDecisionCodeAlgoId = submissionOrDecisionCodeAlgoId;
		this.dtnDateTimeInDTNNoteAlgoId = dtnDateTimeInDTNNoteAlgoId;
		this.projectCodeAlgoId = projectCodeAlgoId;
		this.submissionCodeAlgoId = submissionCodeAlgoId;
		this.expectedTypeOfTransaction = expectedTypeOfTransaction;
		this.primaryRecordSearchFieldID = primaryRecordSearchFieldID;
		this.ownerDocumentNumberFieldID = ownerDocumentNumberFieldID;
		this.contractorDocumentNumberFieldID = contractorDocumentNumberFieldID;
		this.vendorDocumentNumberFieldID = vendorDocumentNumberFieldID;
		this.revisionFieldID = revisionFieldID;
		this.receivedFileUpdateFieldID = receivedFileUpdateFieldID;
		this.decisionFieldID = decisionFieldID;
		this.dtnDateTimeAsPerTBits = dtnDateTimeAsPerTBits;
		this.dtnProcessId = dtnProcessId;
		this.dtnKeywordsId = dtnKeywordsId;
	}

	// may be null
	private String emailPattern;
	private String docControlUserLogin;
	private String expectedDTNFileName;
	private String fromFieldAlgoId;
	private String toFieldAlgoId;
	private String dtnNumberAlgoId;
	private String ownerDocumentNumberAlgoId;
	private String contractorDocumentNumberAlgoId;
	private String vendorDocumentNumberAlgoId;
	private String revisionAlgoId;
	private String submissionOrDecisionCodeAlgoId;
	private String dtnDateTimeInDTNNoteAlgoId;
	private String projectCodeAlgoId;
	private String submissionCodeAlgoId;
	private String expectedTypeOfTransaction;
	private String primaryRecordSearchFieldID;
	private String ownerDocumentNumberFieldID;
	private String contractorDocumentNumberFieldID;
	private String vendorDocumentNumberFieldID;
	private String revisionFieldID;
	private String receivedFileUpdateFieldID;
	private String decisionFieldID;
	private String dtnDateTimeAsPerTBits;
	private Long dtnProcessId;
	private Long dtnKeywordsId;
	
	public Long getDtnKeywordsId() {
		return dtnKeywordsId;
	}


	public void setDtnKeywordsId(Long dtnKeywordsId) {
		this.dtnKeywordsId = dtnKeywordsId;
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmailPattern() {
		return emailPattern;
	}
	public void setEmailPattern(String emailPattern) {
		this.emailPattern = emailPattern;
	}
	public String getDocControlUserLogin() {
		return docControlUserLogin;
	}
	public void setDocControlUserLogin(String docControlUserLogin) {
		this.docControlUserLogin = docControlUserLogin;
	}

	/**
	 * @return the expectedDTNFileName
	 */
	public String getExpectedDTNFileName() {
		return expectedDTNFileName;
	}


	/**
	 * @param expectedDTNFileName the expectedDTNFileName to set
	 */
	public void setExpectedDTNFileName(String expectedDTNFileName) {
		this.expectedDTNFileName = expectedDTNFileName;
	}


	/**
	 * @return the fromFieldAlgoId
	 */
	public String getFromFieldAlgoId() {
		return fromFieldAlgoId;
	}


	/**
	 * @param fromFieldAlgoId the fromFieldAlgoId to set
	 */
	public void setFromFieldAlgoId(String fromFieldAlgoId) {
		this.fromFieldAlgoId = fromFieldAlgoId;
	}


	/**
	 * @return the toFieldAlgoId
	 */
	public String getToFieldAlgoId() {
		return toFieldAlgoId;
	}


	/**
	 * @param toFieldAlgoId the toFieldAlgoId to set
	 */
	public void setToFieldAlgoId(String toFieldAlgoId) {
		this.toFieldAlgoId = toFieldAlgoId;
	}


	/**
	 * @return the dtnNumberAlgoId
	 */
	public String getDtnNumberAlgoId() {
		return dtnNumberAlgoId;
	}


	/**
	 * @param dtnNumberAlgoId the dtnNumberAlgoId to set
	 */
	public void setDtnNumberAlgoId(String dtnNumberAlgoId) {
		this.dtnNumberAlgoId = dtnNumberAlgoId;
	}


	/**
	 * @return the ownerDocumentNumberAlgoId
	 */
	public String getOwnerDocumentNumberAlgoId() {
		return ownerDocumentNumberAlgoId;
	}


	/**
	 * @param ownerDocumentNumberAlgoId the ownerDocumentNumberAlgoId to set
	 */
	public void setOwnerDocumentNumberAlgoId(String ownerDocumentNumberAlgoId) {
		this.ownerDocumentNumberAlgoId = ownerDocumentNumberAlgoId;
	}


	/**
	 * @return the contractorDocumentNumberAlgoId
	 */
	public String getContractorDocumentNumberAlgoId() {
		return contractorDocumentNumberAlgoId;
	}


	/**
	 * @param contractorDocumentNumberAlgoId the contractorDocumentNumberAlgoId to set
	 */
	public void setContractorDocumentNumberAlgoId(
			String contractorDocumentNumberAlgoId) {
		this.contractorDocumentNumberAlgoId = contractorDocumentNumberAlgoId;
	}


	/**
	 * @return the vendorDocumentNumberAlgoId
	 */
	public String getVendorDocumentNumberAlgoId() {
		return vendorDocumentNumberAlgoId;
	}


	/**
	 * @param vendorDocumentNumberAlgoId the vendorDocumentNumberAlgoId to set
	 */
	public void setVendorDocumentNumberAlgoId(String vendorDocumentNumberAlgoId) {
		this.vendorDocumentNumberAlgoId = vendorDocumentNumberAlgoId;
	}


	/**
	 * @return the revisionAlgoId
	 */
	public String getRevisionAlgoId() {
		return revisionAlgoId;
	}


	/**
	 * @param revisionAlgoId the revisionAlgoId to set
	 */
	public void setRevisionAlgoId(String revisionAlgoId) {
		this.revisionAlgoId = revisionAlgoId;
	}


	/**
	 * @return the submissionOrDecisionCodeAlgoId
	 */
	public String getSubmissionOrDecisionCodeAlgoId() {
		return submissionOrDecisionCodeAlgoId;
	}


	/**
	 * @param submissionOrDecisionCodeAlgoId the submissionOrDecisionCodeAlgoId to set
	 */
	public void setSubmissionOrDecisionCodeAlgoId(
			String submissionOrDecisionCodeAlgoId) {
		this.submissionOrDecisionCodeAlgoId = submissionOrDecisionCodeAlgoId;
	}


	/**
	 * @return the dtnDateTimeInDTNNoteAlgoId
	 */
	public String getDtnDateTimeInDTNNoteAlgoId() {
		return dtnDateTimeInDTNNoteAlgoId;
	}


	/**
	 * @param dtnDateTimeInDTNNoteAlgoId the dtnDateTimeInDTNNoteAlgoId to set
	 */
	public void setDtnDateTimeInDTNNoteAlgoId(String dtnDateTimeInDTNNoteAlgoId) {
		this.dtnDateTimeInDTNNoteAlgoId = dtnDateTimeInDTNNoteAlgoId;
	}


	/**
	 * @return the projectCodeAlgoId
	 */
	public String getProjectCodeAlgoId() {
		return projectCodeAlgoId;
	}


	/**
	 * @param projectCodeAlgoId the projectCodeAlgoId to set
	 */
	public void setProjectCodeAlgoId(String projectCodeAlgoId) {
		this.projectCodeAlgoId = projectCodeAlgoId;
	}


	/**
	 * @return the submissionCodeAlgoId
	 */
	public String getSubmissionCodeAlgoId() {
		return submissionCodeAlgoId;
	}


	/**
	 * @param submissionCodeAlgoId the submissionCodeAlgoId to set
	 */
	public void setSubmissionCodeAlgoId(String submissionCodeAlgoId) {
		this.submissionCodeAlgoId = submissionCodeAlgoId;
	}


	/**
	 * @return the expectedTypeOfTransaction
	 */
	public String getExpectedTypeOfTransaction() {
		return expectedTypeOfTransaction;
	}


	/**
	 * @param expectedTypeOfTransaction the expectedTypeOfTransaction to set
	 */
	public void setExpectedTypeOfTransaction(String expectedTypeOfTransaction) {
		this.expectedTypeOfTransaction = expectedTypeOfTransaction;
	}


	/**
	 * @return the primaryRecordSearchFieldID
	 */
	public String getPrimaryRecordSearchFieldID() {
		return primaryRecordSearchFieldID;
	}


	/**
	 * @param primaryRecordSearchFieldID the primaryRecordSearchFieldID to set
	 */
	public void setPrimaryRecordSearchFieldID(String primaryRecordSearchFieldID) {
		this.primaryRecordSearchFieldID = primaryRecordSearchFieldID;
	}


	/**
	 * @return the ownerDocumentNumberFieldID
	 */
	public String getOwnerDocumentNumberFieldID() {
		return ownerDocumentNumberFieldID;
	}


	/**
	 * @param ownerDocumentNumberFieldID the ownerDocumentNumberFieldID to set
	 */
	public void setOwnerDocumentNumberFieldID(String ownerDocumentNumberFieldID) {
		this.ownerDocumentNumberFieldID = ownerDocumentNumberFieldID;
	}


	/**
	 * @return the contractorDocumentNumberFieldID
	 */
	public String getContractorDocumentNumberFieldID() {
		return contractorDocumentNumberFieldID;
	}


	/**
	 * @param contractorDocumentNumberFieldID the contractorDocumentNumberFieldID to set
	 */
	public void setContractorDocumentNumberFieldID(
			String contractorDocumentNumberFieldID) {
		this.contractorDocumentNumberFieldID = contractorDocumentNumberFieldID;
	}


	/**
	 * @return the vendorDocumentNumberFieldID
	 */
	public String getVendorDocumentNumberFieldID() {
		return vendorDocumentNumberFieldID;
	}


	/**
	 * @param vendorDocumentNumberFieldID the vendorDocumentNumberFieldID to set
	 */
	public void setVendorDocumentNumberFieldID(String vendorDocumentNumberFieldID) {
		this.vendorDocumentNumberFieldID = vendorDocumentNumberFieldID;
	}


	/**
	 * @return the revisionFieldID
	 */
	public String getRevisionFieldID() {
		return revisionFieldID;
	}


	/**
	 * @param revisionFieldID the revisionFieldID to set
	 */
	public void setRevisionFieldID(String revisionFieldID) {
		this.revisionFieldID = revisionFieldID;
	}


	/**
	 * @return the receivedFileUpdateFieldID
	 */
	public String getReceivedFileUpdateFieldID() {
		return receivedFileUpdateFieldID;
	}


	/**
	 * @param receivedFileUpdateFieldID the receivedFileUpdateFieldID to set
	 */
	public void setReceivedFileUpdateFieldID(String receivedFileUpdateFieldID) {
		this.receivedFileUpdateFieldID = receivedFileUpdateFieldID;
	}


	/**
	 * @return the decisionFieldID
	 */
	public String getDecisionFieldID() {
		return decisionFieldID;
	}


	/**
	 * @param decisionFieldID the decisionFieldID to set
	 */
	public void setDecisionFieldID(String decisionFieldID) {
		this.decisionFieldID = decisionFieldID;
	}


	/**
	 * @return the dtnDateTimeAsPerTBits
	 */
	public String getDtnDateTimeAsPerTBits() {
		return dtnDateTimeAsPerTBits;
	}


	/**
	 * @param dtnDateTimeAsPerTBits the dtnDateTimeAsPerTBits to set
	 */
	public void setDtnDateTimeAsPerTBits(String dtnDateTimeAsPerTBits) {
		this.dtnDateTimeAsPerTBits = dtnDateTimeAsPerTBits;
	}


	/**
	 * @return the dtnProcessId
	 */
	public Long getDtnProcessId() {
		return dtnProcessId;
	}


	/**
	 * @param dtnProcessId the dtnProcessId to set
	 */
	public void setDtnProcessId(Long dtnProcessId) {
		this.dtnProcessId = dtnProcessId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FirmProperty [id=" + id
				+ ", emailPattern=" + emailPattern + ", docControlUserLogin="
				+ docControlUserLogin + ", expectedDTNFileName="
				+ expectedDTNFileName + ", fromFieldAlgoId=" + fromFieldAlgoId
				+ ", toFieldAlgoId=" + toFieldAlgoId + ", dtnNumberAlgoId="
				+ dtnNumberAlgoId + ", ownerDocumentNumberAlgoId="
				+ ownerDocumentNumberAlgoId
				+ ", contractorDocumentNumberAlgoId="
				+ contractorDocumentNumberAlgoId
				+ ", vendorDocumentNumberAlgoId=" + vendorDocumentNumberAlgoId
				+ ", revisionAlgoId=" + revisionAlgoId
				+ ", submissionOrDecisionCodeAlgoId="
				+ submissionOrDecisionCodeAlgoId
				+ ", dtnDateTimeInDTNNoteAlgoId=" + dtnDateTimeInDTNNoteAlgoId
				+ ", projectCodeAlgoId=" + projectCodeAlgoId
				+ ", submissionCodeAlgoId=" + submissionCodeAlgoId
				+ ", expectedTypeOfTransaction=" + expectedTypeOfTransaction
				+ ", primaryRecordSearchFieldID=" + primaryRecordSearchFieldID
				+ ", ownerDocumentNumberFieldID=" + ownerDocumentNumberFieldID
				+ ", contractorDocumentNumberFieldID="
				+ contractorDocumentNumberFieldID
				+ ", vendorDocumentNumberFieldID="
				+ vendorDocumentNumberFieldID + ", revisionFieldID="
				+ revisionFieldID + ", receivedFileUpdateFieldID="
				+ receivedFileUpdateFieldID + ", decisionFieldID="
				+ decisionFieldID + ", dtnDateTimeAsPerTBits="
				+ dtnDateTimeAsPerTBits + ", dtnProcessId=" + dtnProcessId
				+ ", dtnKeywordsId=" + dtnKeywordsId + "]";
	}
}
