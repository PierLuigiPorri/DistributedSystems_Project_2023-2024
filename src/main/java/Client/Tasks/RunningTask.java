package Client.Tasks;

import Client.Node;
import Client.ReliableBroadcastLibrary;

public abstract class RunningTask {
    protected ReliableBroadcastLibrary library;
    public RunningTask(ReliableBroadcastLibrary library){
        this.library = library;
    }
}
