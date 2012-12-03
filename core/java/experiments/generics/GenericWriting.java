import java.lang.*;
import java.util.*;

class Fruit
{

}

class Apple extends Fruit
{

}

public class GenericWriting
{
	static <T> void writeExact( List<T> list, T item)
	{
		System.out.println("Adding " + item + " to " + list);
		list.add(item);
	}

	static List<Apple> apples = new ArrayList<Apple>();
	static List<Fruit> fruit = new ArrayList<Fruit>();

	static void f1()
	{
		writeExact(apples, new Apple());
		writeExact(fruit, new Apple());
	}

	static <T> void writeWithWildcard( List<? super T> list, T item)
	{
		System.out.println("Adding " + item + " to " + list);
		list.add(item);
	}

	// static <T> void writeWithWildcardExtends( List<? extends T> list, T item)
	// {
	// 	System.out.println("Adding " + item + " to " + list);
	// 	list.add(item);
	// }

	static void f2()
	{
		writeWithWildcard(apples,new Apple());
		writeWithWildcard(fruit, new Apple());
	}

	static void f3()
	{
		List<? extends Fruit> fruits = new ArrayList<Apple>();
		// fruits.add(new Apple());
		// fruits.add(new Fruit());

		Fruit f = fruits.get(0);

		// Apple a = fruits.get(0);

		// List<? super Fruit> fts = new ArrayList<Apple>();
		List<? super Fruit> fts = new ArrayList<Fruit>();
		fts.add(new Fruit());
		fts.add(new Apple());

		fts.add(new Object());

		List<Fruit> frs = new ArrayList<Apple>();

		
	}

	public static void main(String argv[])
	{
		f1();
		f2();
		f3();
	}
	

}