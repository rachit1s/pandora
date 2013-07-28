package com.nattubaba.problems;

import java.math.BigInteger;
import java.util.ArrayList;

public class BigPrime 
{
	private static BigInteger zero = new BigInteger("0");
	private static BigInteger one = new BigInteger("1");
	private static BigInteger two = new BigInteger("2");
	public static boolean isPrime( BigInteger bi )
	{
		BigInteger half = bi.divide(two).add(one);
		BigInteger i = two;
		while( i.compareTo(half) < 1 )
		{
			if( bi.divideAndRemainder(i)[1].equals(zero) )
			{
				System.out.println(bi + " is divisible by " + i);
				return false;
			}
			
			i = i.add(one);
		}
		
		return true;
	}
	
	public static void allPrimes(BigInteger bi)
	{
		ArrayList<Boolean>  bools = new ArrayList<Boolean>();
	}
	
	public static void main(String argv[])
	{
		BigInteger number =null;
		if( argv.length == 1 )
		{
			number = new BigInteger(argv[0]);
		}
		
		if( null == number )
			number = getBigNumber();
		
		System.out.println(number + " is " + ( isPrime(number) ? "" : " not " ) + "prime.");
	}

	private static BigInteger getBigNumber() 
	{
		BigInteger bi = two;
		int power = 4311;
		while( power != 0 )
		{
			bi = bi.multiply(two);
			power--;
		}
		bi = bi.subtract(two).subtract(one);
		System.out.println("big integer : " + bi);
		return bi;
	}
}
