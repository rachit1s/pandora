package com.tbitsglobal.ddc.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class TestClass 
{
	String getValue(String string, int i)
	{
		return string;
	}
	
//	String getValue(int i)
//	{
//		return i+"";
//	}
	
	double  getValue(double d)
	{
		return 0;
	}
	
	public static void main(String[] args) {
		double d = 1;
		int i = 1;
		new TestClass().getValue(i);
		
		ClassLoader c ;
		
		Class k = TestClass.class;
		
		try {
			
			Object object =  k.newInstance();
			
			TestClass tc = (TestClass)object;
			
			tc.getValue(d);
			
			Method method = k.getMethod("getValue", double.class);
			
			method.invoke(object,d);
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
