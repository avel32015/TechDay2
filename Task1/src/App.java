/*
 * Задача: Producer & Consumer
 * Реализуйте потоки производителя (Producer) и потребителя (Consumer), совместно пользующихся буфером фиксированного размера.
 * 
 * Первый поток должен помещать числа в буфер в бесконечном цикле, а второй — бесконечно извлекать их оттуда. 
 * Порядок добавления и извлечения чисел не имеет значения. Данные производителя не должны теряться: либо считаться потребителем, либо остаться в буфере.
 * 
 * Решение по организации ожидания чтения, в случае пустого буфера, или записи, в случае заполненного буфера, остается за вами.
 * 
 * Описание:
 * Быстрая реализация возможна с использованием стандартных блокирующих очередей ArrayBlockingQueue, LinkedBlockingQueue.
 * Недостатки ArrayBlockingQueue - использование блокировок при добавлении и удалении.
 * LinkedBlockingQueue - раздельная блокировка добавления и удаления, но повышенный расход памяти при использовании связанного списка.
 * 
 * Реализуем алгоритм раздельного блокирования добавления или удаления только в случае переполнения/опустошения очереди, но без лишнего перераспределения памяти.
*/

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
		long value = anyValue++;
		int next = (head + 1) % capacity; 
		
		// Если очередь заполнена, ждём освобождения
		if (next == tail) {
			lock.lock();
			try {
				while (next == tail) {
					System.out.println("Buffer is full, tail " + tail);
					consume.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			} finally {
				lock.unlock();
			}
		}
		
		System.out.println("Produce " + value + ", head " + head);
		buffer[head] = value;
		int prev = head;
		head = next;
		
		// Если очередь была пуста, сигнализируем о заполнении
		if (prev == tail) {
			lock.lock();
			try {
				System.out.println("Buffer produced, tail " + tail);
				produce.signal();
			} finally {
				lock.unlock();
			}
		}
		
		if (value >= 1.5 * capacity) Thread.currentThread().interrupt();
	}
	
	void consumer() {
		long value = 0;
		int next = (tail + 1) % capacity;
		
		// Если очередь пуста, ждём заполнения
		if (head == tail) {
			lock.lock();
			try {
				while (head == tail) {
					System.out.println("Buffer is empty, tail " + tail);
					produce.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			} finally {
				lock.unlock();
			}
		}
		
		value = buffer[tail];
		System.out.println("Consume " + value + ", tail " + tail);
		int prev = tail;
		tail = next;
		next = (head + 1) % capacity;
		
		// Если очередь была заполнена, сигнализируем об освобождении
		if (next == prev) {
			lock.lock();
			try {
				System.out.println("Buffer consumed, prev " + prev);
				consume.signal();
			} finally {
				lock.unlock();
			}
		}
		
		if (value >= 1.5 * capacity) Thread.currentThread().interrupt();
	}

	public static void main(String[] args) {
		App app = new App();
		
		new Thread( () -> {
			while( !Thread.interrupted() ) app.consumer(); 
		} ).start();
		
		new Thread( () -> {
			while( !Thread.interrupted() ) app.producer(); 
		} ).start();
	}
}
