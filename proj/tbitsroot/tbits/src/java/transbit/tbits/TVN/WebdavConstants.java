package transbit.tbits.TVN;

import static transbit.tbits.Helper.TBitsPropEnum.KEY_ATTACHMENTDIR;
import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;

/**
 * 
 * @author Abhishek Agarwal
 *
 */
public interface WebdavConstants {

    // -------------------------------------------------------------- Constants


    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_PROPFIND = "PROPFIND";
    public static final String METHOD_PROPPATCH = "PROPPATCH";
    public static final String METHOD_MKCOL = "MKCOL";
    public static final String METHOD_COPY = "COPY";
    public static final String METHOD_MOVE = "MOVE";
    public static final String METHOD_LOCK = "LOCK";
    public static final String METHOD_UNLOCK = "UNLOCK";
    public static final String METHOD_REPORT = "REPORT";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_CHECKOUT = "CHECKOUT";
	public static final String METHOD_MKACTIVITY = "MKACTIVITY";
	public static final String METHOD_OPTIONS = "OPTIONS";
	public static final String METHOD_DELETE = "DELETE";
	public static final String METHOD_MERGE = "MERGE";
    
    public static final String DEFAULT_NAMESPACE_URI = "DAV:";
    
//    public static String ourAttachmentLocation = Configuration.findAbsolutePath(PropertiesHandler.getProperty(KEY_ATTACHMENTDIR));

    /*-------------- Http Headers ----------------- */
   
    public static final String SVN_OPTIONS_HEADER = "X-SVN-Options";
    public static final String DESTINATION_HEADER = "Destination";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CACHE_CONTROL = "Cache-Control";
	public static final String LOCATION = "Location";
	public static final String TEXT_MD5 = "X-SVN-Result-Fulltext-MD5";
	public static final String SVN_VERSION_NAME = "X-SVN-Version-Name";
	public static final String SVN_CREATION_DATE = "X-SVN-Creation-Date";
	public static final String SVN_LOCK_OWNER = "X-SVN-Lock-Owner";
	public static final String LOCK_TOKEN = "Lock-Token";
	public static final String IF_HEADER = "If";
    /**
     * Default depth is infite.
     */
    public static final int INFINITY = 10; // To limit tree browsing a bit


    /**
     * PROPFIND - Specify a property mask.
     */
    public static final int FIND_BY_PROPERTY = 0;


    /**
     * PROPFIND - Display all properties.
     */
    public static final int FIND_ALL_PROP = 1;


    /**
     * PROPFIND - Return property names.
     */
    public static final int FIND_PROPERTY_NAMES = 2;


    /**
     * Create a new lock.
     */
    public static final int LOCK_CREATION = 0;


    /**
     * Refresh lock.
     */
    public static final int LOCK_REFRESH = 1;


    /**
     * Default lock timeout value.
     */
    public static final int DEFAULT_TIMEOUT = 3600;


    /**
     * Maximum lock timeout.
     */
    public static final int MAX_TIMEOUT = 604800;
	public static final String SVN_SHORT_URL = "!svn";
	public static final String SVN_URL_VCC = SVN_SHORT_URL + "/vcc";
	public static final String SVN_URL_VER = SVN_SHORT_URL + "/ver";
	public static final String SVN_URL_WRK = SVN_SHORT_URL + "/wrk";
	public static final String SVN_URL_BLN = SVN_SHORT_URL + "/bln";
	public static final String SVN_URL_BC = SVN_SHORT_URL + "/bc";
	public static final String SVN_URL_ACT = SVN_SHORT_URL + "/act";
	public static final String SVN_URL_WBL = SVN_SHORT_URL + "/wbl";
	
//	public static final String TEMP_PATH = 
//		Configuration.findAbsolutePath
//												(PropertiesHandler.getProperty(TBitsPropEnum.KEY_TMPDIR));
	public static final String ACT_ID = "actId";
	public static final String CORRUPT_WINDOW = "Window is corrupt";
	
	public static final String NO_MERGE_RESPONSE = "no-merge-response";
	public static final String TARGET_LOCATION = "targetLocation";
	public static final String LOG_REPORT = "log-report";
	public static final String PATH_PREFIX = "path:";
	public static final String FILE_ACTION = "fileAction";
	public static final String FILE_ADDED  = "A";
	public static final String FILE_MODIFIED  = "M";
	public static final String FILE_DELETED  = "D";
	
	public static final String ACTION_TYPE  = "action_type";
	public static final String ADD_REQUEST = "add-request";
	public static final String UPDATE_REQUEST = "update-request";
	public static final int FILE_SIZE_FOR_TRANSFER = 1024*1024;
	public static final String VERSION_NUM = "version-number";
	public static final CharSequence AUTH_SCHEME = "Basic";
	public static final String AUTH_REALM = "Subversion repositories";
	
	/* ----------------------- All SQL TABLES, COLUMNS AND QUERIES MUST BE PLACED HERE --------------*/
	
	public static final String TBL_VERSIONS = "versions";
	public static final String TBL_LOCKS = "locks";
	public static final String TBL_REQUESTS = "requests";
	
	public static final String COL_VERSIONS_SYS_ID = "sys_id";
	public static final String COL_VERSIONS_REQ_ID = "request_id";
	public static final String COL_VERSIONS_VER_NUM = "version_no";
	public static final String COL_VERSIONS_ATTACHMENT = "attachment";
	public static final String COL_VERSIONS_ACTION = "action_id";
	
	public static final String COL_LOCKS_TOKEN = "token";
	public static final String COL_LOCKS_SCOPE = "scope";
	public static final String COL_LOCKS_PATH = "path";
	public static final String COL_LOCKS_TYPE = "type";
	public static final String COL_LOCKS_COMMENT = "comment";
	public static final String COL_LOCKS_OWNER = "owner";
	public static final String COL_LOCKS_DEPTH = "depth";
	public static final String COL_LOCKS_CREATION = "creation_date";
	
	public static final String SELECT_ACTION_BY_SYSTEM_REQUEST_VERSION_ATTACHMENT 
									=	"SELECT action_id from versions " +
									"where sys_id = ? " +
									"and request_id = ? " +
									"and version_no <= ? " +
									"and attachment = ? ";
	
	public static final String SELECT_ATTACHMENT_AND_ACTION_AT_VERSION = 
					"select X.attachment,X.action_id " +
					"from (SELECT  attachment,max(action_id) action_id " +
							"from versions where sys_id = ? and request_id = ? " +
							"and version_no <= ? GROUP BY attachment)X" +
					",versions V where V.action_id=X.action_id and V.attachment = X.attachment " +
					"and V.sys_id = ? and V.request_id = ? and not (V.file_action = 'D')";
	public static final String TVN_NAME = "tvn_name";
	public static final int MAX_TVN_NAME_LENGTH = 15;
	
	

	


}
