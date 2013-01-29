import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 
 */

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 * 
 */
class Meal {
	private static int itemCount = 0;
	private int itemNo;

	Meal() {
		itemNo = itemCount++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Meal [itemNo=" + itemNo + "]";
	}
}

class Restaurant {
	Meal meal = null;
	Chef chef = new Chef(this);
	WaitPerson waitPerson = new WaitPerson(this);
	ExecutorService es = null;

	/**
	 * 
	 */
	public Restaurant() {
		es = Executors.newCachedThreadPool();
		es.execute(chef);
		es.execute(waitPerson);

		// es.shutdown();
		// try {
		// es.awaitTermination(100, TimeUnit.HOURS);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}
}

class Chef implements Runnable {
	Restaurant restaurant = null;
	int count = 0;

	Chef(Restaurant r) {
		restaurant = r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (Thread.interrupted() == false) {
				synchronized (this) {
					while (restaurant.meal != null)
						wait();
				}

				synchronized (restaurant.waitPerson) {
					if (++count == 10) {
						restaurant.es.shutdownNow();
						return;
					}
					restaurant.meal = new Meal();
					System.out.println("Chef prepared : " + restaurant.meal);
					restaurant.waitPerson.notifyAll();
				}
			}
		} catch (Exception e) {
			System.out.println("Chef interrupted");
		}
	}
}

class WaitPerson implements Runnable {
	Restaurant restuarant = null;

	WaitPerson(Restaurant r) {
		restuarant = r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (Thread.interrupted() == false) {
				synchronized (this) {
					while (restuarant.meal == null)
						wait();
				}

				synchronized (restuarant.chef) {
					System.out.println("waitPerson consumed : " + restuarant.meal);
					restuarant.meal = null;
					
					restuarant.chef.notifyAll();
				}
			}
		} catch (Exception e) {
			System.out.println("waitPerson interupted.");
		}
	}
}

public class TestMain {
	public static void main(String[] args) {
		new Restaurant();
	}
}