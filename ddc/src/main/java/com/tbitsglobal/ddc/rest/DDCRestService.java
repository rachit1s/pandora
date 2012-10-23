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

@Path("/ddc-service")
public class DDCRestService {
	
	private static final Logger logger = Logger.getLogger(DDCRestService.class);
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
	public static List<RequestDataType> findDTNs(ArrayList<File> files,String email) throws DDCException, FailedToFindObject
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
	
	public static void main(String argv[]) throws IOException
	{
		File folder = new File("C:\\ddc_test\\12_oct_ddc_files_solomonnita\\docs");
		
		File[] files = folder.listFiles();
		ArrayList<File> pdfFiles = new ArrayList<File>();
		for( File file : files )
			pdfFiles.add(file);
		
		DDCHelper.pdfToTextFileUsingTika(pdfFiles);
	}
}
