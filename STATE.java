
public enum STATE {
    ARRIVED("Arrived"),
    STARTED("Started"), 
    PAUSED("Paused"),
    RESUMED("Resumed"),
    TERMINATED("Terminated");

    String value;

    STATE(String value) {
        this.value = value;
    }
}
