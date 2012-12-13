package imp;

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
 * @author ankit
 *
 */
public class SingleAcknowledgment implements IRule {

	private static final String PRINT = "print";
	private static final String WATER_MARK = "WaterMark";
	private static final String STAMP_RFC_GIF = "Stamp_ACK.gif";
	private static final String TBITSREPORTS = "tbitsreports";
	private static final String GEN_ACK = "singleacknowledgment";

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
			
			if ((fieldsInfo != null) && (fieldsInfo.length == 2)){
				if ((fieldsInfo[0] > 0) && (fieldsInfo[1] > 0)){
					String genACKalue = currentRequest.get(GEN_ACK);
					if ((genACKalue == null) || (!Boolean.valueOf(genACKalue)))
						return new RuleResult(true, "Not applicable.");
					
					Field AttachmentField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), fieldsInfo[1]);
//					Field targetAttachmentField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(), fieldsInfo[2]);
					
					if (AttachmentField == null)
						return new RuleResult(false, "Invalid/missing source field from which attachments have to picked up for Acknowledgment files.");
					
//					if (targetAttachmentField == null)
//						return new RuleResult(false, "Invalid/missing target field to which water-marked(RFC) files have to be put in.");
					
					if (AttachmentField != null){
						String tempDir = Configuration.findAbsolutePath (PropertiesHandler.getProperty(
								transbit.tbits.Helper.TBitsPropEnum.KEY_TMPDIR));
						String tbitsReportsDir = Configuration.findAbsolutePath (TBITSREPORTS);
						String attachmentsDir = Configuration.findAbsolutePath(PropertiesHandler.getProperty(
								transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR));
						String imagePath = tbitsReportsDir + File.separatorChar + STAMP_RFC_GIF;
						File imgFile = new  File(imagePath);
						if (!imgFile.exists())
							return new RuleResult(true, "No Stamp_ACK.gif image file found which is used for Acknowledgment.");
						
						ArrayList<AttachmentInfo> outAIList = new ArrayList<AttachmentInfo>();

						ArrayList<AttachmentInfo> FieldAttachmentsList = (ArrayList<AttachmentInfo>) currentRequest.getObject(
																					AttachmentField);
						if ((FieldAttachmentsList != null) && (!FieldAttachmentsList.isEmpty()))
						{
							ArrayList<File> stampedFilesList = new ArrayList<File>();	

							for(AttachmentInfo ai : FieldAttachmentsList)
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
													outPath, imagePath, 1, 100, true, true, true, 20, 20,
													0.7f, true, 100, 100, user);
											if(outFile!=null)
											stampedFilesList.add(outFile);
											if(outFile==null)
												stampedFilesList.add(inputFile);
										} catch (IllegalArgumentException e) {			
											e.printStackTrace();			
											return new RuleResult(false, e.getMessage() + ".Hence cannot add Acknowledgment to file: " 
													+ ai.name + ". Upload a file without password or" 
													+ " please uncheck \"Acknowledgment\" option.");
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
							currentRequest.setObject(AttachmentField, outAIList);
						}
					}
				}
				else{
					return new RuleResult(true, "Invalid/missing configuration for ACKNOWLEDGMENT stamping of pdf fields. Please check the configuration.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return new RuleResult(true, "Database exception occurred, during water-marking process: " + e.getMessage());
		} catch (DatabaseException e) {
			e.printStackTrace();
			return new RuleResult(true, "Database exception occurred, during water-marking process: " + e.getMessage());
		} 
		return new RuleResult (true);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "- Inserts Acknowledgment into the existing pdf files.";
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
		PreparedStatement ps = connection.prepareStatement("Select * from trn_acknowledgment_fields_info where sys_id=?");
		ps.setInt(1, systemId);
		ResultSet rs = ps.executeQuery();
		if (rs != null)
			if (rs.next()){
				fieldIdsInfo = new Integer[2];
				fieldIdsInfo[0] = rs.getInt("sys_id");
				fieldIdsInfo[1] = rs.getInt("field_id");
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
            User user){

		File outFile = new File(outFileName);
		try
		{
		
        //open the files
        PdfReader reader = new PdfReader(inFilefName);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outFile));
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        
        Image image = Image.getInstance(imgFileName);
        image.setRotationDegrees(0);

        if (scaleOrAbsolute)
            image.scalePercent((sizeX*6/10), (sizeY*6/10));
        else
            image.scaleAbsolute((sizeX*6/10), (sizeY*6/10));

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
//        	cb.setGState(transparent); //set block transparency properties

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

        	image.setAbsolutePosition((cropBox.getTop()/2)-110,cropBox.getTop()-100);

        	cb.addImage(image); 
        	cb.beginText();
        	cb.setFontAndSize(bf, 105/10);
        	cb.setColorFill(Color.red);
        	cb.showTextAligned(Element.ALIGN_MIDDLE, "Acknowledged On: " + getCurrentDate(), (cropBox.getTop()/2)-110, 
        			cropBox.getTop()-110, 0);  
        	cb.endText();            
        	cb.stroke();
        	cb.endLayer();
        }
        stamper.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
			System.err.println("Syntax: ACKInsertionApp <ACKfile> <output folder> <file to be ACKFile> <file to be ACKq> ...");
			return;
		}
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		
		try {
			User user = User.lookupAllByUserLogin("root");
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
		}catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}	
}
