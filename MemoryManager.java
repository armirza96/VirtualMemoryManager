import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MemoryManager implements Runnable {
    Thread thread;

    Map<String, Page> pages = new HashMap<String, Page>();
    MemoryConfig config;
    //Page[] pages;

    public MemoryManager(MemoryConfig memoryConfig) {
        this.config = memoryConfig;
        thread = new Thread(this);
    }

    public void store(String id, String value, int time, String processId) {
        if(pages.size() < config.pages)
            pages.put(id, new Page(id, value, time));
        else {
            Map<String, String> vm = readFromVm();

            vm.put(id, value);

            writeToVm(vm);
        }
        writeToFile(getOutPut("Store", new String[] {id, value}, processId, time));    
    }

    public void release(String id, int time, String processId) {
        if(pages.containsKey(id))
            pages.remove(id);
        else {
            Map<String, String> vm = readFromVm();

            vm.remove(id);

            writeToVm(vm);
        }
        writeToFile(getOutPut("Release", new String[] {id},  processId, time));    
    }

    public String lookup(String id, int time, String processId) {
        // PAGE FAULT HAS OCCURRED
        // vairbale is in disk memory
        if(!pages.containsKey(id)) { 
            Map<String, String> vm = readFromVm();

            
            if(vm.containsKey(id)) {
                // if pages size == confuig pages we got no more space in main memory
                // and must use the lru-k algo step 2
                if(pages.size() == config.pages) {
                    // just setting the first entry to the chose page
                    Page chosen = pages.entrySet().iterator().next().getValue();
                    //String chosenKey = pages.entrySet().iterator().next().getKey();

                    // were going to use this array list
                    // to keep track of any pages that fulfill the assignmet criteria
                    List<Page> foundPages = new ArrayList<Page>();

                    for(Map.Entry<String, Page> set : pages.entrySet()) { 
                        Page p = set.getValue();

                        int difference = p.lastAccessTime - p.history.get(0);

                        // if the difference btween the config timeout is valid for this process p
                        if(difference > config.timeout) {

                            // check if this process p has a history at k 
                            // that is smaller than the current process chosen
                            if(p.history.get(chosen.history.size() - 1) < chosen.history.get(chosen.history.size() - 1))
                                chosen = p;
                            
                            // if not check if process p and chosen have equal times at hostory[k]
                            // if they do add them to the foundPages that are conflcting
                            else if(p.history.get(chosen.history.size() - 1) == chosen.history.get(chosen.history.size() - 1))
                                foundPages.add(p);
                        }
                    }

                    if(foundPages.size() > 0) {
                        foundPages.add(chosen);
                        for (Page page : foundPages) {
                            if(page.history.get(0) < chosen.history.get(0))
                                chosen = page;
                        }
                    }

                    pages.remove(chosen.id);

                    pages.put(id, new Page(id, vm.get(id), time));
                    
                    store(id, vm.get(id), time, processId);
                    replaceVMVariable(vm, id, chosen.id, chosen.content.value, processId, time);
                } else
                    pages.put(id, new Page(id, vm.get(id), time));

                writeToFile(getOutPut("Lookup", new String[] {id, vm.get(id)}, processId, time));    

                return vm.get(id);
            }
            else   
                return "-1";
        } else {
            Page p = pages.get(id);
            int lastAccessTime = p.lastAccessTime;

            int difference = time - lastAccessTime;
            if(difference < config.timeout) {
                p.setAcccessTime(time);
            } else {
                List<Integer> history = p.history;

                int lcp = lastAccessTime - history.get(0);
    
                if(history.size() == 1) {
                    history.set(0, history.get(0) + lcp);
                } else {
                    for(int i = 1; i <= config.k; i++)
                        history.set(i, history.get(i - 1) + lcp);
                }
                p.setAcccessTime(history.get(0));
            }

            writeToFile(getOutPut("Lookup", new String[] {id, p.content.value}, processId, time));    
            return p.content.value;
        }
    }

    public void replaceVMVariable(Map<String, String> vm, String oldId, String newId, String value, String processId, int time) {
        vm.remove(oldId);
        vm.put(newId, value);

        writeToVm(vm);  

        writeToFile(getOutPut("Swap", new String[] {oldId, newId}, processId, time)); 
    }

    public void writeToVm(Map<String, String> vm) {
        String output = "Content: [";

        for (Map.Entry<String, String> set : vm.entrySet()) { 
            output += "(" + set.getKey() + ", " + set.getValue() + "),";
        }

        output += "]";
        try {
            FileWriter w = new FileWriter("vm.txt", false);
            w.write(output);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        /**
     * reads file for each process
     * @throws IOException
     */
    private Map<String, String> readFromVm() {     
        Map<String, String> vm = new HashMap<String, String>();
        try {
            BufferedReader  reader = new BufferedReader(new FileReader("vm.txt"));

            String line = reader.readLine();
            if(line != null) {
                String[] content = line.split(":");

                if(content.length == 0) {
                    String arrString = content[1]; // contains [(?,?),(?,?), etc]
                    
                    java.util.regex.Matcher m = Pattern.compile("\\((.*?)\\)").matcher(arrString);
                    
                    while (m.find()) {
                        String dict = m.group(1);
                        System.out.println(dict);
            
                        String[] keyAndValue = dict.split(",");
            
                        vm.put(keyAndValue[0], keyAndValue[1]);
                    }
                }
            }

            reader.close();  

        } catch (IOException e) {
            e.printStackTrace();
        }

        return vm;
    }

    public void writeToFile(String output) {
    	//System.out.println("Current Thread ID- " + Thread.currentThread().getId() + " For Thread- " + Thread.currentThread().getName());   
        try {
            FileWriter w = new FileWriter("output.txt", true);
            w.write("\n"+output);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOutPut(String commandType, String[] data, String processId, int time) {//getOutPut(String commandType, String variable, String value,String processId, int time) {
        String output = "";
        switch(commandType) {
            case "Store":
                output = "Clock: " + time + ", Process " + processId + ", "+commandType + ": Variable " + data[0] + ", Value: " + data[1]; 
            break;
            case "Lookup":
                output = "Clock: " + time + ", Process " + processId + ", "+commandType + ": Variable " + data[0] + ", Value: " + data[1]; 
            break;
            case "Release":
                output = "Clock: " + time + ", Process " + processId + ", "+commandType + ": Variable " + data[0];
            break;
            case "Swap":
                output = "Clock: " + time + ", Process " + processId + ", "+commandType + ": Variable " + data[0] + ", Value: " + data[1]; 
            break;
        }
        
        return output;
    }

    @Override
    public void run() {
        while(true) {

        }
    }
}
