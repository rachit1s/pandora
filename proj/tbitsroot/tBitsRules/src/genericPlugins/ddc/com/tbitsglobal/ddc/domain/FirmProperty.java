package ddc.com.tbitsglobal.ddc.domain;


public class FirmProperty 
{
	public static final String SearchKeyOwnerDocumentNumber = "OwnerDocumentNumber";
	public static final String SearchKeyContractorDocumentNumber = "ContractorDocumentNumber";
	public static final String SearchKeyVendorDocumentNumber = "VendorDocumentNumber";
	public static final String SearchKeyDTNNumber = "DTNNumber";
	
	private Long id;
	private String  fromAgencyEmailAddress               ;
	private boolean dtnExpected;
	private String  expectedDTNFileName                  ;
	private String  dtnLetterIdentificationString        ;
	private Long  fromFieldAlgoId                      ;
	private Long  toFieldAlgoId                        ;
	private Long  dtnNumberAlgoId                      ;
	private Long  ownerDocumentNumberAlgoId            ;
	private Long  contractorDocumentNumberAlgoId       ;
	private Long  vendorDocumentNumberAlgoId           ;
	private Long  revisionAlgoId                       ;
	private Long  submissionOrDecisionCodeAlgoId       ;
	private Long  dtnDateTimeInDTNNoteAlgoId           ;
	private Long  projectCodeAlgoId                    ;
	private Long  submissionCodeAlgoId                 ;
	private String  expectedTypeOfTransaction            ;
	private String  baPrefix                             ;
	private String primaryKeyForSearch;
	private Integer  primaryRecordSearchFieldId           ;
	private Integer  ownerDocumentNumberFieldID           ;
	private Integer  contractorDocumentNumberFieldID      ;
	private Integer  vendorDocumentNumberFieldID          ;
	private Integer  revisionFieldID                      ;
	private Integer  receivedFileUpdateFieldID            ;
	private Integer  decisionFieldID                      ;
	private String  dtnDateTimeAsPerTBits                ;
	private String  dtnDateTimeToBeUsedInTransaction     ;
	private Long  dtnProcessId                         ;
	private Long dtnKeywordSetId;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * @return the primaryKeyForSearch
	 */
	public String getPrimaryKeyForSearch() {
		return primaryKeyForSearch;
	}
	/**
	 * @param primaryKeyForSearch the primaryKeyForSearch to set
	 */
	public void setPrimaryKeyForSearch(String primaryKeyForSearch) {
		this.primaryKeyForSearch = primaryKeyForSearch;
	}
	/**
	 * @return the dtnExpected
	 */
	public boolean isDtnExpected() {
		return dtnExpected;
	}
	/**
	 * @param dtnExpected the dtnExpected to set
	 */
	public void setDtnExpected(boolean dtnExpected) {
		this.dtnExpected = dtnExpected;
	}
	/**
	 * @return the fromAgencyEmailAddress
	 */
	public String getFromAgencyEmailAddress() {
		return fromAgencyEmailAddress;
	}
	/**
	 * @param fromAgencyEmailAddress the fromAgencyEmailAddress to set
	 */
	public void setFromAgencyEmailAddress(String fromAgencyEmailAddress) {
		this.fromAgencyEmailAddress = fromAgencyEmailAddress;
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
		this.dtnLetterIdentificationString = dtnLetterIdentificationString;
	}
	/**
	 * @return the fromFieldAlgoId
	 */
	public Long getFromFieldAlgoId() {
		return fromFieldAlgoId;
	}
	/**
	 * @param fromFieldAlgoId the fromFieldAlgoId to set
	 */
	public void setFromFieldAlgoId(Long fromFieldAlgoId) {
		this.fromFieldAlgoId = fromFieldAlgoId;
	}
	/**
	 * @return the toFieldAlgoId
	 */
	public Long getToFieldAlgoId() {
		return toFieldAlgoId;
	}
	/**
	 * @param toFieldAlgoId the toFieldAlgoId to set
	 */
	public void setToFieldAlgoId(Long toFieldAlgoId) {
		this.toFieldAlgoId = toFieldAlgoId;
	}
	/**
	 * @return the dtnNumberAlgoId
	 */
	public Long getDtnNumberAlgoId() {
		return dtnNumberAlgoId;
	}
	/**
	 * @param dtnNumberAlgoId the dtnNumberAlgoId to set
	 */
	public void setDtnNumberAlgoId(Long dtnNumberAlgoId) {
		this.dtnNumberAlgoId = dtnNumberAlgoId;
	}
	/**
	 * @return the ownerDocumentNumberAlgoId
	 */
	public Long getOwnerDocumentNumberAlgoId() {
		return ownerDocumentNumberAlgoId;
	}
	/**
	 * @param ownerDocumentNumberAlgoId the ownerDocumentNumberAlgoId to set
	 */
	public void setOwnerDocumentNumberAlgoId(Long ownerDocumentNumberAlgoId) {
		this.ownerDocumentNumberAlgoId = ownerDocumentNumberAlgoId;
	}
	/**
	 * @return the contractorDocumentNumberAlgoId
	 */
	public Long getContractorDocumentNumberAlgoId() {
		return contractorDocumentNumberAlgoId;
	}
	/**
	 * @param contractorDocumentNumberAlgoId the contractorDocumentNumberAlgoId to set
	 */
	public void setContractorDocumentNumberAlgoId(
			Long contractorDocumentNumberAlgoId) {
		this.contractorDocumentNumberAlgoId = contractorDocumentNumberAlgoId;
	}
	/**
	 * @return the vendorDocumentNumberAlgoId
	 */
	public Long getVendorDocumentNumberAlgoId() {
		return vendorDocumentNumberAlgoId;
	}
	/**
	 * @param vendorDocumentNumberAlgoId the vendorDocumentNumberAlgoId to set
	 */
	public void setVendorDocumentNumberAlgoId(Long vendorDocumentNumberAlgoId) {
		this.vendorDocumentNumberAlgoId = vendorDocumentNumberAlgoId;
	}
	/**
	 * @return the revisionAlgoId
	 */
	public Long getRevisionAlgoId() {
		return revisionAlgoId;
	}
	/**
	 * @param revisionAlgoId the revisionAlgoId to set
	 */
	public void setRevisionAlgoId(Long revisionAlgoId) {
		this.revisionAlgoId = revisionAlgoId;
	}
	/**
	 * @return the submissionOrDecisionCodeAlgoId
	 */
	public Long getSubmissionOrDecisionCodeAlgoId() {
		return submissionOrDecisionCodeAlgoId;
	}
	/**
	 * @param submissionOrDecisionCodeAlgoId the submissionOrDecisionCodeAlgoId to set
	 */
	public void setSubmissionOrDecisionCodeAlgoId(
			Long submissionOrDecisionCodeAlgoId) {
		this.submissionOrDecisionCodeAlgoId = submissionOrDecisionCodeAlgoId;
	}
	/**
	 * @return the dtnDateTimeInDTNNoteAlgoId
	 */
	public Long getDtnDateTimeInDTNNoteAlgoId() {
		return dtnDateTimeInDTNNoteAlgoId;
	}
	/**
	 * @param dtnDateTimeInDTNNoteAlgoId the dtnDateTimeInDTNNoteAlgoId to set
	 */
	public void setDtnDateTimeInDTNNoteAlgoId(Long dtnDateTimeInDTNNoteAlgoId) {
		this.dtnDateTimeInDTNNoteAlgoId = dtnDateTimeInDTNNoteAlgoId;
	}
	/**
	 * @return the projectCodeAlgoId
	 */
	public Long getProjectCodeAlgoId() {
		return projectCodeAlgoId;
	}
	/**
	 * @param projectCodeAlgoId the projectCodeAlgoId to set
	 */
	public void setProjectCodeAlgoId(Long projectCodeAlgoId) {
		this.projectCodeAlgoId = projectCodeAlgoId;
	}
	/**
	 * @return the submissionCodeAlgoId
	 */
	public Long getSubmissionCodeAlgoId() {
		return submissionCodeAlgoId;
	}
	/**
	 * @param submissionCodeAlgoId the submissionCodeAlgoId to set
	 */
	public void setSubmissionCodeAlgoId(Long submissionCodeAlgoId) {
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
	 * @return the baPrefix
	 */
	public String getBaPrefix() {
		return baPrefix;
	}
	/**
	 * @param baPrefix the baPrefix to set
	 */
	public void setBaPrefix(String baPrefix) {
		this.baPrefix = baPrefix;
	}
	/**
	 * @return the primaryRecordSearchFieldId
	 */
	public Integer getPrimaryRecordSearchFieldId() {
		return primaryRecordSearchFieldId;
	}
	/**
	 * @param primaryRecordSearchFieldId the primaryRecordSearchFieldId to set
	 */
	public void setPrimaryRecordSearchFieldId(Integer primaryRecordSearchFieldId) {
		this.primaryRecordSearchFieldId = primaryRecordSearchFieldId;
	}
	/**
	 * @return the ownerDocumentNumberFieldID
	 */
	public Integer getOwnerDocumentNumberFieldID() {
		return ownerDocumentNumberFieldID;
	}
	/**
	 * @param ownerDocumentNumberFieldID the ownerDocumentNumberFieldID to set
	 */
	public void setOwnerDocumentNumberFieldID(Integer ownerDocumentNumberFieldID) {
		this.ownerDocumentNumberFieldID = ownerDocumentNumberFieldID;
	}
	/**
	 * @return the contractorDocumentNumberFieldID
	 */
	public Integer getContractorDocumentNumberFieldID() {
		return contractorDocumentNumberFieldID;
	}
	/**
	 * @param contractorDocumentNumberFieldID the contractorDocumentNumberFieldID to set
	 */
	public void setContractorDocumentNumberFieldID(
			Integer contractorDocumentNumberFieldID) {
		this.contractorDocumentNumberFieldID = contractorDocumentNumberFieldID;
	}
	/**
	 * @return the vendorDocumentNumberFieldID
	 */
	public Integer getVendorDocumentNumberFieldID() {
		return vendorDocumentNumberFieldID;
	}
	/**
	 * @param vendorDocumentNumberFieldID the vendorDocumentNumberFieldID to set
	 */
	public void setVendorDocumentNumberFieldID(Integer vendorDocumentNumberFieldID) {
		this.vendorDocumentNumberFieldID = vendorDocumentNumberFieldID;
	}
	/**
	 * @return the revisionFieldID
	 */
	public Integer getRevisionFieldID() {
		return revisionFieldID;
	}
	/**
	 * @param revisionFieldID the revisionFieldID to set
	 */
	public void setRevisionFieldID(Integer revisionFieldID) {
		this.revisionFieldID = revisionFieldID;
	}
	/**
	 * @return the receivedFileUpdateFieldID
	 */
	public Integer getReceivedFileUpdateFieldID() {
		return receivedFileUpdateFieldID;
	}
	/**
	 * @param receivedFileUpdateFieldID the receivedFileUpdateFieldID to set
	 */
	public void setReceivedFileUpdateFieldID(Integer receivedFileUpdateFieldID) {
		this.receivedFileUpdateFieldID = receivedFileUpdateFieldID;
	}
	/**
	 * @return the decisionFieldID
	 */
	public Integer getDecisionFieldID() {
		return decisionFieldID;
	}
	/**
	 * @param decisionFieldID the decisionFieldID to set
	 */
	public void setDecisionFieldID(Integer decisionFieldID) {
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
	 * @return the dtnDateTimeToBeUsedInTransaction
	 */
	public String getDtnDateTimeToBeUsedInTransaction() {
		return dtnDateTimeToBeUsedInTransaction;
	}
	/**
	 * @param dtnDateTimeToBeUsedInTransaction the dtnDateTimeToBeUsedInTransaction to set
	 */
	public void setDtnDateTimeToBeUsedInTransaction(
			String dtnDateTimeToBeUsedInTransaction) {
		this.dtnDateTimeToBeUsedInTransaction = dtnDateTimeToBeUsedInTransaction;
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
	/**
	 * @return the dtnKeywordSetId
	 */
	public Long getDtnKeywordSetId() {
		return dtnKeywordSetId;
	}
	/**
	 * @param dtnKeywordSetId the dtnKeywordSetId to set
	 */
	public void setDtnKeywordSetId(Long dtnKeywordSetId) {
		this.dtnKeywordSetId = dtnKeywordSetId;
	}
	
	/**
	 * @param id
	 * @param fromAgencyEmailAddress
	 * @param dtnExpected
	 * @param expectedDTNFileName
	 * @param dtnLetterIdentificationString
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
	 * @param baPrefix
	 * @param primaryKeyForSearch
	 * @param primaryRecordSearchFieldId
	 * @param ownerDocumentNumberFieldID
	 * @param contractorDocumentNumberFieldID
	 * @param vendorDocumentNumberFieldID
	 * @param revisionFieldID
	 * @param receivedFileUpdateFieldID
	 * @param decisionFieldID
	 * @param dtnDateTimeAsPerTBits
	 * @param dtnDateTimeToBeUsedInTransaction
	 * @param dtnProcessId
	 * @param dtnKeywordSetId
	 */
	public FirmProperty(Long id, String fromAgencyEmailAddress,
			boolean dtnExpected, String expectedDTNFileName,
			String dtnLetterIdentificationString, Long fromFieldAlgoId,
			Long toFieldAlgoId, Long dtnNumberAlgoId,
			Long ownerDocumentNumberAlgoId,
			Long contractorDocumentNumberAlgoId,
			Long vendorDocumentNumberAlgoId, Long revisionAlgoId,
			Long submissionOrDecisionCodeAlgoId,
			Long dtnDateTimeInDTNNoteAlgoId, Long projectCodeAlgoId,
			Long submissionCodeAlgoId, String expectedTypeOfTransaction,
			String baPrefix, String primaryKeyForSearch,
			Integer primaryRecordSearchFieldId,
			Integer ownerDocumentNumberFieldID,
			Integer contractorDocumentNumberFieldID,
			Integer vendorDocumentNumberFieldID, Integer revisionFieldID,
			Integer receivedFileUpdateFieldID, Integer decisionFieldID,
			String dtnDateTimeAsPerTBits,
			String dtnDateTimeToBeUsedInTransaction, Long dtnProcessId,
			Long dtnKeywordSetId) {
		super();
		this.id = id;
		this.fromAgencyEmailAddress = fromAgencyEmailAddress;
		this.dtnExpected = dtnExpected;
		this.expectedDTNFileName = expectedDTNFileName;
		this.dtnLetterIdentificationString = dtnLetterIdentificationString;
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
		this.baPrefix = baPrefix;
		this.primaryKeyForSearch = primaryKeyForSearch;
		this.primaryRecordSearchFieldId = primaryRecordSearchFieldId;
		this.ownerDocumentNumberFieldID = ownerDocumentNumberFieldID;
		this.contractorDocumentNumberFieldID = contractorDocumentNumberFieldID;
		this.vendorDocumentNumberFieldID = vendorDocumentNumberFieldID;
		this.revisionFieldID = revisionFieldID;
		this.receivedFileUpdateFieldID = receivedFileUpdateFieldID;
		this.decisionFieldID = decisionFieldID;
		this.dtnDateTimeAsPerTBits = dtnDateTimeAsPerTBits;
		this.dtnDateTimeToBeUsedInTransaction = dtnDateTimeToBeUsedInTransaction;
		this.dtnProcessId = dtnProcessId;
		this.dtnKeywordSetId = dtnKeywordSetId;
	}
	
	/**
	 * 
	 */
	public FirmProperty() {
		super();
		// TODO Auto-generated constructor stub
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FirmProperty other = (FirmProperty) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FirmProperty [id=" + id + ", fromAgencyEmailAddress="
				+ fromAgencyEmailAddress + ", expectedDTNFileName="
				+ expectedDTNFileName + ", dtnLetterIdentificationString="
				+ dtnLetterIdentificationString + ", fromFieldAlgoId="
				+ fromFieldAlgoId + ", toFieldAlgoId=" + toFieldAlgoId
				+ ", dtnNumberAlgoId=" + dtnNumberAlgoId
				+ ", ownerDocumentNumberAlgoId=" + ownerDocumentNumberAlgoId
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
				+ ", baPrefix=" + baPrefix + ", primaryRecordSearchFieldId="
				+ primaryRecordSearchFieldId + ", ownerDocumentNumberFieldID="
				+ ownerDocumentNumberFieldID
				+ ", contractorDocumentNumberFieldID="
				+ contractorDocumentNumberFieldID
				+ ", vendorDocumentNumberFieldID="
				+ vendorDocumentNumberFieldID + ", revisionFieldID="
				+ revisionFieldID + ", receivedFileUpdateFieldID="
				+ receivedFileUpdateFieldID + ", decisionFieldID="
				+ decisionFieldID + ", dtnDateTimeAsPerTBits="
				+ dtnDateTimeAsPerTBits + ", dtnDateTimeToBeUsedInTransaction="
				+ dtnDateTimeToBeUsedInTransaction + ", dtnProcessId="
				+ dtnProcessId + ", dtnKeywordSetId=" + dtnKeywordSetId + "]";
	}
}
