package Client.Tasks;
/*
 * This class is responsible for sending messages to the other nodes.
 * It sends the message and waits for the acknowledgements.
 */

import Client.VirtualSynchronyLibrary;
import Client.Tuple;
import Messages.ContentMessage;
import Messages.DropMessage;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SendingTask extends RunningTask {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /*
     * Constructor.
     * @param library The library that contains the node.
     */
    public SendingTask(VirtualSynchronyLibrary library) {
        super(library);
    }

    /*
     * This method sends the message to the other nodes and waits for the acknowledgements.
     * If the message is not delivered, it sends a drop message.
     */
    public void run() {
        while (this.library.getNode().getState().equals(Client.State.NORMAL)) {
            try {
                ContentMessage message = this.library.getNode().peekOutgoingMessage();
                this.library.getNode().queueUnstableMessage(message);
                message.setSequenceNumber(this.library.getNode().getSequenceNumber());
                this.library.sendMulticast(message);
                AtomicInteger pseudoTimer = new AtomicInteger();
                Tuple tuple = new Tuple(message.getSequenceNumber(), this.library.getNode().getId());

                final Runnable sendTask = () -> {
                    if (this.library.getNode().getState().equals(Client.State.NORMAL) && pseudoTimer.get() < 50) {
                        pseudoTimer.getAndIncrement();
                        if (this.library.getNode().getAcks(tuple) == this.library.getViewSize() - 1) {
                            try {
                                this.library.getNode().writeOnDisk(this.library.getNode().dequeueOutgoingMessage());
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                this.library.getNode().dequeueUnstableMessage();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            this.library.getNode().incrementSequenceNumber();
                            this.library.getNode().removeAcks(tuple);
                            this.library.getSendingThreads().remove(this);
                            return;
                        }
                    }
                    if (this.library.getNode().getState().equals(Client.State.NORMAL)) {
                        try {
                            this.library.sendMulticast(new DropMessage(this.library.getNode().getId(), message.getSequenceNumber()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        this.library.getNode().removeAcks(tuple);
                    }
                };
                scheduler.scheduleAtFixedRate(sendTask, 0, 20, TimeUnit.MILLISECONDS);
            } catch (IOException e) {
                this.library.getSendingThreads().remove(this);
                System.out.println("Error during sending process.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}