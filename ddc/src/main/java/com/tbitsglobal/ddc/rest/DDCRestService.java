package com.tbitsglobal.ddc.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.RequestDataType;

import com.abbyy.FREngine.IEngine;
import com.googlecode.javacv.FrameGrabber.Array;
import com.lowagie.text.pdf.PdfDocument;
import com.tbitsglobal.ddc.FindDocStructure;
import com.tbitsglobal.ddc.PDF2ImageConverter;
import com.tbitsglobal.ddc.common.DocumentNumberSet;
import com.tbitsglobal.ddc.common.DocumentSet;
import com.tbitsglobal.ddc.dao.FirmPropertyDao;
import com.tbitsglobal.ddc.domain.FirmProperty;
import com.tbitsglobal.ddc.exception.FailedToFindObject;
import com.tbitsglobal.ddc.test.FolderProcessor;

/*
 * Notes
 Detect PageFormat : PageFormat PDDocument.getPageFormat(int pageIndex);
 */
@Path("/ddc-service")
public class DDCRestService {
	
	private static final Logger logger = Logger.getLogger(DDCRestService.class);
	
	/**
	 * 1. Routine – 1: Identification of Agency and Expected Value Identification
	 * 2. Routine for Identification of the Document Transmittal Note
	 * 3. Routine – 2: Identification of all attributes of the documents received using the searching of information from tBits
	 * 4. Routine-3: Extraction of information-OCR
	 * 5. Routine-4: Reconciliation between the information received from OCR & Information received from DTN.
	 * 6. Routine – 5: Updating the Records – upon successful reconciliation of the information received from DTN and OCR of files.
	 * 7. Routine – 6: Proceed with the inward of the information
	 */
	public static void doDDC()
	{
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
		DDCObject ddco = new DDCObject();
		boolean hasAttachments = doesEmailHasAttachments(ddco);
		if( hasAttachments )
		{
			boolean isDDCExpected = isDDCExpectedFromSender(ddco);
			if(isDDCExpected)
			{
				boolean foundPatternMatchingFile = findPatternMatchingFile(ddco);
				if(foundPatternMatchingFile)
				{
					
				}
				else
				{
					boolean foundA4PageSizeFile = findA4PageSizeFile(ddco);
					if( foundA4PageSizeFile )
					{
						processEachA4PageSizeFile(ddco);
					}
					else
					{
						
					}
				}
			}
			else
			{
				selectAllProcessAndProceed(ddco);
			}
		}
		else
		{
			emailDDC(ddco);
		}
	}
	/**
	 * @param ddco
	 */
	private static void processEachA4PageSizeFile(DDCObject ddco) {
		/*
Perform the below test
for all files.
		 */
		
		boolean foundValues = findValuesThroughTextExtraction(ddco);
		if( foundValues )
		{
			
		}
		else
		{
			/*
			Are you able to
			get the values
			expected of the
			DTN?
			*/
			boolean foundValuesThroughOCR = findValuesThroughOCR(ddco);
			if( foundValuesThroughOCR )
			{
				processDTNValues(ddco);
			}
			else
			{
				

			}
		}
	}
	/**
	 * @param ddco
	 */
	private static void processDTNValues(DDCObject ddco) {
		searchAgencyBAForSearch(ddco);
		processAttachments(ddco);
		ocrForDrawings(ddco);
		boolean foundPattern = findPattern(ddco);
		if( foundPattern)
		{
			updateInformation(ddco);
		}
		else
		{
			
		}
		
	}
	/**
	 * @param ddco
	 */
	private static void updateInformation(DDCObject ddco) {
		/*
Update the information received in
the HAshMAP that contains the
information received from DTN and
tBits records.
		 */
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean findPattern(DDCObject ddco) {
		/*
Check if you are able to
extract information
complying to the Pattern
as specified against the
same.
		 */
		return false;
	}
	/**
	 * @param ddco
	 */
	private static void ocrForDrawings(DDCObject ddco) {
		/*
Pass the files through the expected OCR
Values for drawings/documents and
other attributes specified except for the
DTNLetter Value.
		 */
	}
	/**
	 * @param ddco
	 */
	private static void processAttachments(DDCObject ddco) {
		/*
Extract the page information for all the
		attachments – first page – for all files
		except for the DTN Note file.
		 */
	}
	/**
	 * @param ddco
	 */
	private static void searchAgencyBAForSearch(DDCObject ddco) {
		/*
		Search for the BA specified under -
		Agency_BA_For_Search: using the Key =
		Primary_Key_for_Search: and extract the
		request ID’s and all associated fields
		information as has been specified under -
		Relationship_Keys_BAFields for the
		Process.
				 */
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean findValuesThroughOCR(DDCObject ddco) {
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
		return false;
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean findValuesThroughTextExtraction(DDCObject ddco) {
		/*
		Are you able to
		extract the
		values using the
		PDF text
		extraction?
				 */
		return false;
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean findA4PageSizeFile(DDCObject ddco) {
		/*
Are there any files whose
first page size is A4 /
Letter Portraist settings –
		 */
		return false;
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean findPatternMatchingFile(DDCObject ddco) {
		/*
		Is there a file whose name is
		following a pattern similar
		to
		Expected_DTN_File_Naming
		_Convention?
		*/
		return false;
	}
	/**
	 * @param ddco
	 */
	private static void emailDDC(DDCObject ddco) {
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
	 */
	private static void selectAllProcessAndProceed(DDCObject ddco) {
		
		checkProjectCode(ddco);
		determinePageSize(ddco);
		selectApplicableProcesses(ddco);
		doOCR(ddco);
		boolean foundOCRValue = findOCRValue(ddco);
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
			
			performQualityChecks(ddco);
			
			updateAgencyBAForSearch(ddco);
			
			createDTN(ddco);
			
			updateTBitsRecords(ddco);
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
	 * @param ddco
	 */
	private static void updateTBitsRecords(DDCObject ddco) {
		/*
		 Get the DTN
Number created and
update the tBits
records with the
success status.
		 */
	}
	/**
	 * @param ddco
	 */
	private static void createDTN(DDCObject ddco) {

		/*
Create the DTN
using the
information as is
available under -
DTNProcessInputs
		 */
	}
	/**
	 * @param ddco
	 */
	private static void updateAgencyBAForSearch(DDCObject ddco) {
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
	}
	/**
	 * @param ddco
	 */
	private static void performQualityChecks(DDCObject ddco) {
		/*
		Perform the quality
		checks on the
		documents as per
		the required
		parameters.
		*/
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean findOCRValue(DDCObject ddco) {
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
	 * @param ddco
	 */
	private static void doOCR(DDCObject ddco) {
		/*
Run the file through the project
code ONLY search via the OCR.
		 */
	}
	/**
	 * @param ddco
	 */
	private static void selectApplicableProcesses(DDCObject ddco) {
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
	 * @param ddco
	 */
	private static void determinePageSize(DDCObject ddco) {
		/*
Determine the page size of the
incoming files.
		 */
	}
	/**
	 * @param ddco
	 */
	private static void checkProjectCode(DDCObject ddco) {
		/*
Check for the Parameter of
Project Code for the said
processes.
		 */
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean isDDCExpectedFromSender(DDCObject ddco) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * @param ddco
	 * @return
	 */
	private static boolean doesEmailHasAttachments(DDCObject ddco) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * 1. find the dtn file
	 * 2. read the dtn file to get the transmittal number
	 * 	2.1 if transmittal number found then return the request associated with this transmittal
	 * 		2.1.1 find the other drawing numbers in the transmittal file and check if they are provided with the rest of the files.
	 * 			2.1.1.1 do ocr on the drawing files and then read the given number
	 *  2.2 if transmittal number not found then find other number and check the request for that number
	 *  	2.1.1 
	 * 2. get the dtn request for the
	 * @param files
	 * @return returns the request corresponding to the dtn file
	 * @throws DDCException 
	 * @throws FailedToFindObject 
	 */
	/*public static List<RequestDataType> findDTNs(ArrayList<File> files,String email) throws DDCException, FailedToFindObject
	{
		// get the firm
		FirmProperty fp = FirmPropertyDao.getInstance().findFirmPropertyByEmailId(email);
		String sysPrefix = fp.getLoggingBAName();
		
		HashMap<File, String> texts = DDCHelper.extractPDFText(files);
		
		// get dtn file
		File file = DDCHelper.getDTNFile(texts,fp);
		
		if( file == null)
		{
			logger.info("No DTN file found.");
			throw new DDCException("No DTN file found.");
		}
		
		String firstNumber = null, secondNumber = null, thirdNumber = null;
		// find first number
		if( fp.getNumber1Field() != null && null != fp.getNumber1AlgoId())
		{
			firstNumber = DDCHelper.getNumber(file,texts.get(file),fp.getNumber1AlgoId());
		}
		
		if( fp.getNumber2Field() != null && null != fp.getNumber2AlgoId())
		{
			secondNumber = DDCHelper.getNumber(file,texts.get(file),fp.getNumber2AlgoId());
		}
		
		if( fp.getNumber3Field() != null && null != fp.getNumber3AlgoId())
		{
			thirdNumber = DDCHelper.getNumber(file,texts.get(file),fp.getNumber3AlgoId());
		}
		
		HashMap<String,String> searchParams = new HashMap<String,String>(3);
		searchParams.put(fp.getNumber1Field(), firstNumber);
		searchParams.put(fp.getNumber2Field(), secondNumber);
		searchParams.put(fp.getNumber3Field(), thirdNumber);
		
		List<RequestDataType> dtns = DDCHelper.findSearchResults(sysPrefix,searchParams);
		return dtns;
	}
*/

	public static void pdfToTextFileUsingPDFBox(ArrayList<File>files) throws IOException
	{
		for( File file : files )
		{
			if( !file.getName().endsWith(".pdf"))
				continue;
			
			System.out.println("Starting file : " + file.getName());
			PDDocument pdfDoc = PDDocument.load(file);
			
			PDFTextStripper pdfTextStripper = new PDFTextStripper();
			File textFile = new File(file.getAbsolutePath() + ".txt");
			if( !textFile.exists() )
				textFile.createNewFile();
			
			Writer writer = new FileWriter(textFile);
			pdfTextStripper.writeText(pdfDoc, writer);
			
			pdfDoc.close();
//			pdfTextStripper.
		}
	}
	
	@Path("/getText")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static DocumentNumberSet getText(DocumentSet docSet)
	{
		return  new DocumentNumberSet();
	}
	
	@Path("/hello-main")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String getHelloMessage()
	{
		FindDocStructure.main(null);
		return "done";
	}

	@Path("/findText")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String findText(@QueryParam( "imageName") String imageName)
	{
		return FindDocStructure.findText(imageName);
	}
	

	@Path("/findTextAll")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String findTextAll(@QueryParam( "folderName") String folderName)
	{
		IEngine engine = FolderProcessor.loadEngine();
		try
		{
			FolderProcessor.setupFREngine(engine);
		
			return FolderProcessor.findTextAll(engine,folderName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FolderProcessor.print("Unloading Engine...");
			FolderProcessor.unloadEngine(engine);
			FolderProcessor.print("Engine Unloaded...");
		}
		return "Finished....";
	}
	
	@Path("/findTextAllAndCreate")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String findTextAllAndCreate(@QueryParam( "folderName") String folderName)
	{
		IEngine engine = FolderProcessor.loadEngine();
		try
		{
			FolderProcessor.setupFREngine(engine);
		
			return FolderProcessor.findTextAllAndCreate(engine,folderName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FolderProcessor.print("Unloading Engine...");
			FolderProcessor.unloadEngine(engine);
			FolderProcessor.print("Engine Unloaded...");
		}
		return "Finished....";
	}
	
	@Path("/findTextFromImage")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String findTextFromImage(@QueryParam( "imageName") String imageName)
	{
		IEngine engine = FolderProcessor.loadEngine();
		try
		{
			FolderProcessor.setupFREngine(engine);
		
			return FolderProcessor.findTextFromImage(engine,imageName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FolderProcessor.print("Unloading Engine...");
			FolderProcessor.unloadEngine(engine);
			FolderProcessor.print("Engine Unloaded...");
		}
		return "Finished....";
	}
	
	@Path("/findTextAndCreate")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String findTextAndCreate(@QueryParam( "imageName") String imageName)
	{
		IEngine engine = FolderProcessor.loadEngine();
		try
		{
			FolderProcessor.setupFREngine(engine);
		
			return FolderProcessor.findTextAndCreate(engine,imageName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FolderProcessor.print("Unloading Engine...");
			FolderProcessor.unloadEngine(engine);
			FolderProcessor.print("Engine Unloaded...");
		}
		return "Finished....";
	}
	
	@Path("/pdf2png")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String pdf2png(@QueryParam("pdfName") String pdfName)
	{
		File file = new File(FindDocStructure.folderPath + "/" + pdfName);
		PDF2ImageConverter.getImage(file);
		
		return "done converting pdf to png : " + file.getAbsolutePath();
	}
	
	@Path("/pdf2pngFolder")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String pdf2pngFolder(@QueryParam("folderName") String folderName)
	{
		File folder = new File(FindDocStructure.folderPath + "/" + folderName);
		File[] files = folder.listFiles();
		for( File file : files )
		{
			PDF2ImageConverter.getImage(file);
		}
		
		return "done converting pdfs to pngs in folder " + folderName;
	}
	
	/*public static void main(String argv[]) throws IOException
	{
		File folder = new File("/home/nitiraj/work/DC/12_oct_ddc_files_solomonnita/test");
		
		File[] files = folder.listFiles();
		ArrayList<File> pdfFiles = new ArrayList<File>();
		for( File file : files )
			pdfFiles.add(file);
		
		try {
			DDCRestService.findDTNs(pdfFiles, "nitiraj.r@tbitsglobal.com");
		} catch (DDCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FailedToFindObject e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
