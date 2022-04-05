import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class MemoryManager {
    Dictionary<String, Page> pages = new Hashtable<String, Page>();
    MemoryConfig config;
    //Page[] pages;

    public MemoryManager() {
        
    }

    public void store(String id, int value) {
        if(pages.get(id) != null) {

        } else
            pages.put(id, new Page(id, value));
    }

    public void release(String id) {
        pages.remove(id);
    }

    public void lookup(String id) {

    }

    
    private void lruAlgorithm() {

    }

    private void accessPage(int currentAccessTime) {
        Page p = pages.get(id);

        int lastAccessTime = p.lastAccessTime;

        if(currentAccessTime - lastAccessTime < 500) {
            p.lastAccessTime = currentAccessTime;
        } else {
            List<Integer> lastAccessTimes = p.acessTimes;

            int lcp = lastAccessTime - lastAccessTimes.get(0);

            for(int i = 1; i <= config.k; i++)
            lastAccessTimes(i) = 
        }
    }

         /**
     * reads file for each process
     * @throws IOException
     */
    private void readDisk() throws IOException {      
        BufferedReader reader = new BufferedReader(new FileReader("vm.txt"));

        String line = reader.readLine();

        reader.close();
        
         
    }

    public void writeToVm(String output) {
    	System.out.println("Current Thread ID- " + Thread.currentThread().getId() + " For Thread- " + Thread.currentThread().getName());   
        try {
            FileWriter w = new FileWriter("vm.txt", true);
            w.write("\n"+output);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
