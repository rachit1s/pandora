/**
 * 
 */
package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * Each line in the DB config file can either be a comment-line or and empty-line or a valid db-config-line 
 * db-config-line should have following format.
 * <version_number>:<TYPE>:<value>
 * 
 * where 
 * version_number is the double value for which this entry is valid
 * TYPE : can be either of the string 'class_file' or 'script_file' without the quotes.  any other value of TYPE is invalid and will result in error.
 * value : if( TYPE == class_file ) then the value should be fully qualified class name of a concrete class of #link(transbit.tbits.addons.Configurator). An object of this class will be created and executed.  
 *         if( TYPE == script_file ) the the value should be a the relative path of a file containing valid MSSQL script which will be executed on the current DB.
 * 
 * Comments : a line that starts with a # will be created as a comment line and will be ignored.
 * Configurations of one version will be executed together and in order in which they appear in the config file.
 */
public class Config 
{
	public static final String CLASS_FILE = "class_file";
	public static final String SCRIPT_FILE = "script_file";
	
	private int lineNumber;
	private Double version;
	private String type;
	private String value;
	
	/**
	 * @param lineNumber
	 * @param version
	 * @param type
	 * @param value
	 */
	public Config(int lineNumber, Double version, String type, String value) {
		super();
		this.lineNumber = lineNumber;
		this.version = version;
		this.type = type;
		this.value = value;
	}
	/**
	 * @return the version
	 */
	public Double getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(Double version) {
		this.version = version;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	
	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	/**
	 * @param lineNumber the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Config [lineNumber=" + lineNumber + ", version=" + version
				+ ", type=" + type + ", value=" + value + "]";
	}
}
