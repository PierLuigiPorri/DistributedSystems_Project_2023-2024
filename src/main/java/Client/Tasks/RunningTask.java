package Client.Tasks;

import Client.ReliableBroadcastLibrary;

public abstract class RunningTask extends Thread{
    protected ReliableBroadcastLibrary library;
    public RunningTask(ReliableBroadcastLibrary library){
        this.library = library;
    }
}
