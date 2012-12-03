import java.lang.*;

class Manipulator<T extends HasF>
{
	private T t;
	public Manipulator(T t)
	{
		this.t = t;
	}

	public void manipulate()
	{
		t.f();
	}
}

class HasF
{
	public void f()
	{
		System.out.println("f called.");
	}
}

class NoF
{

}
public class TypeErasureTest
{
	public static void main(String argv[])
	{
		Manipulator<HasF> m = new Manipulator<HasF>(new HasF());
		m.manipulate();

			Manipulator<NoF> m2 = new Manipulator<NoF>(new NoF());
		m.manipulate();
	}
}