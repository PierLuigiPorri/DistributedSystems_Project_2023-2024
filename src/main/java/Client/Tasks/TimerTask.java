package Client.Tasks;

import Client.ReliableBroadcastLibrary;

public class TimerTask extends RunningTask implements Runnable {

    public TimerTask(ReliableBroadcastLibrary library) {
        super(library);
    }

    public void run() {
        this.library.getNode().initializeTimer();
        while (true) {
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Call library to send a message to all view members
    }
}
