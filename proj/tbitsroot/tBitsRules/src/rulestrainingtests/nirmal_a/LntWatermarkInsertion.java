/**
 * 
 */
package rulestrainingtests.nirmal_a;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.ibm.icu.text.SimpleDateFormat;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

/**
 * @author lokesh
 *
 */
public class LntWatermarkInsertion implements IRule {

	private static final String PRINT = "print";
	private static final String WATER_MARK = "WaterMark";
	private static final String STAMP_RFC_GIF = "Stamp_RFC.gif";
	private static final String TBITSREPORTS = "tbitsreports";
	private static final String GEN_WMRFC = "GenWMRFC";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
				
		Integer[] fieldsInfo;
		try {			
			
			fieldsInfo = lookupSourceAndTargetFieldsForStamping(connection, ba.getSystemId());
			
			if ((fieldsInfo != null) && (fieldsInfo.length == 3)){
				if ((fieldsInfo[1] > 0) && (fieldsInfo[2] > 0)){
					String genWMRFCValue = currentRequest.get(GEN_WMRFC);
					if ((genWMRFCValue == null) || (!Boolean.valueOf(genWMRFCValue)))
						return new RuleResult(true, "Not applicable.");
					
					Field srcAttachmentField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), fieldsInfo[1]);
					Field targetAttachmentField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), fieldsInfo[2]);
					
					if (srcAttachmentField == null)
						return new RuleResult(false, "Invalid/missing source field from which attachments have to picked up for water-marking(RFC) files.");
					
					if (targetAttachmentField == null)
						return new RuleResult(false, "Invalid/missing target field to which water-marked(RFC) files have to be put in.");
					
					if (srcAttachmentField != null){
						String tempDir = Configuration.findAbsolutePath (PropertiesHandler.getProperty(
								transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
						String tbitsReportsDir = Configuration.findAbsolutePath (TBITSREPORTS);
						String attachmentsDir = Configuration.findAbsolutePath(PropertiesHandler.getProperty(
								transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR));
						String imagePath = tbitsReportsDir + File.separatorChar + STAMP_RFC_GIF;
						File imgFile = new  File(imagePath);
						if (!imgFile.exists())
							return new RuleResult(true, "No RFC image file found which is used for water-marking.");
						
						ArrayList<AttachmentInfo> outAIList = new ArrayList<AttachmentInfo>();

						ArrayList<AttachmentInfo> srcFieldAttachmentsList = (ArrayList<AttachmentInfo>) currentRequest.getObject(
																					srcAttachmentField);
						if ((srcFieldAttachmentsList != null) && (!srcFieldAttachmentsList.isEmpty()))
						{
							ArrayList<File> stampedFilesList = new ArrayList<File>();	

							for(AttachmentInfo ai : srcFieldAttachmentsList)
							{
								String srcFileLocation = Uploader.getFileLocation(ai.getRepoFileId());
								int extStartIndex = srcFileLocation.lastIndexOf(".");
								String fileExtension = srcFileLocation.substring(extStartIndex + 1);
								if ((fileExtension == null) || (!fileExtension.trim().equals("pdf"))){
									outAIList.add(ai);
									continue;
								}

								if (srcFileLocation != null){
									File inputFile = new File(attachmentsDir + File.separatorChar + srcFileLocation);
									String outPath = tempDir + File.separatorChar + ai.getName();
									if (inputFile.exists() && inputFile.canRead()){											
										//File outFile = stampPDF(inputFile.getAbsolutePath(), imagePath, outPath);
										try{
											File outFile = addWatermarkImg(inputFile.getAbsolutePath(), 
													outPath, imagePath, 1, 100, true, true, true, 10, 10,
													0.7f, true, 100, 100, user);
											stampedFilesList.add(outFile);
										} catch (IllegalArgumentException e) {			
											e.printStackTrace();			
											return new RuleResult(false, e.getMessage() + ".Hence cannot add watermark to file: " 
													+ ai.name + ". Upload a file without password or" 
													+ " please uncheck \"Generate Watermark RFC\" option.");
										} 
									}
								}
							}
							Uploader uploader = new Uploader();
							uploader.setFolderHint(ba.getSystemPrefix());
							if (!stampedFilesList.isEmpty()){
								for (File stampedFile : stampedFilesList){
									AttachmentInfo stampedFileInfo = uploader.moveIntoRepository(stampedFile);
									outAIList.add(stampedFileInfo);
								}
							}
							currentRequest.setObject(targetAttachmentField, outAIList);
						}
					}
				}
				else{
					return new RuleResult(true, "Invalid/missing configuration for stamping of pdf fields. Please check the configuration.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return new RuleResult(true, "Database exception occurred, during water-marking process: " + e.getMessage());
		} catch (DatabaseException e) {
			e.printStackTrace();
			return new RuleResult(true, "Database exception occurred, during water-marking process: " + e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new RuleResult(true, "File not found exception occurred during water-marking process : " + e.getMessage());
		} catch (DocumentException e) {
			e.printStackTrace();
			return new RuleResult(true, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			return new RuleResult(true, e.getMessage());
		} 
		return new RuleResult (true);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "- Inserts watermark into the existing pdf files.";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {
		return 0;
	}
	
	public static File stampPDF(String pdfPath, String imagePath, String outPath)
	throws DocumentException, IOException{
		File outFile = new File(outPath);
		PdfReader reader = new PdfReader(pdfPath);
		int n = reader.getNumberOfPages();

		// Create a stamper that will copy the document to a new file
		PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(outFile));
		int i = 1;
		PdfContentByte pdfContentByte;

		Image img = Image.getInstance(imagePath);
		img.setAbsolutePosition(10, 50);
		img.setRotationDegrees(90);
		img.scalePercent(100);
		int[] transparency = {1, 1};
		img.setTransparency(transparency);

		while (i <= n) 
		{
			// Watermark under the existing page
			pdfContentByte = stamp.getUnderContent(i);
			pdfContentByte.addImage(img);
			i++;
		}

		stamp.close();

		return outFile;
	}
	
	public static Integer[] lookupSourceAndTargetFieldsForStamping(Connection connection, int systemId) throws SQLException{
		Integer[] fieldIdsInfo = null;
		PreparedStatement ps = connection.prepareStatement("Select * from trn_watermark_fields_info where sys_id=?");
		ps.setInt(1, systemId);
		ResultSet rs = ps.executeQuery();
		if (rs != null)
			if (rs.next()){
				fieldIdsInfo = new Integer[3];
				fieldIdsInfo[0] = rs.getInt("sys_id");
				fieldIdsInfo[1] = rs.getInt("src_field_id");
				fieldIdsInfo[2] = rs.getInt("target_field_id");
			}
		rs = null;
		ps = null;
		return fieldIdsInfo;
	}
	
	
	public static File addWatermarkImg(String inFilefName, String outFileName,
            String imgFileName, int startPage, int endPage,
            boolean onTop, boolean onScreen, boolean onPrint,
            float posX, float posY, float Opacity,
            boolean scaleOrAbsolute, float sizeX, float sizeY,
            User user) throws IOException, DocumentException {

		File outFile = new File(outFileName);
        //open the files
        PdfReader reader = new PdfReader(inFilefName);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outFile));
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        
        Image image = Image.getInstance(imgFileName);
        image.setRotationDegrees(90);
        if (scaleOrAbsolute)
            image.scalePercent(sizeX, sizeY);
        else
            image.scaleAbsolute(sizeX, sizeY);

        //add water-mark layer
        PdfLayer wmLayer = new PdfLayer(WATER_MARK, stamper.getWriter());
        wmLayer.setOnPanel(true);

        //set layer parameters
        wmLayer.setPrint(PRINT, true);
        wmLayer.setOn(onScreen);
        wmLayer.setView(onScreen); 

        //Prepare transparency
        PdfGState transparent = new PdfGState();
        transparent.setStrokeOpacity(Opacity);
        transparent.setFillOpacity(Opacity); 

        PdfContentByte cb;
        PdfDictionary p;
        int toPage = stamper.getReader().getNumberOfPages();
        for (int i = startPage; i <= toPage; i++) {
        	if (onTop)
        		cb = stamper.getOverContent(i);
        	else
        		cb = stamper.getUnderContent(i);

//        	Rectangle rectangle = stamper.getReader().getPageSizeWithRotation(i);
        	cb.beginLayer(wmLayer);
        	cb.setGState(transparent); //set block transparency properties

        	Rectangle cropBox = stamper.getReader().getCropBox(i);
        	int rotation = reader.getPageRotation( i );
        	float absoluteY = 0.0f;
        	float absoluteX = 0.0f;
        	switch( rotation ) {
        	case 0:
        		absoluteX = cropBox.getLeft(); 
        		absoluteY = cropBox.getBottom();        	     	   
        		break;
        	case 90:
        		absoluteX = cropBox.getBottom();
        		absoluteY = cropBox.getLeft();        	
        		break;
        	case 180:
        		absoluteX = cropBox.getTop() - (98*cropBox.getTop()/100);
        		absoluteY = cropBox.getLeft();        	
        		break;
        	case 270:
        		absoluteX = cropBox.getRight() - (98*cropBox.getRight()/100);
        		absoluteY = 10.0f + cropBox.getLeft();        	
        		break;
        	};

        	image.setAbsolutePosition(absoluteX, absoluteY);
        	cb.addImage(image); 
        	cb.beginText();
        	cb.setFontAndSize(bf, 12);
        	cb.setColorFill(Color.red);
        	cb.showTextAligned(Element.ALIGN_LEFT, "Created On: " + getCurrentDate(), absoluteX + 20, 
        			absoluteY + 175 + image.getHeight(), 90);  
        	cb.endText();            
        	cb.stroke();
        	cb.endLayer();
        }
        stamper.close();
        return outFile;
	}	
	
	public static String getCurrentDate(){
		String curDate = "";
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		curDate = sdf.format(date);
		return curDate;
	}
	
	
	
	
	public static void main(String[] args) {
		
		if(args.length < 3)
		{
			System.err.println("Syntax: WaterMarkInsertionApp <watermarkfile> <output folder> <file to be watermarked> <file to be watermarked> ...");
			return;
		}
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		
		try {
			User user = User.lookupAllByUserLogin("root");
			
//			String[] arr = new String[]{"/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 21 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 20 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/1_L555-00M-00512_Rev-1_sheet 19 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 19 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 18 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 17 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 16 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 15 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 14 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 13 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 12 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 11 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 10 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 9 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 8 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 7 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 6 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 5 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 4 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 3 of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 2of 36.pdf", "/Users/sandeepgiri/Downloads/zipped260/L555-00M-00512_Rev-1_sheet 1of 36.pdf"};
			String watermarkfile = args[0];
			String outputDir = args[1];
			if(!new File(outputDir).exists())
			{
				System.err.println("The directory '" + outputDir + "' does not exist");
				return;
			}
			for(int i=2;i<args.length;i++)
			{
				String file  = args[i];
				String outfile = outputDir +  "/test-" + (i-1) +".pdf";
				addWatermarkImg(file,
					outfile, 
					watermarkfile,
					1, 100, true, true, true, 10, 10, 0.7f, true, 100, 100, user);
				System.out.println("Saved: " + outfile);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}


	
//	PdfDictionary pageDict = reader.getPageN(1);
//	PdfArray boxes[] = {pageDict.getAsArray( PdfName.MEDIABOX ), pageDict.getAsArray( PdfName.CROPBOX ) };
//	float mods[] = {-1000, -1000, 1000, 1000 };
//	float AbsoluteY = 0.0f;
//	for (int h = 0; h < boxes.length; ++h) {
//	  if (boxes[h] == null)
//	    continue; // crop boxes are optional
//	  for (int j = 0; j < 4; ++j) {
//	    PdfNumber curVal = boxes[h].getAsNumber(j);
//	   // PdfNumber newVal = new PdfNumber(curVal.floatValue() + mods[j]);
//	   AbsoluteY = curVal.floatValue() + (rectangle.getHeight() 
//    			+ image.getScaledHeight() - (60 * rectangle.getHeight()/100));
//	    //boxes[h].set( j, newVal );
//	  }
//	}      	
	
	
//	AffineTransform transform = null;
//	double xOffset, yOffset;
//	switch( rotation ) {
//	  case 0:
//	    xOffset = cropBox.getRight() - image.getRight();
//	    yOffset = cropBox.getTop() - image.getLeft();
//	    transform = AffineTransform.getTranslateInstance( xOffset, yOffset );
//	    break;
//	  case 90:
//		xOffset = cropBox.getRight() - image.getRight();
//  	    yOffset = cropBox.getTop() - image.getLeft();
//  	    transform = AffineTransform.getTranslateInstance( xOffset, yOffset );
//	    break;
//	  case 180:
//		 xOffset = cropBox.getRight() - image.getRight();
//  	    yOffset = cropBox.getTop() - image.getLeft();
//  	    transform = AffineTransform.getTranslateInstance( xOffset, yOffset );
//	    break;
//	  case 270:
//		xOffset = cropBox.getRight() - image.getRight();
//  	    yOffset = cropBox.getTop() - image.getLeft();
//  	    transform = AffineTransform.getTranslateInstance( xOffset, yOffset );
//	    break;
//	};
//
//	content.transform( transform );        	
//	image.setAbsolutePosition(posX, AbsoluteY);
//	content.addImage( image );

	//stamper.close();

	
	
}
