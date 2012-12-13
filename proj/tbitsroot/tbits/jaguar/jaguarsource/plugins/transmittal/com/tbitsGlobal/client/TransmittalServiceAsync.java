package transmittal.com.tbitsGlobal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import transbit.tbits.common.DatabaseException;
import transmittal.com.tbitsGlobal.client.models.TrnEditableColumns;
import transmittal.com.tbitsGlobal.server.GenericTransmittalCreator;

import com.google.gson.JsonParseException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsModelData;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public interface TransmittalServiceAsync {

	void getTransmittalForm(String requestIds, String selectedDTNProcess,
			AsyncCallback<Void> callback);

	void getFields(String sysPrefix, AsyncCallback<List<BAField>> callback);

	void getDCRBusinessAreas(AsyncCallback<ArrayList<String>> callback);

	void getTransmittalProcessParameters(int dcrSystemId,
			ArrayList<Integer> dcrRequestList, int transmittalTypeId,
			AsyncCallback<TbitsModelData> callback);

	void getIntegerValue(AsyncCallback<Integer> callback);

	void getHTMLTransmittalPreviewUsingBirt(int dcrSystemId,
			String requestList, String rptDesignFileName, String toAddress,
			String dtnNumber, String subject, String remarks,
			String kindAttentionString,
			ArrayList<String[]> refTransmittalNumbers,
			ArrayList<String[]> drawingsList, String[] approvalCategory,
			String[] documentType, ArrayList<String[]> distributionList,
			String[] loggerInfo, String emailBody, String yourReferenceNumber,
			String transmittalDate, String dtnSerialKey, String toList,
			String ccList, AsyncCallback<String> asyncCallback)
			throws TbitsExceptionClient;

	void getAllTransmittalProcessParametersBySystemId(int dcrSystemId,
			AsyncCallback<ArrayList<TbitsModelData>> callback);

	void getTransmittalDropDownOptions(int aSystemId,
			AsyncCallback<ArrayList<TbitsModelData>> callback);

	void getTransmittalDropDownOption(int aSystemId, int dropdownid,
			AsyncCallback<TbitsModelData> callback);

	void getAttachmentSelectionTableColumns(int trnProcessId,
			AsyncCallback<ArrayList<TbitsModelData>> callback)
			throws TbitsExceptionClient;

	void getHTMLTransmittalPreviewUsingBirt(HashMap<String, String> paramTable,
			AsyncCallback<String> asyncCallback);

	void createTransmittal(
			HashMap<String, String> paramTable,
			HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap,
			AsyncCallback<String> asyncCallback) throws TbitsExceptionClient;

	void getDistributionTableColumns(Integer trnProcessId,
			AsyncCallback<ArrayList<TbitsModelData>> asyncCallback)
			throws TbitsExceptionClient;

	void getTrnExtendedFields(Integer trnProcessId,
			AsyncCallback<ArrayList<TbitsModelData>> asyncCallback)
			throws TbitsExceptionClient;

	void createTransmittalPostApproval(int systemId, int requestId,
			AsyncCallback<String> callback);

	void getValidationRules(int trnProcessId,
			AsyncCallback<ArrayList<TbitsModelData>> asyncCallback);

	void getAllApprovalBASysIds(AsyncCallback<ArrayList<Integer>> asyncCallback);

	void checkUserExistsInRole(int systemId, String roleName, String userLogin,
			AsyncCallback<Boolean> callback);

	void getUserRolesNamesBySysIdAndUserId(int aSystemId, int aUserId,
			AsyncCallback<ArrayList<String>> callback);

	void checkUserExistsInRole(int systemId, int requestId, int userId,
			AsyncCallback<Boolean> callback);

	void checkUserExistsInRole(int systemId, int userId,
			AsyncCallback<Boolean> asyncCallback);

	void getTime(Integer trnProcessId, Integer dcrSysId, Integer targetSysId,
			AsyncCallback<Long> callback);

	void getDataByRequestIds(String sysPrefix, List<Integer> requestIds,
			AsyncCallback<HashMap<Integer, TbitsTreeRequestData>> callback);

	void getDataByRequestId(String sysPrefix, int requestId,
			AsyncCallback<TbitsTreeRequestData> callback);

	void getTansParams(int sysid, int reqid,
			AsyncCallback<HashMap<String, Object>> a);

	void getDefaultTransmittalProcessParams(int sysid,
			AsyncCallback<HashMap<String, Integer>> a);
	
	
	void getEditabeColumnsForTrnWizrad(int systemid,int processId,
			AsyncCallback <List<TrnEditableColumns>>callback)
			throws TbitsExceptionClient;
	
	void getTansParamsBeforeTransmittal(String sysPrefix,int sysid,ArrayList<Integer>requetsId,
			AsyncCallback <HashMap<String,Object>>callback)
			throws TbitsExceptionClient;
	
	void getEditabeAttachmentColumnsForTrnWizrad(int systemid,int processId,
			AsyncCallback <List<TrnEditableColumns>>callback)
			throws TbitsExceptionClient;
	void getValforType(int sysid,int fieldid,String value,String type_value_source,AsyncCallback <String>callback) throws TbitsExceptionClient;
	 void getLoginForName (String name,AsyncCallback <String>callback) throws TbitsExceptionClient;
	 void  fetchArrayListFromJsonArray(String jsonArrayTable,AsyncCallback<ArrayList<String[]>>callback);
	
	 void createTransmittalPostApproval(int systemId, int requestId,HashMap<String, String> paramTable,
				HashMap<Integer, HashMap<String, List<FileClient>>> attachmentInfoClientsMap,AsyncCallback<String>callback)
				throws TbitsExceptionClient;
}
