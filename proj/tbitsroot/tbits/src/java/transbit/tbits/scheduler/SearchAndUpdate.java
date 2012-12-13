package transbit.tbits.scheduler;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.searcher.DqlSearcher;

public class SearchAndUpdate implements TBitsConstants {
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_COMMON);
	public  void searchAndUpdateRequest (String dql, Hashtable<String, String> updatefieldValues, User user, BusinessArea ba)
	{
		
		 DqlSearcher searcher = new DqlSearcher(ba.getSystemId(), user.getUserId(), dql);
		 
         try{
        	 searcher.search();
         }
		 catch (Exception e)
		 {
			 e.printStackTrace();
	            return ;
		 }
		 
		 ArrayList<Integer> requestIds = new ArrayList<Integer>(); 

		 if(searcher.getResult().containsKey(ba.getSystemId())){
			 Collection<Integer> requestIdsFetched = searcher.getResult().get(ba.getSystemId()).keySet();
			 if(requestIdsFetched != null)
				 requestIds.addAll(requestIdsFetched);
		 }
		 
         updatefieldValues.put(Field.BUSINESS_AREA, Integer.toString(ba.getSystemId()));
         
         for(int request : requestIds)
         {
        	 updatefieldValues.put(Field.REQUEST, request+"");
        	 UpdateRequest app = new UpdateRequest();
        	 app.setSource(TBitsConstants.SOURCE_CMDLINE);
        	 
            try {
            	 Request  updatedRequest = app.updateRequest(updatefieldValues);
            	 
            	 LOG.info("Request Id: " + updatedRequest.getRequestId());
            	 LOG.info("Action  Id: " + updatedRequest.getMaxActionId());
            	 //System.out.println(updatedRequest.getDescription());
            }
        	 
             catch (APIException api) {
            	 UpdateRequest.LOG.warn("",(api));
                 return ;
             } catch (Exception e) {
            	 UpdateRequest.LOG.warn("",(e));
                 return ;
             } 
         }
    }
	
	public  void searchAndUpdateRequest(String dql, Hashtable<String, String> updatefieldValues, User user, ArrayList<BusinessArea> bAList)
	{
		for(BusinessArea ba : bAList)
		{
			searchAndUpdateRequest(dql, updatefieldValues, user, ba);
		}
	}
	
	
	public static void main(String arg[]) {
		
		 if (arg.length < 1) {
	            //LOG.info("Usage:\n\t\tSearcher <query>");

	            return ;
	    }
		 
		Hashtable<String, String>  updatefieldValues = new Hashtable<String, String>();
		
		//updatefieldValues.put(Field.BUSINESS_AREA, "1");
		updatefieldValues.put(Field.DESCRIPTION, "The big one: Testing multiple request updates in all BAs");
		updatefieldValues.put(Field.USER,"1");
		int sysID = 1;
		String dql = "has:logger"; 
		SearchAndUpdate sch = new  SearchAndUpdate();
		//sch.updateRequestAllBA(dql, updatefieldValues);
		
        return ;
		
		
	}

}
