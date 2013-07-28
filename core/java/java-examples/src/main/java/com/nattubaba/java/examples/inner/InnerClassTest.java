package com.nattubaba.java.examples.inner;

import java.io.DataInputStream;
import java.util.Hashtable;

import com.nattubaba.java.examples.inner.OuterClass.DefaultInnerClass;
import com.nattubaba.java.examples.inner.OuterClass.DefaultStaticInnerClass;
import com.nattubaba.java.examples.inner.OuterClass.PublicInnerClass;
import com.nattubaba.java.examples.inner.OuterClass.PublicStaticInnerClass;


public class InnerClassTest {
	public static void main(String[] args) {
		OuterClass oc = new OuterClass();
		/*
		 * No enclosing instance of type OuterClass is accessible. Must qualify the allocation with an enclosing instance of type OuterClass (e.g. x.new A() where x is an instance of OuterClass).
		 */
//		PublicInnerClass ic = new PublicInnerClass();
		
		/*
		 * No enclosing instance of type OuterClass is accessible. Must qualify the allocation with an enclosing instance of type OuterClass (e.g. x.new A() where x is an instance of OuterClass).
		 */
//		PublicInnerClass ic1 = new OuterClass.PublicInnerClass();
		
		/*
		 * No enclosing instance of type OuterClass is accessible. Must qualify the allocation with an enclosing instance of type OuterClass (e.g. x.new A() where x is an instance of OuterClass).
		 */
//		DefaultInnerClass dic = new DefaultInnerClass();
		
		/*
		 * OuterClass cannot be resolved to a variable
		 */
//		DefaultInnerClass dic = OuterClass.new DefaultInnerClass();
		
		DefaultInnerClass dick = oc.new DefaultInnerClass();
		
		PublicInnerClass pic = oc.new PublicInnerClass();
		
		
		PublicStaticInnerClass psic = new OuterClass.PublicStaticInnerClass();
		
		DefaultStaticInnerClass dsic = new OuterClass.DefaultStaticInnerClass();
		
		/*
		 * PrivateInnerClass cannot be resolved to a type
		 */
//		PrivateInnerClass picc = new PrivateInnerClass(); 
		 
		OuterClass OuterClass = new OuterClass();
		
		Integer Integer = 1;
		
		String str;
//		str.in
		Runnable r;
		
		DataInputStream dis;
		
		Hashtable ht;
		int i ; Integer i1;
		double d;
		float f;
		System.out.printf("%d,%d", args);
		
		System.out.println(OuterClass);
	}
}
