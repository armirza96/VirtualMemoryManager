import java.util.Random;
import java.util.concurrent.Semaphore;

public class Process implements Runnable {
    Thread thread;
    String id;
    int startTime;
    int burstTime;
    volatile STATE state;
    volatile int processedTime = 0;

    CommandReader commandReader;
    MemoryManager memoryManager;

    Semaphore sem;

    boolean readFile;

    Timer timer;

    public Process(String id, int startTime, int burstTime, 
                    CommandReader commandReader, MemoryManager memoryManager,
                    Semaphore sem, Timer timer) {
        this.id = id;
        this.startTime = startTime;
        this.burstTime = burstTime;

        this.commandReader = commandReader;
        this.memoryManager = memoryManager;

        this.sem = sem;
        this.readFile = false;
        this.timer = timer;

        thread = new Thread(this, id);
    }

	@Override
	public void run() {
        while(state == STATE.STARTED) {
            try {
                System.out.println("Thread is waiting");
                sem.acquire();
                System.out.println("Thread is continuing");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            commandReader.setProcess(this);

            System.out.println("Read file");
            while(readFile) {
                
                commandReader.readNextCommand();
                
                int sleep = calculateAPICallTime();

                try {
                    Thread.sleep(sleep);
                    //processedTime += sleep;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            sem.release();
            System.out.println("Released");
        }
	}

    public void startProcess() {
        state = STATE.STARTED;
        thread.start();
    }

    public void finish() {
        state = STATE.FINISHED;
        //thread.interrupt();
    }

    public void store(String var, String val) {
        memoryManager.store(var, val, timer.getTime(), id, true);
    }

    public void release(String id) {
        memoryManager.release(id, timer.getTime(), id);
    }

    public void lookup(String id) {
        memoryManager.lookup(id, timer.getTime(), id);
    }
    
    public void startReadingFile() {
        System.out.println("starting read");
        readFile = true;
    }

    public void stopReadingFile() {
        System.out.println("stopping read");

        readFile = false;
    }

    private int calculateAPICallTime() {
        Random rand = new Random();
        int remainingTime = burstTime - processedTime;
        remainingTime = remainingTime <= 1 ? 10 : remainingTime;
        //System.out.println("Remaining tie: " + remainingTime);
        return Math.min(rand.ints(1,remainingTime).findFirst().getAsInt(), rand.ints(100, 1000).findFirst().getAsInt());
    }
}

