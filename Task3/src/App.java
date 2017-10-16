/*
FizzBuZZ
Напишите многопоточную версию этого алгоритма.

Один поток проверяет кратность З и выводит «Fizz». Другой поток отвечает за проверку кратности 5 и выводит «Buzz». 
Третий поток отвечает за проверку кратности З и 5 и выводит «FizzBuZZ». Четвертый поток работает с числами.

Алгоритм — https://paste2.org/K8KB9zbL
*/

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class App {
	
	Phaser ph = new Phaser(1);
	ExecutorService ex = Executors.newCachedThreadPool();
	
	volatile int value;
	
    void process(Runnable task) {
    	ph.register();
    	ex.submit( () -> {
    		try {
    			while ( !ph.isTerminated() ) {
    				ph.arriveAndAwaitAdvance();
    				if (ex.isShutdown()) break;
    				task.run();
    			}
    		} finally {
    			ph.arriveAndDeregister();
    		}
    	} );
    }
    
	void run() {
		
		process( () -> {
			if (value % 3 == 0) System.out.println("[Fizz]");
		} );
		
		process( () -> {
			if (value % 5 == 0) System.out.println("[Buzz]");
		} );
		
		process( () -> {  
	        if (value % 3 == 0 && value % 5 == 0) System.out.println("[FizzBuzz]");
		} );

		for (value = 0; value < 20; value++) {
			ph.arriveAndAwaitAdvance();
			System.out.println("[" + value + "]");
		}
		
		ex.shutdownNow();
		ph.arriveAndDeregister();
	}

	public static void main(String[] args) { 
		new App().run();
	}
}
