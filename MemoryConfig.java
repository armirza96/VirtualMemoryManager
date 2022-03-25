public class MemoryConfig {
    int pages;
    int k;
    int timeout; // in mili

    public MemoryConfig(int pages, int k, int timeout) {
        this.pages = pages;
        this.k = k;
        this.timeout = timeout;
    }
}
