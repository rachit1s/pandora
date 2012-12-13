package gmrCorr.others;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import transbit.tbits.api.APIUtil;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.RequestDataType;

public class GMRCorrUtils {

	/**
	 * finds any related request of BA rrBA present in currRelatedREquest not present in prevRelatedRequest
	 * @param currRelatedRequest
	 * @param prevRelatedRequest
	 * @param rrBA
	 * @return : the collection of diff in RelatedRequest of rrBA
	 */
	public static Collection<RequestDataType> getDiffRelatedRequest(String currRelatedRequest,
			String prevRelatedRequest, BusinessArea rrBA) 
	{
		ArrayList<RequestDataType> array = new ArrayList<RequestDataType>();
	
		try
		{
			
			Collection<RequestDataType> crr = APIUtil.getRequestCollection(currRelatedRequest);
			Collection<RequestDataType> prr=null;
			if(prevRelatedRequest != null && !prevRelatedRequest.equals(""))
			prr = APIUtil.getRequestCollection(prevRelatedRequest);
			if( null == rrBA || null == crr )
				return array;
			if(null == prr)
				prr = array;
			
			for(Iterator<RequestDataType> cIter = crr.iterator() ; cIter.hasNext() ; )
			{
				RequestDataType rdt = cIter.next();
				if( rdt.getSysId() != rrBA.getSystemId() )
					cIter.remove();
				
				for( RequestDataType prdt : prr )
				{
					if( rdt.getSysId() == prdt.getSysId() && rdt.getRequestId() == prdt.getRequestId() )
						cIter.remove();
				}
			}
			
			return crr;
		}
		catch(Exception e)
		{
			
			return array;
		}
	}

}
