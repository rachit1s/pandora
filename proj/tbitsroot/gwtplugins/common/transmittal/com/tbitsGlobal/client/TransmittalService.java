package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import transbit.tbits.common.DatabaseException;
import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;
import transmittal.com.tbitsGlobal.server.GenericTransmittalCreator;

import com.google.gson.JsonParseException;
import com.google.gwt.user.client.rpc.RemoteService;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public interface TransmittalService extends RemoteService {
	void getTransmittalForm(String requestIds, String selectedDTNProcess);

	ArrayList<TbitsModelData> getTransmittalDropDownOptions(int aSystemId)
			throws TbitsExceptionClient;

	TbitsModelData getTransmittalDropDownOption(int aSystemId, int dropdownid)
			throws TbitsExceptionClient;

	ArrayList<String> getDCRBusinessAreas() throws TbitsExceptionClient;

	ArrayList<TbitsModelData> getAllTransmittalProcessParametersBySystemId(
			int dcrSystemId) throws TbitsExceptionClient;

	List<BAField> getFields(String sysPrefix) throws TbitsExceptionClient;

	Integer getIntegerValue();

	String getHTMLTransmittalPreviewUsingBirt(int dcrSystemId,
			String requestList, String rptDesignFileName, String toAddress,
			String dtnNumber, String subject, String remarks,
			String kindAttentionString,
			ArrayList<String[]> refTransmittalNumbers,
			ArrayList<String[]> drawingsList, String[] approvalCategory,
			String[] documentType, ArrayList<String[]> distributionList,
			String[] loggerInfo, String emailBody, String yourReferenceNumber,
			String transmittalDate, String dtnSerialKey, String toList,
			String ccList) throws TbitsExceptionClient;

	TbitsModelData getTransmittalProcessParameters(int dcrSystemId,
			ArrayList<Integer> dcrRequestList, int transmittalTypeId)
			throws TbitsExceptionClient;

	ArrayList<TbitsModelData> getAttachmentSelectionTableColumns(
			int trnProcessId) throws TbitsExceptionClient;

	String getHTMLTransmittalPreviewUsingBirt(HashMap<String, String> paramTable)
			throws TbitsExceptionClient;

	String createTransmittal(
			HashMap<String, String> paramTable,
			HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap)
			throws TbitsExceptionClient;

	ArrayList<TbitsModelData> getDistributionTableColumns(Integer trnProcessId)
			throws TbitsExceptionClient;

	ArrayList<TbitsModelData> getTrnExtendedFields(Integer trnProcessId)
			throws TbitsExceptionClient;

	// ArrayList<TbitsModelData> getAppCycleTransientData(int currentSysId,
	// Integer requestId) throws TbitsExceptionClient;
	String createTransmittalPostApproval(int systemId, int requestId)
			throws TbitsExceptionClient;

	ArrayList<TbitsModelData> getValidationRules(int trnProcessId)
			throws TbitsExceptionClient;

	ArrayList<Integer> getAllApprovalBASysIds() throws TbitsExceptionClient;

	boolean checkUserExistsInRole(int systemId, String roleName,
			String userLogin) throws TbitsExceptionClient;

	ArrayList<String> getUserRolesNamesBySysIdAndUserId(int aSystemId,
			int aUserId) throws TbitsExceptionClient;

	boolean checkUserExistsInRole(int systemId, int requestId, int userId)
			throws TbitsExceptionClient;

	boolean checkUserExistsInRole(int systemId, int userId)
			throws TbitsExceptionClient;

	public HashMap<String, Object> getTansParams(int sysid, int reqid)
			throws TbitsExceptionClient, NumberFormatException;

	public String getValforType(int sysid,int fieldid,String value,String type_value_source) throws TbitsExceptionClient;
	/**
	 * Testing for cache
	 */

	Long getTime(Integer trnProcessId, Integer dcrSysId, Integer targetSysId);

	TbitsTreeRequestData getDataByRequestId(String sysPrefix, int requestId)
			throws TbitsExceptionClient;

	List<TrnEditableColumns> getEditabeColumnsForTrnWizrad(int systemid,int processId)
			throws TbitsExceptionClient;
	List<TrnEditableColumns> getEditabeAttachmentColumnsForTrnWizrad(int systemid,int processId)
	throws TbitsExceptionClient;

	HashMap<Integer, TbitsTreeRequestData> getDataByRequestIds(
			String sysPrefix, List<Integer> requestIds);
	 String getLoginForName (String name) throws TbitsExceptionClient;
	 
	 public HashMap<String, Object> getTansParamsBeforeTransmittal(String sysPrefix,int sysid,ArrayList<Integer>requetsId)
		throws TbitsExceptionClient, NumberFormatException;
	 
	 public HashMap<String, Integer> getDefaultTransmittalProcessParams(int sysid)
		throws TbitsExceptionClient, NumberFormatException;
		
	 ArrayList<String[]> fetchArrayListFromJsonArray(String jsonArrayTable);
	 String createTransmittalPostApproval(int systemId, int requestId,HashMap<String, String> paramTable,
				HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap)
				throws TbitsExceptionClient;		
}
