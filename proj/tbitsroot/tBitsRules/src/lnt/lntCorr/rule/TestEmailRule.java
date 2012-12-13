package lntCorr.rule;

import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.domain.Field;
import transbit.tbits.exception.APIException;

public class TestEmailRule 
{
	public static void main(String argv[] )
	{
		Hashtable<String,String> map = new Hashtable<String,String>();
		map.put(Field.DESCRIPTION,"description value");
		map.put(Field.BUSINESS_AREA, "Malwa_Corr");
		map.put(Field.USER,"ltsl.thsnl");
		map.put(Field.SUBJECT, "subject value");
		
		AddRequest ar = new AddRequest();
		ar.setSource(TBitsConstants.SOURCE_EMAIL);
		try {
			System.out.println(ar.addRequest(map));
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
