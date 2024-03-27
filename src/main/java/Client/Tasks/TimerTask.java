package Client.Tasks;
/*
    * This class is responsible for managing the timers of the node.
    * It checks if a node is dead and if it is, it removes it from the list of peers.
 */
import Client.VirtualSynchronyLibrary;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerTask extends RunningTask{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /*
        * Constructor for the TimerTask class.
        * @param library: the library that is used to communicate with the other nodes.
     */
    public TimerTask(VirtualSynchronyLibrary library) {
        super(library);
    }

    /*
        * This method is responsible for running the TimerTask.
        * It checks if a node is dead and if it is, it removes it from the list of peers.
     */
    public void run() {
        this.library.getNode().initializeTimer();
        final Runnable timerTask = () -> {
            try {
                this.library.getNode().incrementTimers();
                int deadNode = this.library.getNode().checkIfSomeoneIsDead();
                if (deadNode != -1) {
                    sleep(1000);
                    deadNode = this.library.getNode().checkIfSomeoneIsDead();
                    if (deadNode != -2) {
                        this.library.removePeer(deadNode);
                    }
                    else {
                        this.library.reconnect();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in TimerTask: " + e.getMessage());
            }
        };
        scheduler.scheduleAtFixedRate(timerTask, 0, 300, TimeUnit.MILLISECONDS);
    }
}
