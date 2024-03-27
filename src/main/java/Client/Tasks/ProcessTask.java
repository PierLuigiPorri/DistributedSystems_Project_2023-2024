package Client.Tasks;
/*
    * This class is responsible for processing the messages received by the node.
    * It sends an ack back to all the nodes in the view and waits for the message to be stable.
    * If the message is stable, it writes the message to the disk and removes the message from the unstable queue.
 */
import Client.Peer;
import Client.VirtualSynchronyLibrary;
import Client.Tuple;
import Messages.AckMessage;
import Messages.ContentMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ProcessTask extends RunningTask{

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /*
    * Constructor
    * @param library The library that is using this task
     */
    public ProcessTask(VirtualSynchronyLibrary library) {
        super(library);
    }

    /*
    * This method is responsible for processing the messages received by the node.
     */
    public void run() {
        try {
            // send an ack back to all the nodes in the view

            ContentMessage contentMessage = this.library.getNode().peekUnstableMessage();
            Tuple tuple = new Tuple(contentMessage.getSequenceNumber(), contentMessage.getSourceId());
            this.library.sendMulticast(new AckMessage(this.library.getNode().getId(), contentMessage.getSequenceNumber(), contentMessage.getSourceId()));
            final Runnable processTask = () -> {
                if (this.library.getNode().getState().equals(Client.State.NORMAL) && this.library.getNode().getAcks(tuple) != -1) {
                    if (this.library.getNode().getAcks(tuple) == this.library.getViewSize() - 1) {
                        try {
                            this.library.getNode().writeOnDisk(this.library.getNode().dequeueUnstableMessage());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        this.library.getNode().removeAcks(tuple);
                        this.library.getNode().getView().stream().filter(peer -> peer.getId() == contentMessage.getSourceId()).forEach(Peer::incrementSequenceNumber);
                        this.library.getProcessThreads().remove(this);
                        return;
                    }
                }
                this.library.getNode().removeAcks(tuple);
                if (this.library.getNode().getState().equals(Client.State.NORMAL)) {
                    try {
                        this.library.getNode().dequeueUnstableMessage();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                this.library.getProcessThreads().remove(this);
            };
            scheduler.scheduleAtFixedRate(processTask, 0, 20, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println("Error in ProcessTask");
        }

    }
}
