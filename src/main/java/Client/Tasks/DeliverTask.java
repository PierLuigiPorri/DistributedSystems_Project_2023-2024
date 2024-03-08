package Client.Tasks;

import Client.Node;
import Client.ReliableBroadcastLibrary;
import Messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DeliverTask extends RunningTask implements Runnable {

    public DeliverTask(ReliableBroadcastLibrary library) {
        super(library);
    }

    @Override
    public void run() {
        try {
            Message processingMessage=this.library.getNode().dequeueMessage();
            if(processingMessage!=null){
                this.library.processMessage(processingMessage, this.library.getNode().getView());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
    }
}