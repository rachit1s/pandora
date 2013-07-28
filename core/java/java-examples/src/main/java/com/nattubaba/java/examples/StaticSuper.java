package com.nattubaba.java.examples;

public class StaticSuper 
{
	public /*final*/ static void publicStaticMethod()
	{
		System.out.println("StaticSuper.publicStaticMethod()");
	}
	
	private /*final*/ static void privateStaticMethod()
	{
		System.out.println("StaticSuper.privateStaticMethod()");
	}
	
	protected /*final*/ static void protectedStaticMethod()
	{
		System.out.println("StaticSuper.protectedStaticMethod()");
	}
}
