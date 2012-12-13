package transbit.tbits.report.test;

import java.io.File;
import java.util.Map;

import transbit.tbits.report.TBitsReportEngine;

public class ReportEngineTestThread extends Thread
{
	public ReportEngineTestThread(int id, String reportFileName,
			Map<Object, Object> reportMap, Map<String, Object> reportParams,
			String outputFilePath, String typeOfFile	) 
	{
		super();
		this.id = id;
		this.reportFileName = reportFileName;
		this.reportMap = reportMap;
		this.reportParams = reportParams;
		this.typeOfFile = typeOfFile;
		this.outputFile = new File( outputFilePath + reportFileName + "_" + this.id + ( this.typeOfFile.equalsIgnoreCase("html") ? ".html" : ".pdf" ) );
	}

	String reportFileName ;
	Map<Object,Object> reportMap;
	Map<String,Object> reportParams;
	File outputFile;
	int id ;
	String typeOfFile = "html";
	
	public void run()
	{
		try
		{
			System.out.println(id + " : Starting thread");
			long start = System.currentTimeMillis();
			
			if( typeOfFile.equalsIgnoreCase("HTML"))
				TBitsReportEngine.getInstance().generateHTMLFile(reportFileName, reportMap, reportParams, outputFile);
			else
				TBitsReportEngine.getInstance().generatePDFFile(reportFileName, reportMap, reportParams, outputFile);
			
			long end = System.currentTimeMillis();
			
			System.out.println(id + " : Finished thread. Time = " + (end-start) + " milliseconds");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
