package transbit.tbits.common;

import static transbit.tbits.Helper.TBitsConstants.PKG_SCHEDULER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import transbit.tbits.scheduler.ICSVImportConfig;

public class CSVImportConfig implements ICSVImportConfig {
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SCHEDULER);
	
	protected String csvFilePath;
	protected FileInputStream inputFileStream;

	protected String logFileDirectoryPath;
	protected FileOutputStream outputFileStream;
	protected File outputFile;

	protected String uniqueColumns;
	ArrayList<String>primaryKeys;

	protected boolean headerAvailable;

	protected boolean columnEqualsFieldName;

	protected String columnsOrderString;
	protected ArrayList<String> columnsOrderList;

	protected int noOfHeaderRows;

	protected String columnFieldMapString;
	protected HashMap<String, String> columnFieldMap;


	protected String columnMetaDataString;
	protected HashMap<String, String> columnMetaDataMap;

	protected String skipColumnString;
	ArrayList<String>skipColumnList;


	protected String dateFormat;
	protected boolean singleTransaction;
	protected String sysPrefix;

	protected String loggerUserLogin;
	protected String emailId;
	
	protected String commaToken;



	public String getCommaToken() {
		return commaToken;
	}

	public CSVImportConfig(HashMap<String,String> params){
		csvFilePath = params.get(CMD_IMPORT_FILE_PATH);
		try {
			inputFileStream=new FileInputStream(new File(csvFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            LOG.error(e.getMessage());
		}

		logFileDirectoryPath = params.get(CMD_IMPORT_LOG_FILES_DIRECTORY);
		if(!logFileDirectoryPath.endsWith("/")){
			logFileDirectoryPath+="/";
		}

		String jobName=params.get(JOB_NAME);
		String trimmedJobName = null;
		if(jobName!=null){
			trimmedJobName=Utilities.trimNonVisibleCharacters(jobName);
		}

		Timestamp ts=new Timestamp(System.currentTimeMillis());
		String timeStamp=ts.toCustomFormat("MM-dd-yyyy-HH-mm");


		String outFilePath=logFileDirectoryPath+trimmedJobName+"-"+timeStamp+".csv";

		try {
			outputFile=new File(outFilePath);
			outputFileStream=new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		uniqueColumns = params.get(CMD_IMPORT_UNIQUE_KEY);
		primaryKeys=Utilities.toArrayList(uniqueColumns);

		headerAvailable = new Boolean(params.get(CMD_HEADER_AVAILABLE));

		columnEqualsFieldName=new Boolean(params.get(CMD_COLUMN_EQUALS_FIELD_NAME));

		try {
			noOfHeaderRows = new Integer(params.get(CMD_NO_OF_HEADER_ROWS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage());
		}

		columnsOrderString = params.get(CMD_HEADER_COLUMN_ORDER);
		columnsOrderList=Utilities.toArrayList(columnsOrderString);

		columnMetaDataString = params.get(CMD_COLUMN_METADATA);
		columnMetaDataMap=new HashMap<String, String>();
		ArrayList<String> metaDataTokens=Utilities.toArrayList(columnMetaDataString);
		for(String str:metaDataTokens){
			String[] tokenArray=str.split(":");
			String columnName=tokenArray[0];
			String columnData=tokenArray[1];
			columnMetaDataMap.put(columnName,columnData);
		}
		columnFieldMapString = params.get(CMD_COLUMN_FIELD_MAPPING);
		columnFieldMap=new HashMap<String, String>();
		ArrayList<String> metaTokens=Utilities.toArrayList(columnFieldMapString);
		for(String str:metaTokens){
			String[] tokenArray=str.split(":");
			String columnName=tokenArray[0];
			String FieldName=tokenArray[1];
			columnFieldMap.put(columnName, FieldName);
		}

		skipColumnString=params.get(CMD_COLUMNS_SKIP);
		skipColumnList=Utilities.toArrayList(skipColumnString);

		dateFormat = params.get(CMD_IMPORT_DATE_FORMAT);
		singleTransaction = new Boolean(params.get(CMD_SINGLE_TRANSACTION));
		sysPrefix = params.get(CMD_BA_PREFIX);
		emailId = params.get(CMD_EMAIL_ID);
		loggerUserLogin=params.get(CMD_LOGGER_USERLOGIN);
        commaToken=params.get(CMD_COMMA_TOKEN);
	}

	public ArrayList<String> getSkipColumnList() {
		return skipColumnList;
	}

	public void setSkipColumnList(ArrayList<String> skipColumnList) {
		this.skipColumnList = skipColumnList;
	}

	public String getUniqueColumns() {
		return uniqueColumns;
	}

	public void setUniqueColumns(String uniqueColumns) {
		this.uniqueColumns = uniqueColumns;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public CSVImportConfig() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> getPrimaryKeys() {
		return primaryKeys;
	}

	public void setPrimaryKeys(ArrayList<String> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public String getLoggerUserLogin() {
		return loggerUserLogin;
	}

	public void setLoggerUserLogin(String loggerUserLogin) {
		this.loggerUserLogin = loggerUserLogin;
	}

	public String getLogFileDirectoryPath() {
		return logFileDirectoryPath;
	}

	public void setLogFileDirectoryPath(String logFileDirectoryPath) {
		this.logFileDirectoryPath = logFileDirectoryPath;
	}

	public boolean isColumnEqualsFieldName() {
		return columnEqualsFieldName;
	}

	public void setColumnEqualsFieldName(boolean columnEqualsFieldName) {
		this.columnEqualsFieldName = columnEqualsFieldName;
	}

	public String getCsvFilePath() {
		return csvFilePath;
	}

	public void setCsvFilePath(String csvFilePath) {
		this.csvFilePath = csvFilePath;
	}

	public FileInputStream getInputFileStream() {
		return inputFileStream;
	}

	public void setInputFileStream(FileInputStream inputFileStream) {
		this.inputFileStream = inputFileStream;
	}

	public String getLogFilePath() {
		return logFileDirectoryPath;
	}

	public void setLogFilePath(String logFilePath) {
		this.logFileDirectoryPath = logFilePath;
	}

	public FileOutputStream getOutputFileStream() {
		return outputFileStream;
	}

	public void setOutputFileStream(FileOutputStream outputFileStream) {
		this.outputFileStream = outputFileStream;
	}


	public boolean getHeaderAvailable() {
		return headerAvailable;
	}

	public void setHeaderAvailable(boolean headerAvailable) {
		this.headerAvailable = headerAvailable;
	}

	public String getColumnsOrder() {
		return columnsOrderString;
	}

	public void setColumnsOrder(String columnsOrder) {
		this.columnsOrderString = columnsOrder;
	}

	public ArrayList<String> getColumnsOrderList() {
		return columnsOrderList;
	}

	public void setColumnsOrderList(ArrayList<String> columnsOrderList) {
		this.columnsOrderList = columnsOrderList;
	}

	public int getNoOfHeaderRows() {
		return noOfHeaderRows;
	}

	public void setNoOfHeaderRows(int noOfHeaderRows) {
		this.noOfHeaderRows = noOfHeaderRows;
	}

	public String getColumnFieldMapString() {
		return columnFieldMapString;
	}

	public void setColumnFieldMap(String columnFieldMap) {
		this.columnFieldMapString = columnFieldMap;
	}

	public String getColumnMetaDataString() {
		return columnMetaDataString;
	}

	public void setColumnMetaDataString(String columnMetaData) {
		this.columnMetaDataString = columnMetaData;
	}

	public HashMap<String, String> getColumnMetaDataMap() {
		return columnMetaDataMap;
	}

	public void setColumnMetaDataMap(HashMap<String, String> columnMetaDataMap) {
		this.columnMetaDataMap = columnMetaDataMap;
	}

	public HashMap<String, String> getColumnFieldMap() {
		return columnFieldMap;
	}

	public void setColumnFieldMap(
			HashMap<String, String> fieldColumnMetaData) {
		this.columnFieldMap = fieldColumnMetaData;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public boolean getSingleTransaction() {
		return singleTransaction;
	}

	public void setSingleTransaction(boolean singleTransaction) {
		this.singleTransaction = singleTransaction;
	}

	public String getSysPrefix() {
		return sysPrefix;
	}

	public void setSysPrefix(String sysPrefix) {
		this.sysPrefix = sysPrefix;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}



}