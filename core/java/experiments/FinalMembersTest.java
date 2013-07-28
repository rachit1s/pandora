// final members
import java.lang.*;

class A
{
	public static int counter = 0 ;
	public final int a = counter++;
}

class B extends A
{

}

class FinalMembersTest
{
	public static void main(String argv[])
	{
		A a = new A();
		B b = new B();

		System.out.println("a.a = " + a.a);
		System.out.println("b.a = " + b.a );
	}
}