package com.nattubaba.java.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class BinarySearchTest {
	public static void main(String[] args) {
		List<Integer> list = new Vector<Integer>();
		
		list.add(15);
		list.add(3);
		list.add(5);
		list.add(19);
		list.add(7);
		list.add(1);
		list.add(23);
		list.add(6);
		
//		Collections.sort(list);
//		
//		System.out.println("list after sort : " + list);
		
//		int index = Collections.binarySearch(list, 5); // 2

//		int index = Collections.binarySearch(list, 8); // -6
		
//		int index = Collections.binarySearch(list, 24); // -9
		
//		int index = Collections.binarySearch(list, 0); //
		
		// index at which to insert = -(index + 1)
		
//		System.out.println("index = " + index);
		
//		list.add(0, 24);
//		
//		System.out.println("list after insert : " + list);
		
//		Comparator<Integer> myComp = new Comparator<Integer>() {
//
//			@Override
//			public int compare(Integer o1, Integer o2) {
//				if(o2 > o1)
//				{
//					return 1;
//				}
//				else if ( o1 > o2 )
//				{
//					return -1;
//				}
//				return 0;
//			}
//		};
		
		Comparator<Integer> myComp = Collections.reverseOrder();
		
		Collections.sort(list,myComp);
		
		System.out.println("list after sort : " + list);
		
		
//		int index = Collections.binarySearch(list, 5,myComp ); // 5

//		int index = Collections.binarySearch(list, 8,myComp); // -4
		
//		int index = Collections.binarySearch(list, 24,myComp); // -1
		
		int index = Collections.binarySearch(list, 0,myComp); // -9
		
		// index at which to insert = -(index + 1)
		
		System.out.println("index = " + index);
		
		List<String> myStr = new ArrayList<String>();
		
		Collections.addAll(myStr, "a", "A");
		
		Collections.sort(myStr);
		
		System.out.print(" " +myStr);

	}
}
