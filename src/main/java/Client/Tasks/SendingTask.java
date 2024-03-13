package Client.Tasks;

import Client.Node;
import Client.ReliableBroadcastLibrary;
import Client.State;
import Messages.ContentMessage;
import Messages.DropMessage;

import java.io.IOException;

public class SendingTask extends RunningTask implements Runnable {

    public SendingTask(ReliableBroadcastLibrary library){
        super(library);
    }

    public void run() {
        Boolean delivered = false;
        while(this.library.getNode().getState().equals(State.NORMAL)) {
        try {
                    //TODO: lock the queue
                    ContentMessage message = this.library.getNode().peekOutgoingMessage();
                    message.setSequenceNumber(this.library.getNode().getSequenceNumber());
                    this.library.send(message);
                    int pseudoTimer = 0;
                    while (this.library.getNode().getState().equals(State.NORMAL) && pseudoTimer < 50) {
                        Thread.sleep(20);
                        pseudoTimer++;
                        if (this.library.getNode().getAcks(message.getSequenceNumber()) == this.library.getViewSize() - 1) {
                            this.library.getNode().queueStableMessage(this.library.getNode().dequeueOutgoingMessage());
                            this.library.getNode().incrementSequenceNumber();
                            this.library.getNode().removeAcks(message.getSequenceNumber());
                            return;
                        }
                    }
                    this.library.send(new DropMessage(this.library.getNode().getId(), message.getSequenceNumber()));
                    this.library.getNode().removeAcks(message.getSequenceNumber());
        } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
        }
        }
    }
}