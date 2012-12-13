package basic;

public class MainThread {
	static void noThread()
	{
		LiftOff liftOff = new LiftOff(10);
		liftOff.run();
	}
	static void withThread()
	{
		LiftOff liftOff = new LiftOff(10);
		Thread t = new Thread(liftOff);
		t.start();
//		t.run();
	}
	
	public static void main(String[] args) {
//		noThread();
		withThread();
		System.out.println("Main Thread ending... ");
	}
}
