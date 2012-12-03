package com.nattubaba.java.examples.otherpackage;

import java.util.ArrayList;

public class TypeErasureTest {
	public static void main(String[] args) {
		ArrayList als = new ArrayList();
		als.add("abc");
		
		((ArrayList)als).add(1);
		
		for( Object obj : als )
		{
			System.out.println("obj = " + obj);
		}
	}
}
