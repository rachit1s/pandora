package com.nattubaba.java.examples;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class TreeSetTest {
	public static <T> void printArray(T[] array)
	{
		for( T t : array)
		{
			System.out.print(t + ",");
		}
		
		System.out.println();
	}
	
	public static void printArray(boolean[] array)
	{
		for(boolean t : array)
		{
			System.out.print(t + ",");
		}
		
		System.out.println();
	}
	public static void main(String[] args) {
//		test1();
		test2();
	}

	private static void test2() {
		TreeSet<Integer> iSet = new TreeSet<Integer>(new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				if( o1 < o2 )
					return -1 ;
				else if ( o1 > o2 )
					return 1;
				
				return 0;
			}
		});
		
		iSet.add(10);
		iSet.add(12);
		iSet.add(5);
		iSet.add(55);
		
		System.out.print("iSet : ");
		printCollection(iSet);
		System.out.println();
		
		Set<Integer> decSet = iSet.descendingSet();
		
		System.out.print("decSet : ");
		printCollection(decSet);
		System.out.println();
		
		decSet.add(3);
		decSet.add(50);
		
		System.out.print("after add decSet : ");
		printCollection(decSet);
		System.out.println();
		
//		Comparator<Integer> comp = Collections.reverseOrder();
		
	}

	private static <T> void printCollection(Collection<T> col) {
		for(T t : col)
		{
			System.out.print(t + ",");
		}
	}

	private static void test1() {
//		Set s = new TreeSet();
//		Set s = new HashSet();
		
		Set s = new TreeSet(new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) 
			{
				if( o1.hashCode() < o2.hashCode() )
					return -1; 
				else if ( o1.hashCode() > o2.hashCode() )
					return 1;
				
				return 0;
			}
		});
		
		boolean[] result = new boolean[5];
		result[0] = s.add("a");
		result[1] = s.add(new Integer(42));
		result[2] = s.add("b");
		result[3] = s.add("a");
		result[4] = s.add(new Object());
		
		System.out.print("s = ");
		for( Object o : s)
		{
			System.out.print(o + ",");
		}
		
		System.out.println();
		
		System.out.print("results : ");
		printArray(result);
		
		
	}
}
