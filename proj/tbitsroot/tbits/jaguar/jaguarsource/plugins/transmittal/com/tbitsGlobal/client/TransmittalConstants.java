package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;

import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.cache.CacheRepository;
import commons.com.tbitsGlobal.utils.client.cache.FieldCache;

public class TransmittalConstants {

	public static final TransmittalServiceAsync dbService = GWT
			.create(TransmittalService.class);

	//public static HashMap<Integer, TbitsTreeRequestData> requests = new HashMap<Integer, TbitsTreeRequestData>();

//	public static HashMap<String, String> approval = new HashMap<String, String>();

//	public static ArrayList<TrnEditableColumns> editableCoumns = new ArrayList<TrnEditableColumns>();
//	public static List<TbitsTreeRequestData> requestList = new ArrayList<TbitsTreeRequestData>();

	public static FieldCache fieldCache = CacheRepository.getInstance()
			.getCache(FieldCache.class);

//	public static TbitsModelData transmittalProcessParams = new TbitsModelData();

	//public static ArrayList<TbitsModelData> attachmentTableColumnsList = new ArrayList<TbitsModelData>();
//
	//public static ArrayList<TbitsModelData> distributionTableColumnsList = new ArrayList<TbitsModelData>();


	//public static ArrayList<BAField> baFieldList = new ArrayList<BAField>();
	public static final String REQUEST_LIST = "requestList";

	public static final String TRN_APPROVAL_CATEGORY_LIST = "trnApprovalCategoryList";

	public static String REVISION_LIST = "revisionList";

	public static String COPY_TYPE_LIST = "copyTypeList";

	public static final String DELIVERABLE_FIELD_NAME = "deliverableFieldName";

	public static final String DRAFTED_BY = "draftedBy";

	public static final String EMAIL_BODY = "emailBody";

	public static final String TRANSMITTAL_SUBJECT = "transmittalSubject";

	public static final String REMARKS = "remarks";

	public static final String ACTUAL_DATE = "actualDate";

	public static final String CC_LIST = "ccList";

	public static final String TO_LIST = "toList";

	public static final String QUANTITY_LIST = "quantityList";

	public static final String REMARKS_LIST = "summaryList";

	static final String PROPERTY_ATTACHMENT_INFO = "attachmentInfo";

	protected static final String COLUMN_ORDER = "column_order";

	protected static final String IS_ACTIVE_COLUMN = "is_active";

	protected static final String IS_EDITABLE = "is_editable";
	// protected static final String IS_EDITABLE = "is_editor_enabled";

	protected static final String RELATED_PROCESS_PARAMETER_COLUMN = "related_process_parameter";

	protected static final String DATA_TYPE_ID_COLUMN = "data_type_id";

	protected static final String FIELD_ID_COLUMN = "field_id";

	protected static final String NAME_COLUMN = "name";

	protected static final String TRN_PROCESS_ID_COLUMN = "trn_process_id";

	protected static final String TYPE_VALUE_SOURCE_COLUMN = "type_value_source";

	public static final int DATE = 2;
	public static final int BOOLEAN = 1;
	public static final int TIME = 3;
	public static final int TEXT = 8;
	public static final int STRING = 7;
	public static final int REAL = 6;
	public static final int INT = 5;
	public static final int DATETIME = 4;
	public static final int TYPE = 9;
	public static final int USERTYPE = 10;
	public static final int ATTACHMENTS = 11;

	protected static final String DRAWING_TABLE_KEY_WORD = "drawingTable";

	protected static final String IS_DEFAULT_TRANSMITTAL_PROCESS = "isDefaultTransmittalProcess";

	public static final String DISTRIBUTION_TABLE = "distributionTable";

	protected static final String FIELD_CONFIG = "field_config";

	protected static final String DEFAULT_VALUE = "default_value";

	protected static final ArrayList<TbitsModelData> trnWizardExtendedFields = new ArrayList<TbitsModelData>();

	protected static final String INTERMEDIATE_TRN_PREFIX = "intermediateTrnPrefix";

	static final String TRANSMITTAL_ID_PREFIX = "transmittal_id_prefix";

	protected static final String USER_INPUT = "{user_input}";

	protected static final String REQUEST_FIELD_PART = "requestFieldPart";

	protected static final String FIELD_NAME_REGEX = "<[a-zA-Z0-9_-]+>";

}
