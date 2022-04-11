import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CommandReader {
    //BufferedReader commandReader;
    RandomAccessFile reader;

    Process currentProcess;

    public CommandReader() {
        try {
            //commandReader = new BufferedReader(new FileReader("commands.txt"));
            reader = new RandomAccessFile("commands.txt", "r");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void readNextCommand() {
        String line;
        try {
            line = reader.readLine();
            //System.out.println(line);
            String[] values = line.split(" ");

            String commandType = values[0];
            switch(commandType) {
                case "Store":
                    String var = values[1];
                    String val = values[2];

                    store(var, val);
                break;
                case "Lookup":
                    lookup(values[1]);
                break;
                case "Release":
                    release(values[1]);
                break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if(reader.getFilePointer() >= reader.length())
                reader.seek(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void store(String var, String val) {
       currentProcess.store(var, val);
    }

    public void release(String id) {
        currentProcess.release(id);
    }

    public void lookup(String id) {
        currentProcess.lookup(id);
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setProcess(Process p) {
        this.currentProcess = p;
    }
}
