import java.lang.*;

class OuterClass<T>
{
	class InnerClass
	{
		T t;
		public InnerClass(T t)
		{
			this.t = t;
		}
	}

	InnerClass ic ;

	public OuterClass(T t)
	{
		ic = new InnerClass(t);
	}

	public T getT()
	{
		return ic.t;
	}
}

public class InnerClassType
{
	public static void main(String argv[])
	{
		OuterClass<String> oc = new OuterClass<String>("abc");
		System.out.println("oc = " + oc.getT() );
	}
}