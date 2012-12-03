import java.lang.*;

class VarargsTest
{
	public static void vararg1( int x, int... args1)
	{
		System.out.println("x = " + x );
		System.out.println("args1.length = " + args1.length );
		System.out.print("args = ");
		for( int i : args1 )
		{
			System.out.print(i + ","); 
		}
		System.out.println();
 	}

 // 	public static void vararg2( int x, int... args1, float... args2)
	// {
	// 	System.out.println("x = " + x );
	// 	System.out.println("args1.length = " + args1.length );
	// 	System.out.print("args1 = ");
	// 	for( int i : args1 )
	// 	{
	// 		System.out.print(i + ","); 
	// 	}
	// 	System.out.println();

	// 	System.out.println("args2.length = " + args2.length );
	// 	System.out.print("args2 = ");
	// 	for( float i : args2 )
	// 	{
	// 		System.out.print(i + ","); 
	// 	}
	// 	System.out.println();
 // 	}

 // 	public static void vararg3( int x, int... args1, float f)
	// {
	// 	System.out.println("x = " + x );
	// 	System.out.println("args1.length = " + args1.length );
	// 	System.out.print("args = ");
	// 	for( int i : args1 )
	// 	{
	// 		System.out.print(i + ","); 
	// 	}
	// 	System.out.println();

	// 	System.out.println("f = " + f);
 // 	}

	public static void main(String argv[])
	{
		vararg1(1,5,6,7);
		// vararg2(1,5,6,7,1.0,2.0);
		// varargs3(1,2,3,4,5.0);

		int [] arr = {2, 3 ,4 ,5};
		vararg1(1,arr);
	}
}