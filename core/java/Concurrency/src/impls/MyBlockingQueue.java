package impls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MyBlockingQueue {
//	Lock lock = new ReentrantLock();
	Object myLock = new Object();
	int maxSize;
	List<Integer> list = new ArrayList<Integer>();
	public MyBlockingQueue(int maxSize)
	{
		this.maxSize = maxSize;
	}
	public void put(Integer t) throws InterruptedException
	{
		synchronized (myLock) {
			while( list.size() == maxSize )
			{
				System.out.println("put : " + Thread.currentThread() + " going into wait.");
				myLock.wait();
			}
			
			System.out.println("put : putting integer " + t );
			list.add(t);
			myLock.notifyAll();
		}
	}
	
	public Integer get() throws InterruptedException
	{
		synchronized (myLock) {
			while(list.size() == 0 )
			{
				System.out.println("get : " + Thread.currentThread()+ " going into wait.");
				myLock.wait();
			}
			
			Integer t = list.get(list.size()-1);
			list.remove(list.size()-1);
			myLock.notifyAll();
			System.out.println("get : removing integer : " + t);
			return t;
		}
	}

	static class Producer extends Thread
	{
		static AtomicInteger ai = new AtomicInteger(0);
		static int producerID = 0;
		int id;
		MyBlockingQueue q; 
		Producer(MyBlockingQueue q)
		{
			this.id = producerID++;
			this.q = q;
		}
		public void run()
		{
			while(true)
			{
				try 
				{
					int i = ai.addAndGet(1);
					q.put(i);
					System.out.println("Producer : " + this + " put Integer : " + i);
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public String toString()
		{
			return "Producer : " + id ;
		}
	}
	
	static class Consumer extends Thread
	{
		static int consumerID = 0;
		int id;
		MyBlockingQueue q; 
		Consumer(MyBlockingQueue q)
		{
			this.id = consumerID++;
			this.q = q;
		}
		public void run()
		{
			while(true)
			{
				Integer i;
				try 
				{
					i = q.get();
			
					System.out.println("Consumer : " + this + " got Integer : " + i);
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public String toString()
		{
			return "Consumer : " + id ;
		}
	}
	public static void main(String[] args) 
	{
		int noOfConsumers = 5;
		int noOfProducers = 10;
		ArrayList<Consumer> consumers = new ArrayList<Consumer>(noOfConsumers);
		ArrayList<Producer> producers = new ArrayList<Producer>(noOfProducers);
		
		MyBlockingQueue q = new MyBlockingQueue(10);
		for( int i = 0 ; i < noOfConsumers ; i++ )
		{
			Consumer c = new Consumer(q);
			consumers.add(c);
			c.start();
		}
		
		for(int i = 0 ; i < noOfProducers ; i++ )
		{
			Producer p = new Producer(q);
			producers.add(p);
			p.start();
		}
		
		try {
			TimeUnit.HOURS.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
}
