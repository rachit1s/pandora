package transbit.tbits.search;

import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;

public class SearchForReport {
	/**
	 * @param args
	 */
	public ArrayList<Result> SearchAllFieldsPerBA(int userid,int sysid, String dql,ArrayList<String> columns)
	{
		System.out.println("\nInside SearchAllFieldsPerBA");		
//    	BusinessArea ba;
//    	System.out.println("\nInside SearchAllFieldsPerBA, after ba");
//    	try{
//    		
//    	   ba = BusinessArea.lookupBySystemId(sysid);
//    	   System.out.println("\nAfter LookupbySystemID");
//    	}
//    	catch (DatabaseException de)
//    	{
//    		System.out.println("***************Database Exception:SearchAllFieldsPerBA "+de);
//    		return new ArrayList<Result>();
//    	}
//    	catch(Exception e)
//    	{
//    		System.out.println("Exception occured: "+ e);
//    	}
//    	catch(Throwable t)
//    	{
//    		System.out.println("\n\n******************* Error ******************\n"+t);
//    	}
//    	System.out.println("\nperfect");
    	Hashtable<Integer,ArrayList<Result>> results = SearchPerBA(userid,sysid,dql,columns);
    	ArrayList<Result> resultArray = results.get(sysid);
    	if(resultArray == null)
    	{
    		return new ArrayList<Result>();
    	}
    	return resultArray; 
    	    	
	}
	
	public ArrayList<Result> SearchFieldsAllBA(int userid, String dql,ArrayList<String> columns)
	{
		ArrayList<BusinessArea> BAlist;
		try{
			 BAlist = BusinessArea.getBusinessAreasByUserId(userid);
		}
		catch (DatabaseException de){
			System.out.println("\nDatabase exception occured");
			return new ArrayList<Result>();
		}
		
		ArrayList<Result> resultsAllBA = new ArrayList<Result>();
		
		for(BusinessArea ba : BAlist)
		{
			int sysid = ba.getSystemId();
			ArrayList<Result> resultsPerBA = SearchPerBA(userid, sysid,dql,columns).get(sysid);
			if(resultsPerBA != null)
			{
				resultsAllBA.addAll(resultsPerBA);
			}
				
		}
		return resultsAllBA;
	}
	
	private Hashtable<Integer,ArrayList<Result>> SearchPerBA(int userid, int sysid, String dql, ArrayList<String> aDisplayHeader)
	{
		Searcher searcher = new Searcher(sysid, userid, dql);
    	searcher.setDisplayHeader(aDisplayHeader);
        try{
       	 searcher.search();
        }
		 catch (Exception e)
		 {
			 e.printStackTrace();
	         return new Hashtable<Integer,ArrayList<Result>>();
		 }
		 catch (Error e)
		 {
			 System.out.println("\nError in SearchPerBA: "+e);
			 return new Hashtable<Integer,ArrayList<Result>>();
		 }
		 ArrayList<String> resultids = searcher.getAllRequestIdList();
		 Hashtable<Integer,ArrayList<Result>> results = searcher.getResultListByBA();
	 
    	 return results;
    }
	
	/*
	 * for testing purpose only
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	  	SearchForReport s = new SearchForReport();
	  	int userid = 1;
	  	int sysid = 1;
	  	String dql = "has:subject";
	  	ArrayList<String> columns = new ArrayList<String>();
	  	columns.add("assignee_ids");
	  	columns.add("category_id");
	  	columns.add("logger_ids");
	  	
	  	ArrayList<Result> results = s.SearchFieldsAllBA(userid,dql,columns); 
	  	for(Result r : results)
		System.out.println("\nResult:" + r);
		return;
    	

	}

}
