package tatapower;


import java.util.Calendar;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;

public class TataJobUtils 
{
	// Getters for the Extended Field Values.
	public static Timestamp getExDate(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{		
		RequestEx rex = getRequestEx(fieldName, DataType.DATE, request );
		//System.out.println("DataType.DATE = " + DataType.DATE + ",   DataType.DATETIME = " +DataType.DATETIME ) ;
		/*if( null == rex)
			rex = getRequestEx( fieldName, DataType.DATETIME, request ) ;*/
		return rex.getDateTimeValue();
	}
	
	public static RequestEx getRequestEx(String fieldName, int dataTypeId, Request request) throws DatabaseException 
	{	
		Field f = Field.lookupBySystemIdAndFieldName(request.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalStateException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalStateException("'" + fieldName + "' is not an extended field");
		}
		//System.out.println( "dataTypeid = " + dataTypeId + "   f.getDataTypeId = " + f.getDataTypeId() + " name = " + f.getName() );
		if(f.getDataTypeId() != dataTypeId)
			throw new IllegalStateException("The field '" + fieldName + "' is not of the type '" + dataTypeId + "'");
		RequestEx rex = request.getExtendedFields().get(f);
		return rex;
	}
	
	public static String getExString(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{
		Field f = Field.lookupBySystemIdAndFieldName(request.getSystemId(), fieldName);
		if(f == null)
			throw new IllegalStateException("The field '" + fieldName + "' doesnt exist.");
		if(!f.getIsExtended())
		{
			throw new IllegalStateException("'" + fieldName + "' is not an extended field");
		}
		RequestEx rex = request.getExtendedFields().get(f);
		if(rex == null)
			return null;
		if((f.getDataTypeId() == DataType.TEXT) )
			return rex.getTextValue();
		else if(f.getDataTypeId() == DataType.STRING)
			return rex.getVarcharValue();
		else throw new IllegalStateException("This field is neither Text nor Varchar");
	}
	
	public static void setExDate(String fieldName, Timestamp value, Request request) throws DatabaseException
	{
		RequestEx rex = getRequestEx(fieldName, DataType.DATE, request);
		rex.setDateTimeValue(value);
	}
	
	public static Boolean getExBoolean(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{
		RequestEx rex = getRequestEx(fieldName, DataType.BOOLEAN, request);
		return rex.getBitValue();
	}
	
	public static Type getExType(String fieldName, Request request) throws DatabaseException, IllegalStateException
	{
		RequestEx rex = getRequestEx(fieldName, DataType.TYPE,request);
		int typeId = rex.getTypeValue();
		return Type.lookupBySystemIdAndFieldNameAndTypeId(request.getSystemId(), fieldName, typeId);
	}

	public static final String FIRST_ASSIGN_DATE_FIELD_NAME = "First_Assign_Date" ; // this should be date type	
	public static final String REPEAT_TYPE_FIELD_NAME = "Repeating_Type" ; // this should be single-select-option (Type) type
	public static final String NEXT_REPEAT_DATE_FIELD_NAME = "Next_Repeat_Date" ; // varchar type : this is the date when the next repeat will occur according to the type in REPEAT_TYPE
	public static final String SPAN_FIELD_NAME = "Span" ; // int type 
	public static final String MONTHLY = "Monthly" ;
	public static final String QUATERLY = "Quaterly" ;
	public static final String YEARLY = "Yearly" ;
	public static final String HALF_YEARLY = "Half-Yearly" ;
	public static final String[] REPEAT_OPTIONS = { MONTHLY , QUATERLY , YEARLY, HALF_YEARLY } ;
	public static final String FIRST_ASSIGNEE_FIELD_NAME = "First_Assignee" ; // this should be varchar type
	public static final String IS_REPEATING_FIELD_NAME = "Is_Repeating" ; // this should be true/false ( Bit )type
	
	public static String inputDateFormat( Timestamp ts ) 
	{
		return ts.toCustomFormat("yyyy-MM-dd 00:00:00") ;
	}

	public static Timestamp addDays( Timestamp ts, int noOfDays )
	{
		Calendar cal = Calendar.getInstance() ;
		cal.clear() ;
		cal.setTime(ts) ;
		
		cal.add(Calendar.DATE, noOfDays) ;
		
		return new Timestamp( cal.getTime().getTime() ) ;
	}

	public static void printTable(Hashtable<String, String> updateFieldValues) 
	{
		System.out.println( "*********START*************") ;
		java.util.Enumeration<String> allKeys = updateFieldValues.keys() ;		
		while(allKeys.hasMoreElements())
		{
			String key = allKeys.nextElement() ;
			String value = updateFieldValues.get(key);
			System.out.println( key + " : " + value ) ;	 
		}	
		System.out.println( "**********END**************") ;
	}

	public static String inputDueDateFormat(Timestamp due_date) {
		return due_date.toCustomFormat("yyyy-MM-dd HH:mm:ss") ;
	}
}
