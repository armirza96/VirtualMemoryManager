import java.util.ArrayList;
import java.util.List;

public class Page {
    String id;
    MemoryVariable content;
    int lastAccessTime;
    List<Integer> history = new ArrayList<Integer>();

    public Page(String id, String value, int time) {
        this.id = id;
        
        lastAccessTime = time;
        history.add(time);

        content = new MemoryVariable(id, value);
    }

    public void setValue(String value, int time) {
        //this.value = value;
        lastAccessTime = time;
        content.setValue(value);
    }

    public void setAcccessTime(int accessTime) {
        this.lastAccessTime = accessTime;
    }
}
