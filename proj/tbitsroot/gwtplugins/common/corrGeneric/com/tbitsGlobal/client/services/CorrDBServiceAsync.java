package corrGeneric.com.tbitsGlobal.client.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.OnBehalfClient;

public interface CorrDBServiceAsync{

	void getOnBehalfList(String sysPrefix, String userLogin,
			AsyncCallback<ArrayList<OnBehalfClient>> callback);

	void getApplicableBas(AsyncCallback<HashMap<String,? extends Serializable>> callback);

	void getInitializingParams(String sysPrefix, String userLogin,
			AsyncCallback<HashMap<String, Object>> callback);

	void getFieldNameMap(String sysPrefix,
			AsyncCallback<HashMap<String, FieldNameEntry>> callback);

	void getOptionMap(String sysPrefix,
			AsyncCallback<HashMap<String, ProtocolOptionEntry>> callback);

	void getUserMap(String sysPrefix, String userLogin,
			AsyncCallback<ArrayList<UserMapEntry>> callback);

	void getOnBehalfMap(
			String sysPrefix,
			String userLogin,
			AsyncCallback<HashMap<String, HashMap<String, HashMap<String, Collection<String>>>>> callback);

	void getPDFUrl(TbitsTreeRequestData ttrd, AsyncCallback<String> callback);

	void getViewRequestParams(String sysPrefix, AsyncCallback<HashMap<String, Object>> callback);

	void getRequestDataForTransferRequest(String loginUser,
			int fromSysId, int requestId, TbitsTreeRequestData fromTtrd,String transferType,
			String toSysprefix, AsyncCallback<HashMap<String,? extends Serializable >> callback);

	void sendMeEmail(UserClient userClient, String sysPrefix, int reqId,
			AsyncCallback<Void> callback);

	void isPreviewPdfEnable(String CurrentBa,AsyncCallback<Boolean> callback);

}
