package Client;

public class Tuple {
    private final int sequenceNumber;
    private final int senderId;

    public Tuple(int sequenceNumber, int senderId) {
        this.sequenceNumber = sequenceNumber;
        this.senderId = senderId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getSenderId() {
        return senderId;
    }

}
