package corrGeneric.com.tbitsGlobal.client.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.domainObjects.UserClient;

import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.OnBehalfClient;

public interface CorrDBService extends RemoteService 
{
	ArrayList<OnBehalfClient> getOnBehalfList( String sysPrefix, String userLogin) throws Exception;
	HashMap<String,? extends Serializable> getApplicableBas() throws Exception;
	HashMap<String,Object> getInitializingParams( String sysPrefix, String userLogin) throws Exception;
	HashMap<String,FieldNameEntry> getFieldNameMap ( String sysPrefix ) throws Exception;
	HashMap<String,ProtocolOptionEntry> getOptionMap( String sysPrefix ) throws Exception;
	ArrayList<UserMapEntry> getUserMap( String sysPrefix, String userLogin) throws Exception;
//	ClientUtility.getOnBehalfMap(map)
	HashMap<String,HashMap<String,HashMap<String,Collection<String>>>> getOnBehalfMap(String sysPrefix, String userLogin) throws Exception;
	
	String getPDFUrl( TbitsTreeRequestData ttrd) throws Exception ;
	HashMap<String,Object> getViewRequestParams(String sysPrefix) throws Exception;

	HashMap<String,? extends Serializable > getRequestDataForTransferRequest(String loginUser,int fromSysid, int requestId, TbitsTreeRequestData fromTtrd,String transferType, String toSysprefix) throws Exception;
	
	void sendMeEmail( UserClient userClient, String sysPrefix, int reqId) throws Exception;
	
	boolean isPreviewPdfEnable(String CurrentBa) throws Exception;
}
