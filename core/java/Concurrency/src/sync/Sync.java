package sync;

class MyThread extends Thread
{
	Sync s;
	public MyThread(Sync s)
	{
		this.s = s;
	}
	public void run()
	{
		s.syncStatement();
	}
}

public class Sync {
	private static int ID = 1;
	private int id ;
	
	public static synchronized void staticSyncMethod()
	{ 
		System.out.println("entered staticSyncMethod" );
		System.out.println("exiting staticSyncMethod" );
	}
	public synchronized void syncMethod()
	{ 
		syncMethod2();
		System.out.println("entered for object " + id );
		System.out.println("exiting for object " + id);
	}

	public synchronized void syncMethod2()
	{ 
		System.out.println("stepped inside " + Thread.currentThread() + " for object " + id);
		System.out.println("exiting for object " + id);
	}
	public void syncStatement()
	{
		System.out.println("stepped inside " + Thread.currentThread() + " for object " + id);
		synchronized (this) {
			System.out.println("doing work " + Thread.currentThread() + " for object " + id);
		}
		System.out.println("stepping outside " + Thread.currentThread() + " for object " + id);
	}
	public Sync()
	{
		id = ID++;
	}
	
	public static void main(String[] args) {
		Sync s1 = new Sync();
		Sync s2 = new Sync();
		
		MyThread t1 = new MyThread(s1);
		MyThread t2 = new MyThread(s1);
		t1.start();
		t2.start();
		
	}

}
