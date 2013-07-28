package com.nattubaba.java.examples;

public class TestStatic {
	public static void main(String[] args) {
		StaticSuper.publicStaticMethod();
		StaticSub.publicStaticMethod();
		
		StaticSuper.protectedStaticMethod();
		StaticSub.protectedStaticMethod();
		
//		StaticSuper.privateStaticMethod();
//		StaticSub.privateStaticMethod();
		
		StaticSub.somePublicMethod();
	}
}
