import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

public class MemoryManager {
    //Dictionary<String, Page> mainMemory = new Hashtable<String, Integer>();
    MemoryConfig config;
    Page[] pages;
    public MemoryManager() {
        
    }

    public void store(String id, int value) {
        if()
        mainMemory.put(id, value);
    }

    public void release(String id) {
        mainMemory.remove(id);
    }

    public void lookup(String id) {

    }

    
    private void lruAlgorithm() {

    }

    private void accessPage(int currentAccessTime) {
        Page p = findPage("1");

    }

    private Page findPage(String name) {
        for(Page p: pages) {
            if(p.content.Name.equals(name))
                return p;
        }
        return null;
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
