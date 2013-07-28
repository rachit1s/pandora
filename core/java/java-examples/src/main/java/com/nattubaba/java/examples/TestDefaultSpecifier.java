package com.nattubaba.java.examples;

public class TestDefaultSpecifier {
	public static void main(String[] args) {
		DefaultSpecifier ds = new DefaultSpecifier();
		System.out.println(ds.defaultSpecVar);
//		System.out.println(ds.privateSpecVar); // generates error
		System.out.println(ds.publicSpecVar);
		System.out.println(ds.protectedSpecVar);
	}
}
