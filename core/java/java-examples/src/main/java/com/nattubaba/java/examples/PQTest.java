package com.nattubaba.java.examples;

import java.util.Comparator;
import java.util.PriorityQueue;

public class PQTest {
	public static void main(String[] args) {
//		PriorityQueue<Integer> pq = new PriorityQueue<Integer>();
		
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(10, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				if( o2 < o1 )
					return -1 ;
				else if ( o2 > o1 )
					return 1;
				
				return 0;
			}
		});
		
		pq.add(5);
		pq.add(7);
		pq.add(3);
		pq.add(6);

		for(int i = 0 ; i < 4 ;i++)
		{
			Integer out = pq.poll();
			System.out.println(out);
		}

//		for(Integer i : pq)
//		{
//			System.out.println(i);
//		}
	}
}
