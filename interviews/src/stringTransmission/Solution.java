package stringTransmission;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class Solution {
	
	// string has been discovered and if it is unique or not.
	public static HashSet<String> uniques = new HashSet<String>();
	public static Hashtable<String,Boolean> periodic = new Hashtable<String,Boolean>();
	private static Hashtable<Pair,List<String>> perms = new Hashtable<Pair, List<String>>();
	
	public static int stringTransmission(String string, int k)
	{
		uniques = new HashSet<String>();
		periodic = new Hashtable<String, Boolean>();
		perms = new Hashtable<Pair, List<String>>();
		
//		System.out.println("stringTransmission string : " + string + " k = " + k );
		if(!isPeriodic(string))
		{
			uniques.add(string);
		}
		
		// generate for each i the combinations of strings
		for( int x = 1; x <= k ; x++)
		{
			List<String> strings = getPermutations(string,x);
			
//			System.out.println("Permutations of : " + string + " for k = " + x + " are : " + strings);
			for( String str : strings )
			{
				if(!uniques.contains(str) )
				{
					if( !isPeriodic(str) )
					{
						
						uniques.add(str);
					}
				}
			}
		}
		
//		System.out.println("uniques : " + uniques + " size = " +  uniques.size());
		return uniques.size();
	}
	
	private static Boolean isPeriodic(String string) {
		Boolean p = periodic.get(string);
		if( p != null )
			return p;
					
		// check periodicity for sizes of length .. 1-len/2
		// remove if the current size does not completely divides the len
		for( int len = 1 ; len <= string.length()/2 ; len++)
		{
			if( string.length() % len != 0 )
				continue;
			
//			int times = string.length() / len;
			String subString = string.substring(0,len);
			int j = 0 ;
			boolean foundPeriodic = true;
			for( int i = 0 ; i < string.length(); i++ )
			{
				if( subString.charAt(j) != string.charAt(i))
				{
					foundPeriodic = false;
					break;
				}
				
				j = (j+1)%len;
			}
			
			if( foundPeriodic )
			{
				periodic.put(string,Boolean.TRUE);
				return Boolean.TRUE;
			}
		}
		
		periodic.put(string, Boolean.FALSE);
		return Boolean.FALSE;
	}
	
	static class Pair
	{
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((string == null) ? 0 : string.hashCode());
			result = prime * result + x;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (string == null) {
				if (other.string != null)
					return false;
			} else if (!string.equals(other.string))
				return false;
			if (x != other.x)
				return false;
			return true;
		}
		public String string;
		public int x;
		public Pair(String string, int x) {
			super();
			this.string = string;
			this.x = x;
		}
		
		
	}
	
	private static List<String> getPermutations(String string, int x) {
		Pair p = new Pair(string,x);
		List<String> permutations = perms.get(p);
		if( null != permutations )
			return permutations;
		
		permutations = new LinkedList<String>();
		
		if( x == 0 )
		{
			permutations.add(string);
			return permutations;
		}
		if( string.length() < x )
			return Collections.EMPTY_LIST;
			
		// calculate permutations
		for( int i = 0 ; i < string.length() - (x-1) ; i++)
		{
			StringBuffer s = new StringBuffer();
			if(i != 0 )
			 s.append(string.substring(0,i));
		
			if(string.charAt(i) == '0')
				s.append("1");
			else
				s.append("0");
			
			String prefix = s.toString();
			List<String> strPers = getPermutations(string.substring(i+1),x-1);
			for( String ps : strPers )
			{
				String s2 = prefix + ps;
				permutations.add(s2);
			}
		}
		
		perms.put(p, permutations);
		return permutations;
	}
	
	public static void main(String[] args) {
		int numberOfTests = 0 ; 
		Scanner scan = new Scanner(System.in);
		numberOfTests = scan.nextInt();
		scan.nextLine();
		
		String [] strings = new String[numberOfTests];
		int [] flips = new int[numberOfTests];
		int size, k;
		
		for( int i = 0 ; i < numberOfTests ; i++ )
		{
			size = scan.nextInt();
			k = scan.nextInt() ;
			scan.nextLine();
			String str = scan.nextLine();
			strings[i] = str;
			flips[i] = k;
		}
		
		for( int i = 0 ; i < numberOfTests ; i++ )
		{
			int combinations = stringTransmission(strings[i], flips[i]);
			System.out.println(combinations);
		}
		
	}

}
