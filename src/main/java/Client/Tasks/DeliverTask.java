package Client.Tasks;

import Client.ReliableBroadcastLibrary;
import Messages.Message;

import java.io.IOException;

public class DeliverTask extends RunningTask {

    public DeliverTask(ReliableBroadcastLibrary library) {
        super(library);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
                Message processingMessage = this.library.getNode().dequeueIncomingMessage();
                if (processingMessage != null) {
                    this.library.processMessage(processingMessage);
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}