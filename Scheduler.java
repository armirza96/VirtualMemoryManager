import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;

public class Scheduler {
    public Scheduler() {

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
}
