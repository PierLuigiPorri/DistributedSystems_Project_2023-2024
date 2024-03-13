package Messages;

public class AckMessage extends Message {
    private final int sequenceNumber;
    private final int senderId;

    public AckMessage(int sourceId, int sequenceNumber, int senderId) {
        super(sourceId);
        this.sequenceNumber = sequenceNumber;
        this.senderId = senderId;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.ACK;
    }

    @Override
    public int getSourceId() { return sourceId; }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getSenderId() {
        return senderId;
    }
}
