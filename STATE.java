
public enum STATE {
    STARTED("Started"), 
    RESUMED("Resumed"),
    FINISHED("Finished");

    String value;

    STATE(String value) {
        this.value = value;
    }
}
