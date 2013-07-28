package basic;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestLocking {
	
	static Lock lock = new ReentrantLock();
	public static void main(String[] args) {
		lock.unlock();
		lock.unlock();
	}
}
