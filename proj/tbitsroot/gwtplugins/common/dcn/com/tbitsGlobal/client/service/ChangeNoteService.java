package dcn.com.tbitsGlobal.client.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.tbitsGlobal.jaguar.client.serializables.RequestData;

import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

public interface ChangeNoteService extends RemoteService {
	
	ChangeNoteConfig lookupChangeNoteConfigSysPrefixAndBAType (String aSysPrefix, String baType);

	ArrayList<ChangeNoteConfig> lookupChangeNoteConfigByBAType(String aBAType);

	RequestData getDCNRequest(ArrayList<Integer> requestIdList,
			ChangeNoteConfig changeNoteConfig);

	ArrayList<String> lookupSrcBusinessAreaByBAType(String baType);
	
	HashMap<String, String> lookupDistinctBATypes();

	ArrayList<ChangeNoteConfig> lookupChangeNoteConfigBySourceSysPrefix(
			String sysPrefix);

	ArrayList<ChangeNoteConfig> lookupAllChangeNoteConfig();

	String generatePdf(int systemId, int requestId, ChangeNoteConfig changeNoteConfig);

}
