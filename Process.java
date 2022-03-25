public class Process implements Runnable {
    Thread thread;
    String id;
    int startingTime;
    int burstTime;

    public Process(String id, int startingTime, int burstTime, int priority) {
        this.id = id;
        this.startingTime = startingTime;
        this.burstTime = burstTime;


        thread = new Thread(this, id);
    }

    public void increaseTimeSlot() {
        timeSlotsGranted++;
    }

    public void setPriority(int newPriority) {
        this.priority = newPriority;
    }

    public void startProcess() {
        thread.start();
    }

    public void terminate() {
        state = STATE.TERMINATED;
        //thread.interrupt();
    }

    public void changeState() {
        if(processedTime >= burstTime) {
            terminate();
            return;
        }
        switch(state) {
            case ARRIVED:
                state = STATE.STARTED;
                startProcess();
            break;
            case STARTED:
                state = STATE.PAUSED;
                //suspendProcess();
            break;
            case PAUSED:
                state = STATE.RESUMED;
                //resumeProcess();
            break;
            case RESUMED:
                state = STATE.PAUSED;
                //suspendProcess();
            break;
        }
    }

    public void increaseWaitingTime(int wt) {
        this.waitingTime += wt;
    }

	@Override
	public void run() {
		while(state != STATE.TERMINATED) {
	        switch(state) {
            case ARRIVED:
                // do nothing
            break;
	            case STARTED:
	            	// start the process
	            	//System.out.println("ID: " + id + " " + state.value);
	            break;
	            case PAUSED:
	            	// Pause process
	            	//System.out.println("ID: " + id + " " + state.value);
	            break;
	            case RESUMED:
	                // Continue Executing process
	            	//System.out.println("ID: " + id + " " + state.value);
	            break;
	            default:
	            	// state doesnt matter
	            break;
	        }
		}
		
		// we can finish this thread once its state has been set to terminated
		System.out.println("THIS PROCESS ID: " + id + " finished");
		return;
	}
}

