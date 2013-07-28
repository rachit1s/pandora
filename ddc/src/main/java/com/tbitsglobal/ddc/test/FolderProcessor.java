package com.tbitsglobal.ddc.test;

import java.io.File;

import org.apache.log4j.Logger;

import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.IEngine;
import com.tbitsglobal.ddc.FindDocStructure;
import com.tbitsglobal.ddc.SamplesConfig;

public class FolderProcessor {

	public static final Logger logger = Logger.getLogger(FolderProcessor.class);
	public static void unloadEngine(IEngine engine) {
		print( "Deinitializing Engine..." );
		engine = null;
		System.gc();
		System.runFinalization();
		Engine.Unload();
	}
	
	public static IEngine loadEngine() {
		print( "Initializing Engine..." );
		IEngine engine = Engine.Load( SamplesConfig.GetDllFolder(), SamplesConfig.GetDeveloperSN(), null, null );
		return engine;
	}

	public static void print(String string) {
		logger.info(string);
	}

	
	public static void setupFREngine(IEngine engine) {
		print( "Loading predefined profile..." );
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
	
	public static String findTextAll(IEngine engine, String folderName) 
	{
		StringBuffer sb = new StringBuffer();
		File imageFolder = new File( FindDocStructure.folderPath + "/" + folderName);
		if( !imageFolder.exists() )
			return "Error : folder does not exists : " + folderName;
		
		File[] files = imageFolder.listFiles();
		
		for( File file : files )
		{
			if( file.getName().endsWith(".png"))
			{
				sb.append( FindDocStructure.processImage(engine, file) );
			}
		}
		
		return sb.toString();
	}
	
	public static String findTextAllAndCreate(IEngine engine, String folderName) 
	{
		StringBuffer sb = new StringBuffer();
		File imageFolder = new File( FindDocStructure.folderPath + "/" + folderName);
		if( !imageFolder.exists() )
			return "Error : folder does not exists : " + folderName;
		
		File[] files = imageFolder.listFiles();
		
		for( File file : files )
		{
			if( file.getName().endsWith(".png"))
			{
				sb.append( FindDocStructure.processImageAndCreate(engine, file));
			}
		}
		
		return sb.toString();
	}
	
	public static String findTextFromImage(IEngine engine, String imageName) 
	{
		StringBuffer sb = new StringBuffer();
		File imageFile = new File( FindDocStructure.folderPath + "/" + imageName);
		if( !imageFile.exists() )
			return "Error : folder does not exists : " + imageName;
		
		if( imageFile.getName().endsWith(".png"))
		{
			sb.append( FindDocStructure.processImage(engine, imageFile) );
		}
		
		return sb.toString();
	}

	public static String findTextAndCreate(IEngine engine, String imageName) 
	{
		StringBuffer sb = new StringBuffer();
		File imageFile = new File( FindDocStructure.folderPath + "/" + imageName);
		if( !imageFile.exists() )
			return "Error : folder does not exists : " + imageName;
		
		if( imageFile.getName().endsWith(".png"))
		{
			sb.append( FindDocStructure.processImageAndCreate(engine, imageFile) );
		}
		
		return sb.toString();
	}

}
