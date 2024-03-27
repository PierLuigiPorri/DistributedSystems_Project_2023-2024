package Client.Tasks;
/*
    * This class is responsible for delivering messages to the application layer.
    * It is a subclass of RunningTask, therefore a runnable class.
 */
import Client.VirtualSynchronyLibrary;
import Messages.Message;

import java.io.IOException;

public class DeliverTask extends RunningTask {

    /*
        * Constructor for the DeliverTask class.
        * @param library: The library that the task is associated with.
     */
    public DeliverTask(VirtualSynchronyLibrary library) {
        super(library);
    }

    /*
        * This method is responsible for delivering messages to the application layer.
        * It runs in an infinite loop, and processes messages as they arrive.
     */
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
            System.out.println("Error while delivering a message.");
        }
    }
}