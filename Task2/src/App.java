import java.util.concurrent.*;

public class App {

	public class Foo {
		
		private Phaser ph = new Phaser(3);
		
	    private boolean awaitPhase(int phase) {
	    	int p = ph.getPhase();
	    	if (phase < p) return false;
	    	while (p < phase) {
	    		if (ph.isTerminated()) return false; 
	    		//System.out.println((p + 1) + "/" + phase);
	    		p = ph.arriveAndAwaitAdvance();
	    	}
	    	return true;
	    }
	    
	    public void first() {
	    	if (awaitPhase(1)) {
	    		System.out.println("first");
		    	ph.arriveAndDeregister();
	    	}
	    	else System.out.println("illegal first");
	    }
	    
	    public void second() {
	    	if (awaitPhase(2)) {
	    		System.out.println("second");
		    	ph.arriveAndDeregister();
	    	}
	    	else System.out.println("illegal second");
	    }
	    
	    public void third() {
	    	if (awaitPhase(3)) {
	    		System.out.println("third");
		    	ph.arriveAndDeregister();
	    	}
	    	else System.out.println("illegal third");
	    }
	}
	
	public static void main(String[] args) {
		App app = new App();
		ExecutorService exec = Executors.newCachedThreadPool();
		
		Foo f1 = app.new Foo();
		exec.submit( () -> { f1.third(); } );
		exec.submit( () -> { f1.second(); } );
		exec.submit( () -> { f1.first(); } );
	}

}
