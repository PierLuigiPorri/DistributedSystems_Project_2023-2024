package Client.Tasks;
/*
    * This class is a part of the Client class. It is responsible for sending a ping message to all the nodes in the view.
    * It is a subclass of RunningTask, therefore it is runnable.
 */
import Client.VirtualSynchronyLibrary;
import Messages.PingMessage;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingTask extends RunningTask {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /*
        * Constructor for the PingTask class.
        * @param library: The library object that is used to send the ping message.
     */
    public PingTask(VirtualSynchronyLibrary library) {
        super(library);
    }

    /*
        * This method is used to send a ping message to all the nodes in the view.
        * It is called by the scheduler at a fixed rate of 250 milliseconds.
     */
    public void run() {
        final Runnable ping = () -> {
            if (this.library.getNode().getState().equals(Client.State.NORMAL)) {
                try {
                    this.library.sendMulticast(new PingMessage(this.library.getNode().getId()));
                } catch (IOException e) {
                    System.out.println("Error in pinging thread.");
                }
            }
        };
        scheduler.scheduleAtFixedRate(ping, 0, 250, TimeUnit.MILLISECONDS);
    }
}