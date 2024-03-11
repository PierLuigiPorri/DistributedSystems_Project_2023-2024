package Messages;

public class AckMessage extends Message {
    private final int sequenceNumber;

    public AckMessage(int sourceId, int sequenceNumber) {
        super(sourceId);
        this.sequenceNumber = sequenceNumber;
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
}
