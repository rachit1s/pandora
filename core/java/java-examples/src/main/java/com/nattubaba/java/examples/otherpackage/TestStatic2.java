package com.nattubaba.java.examples.otherpackage;

import com.nattubaba.java.examples.StaticSub;
import com.nattubaba.java.examples.StaticSuper;

public class TestStatic2 {
	public static void main(String[] args) {
		StaticSuper.publicStaticMethod();
		StaticSub.publicStaticMethod();
		
//		StaticSuper.protectedStaticMethod();
//		StaticSub.protectedStaticMethod();
		
//		StaticSuper.privateStaticMethod();
//		StaticSub.privateStaticMethod();
	}
}
