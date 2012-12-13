package corrGeneric.com.tbitsGlobal.server.util;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.PluginManager;
import transbit.tbits.report.TBitsReportEngine;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportIdPlugin;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportJavaObject;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.CorrPluginManager;
import corrGeneric.com.tbitsGlobal.server.managers.ReportManager;
import corrGeneric.com.tbitsGlobal.server.managers.ReportNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.ReportParamsManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ReportParamEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class GenPDFUtil 
{
	public static File generateAndGetFile(Connection con,CorrObject coob, File outputFile ) throws CorrException
	{
		ArrayList<ReportEntry> entries = ReportManager.getReportMapFromCache(coob.getBa().getSystemPrefix());
		
		ReportEntry myReportEntry = null ;
		Integer myReportId = null ;
		if( null != entries )
		{
			for( ReportEntry re : entries )
			{
				if( (re.getType1() == null && coob.getReportType1() == null) || 
					( re.getType1() != null && coob.getReportType1() != null && re.getType1().equals(coob.getReportType1().getName()) ) 
				  )
				{
					if( (re.getType2() == null && coob.getReportType2() == null) || 
							( re.getType2() != null && coob.getReportType2() != null && re.getType2().equals(coob.getReportType2().getName()) ) 
						  )
						{
						if( (re.getType3() == null && coob.getReportType3() == null) || 
								( re.getType3() != null && coob.getReportType3() != null && re.getType3().equals(coob.getReportType3().getName()) ) 
							  )
							{
							if( (re.getType4() == null && coob.getReportType4() == null) || 
									( re.getType4() != null && coob.getReportType4() != null && re.getType4().equals(coob.getReportType4().getName()) ) 
								  )
								{
								if( (re.getType5() == null && coob.getReportType5() == null) || 
										( re.getType5() != null && coob.getReportType5() != null && re.getType5().equals(coob.getReportType5().getName()) ) 
									  )
									{
										myReportEntry = re;
										break;
									}
								}
							}
						}
				}
			}
		}
		
		Hashtable<String,Object> params = new Hashtable<String,Object>();
		
		if( null != con )
			params.put(IReportIdPlugin.CONNECTION, con);
		if( null != coob )
			params.put(IReportIdPlugin.CORROBJECT, coob);
		if( null != myReportEntry )
			params.put(IReportIdPlugin.REPORTENTRY, myReportEntry);
		
		Integer reportIdFromPlugin = CorrPluginManager.getInstance().executeReportIdPlugins(params);
			
		if( null != reportIdFromPlugin )
			myReportId = reportIdFromPlugin;
		else if( null != myReportEntry )
			myReportId = myReportEntry.getReportId();
		else
		{
			throw new CorrException("ReportId not found. Cannot generate the report.");
		}
		
		ReportNameEntry reportName = ReportNameManager.lookupReportNameEntry(myReportId);
		
		if( null == reportName || null == reportName.getReportFileName() )
			throw new CorrException("Report Name not found. Cannot generate the report.");
		
//		IReportDocument ird = null ;
		TBitsReportEngine tre = null ;
		try
		{
			tre = TBitsReportEngine.getInstance();
			if(tre == null)
			{
				Utility.LOG.info("Unable to get the instance of ReportEngine.");
				throw new CorrException("Generating correspondence file failed.") ;
			}

			HashMap taskParams = new HashMap();
			HashMap reportVariables = new HashMap();
			
			Hashtable<String, ReportParamEntry> reportParams = ReportParamsManager.getReportParamMapFromCache(myReportId);
			
			Hashtable<String,Object> pluginParams = new Hashtable<String,Object>();
			pluginParams.put(IReportParamPlugin.CONNECTION, con);
			pluginParams.put(IReportParamPlugin.CORROBJECT, coob);
			pluginParams.put(IReportParamPlugin.REPORTNAMEENTRY, reportName);
			
			if( null != reportParams )
			{
				for( Enumeration<String> keys = reportParams.keys() ; keys.hasMoreElements() ; )
				{			
					Object pv = null ;
					String key = keys.nextElement() ;
					ReportParamEntry rp = reportParams.get(key) ;
					String paramValueType = rp.getParamValueType() ;
					if( paramValueType.equals(GenericParams.ParamValueType_Const ) )
					{
						pv = rp.getParamValue();
					}
					else if( paramValueType.equals(GenericParams.ParamValueType_JavaClass))
					{
						String className = rp.getParamValue();
						pluginParams.remove(IReportParamPlugin.REPORTPARAMENTRY);
						pluginParams.put(IReportParamPlugin.REPORTPARAMENTRY, rp);
						pv = CorrPluginManager.getInstance().executeReportParamPlugin(pluginParams, className);
					}
					else if( paramValueType.equals(GenericParams.ParamValueType_JavaObject))
					{
						// get the given class, create its object and pass that object in reportVariables
						Class klass = PluginManager.getInstance().findPluginsByClassName(rp.getParamValue());
						if( klass == null )
						{
							throw new CorrException("plugin class : '" + rp.getParamValue() + "' mapped to param = " + rp.getParamName() + " not found.");
						}
						
						Object obj = klass.newInstance();
						if( !(obj instanceof IReportJavaObject) )
						{
							throw new CorrException("The mapping for param_name : " + rp.getParamName() + " was with value " + rp.getParamValue() + ". But it is not instanceof " + IReportJavaObject.class.getName() );
						}
						
						IReportJavaObject rjo = (IReportJavaObject)obj;
						
						Hashtable<String,Object> contextVars = new Hashtable<String,Object>();
						contextVars.put(IReportJavaObject.CORROBJECT, coob);
						contextVars.put(IReportJavaObject.CONNECTION,con);
						contextVars.put(IReportJavaObject.REPORTNAMEENTRY, reportName);
						contextVars.put(IReportJavaObject.REPORTPARAMENTRY, rp);
						
						rjo.initialize(contextVars);
						pv = rjo;
					}
					else
					{
						throw new CorrException("Param Value Type : '" + paramValueType + "' for Param : " + rp.getParamName() + " is not supported.");
					}
					
					if( null == pv )
						throw new CorrException("Cannot find the value of report parameter : " + rp.getParamName());
					
					if( rp.getParamType().equals(GenericParams.ParamType_Variable))
					{
						reportVariables.put(rp.getParamName(),pv) ;
					}
					else
					{
						taskParams.put(rp.getParamName(), pv);
					}
				}
			}
			
//			ird = tre.getReportDocument(reportDesign, taskParams) ;
			
			if( null == outputFile )
			{
				outputFile = File.createTempFile("report_temp_file", ".pdf");
//				outputFile.deleteOnExit();
			}
//			outputFile = tre.getPDFReport(ird,outputFile);
			File pdfFile = tre.generatePDFFile(reportName.getReportFileName(), reportVariables, taskParams, outputFile);
			/////// print file info
			if( pdfFile != null ) 
			{
				Utility.LOG.info( "Name:" + pdfFile.getName() + " path = " + pdfFile.getAbsolutePath() ) ;
				return outputFile ;
			}
			else
			{
				Utility.LOG.info("Output file is null" ) ;
				throw new CorrException("Generating correspondence file failed.") ;
			}
		}
		catch(TBitsException te)
		{
			Utility.LOG.error(TBitsLogger.getStackTrace(te));
			throw new CorrException(te.getDescription());
		}
		catch(CorrException e)
		{
			Utility.LOG.debug(TBitsLogger.getStackTrace(e));
			throw e;
		}
		catch(Exception e)
		{
			System.out.println("stack-trace : " );
			e.printStackTrace();
			Utility.LOG.debug(TBitsLogger.getStackTrace(e));
			throw new CorrException("Error occured while generating PDF. Please consider trying again.\nError Msg : " + e.getMessage());
		}
		catch( Throwable t )
		{
			t.printStackTrace();
			throw new CorrException("Error occured while generating PDF. : " + TBitsLogger.getStackTrace(t));
		}
	}
}
