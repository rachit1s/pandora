package com.nattubaba.java.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayToListToArray {
	public static void main(String[] args) {
		Integer[] ints = new Integer[5];
		for( int i = 0 ; i < ints.length ; i++ )
		{
			ints[i] = i;
		}
		
		List<Integer> intList = Arrays.asList(ints);
		intList.set(2, 44);
		
		System.out.print("ints = ");
		printArray(ints);
		System.out.println("list = " + intList);
		
		ints[0] = 10;
		
		System.out.print("ints = ");
		printArray(ints);
		System.out.println("list = " + intList  + "\n\n\n\n");
//		intList.add(20); //java.lang.UnsupportedOperationException
//		intList.remove(1); //  java.lang.UnsupportedOperationException
		
		List<Integer> iList = new ArrayList<Integer>();
		
		for( int i = 0 ; i < 5 ; i++)
		{
			iList.add(i);
		}
		
		int[] arrs = new int[5];
		System.out.print("arrs =" );
		printArray(arrs);
		
		char[] charArrs = new char[5];
		System.out.print("charArrs =" );
		printArray(charArrs);
		
		char zero = '\0';
		System.out.println("zero = " + (int)zero);
		
//		char defaultChar ;
//		System.out.println("defaultChar = " + (int)print(defaultChar));
		
		
//		Integer[] arr = new Integer[5];
		Integer[] arr = new Integer[6];
		Arrays.fill(arr, 343);
		System.out.print("Initial arr = ");
		printArray(arr);
		
//		arr = iList.toArray(arr);
		Integer[] newArray = iList.toArray(arr);
		System.out.print("arr = ");
		printArray(arr);
		System.out.print("newArray = ");
		printArray(newArray);
		System.out.println("iList = " + iList);
		
		iList.set(1, 20);

		System.out.print("arr = ");
		printArray(arr);
		System.out.println("iList = " + iList);

	}
	
	private static int print(char defaultChar) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static <T> void printArray(T[] array)
	{
		for( T t : array)
		{
			System.out.print(t + ",");
		}
		
		System.out.println();
	}
	
	public static void printArray(int[] array)
	{
		for(int t : array)
		{
			System.out.print(t + ",");
		}
		
		System.out.println();
	}
	
	public static void printArray(char[] array)
	{
		System.out.println("array.length = " + array.length);
		for(int t : array)
		{
			System.out.print(t + ",");
		}
		
		System.out.println();
	}
}
