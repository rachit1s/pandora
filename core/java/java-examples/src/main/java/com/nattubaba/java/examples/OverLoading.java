package com.nattubaba.java.examples;

class A
{
	public int call(int i )
	{
		System.out.println( "A : " + i);
		return i;
	}
}

class B extends A
{
	public int call( int i )
	{
		System.out.println( "B : " + i);
		return i;
	}
	
	public float call( int i, float j )
	{
		System.out.println( "B : " + i);
		return i;
	}
	
	public int call( float i, int j )
	{
		System.out.println( "B : " + i);
		return j;
	}
	
	public String call(String s)
	{
		System.out.println(s);
		return s;
	}
}

class C extends B
{
	
}

public class OverLoading {
	
	public static void main(String[] args) {
		B b = new B();
		b.call(10); // b
		b.call("343");
		
		A a = new A();
		
		a.call(10); // a
		
		a = b;
		
		a.call(11); // b
		
	}

}
