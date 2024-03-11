package Client.Tasks;

import Client.ReliableBroadcastLibrary;
public class ProcessTask extends RunningTask implements Runnable {

    public ProcessTask(ReliableBroadcastLibrary library) {
            super(library);
        }

        public void run() {
            while (true) {
                try {
                    //If the number of AckMessages received is equal to the number of members in the view, then the message is stable
                    if (this.library.getNode().getAcks().get(this.library.getContentMessageSequenceNumber()) == this.library.getViewSize()-1) {
                        this.library.getNode().queueStableMessage(this.library.getContentMessage());
                        this.library.getNode().getAcks().remove(this.library.getContentMessageSequenceNumber());
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


}
