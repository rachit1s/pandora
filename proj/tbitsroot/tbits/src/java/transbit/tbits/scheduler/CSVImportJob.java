package transbit.tbits.scheduler;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import transbit.tbits.common.CSVImportConfig;
import transbit.tbits.common.CSVImportProcessor;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.scheduler.ui.ITBitsJob;
import transbit.tbits.scheduler.ui.JobParameter;
import transbit.tbits.scheduler.ui.ParameterType;

public class CSVImportJob implements ITBitsJob,ICSVImportConfig {

	// Application logger.
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Customized CSV Import";
	}

	@Override
	public Hashtable<String, JobParameter> getParameters() throws SQLException {
		// TODO Auto-generated method stub
		Hashtable<String, JobParameter> params =new Hashtable<String, JobParameter>();
		JobParameter param;

		param = new JobParameter();
		param.setName(CMD_BA_PREFIX);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_BA_PREFIX, param);

		param = new JobParameter();
		param.setName(CMD_LOGGER_USERLOGIN);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_BA_PREFIX, param);

		param = new JobParameter();
		param.setName(CMD_EMAIL_ID);
		param.setType(ParameterType.Text);
		params.put(CMD_EMAIL_ID, param);
		param.setMandatory(true);

		param = new JobParameter();
		param.setName(CMD_SINGLE_TRANSACTION);
		param.setType(ParameterType.CheckBox);
		param.setMandatory(true);
		params.put(CMD_SINGLE_TRANSACTION, param);


		param = new JobParameter();
		param.setName(CMD_HEADER_AVAILABLE);
		param.setMandatory(true);
		param.setType(ParameterType.CheckBox);
		params.put(CMD_HEADER_AVAILABLE, param);

		//if no of rows 2  first row is treated as column name
		//if no of rows 1  first row is treated as column name
		param = new JobParameter();
		param.setName(CMD_NO_OF_HEADER_ROWS);
		param.setType(ParameterType.Text);
		params.put(CMD_NO_OF_HEADER_ROWS, param);

		param = new JobParameter();
		param.setName(CMD_COLUMN_EQUALS_FIELD_NAME);
		param.setType(ParameterType.CheckBox);
		params.put(CMD_COLUMN_EQUALS_FIELD_NAME, param);

		param = new JobParameter();
		param.setName(CMD_COLUMN_FIELD_MAPPING);
		param.setType(ParameterType.Text);
		params.put(CMD_COLUMN_FIELD_MAPPING, param);

		param = new JobParameter();
		param.setName(CMD_COLUMN_METADATA);
		param.setType(ParameterType.Text);
		params.put(CMD_COLUMN_METADATA, param);

		param = new JobParameter();
		param.setName(CMD_HEADER_COLUMN_ORDER);
		param.setType(ParameterType.Text);
		params.put(CMD_HEADER_COLUMN_ORDER, param);

		param = new JobParameter();
		param.setName(CMD_IMPORT_DATE_FORMAT);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_IMPORT_DATE_FORMAT, param);

		param = new JobParameter();
		param.setName(CMD_IMPORT_FILE_PATH);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_IMPORT_FILE_PATH, param);

		param = new JobParameter();
		param.setName(CMD_IMPORT_LOG_FILES_DIRECTORY);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_IMPORT_LOG_FILES_DIRECTORY, param);

		param = new JobParameter();
		param.setName(CMD_IMPORT_UNIQUE_KEY);
		param.setType(ParameterType.Text);
		param.setMandatory(true);
		params.put(CMD_IMPORT_UNIQUE_KEY, param);

		param = new JobParameter();
		param.setName(CMD_COMMA_TOKEN);
		param.setType(ParameterType.Text);
		params.put(CMD_COMMA_TOKEN, param);


		return params;
	}

	@Override
	public boolean validateParams(Hashtable<String, String> params)
	throws IllegalArgumentException {
		String csvFilePath=params.get(CMD_IMPORT_FILE_PATH);
		String	logFilePathDirectory=params.get(CMD_IMPORT_LOG_FILES_DIRECTORY);
		String	uniqueColumn=params.get(CMD_IMPORT_UNIQUE_KEY);
		String	headerAvailable=params.get(CMD_HEADER_AVAILABLE);
		String	columnsOrder=params.get(CMD_HEADER_COLUMN_ORDER);
		String	noOfHeaderRows=params.get(CMD_NO_OF_HEADER_ROWS);
		String	columnFieldMap=params.get(CMD_COLUMN_FIELD_MAPPING);
		String	columnMetaData=params.get(CMD_COLUMN_METADATA);
		String	dateFormat=params.get(CMD_IMPORT_DATE_FORMAT);
		String	singleTransaction=params.get(CMD_SINGLE_TRANSACTION);
		String	sysPrefix=params.get(CMD_BA_PREFIX);
		String	emailId=params.get(CMD_EMAIL_ID);
		String  columnFieldNameSame=params.get(CMD_COLUMN_EQUALS_FIELD_NAME);



		if("".equals(headerAvailable))
			throw new IllegalArgumentException("specify Availibility of header");

		if(headerAvailable.equalsIgnoreCase("true")|| headerAvailable.equals("1"))
		{
			if(noOfHeaderRows.equals(""))
			{
				throw new IllegalArgumentException("specify no of header rows");
			}
		}else
		{
			if(columnsOrder.equals(""))
				throw new IllegalArgumentException("specify columnsOrder");
			if(columnFieldNameSame.equalsIgnoreCase("false")||columnFieldNameSame.equalsIgnoreCase("0"))
				if(columnFieldMap==null||columnFieldMap.equals(""))
				{
					throw new IllegalArgumentException("specify columnFieldMap");
				}
		}
		return true;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		JobDetail jd=arg0.getJobDetail();
		try {

			JobDataMap jdm=jd.getJobDataMap();
			System.out.println(jdm.getKeys());
			HashMap<String,String> importParams = new HashMap<String,String>();
			
			String noOfHeaderRows = jdm.getString(CMD_NO_OF_HEADER_ROWS);
			String columnsOrderString = jdm.getString(CMD_HEADER_COLUMN_ORDER);
			String columnFieldMapString = jdm.getString(CMD_COLUMN_FIELD_MAPPING);
			String columnMetaDataString = jdm.getString(CMD_COLUMN_METADATA);
			String skipColumnString=jdm.getString(CMD_COLUMNS_SKIP);
			String dateFormat = jdm.getString(CMD_IMPORT_DATE_FORMAT);
			String csvFilePath = jdm.getString(CMD_IMPORT_FILE_PATH);
			String logFileDirectoryPath = jdm.getString(CMD_IMPORT_LOG_FILES_DIRECTORY);
			String uniqueColumns = jdm.getString(CMD_IMPORT_UNIQUE_KEY);
			String sysPrefix = jdm.getString(CMD_BA_PREFIX);
			String emailId = jdm.getString(CMD_EMAIL_ID);
			String loggerUserLogin=jdm.getString(CMD_LOGGER_USERLOGIN);
			String commaToken=jdm.getString(CMD_COMMA_TOKEN);

			Boolean ha = jdm.getBoolean(CMD_HEADER_AVAILABLE);
			String headerAvailable=null;
			if(ha!=null){
				headerAvailable=Boolean.toString(ha);
			}
			Boolean cefn=jdm.getBoolean(CMD_COLUMN_EQUALS_FIELD_NAME);
			String columnEqualsFieldName=null;
			if(cefn!=null){
				columnEqualsFieldName=Boolean.toString(cefn);
			}


			Boolean st = jdm.getBoolean(CMD_SINGLE_TRANSACTION);
			String singleTransaction=null;
			if(cefn!=null){
				singleTransaction=Boolean.toString(st);
			}

			importParams.put(CMD_HEADER_COLUMN_ORDER,columnsOrderString );
			importParams.put(CMD_COLUMN_FIELD_MAPPING,columnFieldMapString );
			importParams.put(CMD_COLUMN_METADATA,columnMetaDataString );
			importParams.put(CMD_COLUMNS_SKIP,skipColumnString);
			importParams.put(CMD_IMPORT_DATE_FORMAT,dateFormat );
			importParams.put(CMD_IMPORT_FILE_PATH,csvFilePath );
			importParams.put(CMD_IMPORT_LOG_FILES_DIRECTORY,logFileDirectoryPath );
			importParams.put(CMD_IMPORT_UNIQUE_KEY,uniqueColumns );
			importParams.put(CMD_BA_PREFIX,sysPrefix );
			importParams.put(CMD_EMAIL_ID,emailId );
			importParams.put(CMD_LOGGER_USERLOGIN,loggerUserLogin);
			importParams.put(CMD_HEADER_AVAILABLE,headerAvailable );
			importParams.put(CMD_COLUMN_EQUALS_FIELD_NAME,columnEqualsFieldName);
			importParams.put(CMD_SINGLE_TRANSACTION,singleTransaction );
			importParams.put(CMD_NO_OF_HEADER_ROWS,noOfHeaderRows);
			importParams.put(CMD_COMMA_TOKEN,commaToken);



			new CSVImportProcessor(new CSVImportConfig(importParams)).operate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
