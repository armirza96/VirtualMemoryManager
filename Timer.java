
public class Timer implements Runnable {
    Thread thread;
    boolean on;
    int time = 1000;
    
    public Timer() {
        thread = new Thread(this);
        on = true;
        thread.start();
        
    }

    @Override
    public void run() {
        while(on) {
            
            //System.out.println("Current Time: " + time);
            synchronized(thread) {
                try {
                    thread.wait(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            time++;
        }
    }

    public int getTime() {
        return time; 
    }
    
    public void stop() {
        on = false;
    }

    public void start() {
        on = true;
    }
    
    public void join() {
		try {
			on = false;
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

