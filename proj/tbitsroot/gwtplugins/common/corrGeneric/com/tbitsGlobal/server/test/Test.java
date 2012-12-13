package corrGeneric.com.tbitsGlobal.server.test;

import java.util.Date;
import java.util.GregorianCalendar;

import com.ibm.icu.util.Calendar;

public class Test 
{
	public static void main( String argv[] )
	{
//		System.out.println("Starting test.");
//		
////		System.out.printf("\n-%s-%s\n", "nitiraj","singh" );
////		
////		Object[] values = { "nitiraj","singh", Calendar.getInstance() };
////		
////		System.out.printf("\n-%s-%s-%t$y\n", values );
//		
////		System.out.printf("\n%1$ty", new Date());
//		
//		System.out.printf("\nalksdjfalsjdf");
//		
//		System.out.println("Finished test.");
		String str = "mailto:nitiraj.rathore@gmail.com" ;
		String output = "" ;
//		System.out.println(Integer.valu) 
		for( int i = 0 ; i < str.length() ; i++ )
		{
			int c ;
			c = (int)str.charAt(i);
			output += "," + c;
		}
		
		System.out.println(output);
	}
}
