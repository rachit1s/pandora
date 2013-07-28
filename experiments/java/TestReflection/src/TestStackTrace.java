

public class TestStackTrace {
	/**
	 * Get the method name for a depth in call stack. <br />
	 * Utility function
	 * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
	 * @return method name
	 */
	public static String getMethodName(final int depth)
	{
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	
	  //System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
	  // return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
	  return ste[ste.length - 1 - depth].getMethodName(); //Thank you Tom Tresansky
	}
	
	public static String getMethodName2(int depth)
	{
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[depth].getMethodName();
	}
	
	public static void main(String[] args) {
//		String value = TestStackTrace.getMethodName2(1);
//		System.out.println("first element : " + value);
//		printStackTrace();
		String name = AnonymousClassMethod();
		System.out.println("name = " + name);
	}
	
	public static String AnonymousClassMethod()
	{
		return new Object(){}.getClass().getEnclosingMethod().getName();
	}

	private static void printStackTrace() {
		
		  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			
		  for( int i = 0 ; i < ste.length ; i++ )
		  System. out.println( ste[i].getFileName() + "#" + ste[i].getLineNumber() + "#" + ste[i].getClassName()+"#"+ste[i].getMethodName());
	}
}