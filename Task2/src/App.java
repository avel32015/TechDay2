import java.util.concurrent.*;

public class App {

	public class Foo {
		
		private Phaser ph = new Phaser(3);
		
	    private boolean awaitPhase(int phase) {
	    	int n = 0; 
	    	if (ph.getPhase() < phase) {
	    		while (0 <= n && n < phase) {
	    			if (n + 1 == phase) { 
	    				n = ph.arriveAndDeregister(); 
	    				return true; 
	    			}
	    			n = ph.arriveAndAwaitAdvance();
	    		}
	    	}
	    	return false;
	    }
	    
	    
	    public void first() {
	    	if (awaitPhase(1)) System.out.println("first");
	    	else System.out.println("illegal first");
	    }
	    
	    public void second() {
	    	if (awaitPhase(2)) System.out.println("second");
	    	else System.out.println("illegal second");
	    }
	    
	    public void third() {
	    	if (awaitPhase(3)) System.out.println("third");
	    	else System.out.println("illegal third");
	    }
	}
	
	public static void main(String[] args) {
		App app = new App();
		ExecutorService exec = Executors.newCachedThreadPool();
		
		Foo f1 = app.new Foo();
		exec.submit( () -> { f1.second(); } );
		exec.submit( () -> { f1.third(); } );
		exec.submit( () -> { f1.first(); } );
	}

}
