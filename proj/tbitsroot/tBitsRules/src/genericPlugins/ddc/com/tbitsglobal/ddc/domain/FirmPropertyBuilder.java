package ddc.com.tbitsglobal.ddc.domain;

public class FirmPropertyBuilder {
	private FirmProperty fm = new FirmProperty();

	public FirmProperty build() {
		return fm;
	}

	public FirmPropertyBuilder setId(Long id) {
		fm.setId(id);
		return this;
	}

	public FirmPropertyBuilder setFromAgencyEmailAddress(
			String fromAgencyEmailAddress) {
		fm.setFromAgencyEmailAddress(fromAgencyEmailAddress);
		return this;
	}

	public FirmPropertyBuilder setDtnExpected(
			boolean dtnExpected) {
		fm.setDtnExpected(dtnExpected);
		return this;
	}
	
	public FirmPropertyBuilder setExpectedDTNFileName(String expectedDTNFileName) {
		fm.setExpectedDTNFileName(expectedDTNFileName);
		return this;
	}

	public FirmPropertyBuilder setDtnLetterIdentificationString(
			String dtnLetterIdentificationString) {
		fm.setDtnLetterIdentificationString(dtnLetterIdentificationString);
		return this;
	}

	public FirmPropertyBuilder setFromFieldAlgoId(Long fromFieldAlgoId) {
		fm.setFromFieldAlgoId(fromFieldAlgoId);
		return this;
	}

	public FirmPropertyBuilder setToFieldAlgoId(Long toFieldAlgoId) {
		fm.setToFieldAlgoId(toFieldAlgoId);
		return this;
	}

	public FirmPropertyBuilder setDtnNumberAlgoId(Long dtnNumberAlgoId) {
		fm.setDtnNumberAlgoId(dtnNumberAlgoId);
		return this;
	}

	public FirmPropertyBuilder setOwnerDocumentNumberAlgoId(
			Long ownerDocumentNumberAlgoId) {
		fm.setOwnerDocumentNumberAlgoId(ownerDocumentNumberAlgoId);
		return this;
	}

	public FirmPropertyBuilder setContractorDocumentNumberAlgoId(
			Long contractorDocumentNumberAlgoId) {
		fm.setContractorDocumentNumberAlgoId(contractorDocumentNumberAlgoId);
		return this;
	}

	public FirmPropertyBuilder setVendorDocumentNumberAlgoId(
			Long vendorDocumentNumberAlgoId) {
		fm.setVendorDocumentNumberAlgoId(vendorDocumentNumberAlgoId);
		return this;
	}

	public FirmPropertyBuilder setRevisionAlgoId(Long revisionAlgoId) {
		fm.setRevisionAlgoId(revisionAlgoId);
		return this;
	}

	public FirmPropertyBuilder setSubmissionOrDecisionCodeAlgoId(
			Long submissionOrDecisionCodeAlgoId) {
		fm.setSubmissionOrDecisionCodeAlgoId(submissionOrDecisionCodeAlgoId);
		return this;
	}

	public FirmPropertyBuilder setDtnDateTimeInDTNNoteAlgoId(
			Long dtnDateTimeInDTNNoteAlgoId) {
		fm.setDtnDateTimeInDTNNoteAlgoId(dtnDateTimeInDTNNoteAlgoId);
		return this;
	}

	public FirmPropertyBuilder setProjectCodeAlgoId(Long projectCodeAlgoId) {
		fm.setProjectCodeAlgoId(projectCodeAlgoId);
		return this;
	}

	public FirmPropertyBuilder setSubmissionCodeAlgoId(Long submissionCodeAlgoId) {
		fm.setSubmissionCodeAlgoId(submissionCodeAlgoId);
		return this;
	}

	public FirmPropertyBuilder setExpectedTypeOfTransaction(
			String expectedTypeOfTransaction) {
		fm.setExpectedTypeOfTransaction(expectedTypeOfTransaction);
		return this;
	}

	public FirmPropertyBuilder setBaPrefix(String baPrefix) {
		fm.setBaPrefix(baPrefix);
		return this;
	}

	public FirmPropertyBuilder setPrimaryKeyForSearch(
			String primaryKeyForSearch) {
		fm.setPrimaryKeyForSearch(primaryKeyForSearch);
		return this;
	}
	
	public FirmPropertyBuilder setPrimaryRecordSearchFieldId(
			Integer primaryRecordSearchFieldId) {
		fm.setPrimaryRecordSearchFieldId(primaryRecordSearchFieldId);
		return this;
	}

	public FirmPropertyBuilder setOwnerDocumentNumberFieldID(
			Integer ownerDocumentNumberFieldID) {
		fm.setOwnerDocumentNumberFieldID(ownerDocumentNumberFieldID);
		return this;
	}

	public FirmPropertyBuilder setContractorDocumentNumberFieldID(
			Integer contractorDocumentNumberFieldID) {
		fm.setContractorDocumentNumberFieldID(contractorDocumentNumberFieldID);
		return this;
	}

	public FirmPropertyBuilder setVendorDocumentNumberFieldID(
			Integer vendorDocumentNumberFieldID) {
		fm.setVendorDocumentNumberFieldID(vendorDocumentNumberFieldID);
		return this;
	}

	public FirmPropertyBuilder setRevisionFieldID(Integer revisionFieldID) {
		fm.setRevisionFieldID(revisionFieldID);
		return this;
	}

	public FirmPropertyBuilder setReceivedFileUpdateFieldID(
			Integer receivedFileUpdateFieldID) {
		fm.setReceivedFileUpdateFieldID(receivedFileUpdateFieldID);
		return this;
	}

	public FirmPropertyBuilder setDecisionFieldID(Integer decisionFieldID) {
		fm.setDecisionFieldID(decisionFieldID);
		return this;
	}

	public FirmPropertyBuilder setDtnDateTimeAsPerTBits(
			String dtnDateTimeAsPerTBits) {
		fm.setDtnDateTimeAsPerTBits(dtnDateTimeAsPerTBits);
		return this;
	}

	public FirmPropertyBuilder setDtnDateTimeToBeUsedInTransaction(
			String dtnDateTimeToBeUsedInTransaction) {
		fm.setDtnDateTimeToBeUsedInTransaction(dtnDateTimeToBeUsedInTransaction);
		return this;
	}

	public FirmPropertyBuilder setDtnProcessId(Long dtnProcessId) {
		fm.setDtnProcessId(dtnProcessId);
		return this;
	}
	
	public FirmPropertyBuilder setDtnKeywordSetId(Long dtnKeywordSetId) {
		fm.setDtnKeywordSetId(dtnKeywordSetId);
		return this;
	}
	
}
