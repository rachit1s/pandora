package commons.com.tbitsGlobal.utils.client.domainObjects;

import java.util.HashMap;

import commons.com.tbitsGlobal.utils.client.TbitsModelData;

//pojo for Permission
public class PermissionClient extends TbitsModelData {

	public static final int ADD       = 1;
    public static final int CHANGE    = 2;
    public static final int VIEW      = 4;
    public static final int DISPLAY   = 8;
    public static final int EMAIL_VIEW = 8;
    public static final int D_ACTION  = 16;
    public static final int SEARCH    = 32;
    public static final int SET       = 64;
    public static final int HYPERLINK = 128;
    public static final int IS_REQUEST_UNIQUE = 256;
    public static final int IS_ACTION_UNIQUE = 512;
    
	public static String[] FIELD_CONTROLS = {"Add","View","Change","Display","D-action","Search","Carryover","Hyperlink","Action-Unique", "Request-Unique"};

	public static HashMap< String, Integer> PERMISSIONMAP = new HashMap<String, Integer>();
	static{
		PERMISSIONMAP.put(FIELD_CONTROLS[0], ADD);
		PERMISSIONMAP.put(FIELD_CONTROLS[1], VIEW);
		PERMISSIONMAP.put(FIELD_CONTROLS[2], CHANGE);
		PERMISSIONMAP.put(FIELD_CONTROLS[3], DISPLAY);
		PERMISSIONMAP.put(FIELD_CONTROLS[4], D_ACTION);
		PERMISSIONMAP.put(FIELD_CONTROLS[5], SEARCH);
		PERMISSIONMAP.put(FIELD_CONTROLS[6], SET);
		PERMISSIONMAP.put(FIELD_CONTROLS[7], HYPERLINK);
		PERMISSIONMAP.put(FIELD_CONTROLS[8], IS_ACTION_UNIQUE);
		PERMISSIONMAP.put(FIELD_CONTROLS[9], IS_REQUEST_UNIQUE);
	}
	// default constructor
	public PermissionClient() {
		super();
	}

}