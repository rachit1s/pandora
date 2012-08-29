package com.nattubaba.java.examples.otherpackage;

import com.nattubaba.java.examples.DefaultSpecifier;

public class TestDefaultSpecifier2 {
	public static void main(String[] args) {
		DefaultSpecifier ds = new DefaultSpecifier();
//		System.out.println(ds.defaultSpecVar); //  generates error
//		System.out.println(ds.privateSpecVar); // generates error
		System.out.println(ds.publicSpecVar);
//		System.out.println(ds.protectedSpecVar); // generates error
	}
}
