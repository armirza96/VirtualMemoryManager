import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Scheduler {
	
	//Globa timer variable to keep track of time
	//starts at t = 1000
    Timer timer;
    
    Thread fileReader;
    Thread scheduler;

    Queue<Process> mainQueue;
    Queue<Process> waitingQueue;
    Queue<Process> allProcesses;

    int totalCores = 0;

    Semaphore semaphore = new Semaphore(1);

    /**
     * We define the constructor for t=0
     * @param processes
     */
    public Scheduler() {
        mainQueue = new LinkedList<Process>();
        waitingQueue = new LinkedList<Process>();

        allProcesses = new LinkedList<Process>();

    }

    public void start() {
        // start timer
        // executes on another thread
        timer = new Timer();

        try {
            readProcessesFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Runnable process = () -> {
            scheduleProcesses();
        };

        // put all other execution of the scheduler
        // on another thread
        // we simulate the scheduler here
        scheduler = new Thread(process, "Scheduler");
        scheduler.start();
    }

    // main scheduling method
    private void scheduleProcesses() {

            int currentTime = timer.getTime();
            System.out.println("Scheduler started.");
            while(!mainQueue.isEmpty()) {
                currentTime = timer.getTime();
                // gets the current head of the queue and returns it
                // removes it as well
                Process p = mainQueue.poll();
                
                // not neccessarily needed but its a good check just in case
                if(p != null) {
                    //System.out.println("Process:" + p.id +", Starting Time: " + p.startTime + ", curret time " + getTime());
                    
                    // skip to next process if arrival time of the next 
                    // process is more than the current time
                    if(p.startTime > currentTime) {
                        //System.out.println("Skip over Process:" + p.id +", Time: " + getTime());
                        mainQueue.add(p);
                        continue;
                    }
                    
                    int timeSlotGranted = calculateTimeSlot(p);
    
                    // write to the file that the process has started or resumed
                    if(currentTime >= p.startTime && p.state != STATE.STARTED) {
                        System.out.println("Clock: " + getTime() + ", Process " + p.id + ": Started.");
                        writeToFile("Clock: " + getTime() + ", Process " + p.id + ": Started.");
                        p.startProcess();
                    }
    
                    // add to the processed time 
                    // all the time slots time its been granted
                    p.processedTime += timeSlotGranted;
                    
                    
                    int stopTime = currentTime + timeSlotGranted;
                   // System.out.println("Process:" + p.id +", Current time: " + currentTime + ", Stoptime: " + stopTime);
                    Process nextProcess = getNextProcessToStart();
                    // run current process p for the requested duration
                    p.startReadingFile();
    
                    while(currentTime < stopTime) {
                        currentTime = getTime();

                        if(nextProcess != null && currentTime == nextProcess.startTime) {
                            writeToFile("Clock: " + currentTime + ", Process " + nextProcess.id + ": Started.");
                            nextProcess.startProcess();
                            nextProcess = null;
                        }
                    }
    
                    p.stopReadingFile();
    
                    System.out.println("Stoptime: " + stopTime + ", Current time: " + getTime());
    
                    // check to see if we need to add this process back into the main queue
                    // or add a process from the waiting queue
                    if(p.processedTime < p.burstTime) {
                        mainQueue.add(p);
                    } else {
                        try {
                            p.finish();
                            p.thread.join();
                            
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                        writeToFile("Clock: " + getTime() + ", Process " + p.id + ": Finished.");
                        mainQueue.add(waitingQueue.poll());
                    }
                    
    
                    System.out.println("Finished: " +p.id+", time: " + getTime() );
                }
            } // end while loop

        
        

        if(!mainQueue.isEmpty()) {
            printOutQueue(mainQueue);
            scheduleProcesses();
        } else {
            timer.stop();
            timer.join();
            System.out.println("DONE");
        }

    }

    /**
     * reads file for each process
     * @throws IOException
     */
    private void readProcessesFile() throws IOException {      
        BufferedReader reader = new BufferedReader(new FileReader("processes.txt"));

        totalCores  = Integer.parseInt(reader.readLine());
        int totalProcessCount  = Integer.parseInt(reader.readLine());

        String line;

        CommandReader cmdReader = new CommandReader();
        MemoryManager manager = readMemoryConfigFile();

        int processCount = 0;
        while((line = reader.readLine()) != null) {
            String[] values = line.split(" ");

            String id = values[0];
            int arrivalTime = Integer.parseInt(values[1]);
            int burstTime = Integer.parseInt(values[2]);

            Process p = new Process(id, arrivalTime, burstTime, cmdReader, manager,semaphore, timer);
            
            if(processCount < totalCores) {
                mainQueue.add(p);
            } else {
                waitingQueue.add(p);
            }
            allProcesses.add(p);

            processCount++;
        }
        
        reader.close();
    }

        /**
     * reads file for each process
     * @throws IOException
     */
    private MemoryManager readMemoryConfigFile() throws IOException {      
        BufferedReader reader = new BufferedReader(new FileReader("memconfig.txt"));

        int totalPages  = Integer.parseInt(reader.readLine());
        int k  = Integer.parseInt(reader.readLine());
        int timeOut  = Integer.parseInt(reader.readLine());

        reader.close();

        return new MemoryManager(new MemoryConfig(totalPages, k, timeOut));
    }

    public void writeToFile(String output) {
        try {
            FileWriter w = new FileWriter("output.txt", true);
            w.write("\n"+output);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateTimeSlot(Process p) {
        int burstTime = p.burstTime;
        int processedTime = p.processedTime;

        Random rand = new Random();
        
        return Math.max(1,Math.min(rand.ints(0, burstTime).findFirst().getAsInt(), rand.ints(0, burstTime - processedTime).findFirst().getAsInt()));
    }

    public int getTime() {
        return timer.getTime();
    }

    public Process getNextProcessToStart() {
        Iterator<Process> it = allProcesses.iterator();
        
        while(it.hasNext()) {
            Process p = it.next();
            if(p.state != STATE.STARTED) {
                System.out.println("Next Process: " + p.id);
                return p;
            }

        }
        return null;
    }

    // just for debugging purposes
    private void printOutQueue(Queue<Process> queue) {
    	System.out.println("-------------------------------------");
        queue.forEach( p -> System.out.println(p.id));
    }


}
