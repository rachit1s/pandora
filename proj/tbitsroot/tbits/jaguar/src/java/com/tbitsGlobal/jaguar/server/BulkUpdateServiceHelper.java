package com.tbitsGlobal.jaguar.server;

import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;

import commons.com.tbitsGlobal.utils.client.IFixedFields;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.bulkupdate.IBulkUpdateConstants;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.server.GWTServiceHelper;

/**
 * Helper class for Bulk Operations.
 * 
 * @author sourabh
 *
 */
public class BulkUpdateServiceHelper {
	private static final TBitsLogger LOG = TBitsLogger.getLogger("bulkupdate.com.tbitsglobal.bulkupdate.server");
	
	/**
	 * Performs Bulk Update of requests.
	 * 
	 * @param user
	 * @param sysPrefix
	 * @param models
	 * @return The Map of status per request.
	 * @throws TbitsExceptionClient 
	 */
	public static HashMap<Integer, TbitsTreeRequestData> bulkUpdate(User user, String sysPrefix, List<TbitsTreeRequestData> models) throws TbitsExceptionClient{
		HashMap<Integer, TbitsTreeRequestData> resp = new HashMap<Integer, TbitsTreeRequestData>();
		
		BusinessArea ba = null;
		try {
			ba = BusinessArea.lookupBySystemPrefix(sysPrefix);	
		}catch (DatabaseException e) {
			LOG.info(TBitsLogger.getStackTrace(e));
			throw new TbitsExceptionClient(e);
		}
		
		if(user == null || ba == null)
			return null;
		
		for(TbitsTreeRequestData model : models){
			model = addOrUpdateRequest(model, user, ba, resp);
		}
		return resp;
	}
	
	private static TbitsTreeRequestData addOrUpdateRequest(TbitsTreeRequestData model, User user, BusinessArea ba, HashMap<Integer, TbitsTreeRequestData> resp){
		List<ModelData> childs = model.getChildren();
		POJO rowNoPojo = model.getAsPOJO(IBulkUpdateConstants.ROW_NUMBER);
		try{
			if(model.getRequestId() == 0){ // add request
				model = GWTServiceHelper.addRequest(model, user, ba);
			}else{
				model = GWTServiceHelper.updateRequest(model, user, ba);
			}
			
			if(childs != null){
				for(ModelData child : childs){
					child.set(IFixedFields.PARENT_REQUEST_ID, model.getRequestId());
					child = addOrUpdateRequest((TbitsTreeRequestData) child, user, ba, resp);
					model.add(child);
				}
			}
		}catch(TbitsExceptionClient e){
			model.setError(e);
			
			if(childs != null){
				for(ModelData child : childs){
					((TbitsTreeRequestData) child).setError(new TbitsExceptionClient("Add/Update of parent failed"));
					POJO rowNoPojoChild = ((TbitsTreeRequestData) child).getAsPOJO(IBulkUpdateConstants.ROW_NUMBER);
					resp.put((Integer)rowNoPojoChild.getValue(), (TbitsTreeRequestData) child);
				}
			}
		}finally{
			model.set(IBulkUpdateConstants.ROW_NUMBER, rowNoPojo);
			
			resp.put((Integer)rowNoPojo.getValue(), model);
		}
		
		return model;
	}
}
