package com.tbitsglobal.ddc;
// � ABBYY. 2011.
// SAMPLES code is property of ABBYY, exclusive rights are reserved. 
//
// DEVELOPER is allowed to incorporate SAMPLES into his own APPLICATION and modify it under 
// the  terms of  License Agreement between  ABBYY and DEVELOPER.


// ABBYY FineReader Engine 10 Sample

// This sample shows basic steps of ABBYY FineReader Engine usage:
// Initializing, opening image file, recognition and export.

import java.io.File;

import org.apache.log4j.Logger;

import com.abbyy.FREngine.*;

public class FindDocStructure {

	public static final Logger logger = Logger.getLogger(FindDocStructure.class);
	public static final String folderPath = "D:\\ddc_test\\";
	public static void main( String[] args ) {
		findText("abc.jpg");
	}

	public static String Run(String imageName) {
		// Load ABBYY FineReader Engine
		loadEngine();
		try{
			// Process with ABBYY FineReader Engine
			return processWithEngine(imageName);
		} finally {
			// Unload ABBYY FineReader Engine
			unloadEngine();
		}
	}

	private static void loadEngine() {
		displayMessage( "Initializing Engine..." );
		engine = Engine.Load( SamplesConfig.GetDllFolder(), SamplesConfig.GetDeveloperSN(), null, null );
	}

	public static String processWithEngine(String imageName) {
		try {
			// Setup FREngine
			setupFREngine();

			// Process sample image
			return processImage(imageName);
		} catch( Exception ex ) {
			ex.printStackTrace();
//			displayMessage( ex.getMessage() );
			return "Error : " + ex.getMessage();
		}
	}

	public static void setupFREngine() {
		displayMessage( "Loading predefined profile..." );
		engine.LoadPredefinedProfile( "DocumentConversion_Accuracy" );
		// Possible profile names are:
		//   "DocumentConversion_Accuracy", "DocumentConversion_Speed",
		//   "DocumentArchiving_Accuracy", "DocumentArchiving_Speed",
		//   "BookArchiving_Accuracy", "BookArchiving_Speed",
		//   "TextExtraction_Accuracy", "TextExtraction_Speed",
		//   "FieldLevelRecognition",
		//   "BarcodeRecognition",
		//   "Version9Compatibility",
		//   "Default"
	}

	public static String processImage(String imageName) {
		// Create document
		IFRDocument document = engine.CreateFRDocument();

		try {
			// Add image file to document
			displayMessage( "Loading image..." );
			String imagePath = folderPath + imageName;
			File file = new File(imagePath);
			if( file.exists() == false )
				return "Error : file does not exists with name : " + imageName;
			
			document.AddImageFile( imagePath, null, null );

			// Process document
			displayMessage( "Process..." );
			document.Process( null, null, null );
			
			IDocumentStructure docStruct = document.getDocumentStructure();
			int docSecCount = docStruct.getDocumentSectionsCount();
			for( int i = 0 ; i < docSecCount ; i++ )
			{
				IDocumentSection docSec = docStruct.getDocumentSection(i);
				
//				IDocumentStream mainTextStream = docSec.getMainTextStream();
				
				int docStreams = docSec.getDocumentStreamsCount();
				for( int j = 0 ; j < docStreams ; j++ )
				{
					IDocumentStream mainTextStream = docSec.getDocumentStream(j); 
					printDocument(mainTextStream);
				}
				
			}
			
			String completeText = document.getPlainText().getText();
			System.out.println("Extracted Text : " + completeText );

			// Save results
			displayMessage( "Saving results..." );

			// Save results to rtf with default parameters
			String rtfExportPath = folderPath + imageName + ".rtf";
			document.Export( rtfExportPath, FileExportFormatEnum.FEF_RTF, null );

			// Save results to pdf using 'balanced' scenario
			IPDFExportParams pdfParams = engine.CreatePDFExportParams();
			pdfParams.setScenario( PDFExportScenarioEnum.PES_Balanced );

			String pdfExportPath = folderPath + imageName + ".pdf";
			document.Export( pdfExportPath, FileExportFormatEnum.FEF_PDF, pdfParams );
			return completeText;
		}catch( Exception e ){
			e.printStackTrace();
			return "Error : " + e.getMessage();
		} finally {
			// Close document
			document.Close();
		}
	}

	public static String processImage(IEngine engine, File image) {
		// Create document
		IFRDocument document = engine.CreateFRDocument();

		try {
			// Add image file to document
			print( "Loading image... : " + image.getName());
			String imagePath = image.getAbsolutePath();
			File file = new File(imagePath);
			if( file.exists() == false )
				return "Error : file does not exists with name : " + image.getAbsolutePath();
			
			document.AddImageFile( imagePath, null, null );

			// Process document
//			print( "Process..." );
			document.Process( null, null, null );
			
			IDocumentStructure docStruct = document.getDocumentStructure();
			int docSecCount = docStruct.getDocumentSectionsCount();
			for( int i = 0 ; i < docSecCount ; i++ )
			{
				IDocumentSection docSec = docStruct.getDocumentSection(i);
				
//				IDocumentStream mainTextStream = docSec.getMainTextStream();
				
				int docStreams = docSec.getDocumentStreamsCount();
				for( int j = 0 ; j < docStreams ; j++ )
				{
					IDocumentStream mainTextStream = docSec.getDocumentStream(j); 
					printDocument(mainTextStream);
				}
				
			}
			
			/*
			String completeText = document.getPlainText().getText();
			print("Extracted Text : " + completeText );

			// Save results
			print( "Saving results..." );

			// Save results to rtf with default parameters
			String rtfExportPath = image.getParent() + "/" + image.getName() + ".rtf";
			document.Export( rtfExportPath, FileExportFormatEnum.FEF_RTF, null );

			// Save results to pdf using 'balanced' scenario
			IPDFExportParams pdfParams = engine.CreatePDFExportParams();
			pdfParams.setScenario( PDFExportScenarioEnum.PES_Balanced );

			String pdfExportPath = image.getParent() + "/" + image.getName() + ".pdf";
			document.Export( pdfExportPath, FileExportFormatEnum.FEF_PDF, pdfParams );
			return completeText;
			*/
			return "";
		}catch( Exception e ){
			e.printStackTrace();
			return "Error : " + e.getMessage();
		} finally {
			// Close document
//			print("NOT Closing document .. " + image.getName());
//			document.Close();
		}
	}
	
	public static String processImageAndCreate(IEngine engine, File image) {
		// Create document
		IFRDocument document = engine.CreateFRDocument();

		try {
			// Add image file to document
			print( "Loading image... : " + image.getName());
			String imagePath = image.getAbsolutePath();
			File file = new File(imagePath);
			if( file.exists() == false )
				return "Error : file does not exists with name : " + image.getAbsolutePath();
			
			document.AddImageFile( imagePath, null, null );

			// Process document
//			print( "Process..." );
			document.Process( null, null, null );
			
			IDocumentStructure docStruct = document.getDocumentStructure();
			int docSecCount = docStruct.getDocumentSectionsCount();
			for( int i = 0 ; i < docSecCount ; i++ )
			{
				IDocumentSection docSec = docStruct.getDocumentSection(i);
				
//				IDocumentStream mainTextStream = docSec.getMainTextStream();
				
				int docStreams = docSec.getDocumentStreamsCount();
				for( int j = 0 ; j < docStreams ; j++ )
				{
					IDocumentStream mainTextStream = docSec.getDocumentStream(j); 
					printDocument(mainTextStream);
				}
				
			}
			
//			/*
//			String completeText = document.getPlainText().getText();
//			print("Extracted Text : " + completeText );

			// Save results
			print( "Saving results..." );

			// Save results to rtf with default parameters
			String rtfExportPath = image.getParent() + "/" + image.getName() + ".rtf";
			document.Export( rtfExportPath, FileExportFormatEnum.FEF_RTF, null );

			// Save results to pdf using 'balanced' scenario
//			IPDFExportParams pdfParams = engine.CreatePDFExportParams();
//			pdfParams.setScenario( PDFExportScenarioEnum.PES_Balanced );
//
//			String pdfExportPath = image.getParent() + "/" + image.getName() + ".pdf";
//			document.Export( pdfExportPath, FileExportFormatEnum.FEF_PDF, pdfParams );
//			return completeText;
//			*/
			return "";
		}catch( Exception e ){
			e.printStackTrace();
			return "Error : " + e.getMessage();
		} finally {
			// Close document
//			print("NOT Closing document .. " + image.getName());
//			document.Close();
		}
	}

	public static void printDocument(IDocumentStream docStream) {
		IDocumentElement currElement = docStream.getFirstElement();
		int i = 0 ; 
		while( currElement != null )
		{
			DocumentElementTypeEnum t = currElement.getType() ;
			String ts = getTypeString(t);
//			print(i++ + ". Element : " + ts);
			
			handleElement(currElement);
			
			currElement = docStream.getNextElement(currElement);
		}
	}

	public static void handleElement(IDocumentElement currElement) 
	{
		DocumentElementTypeEnum type = currElement.getType() ;
		if( type == DocumentElementTypeEnum.DET_Barcode)
		{
			handleBarCode(currElement);
		}
		else if ( type == DocumentElementTypeEnum.DET_Paragraph )
		{
			handleParagraph(currElement);
		}
		else if ( type == DocumentElementTypeEnum.DET_Picture )
		{
			handlePicture(currElement);
		}
		else if ( type == DocumentElementTypeEnum.DET_Table )
		{
			handleTable(currElement);
		}
		else
		{
//			print("cannot handle unknown type");
		}
	}

	public static void handleParagraph(IDocumentElement currElement) {
//		print("Handle Paragraph");
//		IParagraph para = currElement.GetAsParagraph();
//		print("Paragraph Text : " + para.getText());
	}

	public static void handlePicture(IDocumentElement currElement) {
//		ITextPicture pic = currElement.GetAsPicture();
//		IFRPage page = pic.getPage();
//		IPlainText text = page.getPlainText();
//		print("text of pic : " + text.getText());
	}

	public static void handleTable(IDocumentElement currElement) {
		
		ITextTable table = currElement.GetAsTable();
		int cellCount = table.getCellsCount();
		print("Handle table.\nTotal cells for this table are : " + cellCount + "\nCell values are:\n" );
		for( int i = 0 ; i < cellCount ; i++ )
		{
			ITextTableCell cell = table.getCell(i);
//			IPlainText cellValue = cell.getElement().getPage().getPlainText() ;
//			print(i + ": " + cellValue.getText());
//			
			IPageElement pageElement = cell.getElement();
			String cellText = handlePageElement(pageElement);
			print(i + ": " + cellText);
		}
	}

	private static String handlePageElement(IPageElement pageElement) {
		if( PageElementTypeEnum.PET_Barcode ==  pageElement.getType() )
		{
//			print("Cannot handle PageElement Barcode.");
		}
		else if( PageElementTypeEnum.PET_Picture ==  pageElement.getType() )
		{
//			print("Cannot handle PageElement Picture.");
		}
		else if( PageElementTypeEnum.PET_Table ==  pageElement.getType() )
		{
//			print("Cannot handle PageElement Table.");
		}
		else if( PageElementTypeEnum.PET_Text ==  pageElement.getType() )
		{
//			print("handling page element of text type.");
			IText text = pageElement.GetAsText();
			IParagraphs paras = text.getParagraphs();
			int count = paras.getCount();
			StringBuffer sb = new StringBuffer();
			for( int i = 0 ; i < count ; i++ )
			{
				IParagraph para = paras.getElement(i);
				sb.append(para.getText());
				
			}
			return sb.toString();
		}
		else
		{
//			print("Cannot handle PageElement Unknown type.");
		}
		return "";
	}

	public static void handleBarCode(IDocumentElement currElement) {
//		print("cannot Handle Barcode");
	}

	public static void print(String string) {
//		System.out.println(string);
		logger.info(string);
	}

	public static String getTypeString(DocumentElementTypeEnum type) 
	{
		if( type == DocumentElementTypeEnum.DET_Barcode)
		{
			return "BarCode"; 
		}
		else if ( type == DocumentElementTypeEnum.DET_Paragraph )
		{
			return "Paragraph";
		}
		else if ( type == DocumentElementTypeEnum.DET_Picture )
		{
			return "Picture";
		}
		else if ( type == DocumentElementTypeEnum.DET_Table )
		{
			return "Table";
		}
		else
			return "UnknowType";
	}

	public static void unloadEngine() {
		displayMessage( "Deinitializing Engine..." );
		engine = null;
		System.gc();
		System.runFinalization();
		Engine.Unload();
	}

	public static void displayMessage( String message ) {
		System.out.println( message );
	}
	
	public static String findText(String imageName)
	{
		try {
			return FindDocStructure.Run(imageName);
		} catch( Exception ex ) {
			displayMessage( ex.getMessage() );
			return "Error : " + ex.getMessage() ;
		}
	}

	private static IEngine engine = null;

}
