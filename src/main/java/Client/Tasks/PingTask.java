package Client.Tasks;

import Client.ReliableBroadcastLibrary;
import Messages.PingMessage;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class PingTask extends RunningTask implements Runnable {

    public PingTask(ReliableBroadcastLibrary library) {
        super(library);
    }

    //this method sends a ping to all the nodes in the view, which is accessible by node.getView()
    public void run() {
        try {
            //TODO: CHECK IT OUT
            while (true/*this.library.getNode().getState().equals("RUNNING")*/) {
                sleep(50);
                this.library.send(new PingMessage(this.library.getNode().getId()), this.library.getNode().getView());
            }
        } catch (InterruptedException | IOException e){
            e.printStackTrace();
        }
    }
}