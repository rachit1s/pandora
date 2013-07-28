package com.nattubaba.java.examples;


public class StaticSub extends StaticSuper {
	public /*final*/ static void publicStaticMethod()
	{
		System.out.println("StaticSub.publicStaticMethod()");
	}
	
	private /*final*/ static void privateStaticMethod()
	{
		System.out.println("StaticSub.privateStaticMethod()");
	}
	
	
	public static void somePublicMethod()
	{
		privateStaticMethod();
		protectedStaticMethod();
	}
	
//	protected /*final*/ static void protectedStaticMethod()
//	{
//		System.out.println("StaticSub.protectedStaticMethod()");
//	}
}
