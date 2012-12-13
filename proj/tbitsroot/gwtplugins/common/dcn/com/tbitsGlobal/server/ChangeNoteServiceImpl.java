/**
 * 
 */
package dcn.com.tbitsGlobal.server;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.birt.core.exception.BirtException;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;

import com.tbitsGlobal.jaguar.client.serializables.RequestData;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

import dcn.com.tbitsGlobal.client.service.ChangeNoteService;
import dcn.com.tbitsGlobal.server.utils.ServerUtilities;
import dcn.com.tbitsGlobal.shared.ChangeNoteConfig;

/**
 * @author Lokesh
 *
 */
public class ChangeNoteServiceImpl extends TbitsRemoteServiceServlet implements ChangeNoteService {
	
	public RequestData getDCNRequest(ArrayList<Integer> requestIdList, ChangeNoteConfig changeNoteConfig) 
	{
		return ServerUtilities.getRequestData(this.getRequest(), requestIdList, changeNoteConfig);
	}

	public ArrayList<ChangeNoteConfig> lookupChangeNoteConfigByBAType(String baType) {
		return ChangeNoteConfigHandler.lookupChangeNoteConfigByBAType(baType);
	}

	public ChangeNoteConfig lookupChangeNoteConfigSysPrefixAndBAType(
			String sysPrefix, String baType) {
		return ChangeNoteConfigHandler.lookupChangeNoteConfigBySysPrefixAndBAType(sysPrefix, baType);
	}

	public HashMap<String, String> lookupDistinctBATypes() {
		return ChangeNoteConfigHandler.lookDistinctBATypes();
	}
	
	public ArrayList<String> lookupSrcBusinessAreaByBAType(String baType) {
		return ChangeNoteConfigHandler.lookupSrcBusinessAreaByBAType(baType);
	}
	
	public ArrayList<ChangeNoteConfig> lookupChangeNoteConfigBySourceSysPrefix(
			String sysPrefix) {		
		return ChangeNoteConfigHandler.lookupChangeNoteConfigBySourceSysPrefix(sysPrefix);
	}
	
	public ChangeNoteConfig lookupChangeNoteConfigBySrcSysPrefixAndTargetSysPrefix(
			String srcSysPrefix, String targetSysPrefix) {		
		return ChangeNoteConfigHandler.lookupChangeNoteConfigBySrcSysPrefixAndTargetSysPrefix(
				srcSysPrefix, targetSysPrefix);
	}
	
	public ArrayList<ChangeNoteConfig> lookupAllChangeNoteConfig() {
		return ChangeNoteConfigHandler.lookupAllChangeNoteConfig();
	}
	
	public String generatePdf(int aSystemId, int aRequestId, ChangeNoteConfig cnc) {
		try {
			return ServerUtilities.generatePdf(this.getRequest(), aSystemId, aRequestId, cnc);
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 * @throws TBitsException 
	 * @throws BirtException 
	 * @throws TbitsExceptionClient 
	 */
	public static void main(String[] args) throws DatabaseException, TBitsException, BirtException, TbitsExceptionClient {
		ChangeNoteServiceImpl impl = new ChangeNoteServiceImpl();
//		Request request = Request.lookupBySystemIdAndRequestId(175, 5);
		
		System.out.println("Done: " + ServerUtilities.getChangeNoteFieldMap(31));
		
//		ServerUtilities.generatePdf(175, 5);System.out.println("%%%%%%%%%%%%%%%DONE");
	}	
}
