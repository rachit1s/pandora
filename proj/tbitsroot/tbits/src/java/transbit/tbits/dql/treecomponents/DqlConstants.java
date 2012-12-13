package transbit.tbits.dql.treecomponents;

public interface DqlConstants {

	// DQL Grammar delimiters
	public static final String SELECT = "SELECT";
	public static final String FROM = "FROM";
	public static final String WHERE = "WHERE";
	public final static String HAS_TEXT = "HAS TEXT";
	public static final String ORDER_BY = "ORDER BY";
	public static final String LIMIT = "LIMIT";
	public static final String ASC_ORDER = "ASC";
	public static final String DESC_ORDER = "DESC";
	
	// White Space delimiters
	public static final String WS_SPACE = " ";
	public static final String WS_TAB = "\t";
	public static final String WS_NEWLINE = "\n";
	
	// DQL Expression delimiters
	public static final String NESTING_CLOSE = ")";
	public static final String NESTING_OPEN = "(";
	public static final String STRING_OPEN = "\"";
	public static final String STRING_CLOSE = "\"";
	public static final String OR_DELIM = "OR ";
	public static final String AND_DELIM = "AND ";
	public static final String NOT_DELIM = "NOT ";
	public static final String IN_DELIM = "IN ";
	public static final String IN_NESTING_OPEN = "{";
	public static final String IN_NESTING_CLOSE = "}";
	public static final String FIELDVALUE_DELIM = ":";
	public static final String COMMA_DELIM = ",";
	public static final String DATE_DELIM = "/";
	public static final String ESCAPE_SEQUENCE = "\\";
	
	// DQL Boolean values
	public static final String TRUE_BOOLEAN = "true";
	public static final String YES_BOOLEAN = "yes";
	public static final String FALSE_BOOLEAN = "false";
	public static final String NO_BOOLEAN = "no";
	
	// DQL Null value
	public static final String NULL = "NULL";
	
	// DQL comparators
	public static final String E_COMP = "=";
	public static final String LE_COMP = "<=";
	public static final String GE_COMP = ">=";
	public static final String L_COMP = "<";
	public static final String G_COMP = ">";
	public static final String NE_COMP = "<>";
	
	// Dql Column Names
	public static final String REQUEST_COL = "REQUEST_OBJECT_COL_NAME";

	// Lucene field names
	public static final String ALL_LUCENE = "all";
	public static final String ALL_TEXT_LUCENE = "alltext";
	
	// Enums for Dql
	// Ordering
	public enum Order{
		ASC, DESC
	}
	
	// Supported operators connecting EXPRESSION, CONSTRAINT, VALUE
	public enum Operator {
		AND, OR
	}
	
	// Comparators for PARAMETER
	public enum Comparator {
		L, LE, E, GE, G, NE
	}
	
	// Types of PARAMETER
	public enum ParamType{
		STRING, NUMERIC, DATETIME, BOOLEAN, NULL, UNKNOWN
	}
	
}
