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
                sleep(300);
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
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
