package transbit.tbits.domain;

import transbit.tbits.common.DatabaseException;
import junit.framework.TestCase;


public class RequestTest extends TestCase{

	Request request;
	@Override
	protected void setUp() throws Exception
	{
			request = Request.lookupBySystemIdAndRequestId(1, 22);
	}
	
	public void testString()
	{
		try {
			String value = "strfieldvalue";
			String fieldName = "varcharfield";
			
			request.setExString(fieldName, value);
			assertEquals(value, request.getExString(fieldName));
			
			fieldName = "textfield";
			
			request.setExString(fieldName, value);
			assertEquals(value, request.getExString(fieldName));

		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void testText()
	{
		try {
			String value = "strfieldvalue";
			String fieldName = "textfield";
			
			request.setExString(fieldName, value);
			assertEquals(value, request.getExString(fieldName));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void testVarchar()
	{
		try {
			String value = "strfieldvalue";
			String fieldName = "varcharfield";
			
			request.setExString(fieldName, value);
			assertEquals(value, request.getExString(fieldName));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testInt()
	{
		try {
			int value = 10;
			String fieldName = "intfield";
			
			request.setExInt(fieldName, value);
			assertEquals(value, request.getExInt(fieldName));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testReal()
	{
		try {
			double value = 10.5;
			String fieldName = "realfield";
			
			request.setExReal(fieldName, value);
			assertEquals(value, request.getExReal(fieldName));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void testBoolean()
	{
		try {
			boolean value = true;
			String fieldName = "boolfield";
			
			request.setExBoolean(fieldName, value);
			assertTrue(request.getExBoolean(fieldName));
			
			value = false;
			request.setExBoolean(fieldName, value);
			assertFalse(request.getExBoolean(fieldName));

		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testType()
	{
		try {
			String value = "A1";
			String fieldName = "A";
			
			request.setExType(fieldName, value);
			assertEquals(value, request.getExType(fieldName).getName());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
