package transbit.tbits.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IDataIterator;
import org.eclipse.birt.report.engine.api.IExtractionResults;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IResultSetItem;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.ReportRunner;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.ConnectionProperties;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;

/**
 * 
 * @author nitiraj
 *
 */
/**
 * This is a Singleton class which wraps the BirtReportEngine and provide developer-friendly methods 
 * to create the reports. This introduces new generateXXX methods for easy generation of PDF / HTML file and output-stream
 */
public class TBitsReportEngine {
	private static final int MAX_RANDOM = 1000000;
	private static final String PASSWORD = "password";
	private static final String USER = "user";
	private static final String DRIVER_U_R_L = "driverURL";
	private static final String DRIVER_CLASS = "driverClass";
	private static final String TBITS_DATA_SOURCE = "tbitsdatasource";
	String home;
	private static int instance_id = 0;
	private IReportEngine engine = null;
	
	public static String FILE_TYPE_PDF = "pdf";
	public static String FILE_TYPE_HTML = "html";
	public static String FILE_TYPE_DOC="doc";
	
	// Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_REPORT);
	private static TBitsReportEngine instance = null;
	private TBitsReportEngine() throws BirtException
	{
		instance_id++;
		EngineConfig config = new EngineConfig();
		
		home = Configuration.findPath("birt-runtime/ReportEngine").getAbsolutePath();
		config.setEngineHome(home);
		String logDir = Configuration.findPath("../logs").getAbsolutePath();
		config.setLogConfig(logDir, Level.SEVERE);
		
		Platform.startup( config );
		IReportEngineFactory factory = (IReportEngineFactory) Platform
		.createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );
		engine = factory.createReportEngine( config );
		engine.changeLogLevel( Level.WARNING );
		engine.getConfig().getAppContext().put("out", System.out);
		engine.getConfig().getAppContext().put("LOG", LOG);
	}
	
	public synchronized static TBitsReportEngine getInstance() throws TBitsException
	{
		if( null == instance )
			try {
				instance = new TBitsReportEngine();
			} catch (Exception e) {
				LOG.error(e);
				throw new TBitsException(e);
			}
		
		return instance;
	}
	
	public IRunAndRenderTask createRunAndRenderTask(IReportRunnable design) throws TBitsException
	{
		return engine.createRunAndRenderTask(design);
	}
	
	public IGetParameterDefinitionTask createGetParameterDefinitionTask(IReportRunnable design) throws TBitsException
	{
		return engine.createGetParameterDefinitionTask(design);
	}
	
	public IReportRunnable openReportDesign(String reportFileName) throws TBitsException
	{
		return getReportDesign(reportFileName);
	}
	
	private static boolean isInList(String str, String[] list)
	{
		for(String s:list)
		{
			if(s.equals(str))
				return true;
		}
		return false;
	}
	
	public boolean containsResults(IReportDocument ird, String[] essentialData) throws EngineException
	{
		if(essentialData.length == 0)
			return true;
		boolean containstResults = false;
		//Create Data Extraction Task		
		IDataExtractionTask iDataExtract = createDataExtractionTask(ird);

		//Get list of result sets		
		ArrayList resultSetList = (ArrayList)iDataExtract.getResultSetList( );

		for(int i = 0; i < resultSetList.size(); i++)
		{
			IResultSetItem resultItem = (IResultSetItem)resultSetList.get( i );
			String dispName = resultItem.getResultSetName();
			
			iDataExtract.selectResultSet( dispName );
			//System.out.println("Display Name:" + dispName);
			if(!isInList(dispName, essentialData))
			{
				LOG.debug("Skipping report element '" + dispName + "' from counting the results");
				continue;
			}
			IExtractionResults iExtractResults = iDataExtract.extract();
			IDataIterator iData = null;
			try{
				if ( iExtractResults != null )
				{
					iData = iExtractResults.nextResultIterator( );

					//iterate through the results
					if ( iData != null  ){
						if ( iData.next( ) )
						{	
							containstResults = true;
						}
						iData.close();
					}
					else
						LOG.info("iData is null");
				}
				else
					LOG.info("iExtractResults is null");
			}catch( Exception e){
					e.printStackTrace();
			}

		}
		return containstResults;
	}
	
	public IReportRunnable getReportDesign(String reportFileName) throws TBitsException
	{
		try
		{
			if(engine == null)
			{
				LOG.error("The report engine has not been initialized. Please check if the birt-runtime directory exists.");
				return null; 
			}
			IReportRunnable design;
			File f = Configuration.findPath("tbitsreports/" + reportFileName);
			if( null == f )
			{
				throw new IllegalArgumentException("Report file does not exists : " + reportFileName );
			}
			if( !f.exists() )
			{
				throw new IllegalArgumentException("Report file does not exists : " + f.getAbsolutePath() );
			}
			
			design = engine.openReportDesign(f.getAbsolutePath());
			
			updateDBProperties(design);
			return design;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error(e);
			throw new TBitsException(e);
		}
	}
	
	public void updateDBProperties(IReportRunnable design) throws TBitsException
	{
		try
		{
			//modify report
			ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle( );
			DataSourceHandle dsH = designHandle.findDataSource(TBITS_DATA_SOURCE);
			if(dsH == null || !(dsH instanceof OdaDataSourceHandle))
			{
				return;
				//throw new IllegalArgumentException("Could not find the tBits data source. Please ensure that the report contains a datasource name '" 
				//		+ TBITS_DATA_SOURCE + "'");
			}
			
			OdaDataSourceHandle dsHandle = (OdaDataSourceHandle) dsH;
			
			dsHandle.setProperty( "odaDriverClass",ConnectionProperties.getDBPoolProperty(DRIVER_CLASS));
			dsHandle.setProperty( "odaURL", ConnectionProperties.getDBPoolProperty(DRIVER_U_R_L));
			dsHandle.setProperty( "odaUser", ConnectionProperties.getDBPoolProperty(USER) );
			dsHandle.setProperty( "odaPassword", ConnectionProperties.getDBPoolProperty(PASSWORD) );
		}
		catch(Exception e)
		{
			LOG.error(e);
			throw new TBitsException(e);
		}
	}
	
	public IReportDocument getReportDocument(IReportRunnable design, 
			HashMap<String, String> params) throws EngineException, IOException
	{
		IRunTask task = engine.createRunTask(design);
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader()); 
		
		//Set parameter values and validate
		for(String paramKey:params.keySet())
		{
			task.setParameterValue(paramKey, params.get(paramKey));
		}
		task.validateParameters();
		
		//System.out.println("The Report file: " + design.getReportName());
		File f = findUniqueFile(".rptdocument");
		
		task.run(f.getAbsolutePath());
		task.close();
		IReportDocument iReportDocument = engine.openReportDocument(f.getAbsolutePath());
		return iReportDocument;
	}
	
	public File getHTMLReport(IReportDocument iReportDocument) throws EngineException, IOException
	{
		IRenderTask task = engine.createRenderTask(iReportDocument);
		//Set parent classloader report engine
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader()); 
		
		//Setup rendering to HTML
		HTMLRenderOption options = new HTMLRenderOption();
		
		File of = findUniqueFile(".html");
		
		options.setOutputFileName(of.getAbsolutePath());
		options.setOutputFormat("html");
		//Setting this to true removes html and body tags
		options.setEmbeddable(false);

		task.setRenderOption(options);
		
		//run and render report
		try {
			task.render();
			if(task.getStatus() != task.STATUS_SUCCEEDED)
				throw new EngineException("The task didnt succeed.");
		} catch (EngineException e) {
			System.out.println(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		task.close();
		
		return of; 
	}

	/**
	 * use this method to specifically give the output file of the report 
	 * @param iReportDocument
	 * @param outputFile
	 * @return
	 * @throws EngineException
	 */
	public File getPDFReport(IReportDocument iReportDocument, File outputFile ) throws EngineException
	{
		IRenderTask task = engine.createRenderTask(iReportDocument);
		//Set parent classloader report engine
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader()); 
		
		HTMLRenderOption options = new HTMLRenderOption( );
        options.setOutputFormat( HTMLRenderOption.OUTPUT_FORMAT_PDF );

        options.setOutputFileName(outputFile.getAbsolutePath());
		
		//Setting this to true removes html and body tags
		options.setEmbeddable(true);

		task.setRenderOption(options);
		
		//run and render report
		try 
		{
			task.render();
			if(task.getStatus() != task.STATUS_SUCCEEDED)
				throw new EngineException("The task didnt succeed.");
		} catch (EngineException e) 
		{		
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		task.close();
		
		return outputFile; 
	}
	
	public File getPDFReport(IReportDocument iReportDocument) throws EngineException, IOException
	{
		IRenderTask task = engine.createRenderTask(iReportDocument);
		//Set parent classloader report engine
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader()); 
		
		//Setup rendering to PDF
//		PDFRenderOption options = new PDFRenderOption();		
//		File of = findUniqueFile(".pdf");		
//		options.setOutputFileName(of.getAbsolutePath());
//		options.setOutputFormat( IRenderOption.OUTPUT_FORMAT_PDF ) ;
		
		//options.setEmbededFont(true) ;		
		//Setting this to true removes html and body tags
		//options.setEmbeddable(false);
		
		// Render html within pdf : not working
		HTMLRenderOption options = new HTMLRenderOption( );
        options.setOutputFormat( HTMLRenderOption.OUTPUT_FORMAT_PDF );
        File of = findUniqueFile(".pdf");		
		options.setOutputFileName(of.getAbsolutePath());
		
		//Setting this to true removes html and body tags
		options.setEmbeddable(true);

		task.setRenderOption(options);
		
		//run and render report
		try {
			task.render();
			if(task.getStatus() != task.STATUS_SUCCEEDED)
				throw new EngineException("The task didnt succeed.");
		} catch (EngineException e) {
			System.out.println(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		task.close();
		
		return of; 
	}

	public IRunTask createRunTask(IReportRunnable reportRunnable)
	{
		return engine.createRunTask(reportRunnable);
	}
	
	public IRenderTask createRenderTask(IReportDocument reportDocument)
	{
		return engine.createRenderTask(reportDocument);
	}

	public IRenderTask createRenderTask(IReportDocument reportDocument, IReportRunnable reportRunnable)
	{
		return engine.createRenderTask(reportDocument, reportRunnable);
	}
	
	public IDataExtractionTask createDataExtractionTask(IReportDocument reportDocument)
	{
		return engine.createDataExtractionTask(reportDocument);
	}
	
	private File findUniqueFile(String fileExt) throws IOException
	{
		File tbitsTmpFile = Configuration.findPath("tmp");
		if( !tbitsTmpFile.exists() )
			tbitsTmpFile.mkdirs();
		
		File tmpFile = File.createTempFile("tmp_report_file", fileExt, tbitsTmpFile);
		tmpFile.deleteOnExit();
		return tmpFile;
//		String tmpOutputLoc = home + "/tmp";
//		File of = null;
//		String outFileName;
//		int i = 0;
//		do
//		{
//			Random r = new Random();
//			outFileName = "outputfile-" + instance_id + "-" + r.nextInt(MAX_RANDOM) + "-" + i++ + fileExt;
//			of = new File(tmpOutputLoc + "/" + outFileName);
//		}
//		while(of.exists());
//		return of;
	}
	
	public File getHTMLReport(IReportRunnable design, HashMap<String, String> params) throws SemanticException, IllegalArgumentException, EngineException, IOException
	{
		IReportDocument reportDoc = getReportDocument(design, params);
		
		File output =  getHTMLReport(reportDoc);
		
		File f = new File(reportDoc.getName());
		reportDoc.close();
		
		if(f.exists())
		{
			LOG.debug("Deleting report file.");
			if(f.delete())
				LOG.debug("Successfully deleted.report document '" + f.getAbsolutePath() + "'");
			else
				LOG.error("failed to delete report document '" + f.getAbsolutePath() + "'");
		}
		else
			LOG.error("report report document '" + f.getAbsolutePath() + "' doesnt exist.");
		
		return output;
	}
	
	public String getReportParameters(String reportFileName){	
		//Open a report design 
		String paramList = "";
		IReportRunnable design = null;
		try {
			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Opening report");
			design = engine.openReportDesign(Configuration.findPath("/tbitsreports").getAbsolutePath()+ "\\"+reportFileName);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
				
		IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask( design );
		Collection params = task.getParameterDefns( true );

		Iterator iter = params.iterator( );
		//Iterate over all parameters
		while ( iter.hasNext( ) )
		{
			IParameterDefnBase param = (IParameterDefnBase) iter.next( );
			//Group section found
			if ( param instanceof IParameterGroupDefn )
			{
				//Get Group Name
				IParameterGroupDefn group = (IParameterGroupDefn) param;
				System.out.println( "Parameter Group: " + group.getName( ) );
				
				//Get the parameters within a group
				Iterator i2 = group.getContents( ).iterator( );
				while ( i2.hasNext( ) )
				{
					IScalarParameterDefn scalar = (IScalarParameterDefn) i2.next( );
					System.out.println("	" + scalar.getName());
				}
				        
			}
			else
			{
				//Parameters are not in a group
				IScalarParameterDefn scalar = (IScalarParameterDefn) param;
				paramList = (paramList.equals(""))? scalar.getName(): paramList + "," + scalar.getName(); 				
			}
		}
				
		task.close();
		return paramList;		
	}
	
	private void destroy()
	{
		LOG.info("TBitsReportEngine : destroy called.");
		if(engine != null)
		{
			engine.destroy();
		}
	}
	protected void finalize() throws Throwable 
	{
		LOG.info("TBitsReportEngine : finalize called.");
	    try 
	    {
	    	this.destroy();
	    } finally {
	        super.finalize();
	    }
	}
	
	/**
	 * Generates any file supported by birt and given in the renderOptions
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param renderOptions : these are the Birt based IRenderOption where you can set the output file/ stream and type of file 
	 * 						  and any other handler required.
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @return			: void
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */
	public void generateReportFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, IRenderOption renderOptions) throws TBitsException
	{
		try
		{
			IReportRunnable design  = openReportDesign(reportFileName);
			
			IRunAndRenderTask task = createRunAndRenderTask(design);
			
			if( null != reportVariables )
				task.setAppContext(reportVariables);
			
			if( null != reportParams )
			{
				//Set parameter values
				task.setParameterValues(reportParams);
			}
			
			task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader()); 
			
			//Setting this to true removes html and body tags
			task.setRenderOption(renderOptions);
			task.run();
			task.close();
		}
		catch(TBitsException te)
		{
			te.printStackTrace();
			LOG.error("",(te));
			throw te;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error("",(e));
			throw new TBitsException(e.getMessage());
		}		

	}
	
	/**
	 * Generates either PDF or HTML and directs the output to the given non-null OutputStream for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputStream : the specific OutputStream to which the data has to be written. If null then an error is thrown.
	 * @param fileType	: one of the two constants defined FILE_TYPE_PDF / FILE_TYPE_HTML for the respective file
	 * @return			: returns the OutputStream object which streams the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */
	public OutputStream generateReportFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, OutputStream outputStream, String fileType) throws TBitsException
	{
		try
		{
			IRenderOption renderOptions = null;
			if( fileType.equalsIgnoreCase(FILE_TYPE_PDF))
			{
				renderOptions = new PDFRenderOption();
				renderOptions.setOutputFormat( HTMLRenderOption.OUTPUT_FORMAT_PDF );			
			}
			else if(fileType.equalsIgnoreCase(FILE_TYPE_HTML))
			{
				renderOptions = new HTMLRenderOption();
				renderOptions.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
				((HTMLRenderOption) renderOptions).setEmbeddable(false);
			}
			else
			{	
				renderOptions = new PDFRenderOption();
				renderOptions.setOutputFormat( FILE_TYPE_DOC );
				
			}

			renderOptions.setOutputStream(outputStream);

			generateReportFile(reportFileName, reportVariables, reportParams, renderOptions);
			
			return outputStream;
		}
		catch(TBitsException te)
		{
			te.printStackTrace();
			LOG.error("",(te));
			throw te;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error("",(e));
			throw new TBitsException(e.getMessage());
		}		
	}

	/**
	 * Generates either PDF and directs the output to the given non-null OutputStream for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputStream : the specific OutputStream to which the data has to be written. If null then an error is thrown.
	 * @param fileType	: one of the two constants defined FILE_TYPE_PDF / FILE_TYPE_HTML for the respective file
	 * @return			: returns the OutputStream object which streams the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */
	public OutputStream generatePDFFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, OutputStream outputStream) throws TBitsException
	{
		return generateReportFile(reportFileName, reportVariables, reportParams, outputStream, FILE_TYPE_PDF);
	}
	
	/**
	 * Generates either HTML and directs the output to the given non-null OutputStream for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputStream : the specific OutputStream to which the data has to be written. If null then an error is thrown.
	 * @param fileType	: one of the two constants defined FILE_TYPE_PDF / FILE_TYPE_HTML for the respective file
	 * @return			: returns the OutputStream object which streams the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */
	public OutputStream generateHTMLFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, OutputStream outputStream) throws TBitsException
	{
		return generateReportFile(reportFileName, reportVariables, reportParams, outputStream, FILE_TYPE_HTML);
	}
	/**
	 * Generates either Doc and directs the output to the given non-null OutputStream for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputStream : the specific OutputStream to which the data has to be written. If null then an error is thrown.
	 * @param fileType	: one of the two constants defined FILE_TYPE_PDF / FILE_TYPE_HTML for the respective file
	 * @return			: returns the OutputStream object which streams the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */
	public OutputStream generateDOCFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, OutputStream outputStream) throws TBitsException
	{
		return generateReportFile(reportFileName, reportVariables, reportParams, outputStream, FILE_TYPE_DOC);
	}
	
	/**
	 * Generates either PDF of HTML File for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputFile : the specific file to which the data has to be written. If null then a temporary file will be created.
	 * 						If not null and the file does not exists then it will be created.	
	 * @param fileType	: Extension of the file type i.e (.doc,.pdf,.html)
	 * @return			: returns the file Object which contains the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */
	public File generateReportFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, File outputFile, String fileType) throws TBitsException
	{
		try
		{
			if( outputFile == null )
			{
				outputFile = File.createTempFile(reportFileName + "output",( "." + (fileType) ) );
			}
			else if( !outputFile.exists() )
				outputFile.createNewFile();
			
			IRenderOption renderOptions = null;
			if( fileType.equalsIgnoreCase(FILE_TYPE_PDF))
			{
				renderOptions = new PDFRenderOption();
				renderOptions.setOutputFormat( HTMLRenderOption.OUTPUT_FORMAT_PDF );
				
			
			}
			else if(fileType.equalsIgnoreCase(FILE_TYPE_HTML))
			{
				renderOptions = new HTMLRenderOption();
				renderOptions.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
				((HTMLRenderOption) renderOptions).setEmbeddable(false);
			}
			else
			{	
				renderOptions = new PDFRenderOption();
				renderOptions.setOutputFormat( FILE_TYPE_DOC );
				
			}	

	        renderOptions.setOutputFileName(outputFile.getAbsolutePath());
			
			generateReportFile(reportFileName, reportVariables, reportParams, renderOptions);
	        return outputFile;
		}
		catch(TBitsException te)
		{
			te.printStackTrace();
			LOG.error("",(te));
			throw te;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error("",(e));
			throw new TBitsException(e.getMessage());
		}		
	}

	/**
	 * Generates either PDF File for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputFile : the specific file to which the data has to be written. If null then a temporary file will be created.
	 * 						If not null and the file does not exists then it will be created.	
	 * @return			: returns the file Object which contains the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */

	public File generatePDFFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, File outputFile) throws TBitsException
	{
		return generateReportFile(reportFileName, reportVariables, reportParams, outputFile, FILE_TYPE_PDF);
	}
	
	/**
	 * Generates either HTML File for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputFile : the specific file to which the data has to be written. If null then a temporary file will be created.
	 * 						If not null and the file does not exists then it will be created.	
	 * @return			: returns the file Object which contains the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */

	public File generateHTMLFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, File outputFile) throws TBitsException
	{
		return generateReportFile(reportFileName, reportVariables, reportParams, outputFile, FILE_TYPE_HTML);
	}
	/**
	 * Generates Word File for the given report.
	 * @param reportFileName : name of the rptdesign file used. It must be present in the tbitsreports folder of tBits.
	 * @param reportVariables : these variable are passed to the task's app-context.
	 * 							Should contain any Java Object or JavaScript variables used in the report rptdesign
	 * @param reportParams : these are ReportParameters used in the report which are usually used in any db-query of the report
	 * @param outputFile : the specific file to which the data has to be written. If null then a temporary file will be created.
	 * 						If not null and the file does not exists then it will be created.	
	 * @return			: returns the file Object which contains the report
	 * @throws TBitsException : all the exception's will be wrapped in TBitsException.
	 */

	public File generateDOCFile(String reportFileName, Map<Object,Object> reportVariables, Map<String,Object> reportParams, File outputFile) throws TBitsException
	{
		return generateReportFile(reportFileName, reportVariables, reportParams, outputFile, FILE_TYPE_DOC);
	}

	private File getPDFReport(String rptDocPath, String pdfFilePath ) throws EngineException, IOException
	{
		File outputFile = new File(pdfFilePath);
		if( outputFile.exists() == false )
			outputFile.createNewFile();
		IReportDocument rptDoc = engine.openReportDocument(rptDocPath);
		IRenderTask task = engine.createRenderTask(rptDoc);
		//Set parent classloader report engine
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader()); 
		
		HTMLRenderOption options = new HTMLRenderOption( );
        options.setOutputFormat( HTMLRenderOption.OUTPUT_FORMAT_PDF );

        options.setOutputFileName(outputFile.getAbsolutePath());
		
		//Setting this to true removes html and body tags
		options.setEmbeddable(true);

		task.setRenderOption(options);
		
		//run and render report
		try 
		{
			task.render();
			if(task.getStatus() != task.STATUS_SUCCEEDED)
				throw new EngineException("The task didnt succeed.");
		} catch (EngineException e) 
		{		
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		task.close();
		
		return outputFile; 

	}
}
