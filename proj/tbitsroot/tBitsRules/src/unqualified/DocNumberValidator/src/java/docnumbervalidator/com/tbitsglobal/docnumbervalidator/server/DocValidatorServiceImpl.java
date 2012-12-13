package docnumbervalidator.com.tbitsglobal.docnumbervalidator.server;

import java.util.ArrayList;
import java.util.HashMap;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.plugin.GWTProxyServletManager;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;
import transbit.tbits.search.Searcher;
import docnumbervalidator.com.tbitsglobal.docnumbervalidator.client.DocValidatorService;

public class DocValidatorServiceImpl extends TbitsRemoteServiceServlet implements DocValidatorService {
	private static final long serialVersionUID = 1L;

	static{
		GWTProxyServletManager.getInstance().subscribe(DocValidatorService.class.getName(), DocValidatorServiceImpl.class);
		System.out.println("Subscribed " + DocValidatorServiceImpl.class.getName() + " in plugins at url : db");
	}

	public HashMap<String, Integer> testNumbers(String sysPrefix, String fieldName, ArrayList<String> numbers) {
		HashMap<String, Integer> resp = new HashMap<String, Integer>();
		
		try {
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
			if(ba == null)
				return resp;
			
			String userLogin = this.getRequest().getRemoteUser();
			User user = User.lookupAllByUserLogin(userLogin);
			
			String dql = "";
			
			for(String value : numbers){
				if(!dql.equals(""))
					dql += " OR ";
				dql += fieldName + ":\"" + value + "\"";
			}
			
			Searcher searcher = new Searcher(ba.getSystemId(), user.getUserId(), dql);
			searcher.search();
			
			ArrayList<String> ids = searcher.getAllRequestIdListByBA().get(ba.getSystemId());
			if(ids == null)
				return resp;
			
			HashMap<String, Request> requestMap = new HashMap<String, Request>();
			
			for(String id : ids){
				int requestId = Integer.parseInt(id.split("_")[1]);
				Request r = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
				String value = r.get(fieldName);
				if(value != null && !value.trim().equals(""))
					requestMap.put(value, r);
			}
			
			for(String value : numbers){
				if(requestMap.containsKey(value)){
					Request r = requestMap.get(value);
					if(r != null)
						resp.put(value, r.getRequestId());
				}
			}
			
			return resp;
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
