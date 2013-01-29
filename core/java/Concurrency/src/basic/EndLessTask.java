package basic;

public class EndLessTask implements Runnable{
	private static int taskCount = 0;
	private final int id = taskCount++;
	public EndLessTask()
	{
		System.out.println("TaskId : " + taskCount);
	}
	@Override
	public void run(){
		while(true)
		try{
			Thread.sleep(100000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		try {
			Thread.currentThread().sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true)
		{
			new Thread(new EndLessTask()).start();
		}
	}
}
