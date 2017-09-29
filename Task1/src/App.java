import java.util.concurrent.locks.*;

public class App {

	volatile int head = 0;
	volatile int tail = 0;
	
	int capacity = 100;
	long buffer[] = new long[capacity];
	long anyValue = 0;
	
	final Lock lock = new ReentrantLock();
	final Condition consume = lock.newCondition();
	final Condition produce = lock.newCondition();
	
	void producer() {
		long value = ++anyValue;
		int pos = (head + 1) % capacity; 
		lock.lock();
		try {
			while ( pos == tail ) {
				System.out.println("Buffer is full");
				consume.await();
			}
			System.out.println("Produce " + value + ", head " + head);
			buffer[head] = value;
			head = pos;
			produce.signal();
		} catch (InterruptedException e) {
		} finally {
			lock.unlock();
		}
		try {
			Thread.sleep(0);
		} catch (InterruptedException e) { }
	}
	
	void consumer() {
		long value = 0;
		int pos = (tail + 1) % capacity;
		lock.lock();
		try {
			while (tail == head ) {
				System.out.println("Buffer is empty");
				produce.await();
			}
			System.out.println("Consume " + value + ", tail " + tail);
			value = buffer[tail];
			tail = pos;
			consume.signal();
		} catch (InterruptedException e) {
		} finally {
			lock.unlock();
		}
	}

	public static void main(String[] args) {
		App app = new App();
		new Thread( () -> {
			while( !Thread.interrupted() ) { app.producer(); Thread.yield(); } 
		} ).start();
		new Thread( () -> {
			while( !Thread.interrupted() ) { app.consumer(); Thread.yield(); } 
		} ).start();
	}
}
