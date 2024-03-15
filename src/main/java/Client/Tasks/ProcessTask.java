package Client.Tasks;

import Client.Peer;
import Client.ReliableBroadcastLibrary;
import Client.Tuple;
import Messages.AckMessage;
import Messages.ContentMessage;


public class ProcessTask extends RunningTask{

    public ProcessTask(ReliableBroadcastLibrary library) {
        super(library);
    }

    public void run() {

        try {
            // send an ack back to all the nodes in the view

            ContentMessage contentMessage = this.library.getNode().peekUnstableMessage();
            Tuple tuple = new Tuple(contentMessage.getSequenceNumber(), contentMessage.getSourceId());
            this.library.sendMulticast(new AckMessage(this.library.getNode().getId(), contentMessage.getSequenceNumber(), contentMessage.getSourceId()));

            while (this.library.getNode().getState().equals(Client.State.NORMAL) && this.library.getNode().getAcks(tuple) != -1) {
                Thread.sleep(20);
                //If the number of AckMessages received is equal to the number of members in the view, then the message is stable
                if (this.library.getNode().getAcks(tuple) == this.library.getViewSize() - 1) {
                    this.library.getNode().writeOnDisk(this.library.getNode().dequeueUnstableMessage());
                    this.library.getNode().removeAcks(tuple);
                    this.library.getNode().getView().stream().filter(peer -> peer.getId() == contentMessage.getSourceId()).forEach(Peer::incrementSequenceNumber);
                    this.library.getProcessThreads().remove(this);
                    return;
                }
            }
            this.library.getNode().removeAcks(tuple);
            if (this.library.getNode().getState().equals(Client.State.NORMAL)) {
                this.library.getNode().dequeueUnstableMessage();
            }
            this.library.getProcessThreads().remove(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
