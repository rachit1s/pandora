package ddc.com.tbitsglobal.ddc.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import static ddc.com.tbitsglobal.ddc.dao.FirmPropertyDao.*;

import org.apache.log4j.Logger;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.addons.TbitsContext;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.TextDataType;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;


import ddc.com.tbitsglobal.ddc.dao.FirmPropertyDao;
import ddc.com.tbitsglobal.ddc.domain.FirmProperty;
import ddc.com.tbitsglobal.ddc.exception.FailedToFindObject;


/*
 * Notes
 Detect PageFormat : PageFormat PDDocument.getPageFormat(int pageIndex);
 */
public class DDCObject {
	
	private static final Logger logger = Logger.getLogger(DDCObject.class);

	//derived from ddcRequestId : initialize ddcRequest
	private Request ddcRequest;
	//input : sender email address
	private String senderEmailAddress;
	
	private User sender;
	// derived from senderEmailAddress
	private List<FirmProperty> firmList;
	
	// all files
	private HashMap<AttachmentInfo,File> allFiles = new HashMap<AttachmentInfo, File>();
	
	// selected by firm-property
	private HashMap<FirmProperty,List<AttachmentInfo>> filePatternMatchedWithFirmProperty = new HashMap<FirmProperty, List<AttachmentInfo>>();
	
	// maps the attachment to the found text through pdf extration method.
	private HashMap<AttachmentInfo,String> pdfTexts = new HashMap<AttachmentInfo, String>();

	// maps the attachment to the found text through pdf extration method.
	private HashMap<AttachmentInfo,File> textFiles = new HashMap<AttachmentInfo, File>();

	// the files which have A4 size first page
	private HashMap<AttachmentInfo,File> a4Files = new HashMap<AttachmentInfo, File>();
	
	// the searched DTN request.
	private Request dtnRequest;
	// the dtn parameters found out after text extraction etc..
//	private HashMap<AttachmentInfo,DTNParameter> dtnParams = new HashMap<AttachmentInfo, DTNParameter>();
	
	private DTNParameter dtnParameter;
	// the final firmProperty to use 
	private FirmProperty firmProperty;
	
	// the final dtn File
	private File dtnFile;
	
	private BusinessArea ddcBA;
	// input : 
	private String ddcBAPrefix;
	// input : 
	private Integer ddcRequestId;
	
	private static final String DDC_ATTACHMENT_FIELD_NAME = "attachments";
	
	public DDCObject(String ddcBAPrefix, Integer ddcRequestId) throws DatabaseException, FailedToFindObject
	{
		this.ddcBAPrefix = ddcBAPrefix;
		this.ddcRequestId = ddcRequestId;
		
		this.ddcBA = BusinessArea.lookupBySystemPrefix(this.ddcBAPrefix);
		this.ddcRequest = Request.lookupBySystemIdAndRequestId(ddcBA.getSystemId(), this.ddcRequestId);
		this.sender = User.lookupByUserId(ddcRequest.getUserId());
		this.senderEmailAddress = sender.getEmail();
		
		//initialize all the firms
		firmList = FirmPropertyDao.getInstance().getAllByEmailId(this.senderEmailAddress);
	}
	/**
	 * 1. Routine – 1: Identification of Agency and Expected Value Identification
	 * 2. Routine for Identification of the Document Transmittal Note
	 * 3. Routine – 2: Identification of all attributes of the documents received using the searching of information from tBits
	 * 4. Routine-3: Extraction of information-OCR
	 * 5. Routine-4: Reconciliation between the information received from OCR & Information received from DTN.
	 * 6. Routine – 5: Updating the Records – upon successful reconciliation of the information received from DTN and OCR of files.
	 * 7. Routine – 6: Proceed with the inward of the information
	 * @throws DDCException 
	 * @throws DatabaseException 
	 */
	public   void doDDC() throws DatabaseException, DDCException
	{
		logger.info("Processing DDC for " + ddcBAPrefix + "#" + ddcRequestId);
		if( 0 == firmList.size() )
		{
			logger.info("No Firm found with this id. So aborting the DDC");
			return;
		}
		/*
		New Email received in Inboxes related to the
		digital document controller assistant BA.
		There can be several email ID’s from which
		the emails will be collected into the system
		*/
		
		/*
		 * Fetch Emails from the inbox and create the
record in the inward. Send
Acknowledgement.
		 */
		/*
		Determine the sender of the email, compare
the same with the allowance processes for
From_Agency_Email_Address:
*/
		boolean hasAttachments = doesEmailHasAttachments();
		if( hasAttachments )
		{
			boolean isDDCExpected = isDDCExpectedFromSender();
			if(isDDCExpected)
			{
				logger.info("DTN is expected from the sender.");
				boolean foundPatternMatchingFile = findPatternMatchingFile();
				if(foundPatternMatchingFile)
				{
					logger.info("Processing the files which match the given naming pattern.");
					// filePatternMatchedWithFirmProperty.values()
					List<AttachmentInfo> completeList = new ArrayList<AttachmentInfo>();
					for( List<AttachmentInfo> list : filePatternMatchedWithFirmProperty.values())
						completeList.addAll(list);
					
					boolean textExtracted = canTextBeExtracted(completeList);
					if( textExtracted )
					{
						logger.info("Extracted text from some of the files : " + pdfTexts);
						boolean foundExpectedDTNParams = getDTNParams();
						if( foundExpectedDTNParams )
						{
							processDTNValues();
						}
						else
						{
							processWithDTNNumber();
						}
					}
					else
					{
						// for each file
						{
							boolean foundText = processFirstPageWithOCR();
							if( foundText )
							{
								// 
								searchAgencyBAForSearch();
							}
							else
							{
								// do nothing .. continue;
							}
						}
							
					}
				}
				else
				{
					boolean foundA4PageSizeFile = findA4PageSizeFile();
					if( foundA4PageSizeFile )
					{
						processEachA4PageSizeFile();
					}
					else
					{
						dtnNotFound();
					}
				}
			}
			else
			{
				selectAllProcessAndProceed();
			}
		}
		else
		{
			emailDDC();
		}
	}
	/**
	 * 
	 */
	private void processWithDTNNumber() {
		getDTNNumber();
		boolean foundValues = findValues();
		if( foundValues )
		{
			boolean foundAnnotations = findAnnotations();
			if( foundAnnotations )
			{
				boolean dntAlreadyAcc = isDTNAlreadyAccknowledged();
				if( dntAlreadyAcc )
				{
					acknowledgeReceipt();
					//Ends. Successfully.
				}
				else
				{
					updateInDCBA();
					// ENDS. Unsuccessfully.
				}
			}
			else
			{
				updateRecordWithAck();
				// Process ENDS
			}
		}
		else
		{
			updateWithValuesNotFound();
		}
	}
	/**
	 * 
	 */
	private void dtnNotFound() {
		/*
DTN Could not be found by 
either OCR or Text 
Extraction. End 
Unsuccessfully
		 */
	}
	/**
	 * 
	 */
	private void updateWithValuesNotFound() {
/*
The File Could NOT 
be processed. 
Human Intervention 
requested. 
 */
	}
	/**
	 * 
	 */
	private void updateRecordWithAck() {
		/*
		Update the record with information that 
		DTN letter generated by tBits received – 
		but no acknowledgement stamp / 
		annotation matching word 
		acknowledged was found. Process ENDS
		*/
	}
	/**
	 * 
	 */
	private void updateInDCBA() {
		/*
		 Update the record in 
DC BA – stating that 
we have received a 
DTN – that has been 
created using tBits 
and it has already 
been acknowledged 
and hence human 
intervention 
required.
		 */
	}
	/**
	 * 
	 */
	private void acknowledgeReceipt() {
		/*
Acknowedge the 
receipt of the DTN 
Letter by updating 
the BA and 
associated steps.
		 */
		
	}
	/**
	 * @return
	 */
	private boolean isDTNAlreadyAccknowledged() {
		/*
Is this DTN letter 
already 
acknowledged?
		 */
		return false;
	}
	/**
	 * @return
	 */
	private boolean findAnnotations() {
/*
Is there an annotation with 
the word – Received, 
Acknowledged mentioned 
in the same.
 */
		return false;
	}
	/**
	 * @return
	 */
	private boolean findValues() {
/*
 Are you able to 
fetch the values?
 */
		return false;
	}
	/**
	 * 
	 */
	private void getDTNNumber() {
		/*
Get the DTN 
Number – from text 
extraction for the 
field with KEY DTN 
No and the value in 
the next rectange.
		 */
	}
	
//	/**
//	 * @return
//	 */
//	private boolean findExpectedDTNParams() {
//
//		// if more than one dtn params are satisfied then this will return false
//		// as we cannot decide what to do in that case.
//		if(dtnParams.size() != 1 )
//			return false;
//		
//		AttachmentInfo ai = dtnParams.keySet().iterator().next();
//		dtnParameter = dtnParams.get(ai);
//		boolean foundFP = false;
//		for( FirmProperty fp : filePatternMatchedWithFirmProperty.keySet() )
//		{
//			for( AttachmentInfo atInfo : filePatternMatchedWithFirmProperty.get(fp) )
//			{
//				if( ai.equals(atInfo))
//				{
//					firmProperty = fp;
//					foundFP = true;
//					break;
//				}
//			}
//			
//			if( foundFP )
//				break;
//		}
//		
//		dtnFile = allFiles.get(ai);
//		
//		return true;
//	}
	/**
	 * @return 
	 * 
	 */
	private boolean getDTNParams() {
		/*
		 Determine the DTN 
Parameter values 
for the specified 
process from the 
file.
		 */
		/*
Are you able to get 
expected values from 
text extraction for the 
DTN?
		 */
		// parameters are as follows
//		dtnKeywordSetId -- TODO :ignoring this for now. As if we have found others then this does not matter
//		dtnLetterIdentificationString
//		fromFieldAlgoId
//		toFieldAlgoId
//		dtnNumberAlgoId
//		ownerDocumentNumberAlgoId
//		contractorDocumentNumberAlgoId
//		vendorDocumentNumberAlgoId
//		revisionAlgoId
//		submissionOrDecisionCodeAlgoId
//		dtnDateTimeInDTNNoteAlgoId
//		projectCodeAlgoId
//		submissionCodeAlgoId
		
		for( AttachmentInfo ai : pdfTexts.keySet() )
		{
			// get the FP
			for(FirmProperty fp : filePatternMatchedWithFirmProperty.keySet() )
			{
				
				List<AttachmentInfo> aiList = filePatternMatchedWithFirmProperty.get(fp);
				if( aiList.contains(ai))
				{
					// process dtn parameters according to this fp
					dtnParameter =  extractDTNParams(fp,ai);
					if( null != dtnParameter )
					{
						// find only the first dtn that matches fp's parameters
						// don't bother about each file which matched the pattern
						firmProperty = fp;
						dtnFile = allFiles.get(ai);
						return true;
					}
				}
			}
		}
		
		return false;
		
	}
	/**
	 * @param fp
	 * @param ai
	 */
	private DTNParameter extractDTNParams(FirmProperty fp, AttachmentInfo ai) 
	{
		logger.info("Extracting DTN Params from " + ai + " for firm : " + fp);
		try
		{
			DTNParameter dtnParam = new DTNParameter();
			dtnParam.setFp(fp);
			
			if( dtnParam.getTotalParams() == 0 )
				return null ;
			
			dtnParam.setAi(ai);
			File file = allFiles.get(ai);
			String text = pdfTexts.get(ai);
			
			// parameters are as follows
	//		dtnKeywordSetId -- TODO :ignoring this for now. As if we have found others then this does not matter
	//		dtnLetterIdentificationString
	//		fromFieldAlgoId
			String fromField = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getFromFieldAlgoId()));
			dtnParam.setFromField(fromField);
	//		toFieldAlgoId
			String toField = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getToFieldAlgoId()));
			dtnParam.setToField(toField);
	//		dtnNumberAlgoId
			String dtnNumber = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getDtnNumberAlgoId()));
			dtnParam.setDtnNumber(dtnNumber);
	//		ownerDocumentNumberAlgoId
			String ownerDocumentNumber = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getOwnerDocumentNumberAlgoId()));
			dtnParam.setOwnerDocumentNumber(ownerDocumentNumber);
	//		contractorDocumentNumberAlgoId
			String contractorDocumentNumber = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getContractorDocumentNumberAlgoId()));
			dtnParam.setContractorDocumentNumber(contractorDocumentNumber);
	//		vendorDocumentNumberAlgoId
			String vendorDocumentNumber = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getVendorDocumentNumberAlgoId()));
			dtnParam.setVendorDocumentNumber(vendorDocumentNumber);
	//		revisionAlgoId
			String revision = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getRevisionAlgoId()));
			dtnParam.setRevision(revision);
	//		submissionOrDecisionCodeAlgoId
			String submissionOrDecisionCode = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getSubmissionOrDecisionCodeAlgoId()));
			dtnParam.setSubmissionOrDecisionCode(submissionOrDecisionCode);
	//		dtnDateTimeInDTNNoteAlgoId
			String dtnDateTimeInDTNNote = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getDtnDateTimeInDTNNoteAlgoId()));
			dtnParam.setDtnDateTimeInDTNNote(dtnDateTimeInDTNNote);
	//		projectCodeAlgoId
			String projectCode = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getProjectCodeAlgoId()));
			dtnParam.setProjectCode(projectCode);
	//		submissionCodeAlgoId
			String submissionCode = getFirstOrNull(DDCHelper.getNumber(file, text, fp.getSubmissionCodeAlgoId()));
			dtnParam.setSubmissionCode(submissionCode);
			
			if( dtnParam.getTotalParams() == dtnParam.getTotalValues() )
			{
				logger.info("Found all params.");
				return dtnParam;
			}
			else
			{
				logger.info("Not all params were found. Params : " + dtnParam);
				return null;
			}
		}
		catch(Exception e)
		{
			logger.error("",e);
		}
		
		return null;
	}
	/**
	 * @param number
	 * @return
	 */
	private String getFirstOrNull(List<String> number) {
		if( null == number )
			return null;
		if( number.size() == 0 )
			return null;
		else return number.iterator().next();
	}
	/**
	 * @return
	 */
	private boolean canTextBeExtracted(Collection<AttachmentInfo> aiList) {
		/*
Are you able to 
extract the 
values using the 
PDF text 
extraction?
		 */
//		for( List<AttachmentInfo> aiList : )
//		{
			for( AttachmentInfo ai : aiList )
			{
				File file = allFiles.get(ai);
				String text = null;
				if( null != file )
				{
					try {
						logger.info("Extracting text from : " + file);
						text = DDCHelper.extractContent(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if( null != text && text.trim().length() != 0 )
				{
					logger.info("Text extracted " + text);
					pdfTexts.put(ai, text);
					
					saveTheFiles(ai,text);
				}
			}
//		}
		
		return (pdfTexts.size() == 0 ? false : true);
	}
	/**
	 * @param ai
	 * @param text
	 * @throws IOException 
	 */
	private void saveTheFiles(AttachmentInfo ai, String text){
		File file = null;
		try {
			file = File.createTempFile(ai.getName(), ".txt");
		
			FileWriter fw = new FileWriter(file);
			fw.append(text);
			fw.close();
			
			textFiles.put(ai, file);
		} catch (IOException e) {
			logger.error("Error occured while writing the text file : " + ai);
		}
	}
	/**
	 * @return 
	 * 
	 */
	private boolean processFirstPageWithOCR() {
		return false;
		/*
Pass the first page 
through the DTN 
Letter Paraemeters 
as specified in 
Extraction of 
information-OCR 
table and compare 
the values with 
those specified in 
the Regex.
		 */
	}
	/**
	 * @param 
	 * @throws DDCException 
	 * @throws DatabaseException 
	 */
	private   void processEachA4PageSizeFile() throws DatabaseException, DDCException {
		/*
Perform the below test
for all files.
		 */
		
		boolean foundValues = findValuesThroughTextExtraction();
		if( foundValues )
		{
			/*
			Are you able to
			get the values
			expected of the
			DTN?
			*/
			boolean foundExpectedDTNValuesFromFirstPage = findExpectedDTNValuesFromFirstPage();
			if( foundExpectedDTNValuesFromFirstPage )
			{
				processDTNValues();
			}
			else
			{
				processWithDTNNumber();
			}			
		}
		else
		{
			processFirstPageThroughDTN();
			boolean foundExpectedDTNValues = findExpectedDTNValues();
			if( foundExpectedDTNValues )
			{
				processDTNValues();
			}
			else
			{
				/// continue with other files
			}
		}
	}
	/**
	 * @return
	 */
	private boolean findExpectedDTNValuesFromFirstPage() {
		/*
Compare all the 
values as extracted 
from the first page 
of the document 
with respect to 
those of expected 
DTN parameters
		 */
		
		/*
Are you able to 
get all the values 
expected of a 
DTN?
		 */
		// TODO : Note just first page but the whole document is considered here.
		// this just do the extraction of parameters for each file and each Firm Property. 
		// Return as soon as you find something. I don't know what else to do
		
		for( FirmProperty fp : firmList )
		{
			for( AttachmentInfo ai : pdfTexts.keySet() )
			{
				dtnParameter = extractDTNParams(fp, ai);
				if( null != dtnParameter )
				{
					// found the dtn
					firmProperty = fp;
					dtnFile = allFiles.get(ai);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @return
	 */
	private boolean findExpectedDTNValues() {
/*
Are you able to 
get the values 
expected of the 
DTN?
 */
		return false;
	}
	/**
	 * 
	 */
	private void processFirstPageThroughDTN() {
		/*
Pass the first page 
through the DTN 
Letter Paraemeters 
as specified in 
Extraction of 
information-OCR 
table and compare 
the values with 
those specified in 
the Regex.
		 */
	}
	/**
	 * @param 
	 * @throws DDCException 
	 * @throws DatabaseException 
	 */
	private   void processDTNValues() throws DatabaseException, DDCException {
		logger.info("Starting to process the DTN : " + dtnFile);
		searchAgencyBAForSearch();
		
		// for each file in attachments except DTN file
		processAttachments();
		ocrForDrawings();
		boolean foundPattern = findPattern();
		if( foundPattern)
		{
			updateInformation();
		}
		else
		{
			identificationInfoNotFound();
		}
		
		boolean foundInformationForAllAttachments = findInfomationForAllAttachments();
		if( foundInformationForAllAttachments )
		{
			boolean reconcileSuccessful = reconcileFiles();
			if( reconcileSuccessful )
			{
				performQualityChecks();
				updateAgencyBAForSearch();
				
				createDTN();
				
				updateTBitsRecords();
				/*
				 Process Ends Successfully.
				 */
				logger.info("Process Ends Successfully.");
			}
			else
			{
				updateRecordWithReconciliation();
			}
		}
		else
		{
			// show error
			updateRecordWithFileFound();
		}
		
	}
	/**
	 * 
	 */
	private void identificationInfoNotFound() {
		/*
The identification 
information related 
to the file could not 
be extracted and 
hence it is NOT 
possible to 
determine the type 
of the file.
Perform the above 
process for all the 
enclosed files.
		 */
	}
	/**
	 * 
	 */
	private void updateRecordWithReconciliation() {
		/*
Update the record with the 
reconciliation results and STOP. 
Human intervention Required.
Ends Unsuccessfully.
		 */
	}
	/**
	 * 
	 */
	private boolean reconcileFiles() {
		/*
		Are you able to reconcile 
		all the files received with 
		the information 
		contained in the DTN/
		tBits Records?
		*/
		return true;
	}
	/**
	 * 
	 */
	private void updateRecordWithFileFound() {
		/*
		Update the record with 
		files found and the files 
		that could not be 
		processed and STOP. 
		Human intervention 
		Required. Ends 
		Unsuccessfully.
		*/
	}
	/**
	 * @return
	 */
	private boolean findInfomationForAllAttachments() {
		/*
		Are you able to get the 
		infromation requested for 
		all the enclosed 
		attachment files?
		*/
		return true;
	}
	/**
	 * @param 
	 */
	private   void updateInformation() {
		/*
Update the information received in
the HAshMAP that contains the
information received from DTN and
tBits records.
		 */
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findPattern() {
		/*
Check if you are able to
extract information
complying to the Pattern
as specified against the
same.
		 */
		return true;
	}
	/**
	 * @param 
	 */
	private   void ocrForDrawings() {
		/*
Pass the files through the expected OCR
Values for drawings/documents and
other attributes specified except for the
DTNLetter Value.
		 */
	}
	/**
	 * @param 
	 */
	private   void processAttachments() {
		/*
Extract the page information for all the
		attachments – first page – for all files
		except for the DTN Note file.
		 */
	}
	/**
	 * @param 
	 * @throws DatabaseException 
	 * @throws DDCException 
	 */
	private   void searchAgencyBAForSearch() throws DatabaseException, DDCException {
		/*
		Search for the BA specified under -
		Agency_BA_For_Search: using the Key =
		Primary_Key_for_Search: and extract the
		request ID’s and all associated fields
		information as has been specified under -
		Relationship_Keys_BAFields for the
		Process.
				 */
		Integer fieldId = null;
		String fieldValue = null;
//		String searchField = firmProperty.getPrimaryKeyForSearch().trim();
//		if( searchField.equals(FirmProperty.SearchKeyContractorDocumentNumber) )
//		{
//			fieldId = firmProperty.getContractorDocumentNumberFieldID();
//			fieldValue = dtnParameter.getContractorDocumentNumber();
//		}
//		else if(  searchField.equals(FirmProperty.SearchKeyOwnerDocumentNumber) )
//		{
//			fieldId = firmProperty.getOwnerDocumentNumberFieldID();
//			fieldValue = dtnParameter.getOwnerDocumentNumber();
//		}
//		else if( searchField.equals(FirmProperty.SearchKeyVendorDocumentNumber))
//		{
//			fieldId = firmProperty.getVendorDocumentNumberFieldID();
//			fieldValue = dtnParameter.getVendorDocumentNumber();
//		}
//		else // assumed to be DTN number if( searchField.equals(FirmProperty.SearchKeyDTNNumber))
//		{
			fieldId = firmProperty.getPrimaryRecordSearchFieldId();
			fieldValue = dtnParameter.getDtnNumber();
//		}
		BusinessArea ba = BusinessArea.lookupBySystemPrefix(firmProperty.getBaPrefix());
		Field field = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), fieldId);
		HashMap<String,String> params = new HashMap<String, String>();
		params.put(field.getName(), fieldValue);
		List<RequestDataType> result = DDCHelper.findSearchResults(ba.getSystemPrefix(), params);
		if(result.size() != 1 )
			throw new DDCException("zero or more than one request matched the field name =" + field.getName() + " and value = " + fieldValue);
		
		RequestDataType rdt = result.get(0);
		dtnRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), rdt.getRequestId());
		
		return;
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findValuesThroughTextExtraction() {
		/*
		Are you able to
		extract the
		values using the
		PDF text
		extraction?
				 */
		return canTextBeExtracted(a4Files.keySet());
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findA4PageSizeFile() {
		/*
Are there any files whose
first page size is A4 /
Letter Portraist settings –
		 */
		
		for( AttachmentInfo ai : allFiles.keySet() )
		{
			try {
				File file = allFiles.get(ai);
			
				if( DDCHelper.isFirstPageA4(file) )
				{
					a4Files.put(ai, file);
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return (a4Files.size() > 0 ? true : false);
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findPatternMatchingFile() {
		/*
		Is there a file whose name is
		following a pattern similar
		to
		Expected_DTN_File_Naming
		_Convention?
		*/

		// keep only those firm proerties whose file pattern matches.
		for( FirmProperty fp : firmList )
		{
			String pattern = fp.getExpectedDTNFileName();
			if( null == pattern )
			{
				logger.info("DTN File pattern was null.");
				return false;
			}
			
			for( AttachmentInfo ai : allFiles.keySet() )
			{
				if( null != ai )
				{
					if(ai.getName().matches(pattern))
					{
						List<AttachmentInfo> atts = filePatternMatchedWithFirmProperty.get(fp);
						if( null == atts )
							atts = new ArrayList<AttachmentInfo>();
						
						atts.add(ai);
						filePatternMatchedWithFirmProperty.put(fp, atts);
					}
				}
			}
		}
		
		return ((filePatternMatchedWithFirmProperty.keySet().size() == 0 )? false : true);
	}
	/**
	 * @param 
	 */
	private   void emailDDC() {
		/*
		Stop – email to document
		controller stating no attachments
		included. May need human
		intervention.
		*/
	}
	
	/**
				 Select all Processes for which
the user is allowed to interact
with the other agency from the
table - Process_DTN_Analysis
AND where the category ID as
what has been specified in the
tBits Record
	 * @throws DDCException 
	 */
	private   void selectAllProcessAndProceed() throws DDCException {
		
		checkProjectCode();
		determinePageSize();
		selectApplicableProcesses();
		doOCR();
		boolean foundOCRValue = findOCRValue();
		if( foundOCRValue )
		{

			/*
			For the selected
			ProcessID – select
			the values of the
			parameter Project
			Code, Revision,
			Contractor
			Document Number
			etc etc as has been
			specified in the table
			of Extraction of
			information-OCR
			*/
			
			/*
Search in the
specified BA as
mentioned under
Agency_BA_For_Sea
rch: for the said BA
to determine the
RequestID to be
updated.

Perform the above
operation for all the
files enclosed.
			 */
			
			performQualityChecks();
			
			updateAgencyBAForSearch();
			
			createDTN();
			
			updateTBitsRecords();
			/*
			 Process Ends Successfully.
			 */
			logger.info("Process Ends Successfully.");
		}
		else
		{
			// NO ALGO MENTIONED in flowchart
		}
	}
	/**
	 * @param 
	 */
	private   void updateTBitsRecords() {
		/*
		 Get the DTN
Number created and
update the tBits
records with the
success status.
		 */
	}
	/**
	 * @param 
	 */
	private   void createDTN() {

		/*
Create the DTN
using the
information as is
available under -
DTNProcessInputs
		 */
	}
	/**
	 * @param 
	 * @throws DDCException 
	 */
	private   void updateAgencyBAForSearch() throws DDCException {
		/*
Update the
Agency_BA_For_Sea
rch: and the
associated records
with the information
as available in
BAUpdationsWtRece
ivedValues
		 */
		
		// update the dtn request with the information we have extracted.
		StringBuffer sb = new StringBuffer();
		sb.append("The Files Processed were :<br>" );
		int i = 0 ;
		for( AttachmentInfo ai : allFiles.keySet())
		{
			sb.append( i + "." + ai.getName() + "<br>");
		}
		
		sb.append("The file detected as dtn was : " + dtnParameter.getAi().getName() + "<br>");
		sb.append("The other parameters extracted from the DTN were :<br>");
		
		FirmProperty fp = dtnParameter.getFp();
//		see how many parameters are given
		if( null != fp.getDtnLetterIdentificationString() ){
			sb.append("DtnLetterIdentificationString" + ": " + dtnParameter.getDtnLetterIdentificationString() + "<br>");
		}
		if( null != fp.getFromFieldAlgoId() )
		{
			sb.append("FromField" + ": " + dtnParameter.getFromField() + "<br>");
		}
		if( null != fp.getToFieldAlgoId() )
		{
			sb.append("ToField" + ": " + dtnParameter.getToField() + "<br>");
		}
		if(null !=fp.getDtnNumberAlgoId() )
		{
			sb.append("DtnNumber" + ": " + dtnParameter.getDtnNumber() + "<br>");
		}
		if( null != fp.getOwnerDocumentNumberAlgoId() )
		{
			sb.append("OwnerDocumentNumber" + ": " + dtnParameter.getOwnerDocumentNumber() + "<br>");
		}
		if(null != fp.getContractorDocumentNumberAlgoId() )
		{
			sb.append("ContractorDocumentNumber" + ": " + dtnParameter.getContractorDocumentNumber()+ "<br>");
		}
		if(null != fp.getVendorDocumentNumberAlgoId() )
		{
			sb.append("VendorDocumentNumber" + ": " + dtnParameter.getVendorDocumentNumber() + "<br>");
		}
		if( null != fp.getRevisionAlgoId() )
		{
			sb.append("Revision" + ": " + dtnParameter.getRevision() + "<br>");
		}
		if( null != fp.getSubmissionCodeAlgoId() )
		{
			sb.append("SubmissionCode" + ": " + dtnParameter.getSubmissionCode() + "<br>");
		}
		if(null != fp.getDtnDateTimeInDTNNoteAlgoId() )
		{
			sb.append("DtnDateTimeInDTNNote" + ": " + dtnParameter.getDtnDateTimeInDTNNote() + "<br>");
		}
		if( null != fp.getProjectCodeAlgoId() )
		{
			sb.append("ProjectCode" + ": " + dtnParameter.getProjectCode() + "<br>");
		}
		if(null != fp.getSubmissionCodeAlgoId() )
		{
			sb.append("SubmissionCode" + ": " + dtnParameter.getSubmissionCode() + "<br>");
		}

		Hashtable<String,String> req = new Hashtable<String,String>(); 
//		uploading all the text files
		ArrayList<AttachmentInfo> ais = new ArrayList<AttachmentInfo>();
		Uploader uploader = new Uploader();
		for( File file : textFiles.values() )
		{
			AttachmentInfo ai = uploader.copyIntoRepository(file);
			ais.add(ai);
		}

		if(ais.size() > 0 )
		{
			req.put(Field.ATTACHMENTS, AttachmentInfo.toJson(ais));
			sb.append("<br>We have also uploaded the text version of the files analyzed in this request.");
		}
		
		String description = sb.toString();
		
		req.put(Field.BUSINESS_AREA, dtnRequest.getSystemId()+"");		
		req.put(Field.REQUEST,dtnRequest.getRequestId()+"");
		req.put(Field.USER,"root");
		req.put(Field.DESCRIPTION,description);


		UpdateRequest up = new UpdateRequest();
		up.setSource(TBitsConstants.SOURCE_CMDLINE);
		
		try {
			Request updatedReq = up.updateRequest(req);
			logger.info("Successfully update request : " + updatedReq.getSystemId() + "#" + updatedReq.getRequestId());
		} catch (Throwable e) {
			e.printStackTrace();
			throw new DDCException("Exception occured while updating the DTN request.", e);
		}
		

	}
	/**
	 * @param 
	 */
	private   void performQualityChecks() {
		/*
		Perform the quality
		checks on the
		documents as per
		the required
		parameters.
		*/
	}
	/**
	 * @param 
	 * @return
	 */
	private   boolean findOCRValue() {
		/*
Are you able to find the
OCR Value matching the
pattern as defined in the
OCR Table for a particular
process ID?
		 */
		return false;
	}
	/**
	 * @param 
	 */
	private   void doOCR() {
		/*
Run the file through the project
code ONLY search via the OCR.
		 */
	}
	/**
	 * @param 
	 */
	private   void selectApplicableProcesses() {
		/*
Based on the page size
specifications – select all the
applicable Processes related to
the search of the ProjectCode:
Parameter from the Extraction
of information-OCR table.
		 */
	}
	/**
	 * @param 
	 */
	private   void determinePageSize() {
		/*
Determine the page size of the
incoming files.
		 */
	}
	/**
	 * @param 
	 */
	private   void checkProjectCode() {
		/*
Check for the Parameter of
Project Code for the said
processes.
		 */
	}
	/**
	 * checks which of the firmProperty has set isDTN expected and keeps only those.
	 * removes others from firmList;
	 * @param 
	 * @return true if atleast one of the firm has dtnExpected to true
	 */
	private   boolean isDDCExpectedFromSender() {
		for( Iterator<FirmProperty> iter = firmList.iterator() ; iter.hasNext() ; )
			if( !iter.next().isDtnExpected() )
				iter.remove();
		
		return (firmList.size() == 0 ? false : true);
	}
	/**
	 * checks if attachments are present, downloads/copies them and set the parameters
	 * @param 
	 * @return
	 */
	private   boolean doesEmailHasAttachments() 
	{
		String attDir = APIUtil.getAttachmentLocation();
		Collection<AttachmentInfo> attachments = (Collection<AttachmentInfo>) ddcRequest.getObject(DDC_ATTACHMENT_FIELD_NAME);
		for( AttachmentInfo ai : attachments )
		{
			try {
				String loc = Uploader.getFileLocation(ai.getRepoFileId());
				File file = new File(attDir + File.separator + loc);
				String fileExtension = getFileExtension(file);
				if( file.exists() )
				{
					// put a copy of original file to avoid any corruption to original
					File tmpFile = File.createTempFile(file.getName(),fileExtension);
					FileInputStream fis = new FileInputStream(file);
					FileOutputStream fos = new FileOutputStream(tmpFile);
					DDCHelper.writeStream(fis,fos);
					fis.close();
					fos.close();
					allFiles.put(ai, tmpFile);
				}
				else
				{
					allFiles.put(ai, null);
				}
			} catch (DatabaseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if( null == attachments || attachments.size() == 0 )
		{
			logger.info("No attachments found in the request.");
			return false;
		}
			
		logger.info("The email has attachments.");
		return true;
	}
	/**
	 * @param file
	 * @return
	 */
	private String getFileExtension(File file) {
		int index = file.getName().lastIndexOf('.');
		if( -1 != index)
		{
			return file.getName().substring(index);
		}
		return "";
	}
	
	public static void main(String[] args) {
		String baPrefix = "bugtesting";
		Integer reqId = 28;
		
		DDCObject ddcObject;
		try {
			ddcObject = new DDCObject(baPrefix,reqId);
			ddcObject.doDDC();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (DDCException e) {
			e.printStackTrace();
		} catch (FailedToFindObject e) {
			e.printStackTrace();
		}
	}
}
