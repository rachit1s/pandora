package transbit.tbits.report.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;

import com.ibm.icu.util.Calendar;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.APIException;
import transbit.tbits.report.TBitsReportEngine;
//import transmittal.com.tbitsGlobal.server.BirtTemplateHelper;

public class ReportEngineTest extends TestCase
{
	public static void main1(String argv[])
	{
		try
		{
			System.out.println("Starting the test.");
			int numOfThread = 10 ;

			String logFileName = "/home/nitiraj/checkouts/birt_test/dailytask_reports/logs.txt";
			PrintStream oldStream = System.out;
			File logFile = new File(logFileName);
			if( !logFile.exists() )
				logFile.createNewFile();
			
			System.setOut(new PrintStream(logFile));
			
			String reportFileName = "dailytaskkist_mom.rptdesign";
			Map<Object, Object> reportMap = new HashMap<Object, Object>();
			
			Map<String, Object> reportParams = new HashMap<String, Object>();
			reportParams.put("user_id", "47");
			
			String outputFilePath = "/home/nitiraj/checkouts/birt_test/dailytask_reports/";
			String typeOfFile = TBitsReportEngine.FILE_TYPE_PDF;
			
			initialize(reportFileName, reportMap, reportParams, outputFilePath, typeOfFile);
			
			long start = System.currentTimeMillis();
			ReportEngineTestThread [] threads = new ReportEngineTestThread[numOfThread];
			for( int i = 0 ; i < numOfThread ; i++ )
			{
				ReportEngineTestThread thread = new ReportEngineTestThread(i, reportFileName, reportMap, reportParams, outputFilePath, typeOfFile);
				threads[i] = thread;
				thread.start();
			}
			
			for( int i = 0 ; i < numOfThread ; i++ )
			{
				threads[i].join();
			}
			
			System.setOut(oldStream);
			
			long end = System.currentTimeMillis();
			
			System.out.println("Finished test. Time = " + (end-start) + " milliseconds");
			System.out.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void initialize(String reportFileName, Map<Object, Object> reportMap, Map<String, Object> reportParams, String outputFilePath, String typeOfFile) throws InterruptedException {
		ReportEngineTestThread thread = new ReportEngineTestThread(1000, reportFileName, reportMap, reportParams, outputFilePath, typeOfFile);
		thread.start();
		thread.join();
	}
	
	/**
	 * This test requires the BirtTemplateHelper object and class in class path
	 * @param argv
	 */
	/*
	public static void main2( String argv[] )
	{
		try
		{
			System.out.println("Starting the test.");
			int numOfThread = 10 ;
			
			String reportFileName = "dtn_template_kdi_standard_for_comments.rptdesign";
			Map<Object, Object> reportMap = new HashMap<Object, Object>();

			String birtObjectPath = "/home/nitiraj/checkouts/birt_test/transmittal_test/input/transmittalObject";
			FileInputStream fis = new FileInputStream(new File(birtObjectPath));
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			BirtTemplateHelper bth = (BirtTemplateHelper) ois.readObject(); 
			
			Map<String, Object> reportParams = new HashMap<String, Object>();
			reportMap.put("BirtTemplateHandler", bth);
			
			String outputFilePath = "/home/nitiraj/checkouts/birt_test/transmittal_test/output/";
			String typeOfFile = TBitsReportEngine.FILE_TYPE_HTML;
			
			initialize(reportFileName, reportMap, reportParams, outputFilePath, typeOfFile);
			
			String logFileName = "/home/nitiraj/checkouts/birt_test/transmittal_test/output/logs.txt";
			File logFile = new File(logFileName);
			PrintStream oldStream = System.out;
			System.setOut(new PrintStream(logFile));
			
			long start = System.currentTimeMillis();
			ReportEngineTestThread [] threads = new ReportEngineTestThread[numOfThread];
			for( int i = 0 ; i < numOfThread ; i++ )
			{
				ReportEngineTestThread thread = new ReportEngineTestThread(i, reportFileName, reportMap, reportParams, outputFilePath, typeOfFile);
				threads[i] = thread;
				thread.start();
			}
			
			for( int i = 0 ; i < numOfThread ; i++ )
			{
				threads[i].join();
			}
			
			System.setOut(oldStream);
			long end = System.currentTimeMillis();
			System.out.println("Finished test. Time = " + (end-start) + " milliseconds");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	*/
	
	public static void main3( String argv[] )
	{
		int noOfReqs = 100 ;
		try
		{
			for( int i = 0 ; i < noOfReqs ; i++ )
			{
				Hashtable<String,String> params = new Hashtable<String,String>();
				params.put("EscalatedTo", "root");
				params.put(Field.ASSIGNEE, "root");
				params.put("status_id", "Open");
				params.put(Field.BUSINESS_AREA, "MoM_KDI");
				params.put(Field.USER, "root");
				params.put(Field.SUMMARY, "This is the summary......");
				
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -3);
				Date d = cal.getTime();
				params.put(Field.DUE_DATE, Timestamp.toCustomFormat(d, TBitsConstants.API_DATE_FORMAT));
				
				AddRequest ar = new AddRequest();
				ar.setSource(TBitsConstants.SOURCE_CMDLINE);
				Request req = ar.addRequest(params);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} catch (APIException e) {
			e.printStackTrace();
		}
	}
	
	public static void main4( String argv[] )
	{
		int noOfReqs = 100 ;
		try
		{
			for( int i = 0 ; i < noOfReqs ; i++ )
			{
				Hashtable<String,String> params = new Hashtable<String,String>();
				params.put("EscalatedTo", "dc_desein");
				params.put(Field.ASSIGNEE, "dc_desein");
				params.put("status_id", "Open");
				params.put(Field.BUSINESS_AREA, "MoM_KDI");
				params.put(Field.USER, "dc_desein"); // user_id = 47
				params.put(Field.SUMMARY, "This is the summary......");
				
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 3);
				Date d = cal.getTime();
				params.put(Field.DUE_DATE, Timestamp.toCustomFormat(d, TBitsConstants.API_DATE_FORMAT));
				
				AddRequest ar = new AddRequest();
				ar.setSource(TBitsConstants.SOURCE_CMDLINE);
				Request req = ar.addRequest(params);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} catch (APIException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main5(String argv[])
	{
		try
		{
			System.out.println("Starting the test.");
			int numOfThread = 100 ;
			
			String reportFileName = "lnt_corr_template.rptdesign";
			Map<Object, Object> reportMap = new HashMap<Object, Object>();
			Map<String, Object> reportParams = new HashMap<String, Object>();
			

			reportParams.put("rid", "3");
			reportParams.put("tbits_base_url", "http://localhost:8080");
			reportParams.put("sys_prefix", "Malwa_Corr");
			
			reportMap.put("refNo","refNo_value");
			reportMap.put("dear","dear_value");
			reportMap.put("to","to_value");
			reportMap.put("kindAttn","kindAttn_value");
			reportMap.put("project","project_value");
			reportMap.put("subject","subject_value");
			reportMap.put("description","description_value");
			reportMap.put("Cc","Cc_value");
			reportMap.put("Att","Att_value");
			reportMap.put("ImagePath","ImagePath_value");
			reportMap.put("for_company","for_company_value");
			reportMap.put("logger","logger_value");
			reportMap.put("subscriber","subscriber_value");
			reportMap.put("otherReference ","otherReference _value");
			reportMap.put("headerImage ","headerImage _value");
			reportMap.put("masterFooter ","masterFooter _value");
			reportMap.put("other_cc ","other_cc _value");

			
			String outputFilePath = "/home/nitiraj/checkouts/birt_test/corr_test/output/";
			String typeOfFile = TBitsReportEngine.FILE_TYPE_PDF;
			
			initialize(reportFileName, reportMap, reportParams, outputFilePath, typeOfFile);
			
			String logFileName = "/home/nitiraj/checkouts/birt_test/corr_test/output/logs.txt";
			File logFile = new File(logFileName);
			PrintStream oldStream = System.out;
			System.setOut(new PrintStream(logFile));
			
			long start = System.currentTimeMillis();
			ReportEngineTestThread [] threads = new ReportEngineTestThread[numOfThread];
			for( int i = 0 ; i < numOfThread ; i++ )
			{
				ReportEngineTestThread thread = new ReportEngineTestThread(i, reportFileName, reportMap, reportParams, outputFilePath, typeOfFile);
				threads[i] = thread;
				thread.start();
			}
			
			for( int i = 0 ; i < numOfThread ; i++ )
			{
				threads[i].join();
			}
			
			System.setOut(oldStream);
			long end = System.currentTimeMillis();
			System.out.println("Finished test. Time = " + (end-start) + " milliseconds");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String argv[])
	{
		main5(argv);
	}
}
