package Client.Tasks;

import Client.ReliableBroadcastLibrary;

public class TimerTask extends RunningTask implements Runnable {

    private TimerTask(ReliableBroadcastLibrary library) {
        super(library);
    }
    private int deadNode=-1;

    public void run() {
        this.library.getNode().initializeTimer();
        while (true) {
            try {
                Thread.sleep(100);
                this.library.getNode().incrementTimers();
                deadNode = this.library.getNode().checkIfSomeoneIsDead();
                if (deadNode != -1) {
                    this.library.triggerViewChange("remove", deadNode);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
