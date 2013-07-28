class A
{
    int x;
}

public class TestCopyConstructure
{
  public static void main(String argv[])
  {
    A a = new A();
    a.x = 10;
    A b = new A(a);
    System.out.println("b = " + b.x ) ;
  }
}
