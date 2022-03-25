import java.util.ArrayList;
import java.util.List;

public class Page {
    String id;
    MemoryVariable content;
    int lastAccessTime;
    List<Integer> history = new ArrayList<Integer>();

    public Page(String id, String value) {
        this.id = id;
        this.value =  value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAcccessTime(int accessTime) {
        this.lastAccessTime = accessTime;
        history.add(accessTime);
    }
}
