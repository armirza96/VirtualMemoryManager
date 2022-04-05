import java.util.ArrayList;
import java.util.List;

public class Page {
    String id;
    MemoryVariable content;
    int lastAccessTime;
    List<Integer> history = new ArrayList<Integer>();

    public Page(String id, int value) {
        this.id = id;
        //this.value =  value;
        content = new MemoryVariable(id, value);
    }

    public void setValue(int value) {
        //this.value = value;
        content.setValue(value);
    }

    public void setAcccessTime(int accessTime) {
        this.lastAccessTime = accessTime;
        history.add(accessTime);
    }
}
