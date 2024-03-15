package Client.Tasks;

import Client.ReliableBroadcastLibrary;
import Client.Tuple;
import Messages.ContentMessage;
import Messages.DropMessage;

import java.io.IOException;

public class SendingTask extends RunningTask{

    public SendingTask(ReliableBroadcastLibrary library) {
        super(library);
    }

    public void run() {
        while (this.library.getNode().getState().equals(Client.State.NORMAL)) {
            try {
                //TODO: lock the queue
                ContentMessage message = this.library.getNode().peekOutgoingMessage();
                this.library.getNode().queueUnstableMessage(message);
                message.setSequenceNumber(this.library.getNode().getSequenceNumber());
                this.library.sendMulticast(message);
                int pseudoTimer = 0;
                Tuple tuple = new Tuple(message.getSequenceNumber(), this.library.getNode().getId());

                while (this.library.getNode().getState().equals(Client.State.NORMAL) && pseudoTimer < 50) {
                    Thread.sleep(20);
                    pseudoTimer++;
                    if (this.library.getNode().getAcks(tuple) == this.library.getViewSize() - 1) {
                        this.library.getNode().writeOnDisk(this.library.getNode().dequeueOutgoingMessage());
                        this.library.getNode().dequeueUnstableMessage();
                        this.library.getNode().incrementSequenceNumber();
                        this.library.getNode().removeAcks(tuple);
                        this.library.getSendingThreads().remove(this);
                        return;
                    }
                }
                if (this.library.getNode().getState().equals(Client.State.NORMAL)) {
                    this.library.sendMulticast(new DropMessage(this.library.getNode().getId(), message.getSequenceNumber()));
                    this.library.getNode().removeAcks(tuple);
                }
            } catch (IOException | InterruptedException e) {
                this.library.getSendingThreads().remove(this);
                e.printStackTrace();
            }
        }
    }
}