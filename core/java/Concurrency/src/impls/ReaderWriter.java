package impls;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReaderWriter {

	public static void main(String[] args) {
		ReadWriteLock lock = new ReentrantReadWriteLock(true);
	}
}
