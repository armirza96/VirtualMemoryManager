

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Scheduler {
	
	//Globa timer variable to keep track of time
	// starts at t = 1000
    Timer timer;

    // a volatile nextProcess reference
    // volatile makes sure that the variable is updated across all thread in real time
    // We use the this global variable to our advtange as java is pass by reference for objects
    // we can null this value aand it will null the reference to our current process that needs to be added to the queue
    // without nulling the process itself
    volatile Process nextProcess;
    
    Thread fileReader;
    Thread scheduler;

    LinkedList<Process>  activeQueue;
    LinkedList<Process> deactiveQueue;
    
    // how many processes are within the file
    int totalProcessCount = 0;
    
    // the current amount of processes added to the queues
    int processCount = 0;
    /**
     * We define the constructor for t=0
     * @param processes
     */
    public Scheduler() {
        activeQueue = new LinkedList<Process>();
        deactiveQueue = new LinkedList<Process>();

        //acts as a output break in the output file
        writeToFile("\n--------------------------------------------------------------------");
    }

    public void start() {
        Runnable input = () -> {
            try {
                readInputFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Runnable process = () -> {
            scheduleProcesses();
        };

        // start the file reader method on a different thread
        // so that we can pause and resume adding the processes as we want
        fileReader = new Thread(input, "FileReader");
        fileReader.start();

        // put all other execution of the scheduler
        // on another thread
        // we simulate the scheduler here
        scheduler = new Thread(process, "Scheduler");
        scheduler.start();
        
        // start timer
        // executes on another thread
        timer = new Timer();
    }

    // main scheduling method
    private void scheduleProcesses() {
    	
    	// switching queues from active to deactive
    	// whenever this method is run 
    	// as it only executes recursively once the current
    	// queue is empty
        LinkedList<Process> old = activeQueue;

        activeQueue = deactiveQueue;

        deactiveQueue = old;
        
        while(!activeQueue.isEmpty()) {
        	// gets the current head of the queue and returns it
        	// removes it as well
            Process p = activeQueue.poll();
            
            // not neccessarily needed but its a good check just in case
            if(p != null) {
            	
                int timeSlotGranted = calculateTimeSlot(p);
                p.increaseTimeSlot();

                // internal process method 
                // will change state of process accoridng to its previous state
                // either start or resume here
                p.changeState();
                
                // write to the file that the process has started or resumed
                writeToFile(getOutPut(p, timeSlotGranted));

                // add to the processed time 
                // all the time slots time its been granted
                p.processedTime += timeSlotGranted;
                
                // increase the waiting time for all other processes within the deactive queue
                addWaitingTimeToProcesses(timeSlotGranted);
                
                int currentTime = timer.getTime();
                int stopTime = currentTime + timeSlotGranted;
                System.out.println("Current time: " + currentTime + ", Stoptime: " + stopTime);
                

                
                while(timer.getTime() < stopTime) {
                	// run Process until given stop time
                	
                	// if we dont set the next process to null then we will have thousands of outputs
            		if(nextProcess != null) 
            			// we check if the current time is equal to the next process arrival time
            			// as a process may arrive during the execution time of another process (P3)
            			
                        if(timer.getTime() == nextProcess.arrivalTime) {
                            writeToFile(getOutPut(nextProcess, 0));
                            
                            // Taking advantage of pass by reference variables in java
                            // without mullifying the orignal value
                            nextProcess = null;
                        }
                }
                
                System.out.println("Stoptime: " + stopTime + ", Current time: " + timer.getTime());
                
                // internal process method 
                // will change state of process accoridng to its previous state
                // either paused or terminated
                p.changeState();

                writeToFile(getOutPut(p, timeSlotGranted));   

                // check to see if we need to add this process back into the queues
                if(p.processedTime < p.burstTime) {
                	if(p.timeSlotsGranted % 2 == 0) {
                        int priority = getProcessPriority(p);
                        p.setPriority(priority);
                        writeToFile("Time " + timer.getTime() + ", " + p.id + ", Priority updated to " + priority);
                    } 
                    
                    addProcess(p);
                }
                
                // processes may arrive at the end of the current time slot 
                // same scenario as before
        		if(nextProcess != null) 
                    if(timer.getTime() == nextProcess.arrivalTime) {
                        writeToFile(getOutPut(nextProcess, 0));
                        nextProcess = null;
                    }

                System.out.println("Finished: " +p.id+", time: " + timer.getTime() );
            }
        } // end while loop
        
        // as long as we havent found all the processes in the file
        // keep executing the next lines
        if(processCount < totalProcessCount || processCount == 0) {
        	
        	// notify the file reader that it can
        	// proceed to add the next process
        	// now that the other process have been added to the deactive queue
            resumeFileReader();
            
            // pause scheduler until the file reader notifies it that the process has been added
            // can cause a stack overflow if calle dtoo many times too quickly.
            pauseScheduler();
            
            // make sure all processes stay in decrease priority order starting from the head
            // so 90, 120, 139 where 90 is the head
            reorderProcesses();
            
            // recursively calls itself
            scheduleProcesses();
        } else {
        	// processes are all read
        	// the keep on executing scheduleProcess() recursively
        	// until the dectiveQueue is empty
        	// else execution is complete
            if(!deactiveQueue.isEmpty()) {
                scheduleProcesses();
            } else {
            	timer.stop();
            	timer.join();
                System.out.println("DONE");
                
                return;
            }
        }

    }
    
    /**
     * reads file for each process
     * @throws IOException
     */
    private void readInputFile() throws IOException {      
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));

        totalProcessCount  = Integer.parseInt(reader.readLine());

        String line;

        while((line = reader.readLine()) != null) {
            String[] values = line.split(" ");

            String id = values[0];
            int arrivalTime = Integer.parseInt(values[1]);
            int burstTime = Integer.parseInt(values[2]);
            int priority =Integer.parseInt(values[3]);
            
            Process p = new Process(id, arrivalTime, burstTime, priority);
            
            // using javas pass by reference for objects
            // to create this gloabl variable (variable only holds reference to objects memory location not the actual object)
            // and make it arrive at the correct time
            nextProcess = p;
            
            //if current time < than the arrival time of the currently created process
            // pause the file reader until notified by the scheduler
            // 
            if(timer.getTime() < arrivalTime) {
                pauseFileReader();
            }

            // by pausing the file reader until the scheduler nofifies the file reader
            // the process only gets added after the scheduler finishes scheduling all processes 
            // within the deactive queue
            
            addProcess(p);
            processCount++;
            
            // resume scheduler as it pauses it self until the new process gets added
            resumeScheduler();
        }
        
        reader.close();
    }

    // returns time slot slice in miliseconds
    private int calculateTimeSlot(Process p) {
        return p.priority < 100 ? (140 - p.priority) * 20 : (140 - p.priority) * 5;
    }
 
    // sets process priority
    private int getProcessPriority(Process p) {
        int waitingTime = (p.waitingTime - p.burstTime);
        int bonus = (int) (10 * waitingTime / (timer.getTime() - p.arrivalTime)); 
        return Math.max(100, Math.min(p.priority - bonus + 5, 139));
    }

    public void writeToFile(String output) {
    	System.out.println("Current Thread ID- " + Thread.currentThread().getId() + " For Thread- " + Thread.currentThread().getName());   
        try {
            FileWriter w = new FileWriter("output.txt", true);
            w.write("\n"+output);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addProcess(Process p) {
    	System.out.println("Process aded: " + p.id + " at time: " + timer.getTime());
    	
    	deactiveQueue.add(p);
    }

    public String getOutPut(Process p, int granted) {
        String output = "";
        switch(p.state) {
            case ARRIVED:
                output = "Time " + timer.getTime() + ", " + p.id + ", " + p.state.value;
            break;
            case STARTED:
                output = "Time " + timer.getTime() + ", " + p.id + ", " + p.state.value + ", Granted: " + granted;
            break;
            case PAUSED:
                output = "Time " + timer.getTime() + ", " + p.id + ", " + p.state.value;
            break;
            case RESUMED:
                output = "Time " + timer.getTime() + ", " + p.id + ", " + p.state.value + ", Granted: " + granted + ", left over: " + (p.burstTime - p.processedTime);
            break;
            case TERMINATED:
                output = "Time " + timer.getTime() + ", " + p.id + ", " + p.state.value;
            break;
        }

        return output;
    }

    private void addWaitingTimeToProcesses(int wt) {
        for(Process p: deactiveQueue) {
            p.increaseWaitingTime(wt);
        }
    }
    
    private void reorderProcesses() {
    	 Collections.sort(deactiveQueue, new Comparator<Process>() {
    	     @Override
    	     public int compare(Process p1, Process p2) {
    	         return p1.priority - p2.priority;
    	     }
    	 });
    }
    
    // just for debugging purposes
    private void printOutQueue(LinkedList<Process> queue) {
    	System.out.println("-------------------------------------");
    	for(Process p: queue)
    		System.out.println("In Queue => ID: " + p.id + ", " + p.priority);
    }
    
    public void resumeScheduler() {
        synchronized(scheduler) {
		    scheduler.notify();
		}
    }
    
    public void pauseScheduler() {
        synchronized(scheduler) {
		    try {
				scheduler.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
    
    public void resumeFileReader() {
        synchronized(fileReader) {
		    fileReader.notify();
		}
    }
    
    public void pauseFileReader() {
        synchronized(fileReader) {
		    try {
				fileReader.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
}
