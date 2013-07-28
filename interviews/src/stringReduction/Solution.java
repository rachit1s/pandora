package stringReduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Anyone can give a hint? Thanks.

Given a string consisting of a,b and c's, we can perform the following operation: Take any two adjacent distinct characters and replace it with the third character. For example, if 'a' and 'c' are adjacent, they can replaced with 'b'. What is the smallest string which can result by applying this operation repeatedly?

Input:
The first line contains the number of test cases T. T test cases follow. Each case contains the string you start with.

Output:
Output T lines, one for each test case containing the smallest length of the resultant string after applying the operations optimally.

Constraints:
1 <= T <= 100
The string will have at most 100 characters.

Sample Input:
3
cab
bcab
ccccc

Sample Output:
2
1
5

Explanation:
For the first case, you can either get cab -> cc or cab -> bb, resulting in a string of length 2.
For the second case, one optimal solution is: bcab -> aab -> ac -> b. No more operations can be applied and the resultant string has length 1.
For the third case, no operations can be performed and so the answer is 5. 
 * @author nitiraj
 *
 */

public class Solution {
	public static int stringReduction(String s)
	{
		String ns = s;
		while(true)
		{
			int i = findDistinct(ns); // I will replace i,i+1
			s = "";
			if( i == -1 )
			{
				break;
			}
			if( i != 0 )
			{
				s = ns.substring(0, i);
			}
			s += getChar(ns.charAt(i),ns.charAt(i+1));
			if( ns.length() > i + 2 )
			{
				s += ns.substring(i+2);
			}
			
			ns = s;
		}
//		System.out.println("final String : " + ns);
		return ns.length();
	}

	private static String getChar(char char1, char char2) {
		switch(char1)
		{
			case 'a' :
				if( char2 == 'b')
					return "c";
				else
					return "b";
			case 'b':
				if( char2 == 'c')
					return "a";
				else 
					return "c";
			case 'c':
				if( char2 == 'a')
					return "b";
				else
					return "a";
		}
		
		return "a";
	}

	private static int findDistinct(String ns) {
		int count= 1 ;
		for( int i = 0 ; i < ns.length() - 1 ; i++)
		{
			if(ns.charAt(i) == ns.charAt(i+1))
			{
				count++;
			}
			else //if( ns.charAt(i) != ns.charAt(i+1) )
			{
				int nextCount = 0 ;
				// find which part to convert the i,i+1 or i+1,i+2
				// find the next count
				if( i < ns.length() - 2 && ns.charAt(i+1) != ns.charAt(i+2))
				{
					nextCount = findCount(ns,i+2);
					if( nextCount > count )
						return i+1;
				}
				
				return i ;
			}
		}
		
		return -1;
	}

	private static int findCount(String ns, int index) {
		int count = 1 ; 
		for( int i = index ; i < ns.length() - 1 ; i++)
		{
			if( ns.charAt(i) == ns.charAt(i+1) )
				count++;
			else
				break;
		}
		
		return count;
	}

	public static void main(String[] args) {
		int numberOfStrings;
		Scanner s = new Scanner(System.in);
		numberOfStrings = s.nextInt();
		s.nextLine();
		List<String> strings = new ArrayList<String>(numberOfStrings);
//		System.out.println("number of string : " + numberOfStrings);
		for( int i = 0 ; i < numberOfStrings ; i++)
		{
			String str = s.nextLine();
			strings.add(str);
		}
//		System.out.println("strings : " + strings);
		
//		List<Integer> lengths = new ArrayList<Integer>(numberOfStrings);
		for( String str : strings )
		{
			int length = stringReduction(str);
//			lengths.add(length);
			System.out.println(length);
		}
		
		
	}
}
