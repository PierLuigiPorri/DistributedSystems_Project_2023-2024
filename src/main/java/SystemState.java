import java.util.HashMap;
import java.util.Map;

public class SystemState {
    private HashMap<Integer, Integer> systemState;

    public SystemState() {
        this.systemState = new HashMap<Integer, Integer>();
    }

    public String toString() {
        return systemState.toString(); //TODO: Implement this method
    }

    public void setSystemState(HashMap<Integer, Integer> systemState) {
        this.systemState = systemState;
    }
}
