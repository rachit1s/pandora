package transbit.tbits.scheduler;

public interface ICSVImportConfig {
	public static String CMD_IMPORT_FILE_PATH="csvFilePath";
	public static String CMD_IMPORT_LOG_FILES_DIRECTORY="logFileDirectoryPath";
	public static String CMD_IMPORT_UNIQUE_KEY="uniqueColumn";
	public static String CMD_HEADER_AVAILABLE="headerAvailable";
	public static String CMD_HEADER_COLUMN_ORDER="columnsOrder";
	public static String CMD_COLUMN_EQUALS_FIELD_NAME="columnEqualsFieldName";
	public static String CMD_NO_OF_HEADER_ROWS="noOfHeaderRows";
	public static String CMD_COLUMN_FIELD_MAPPING="columnFieldMap";
	public static String CMD_COLUMN_METADATA="columnMetaData";
	public static String CMD_IMPORT_DATE_FORMAT="dateFormat";
	public static String CMD_SINGLE_TRANSACTION="singleTransaction";
	public static String CMD_BA_PREFIX="sysPrefix";
	public static String CMD_EMAIL_ID="emailId";
	public static String CMD_LOGGER_USERLOGIN = "loggerUserLogin";
	public static String CMD_COLUMNS_SKIP="skipColumns";
	public static String JOB_NAME = "jobName";
	public static String CMD_COMMA_TOKEN="commaToken";

}