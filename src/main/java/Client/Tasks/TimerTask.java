package Client.Tasks;

import Client.ReliableBroadcastLibrary;

public class TimerTask extends RunningTask{

    public TimerTask(ReliableBroadcastLibrary library) {
        super(library);
    }

    public void run() {
        this.library.getNode().initializeTimer();
        while (true) {
            try {
                Thread.sleep(300);
                this.library.getNode().incrementTimers();
                int deadNode = this.library.getNode().checkIfSomeoneIsDead();
                if (deadNode != -1) {
                    this.library.removePeer(deadNode);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
