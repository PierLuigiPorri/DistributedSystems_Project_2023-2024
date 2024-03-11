package Messages;

public class DropMessage extends Message {
    private final int sequenceNumber;

    public DropMessage(int sourceId, int sequenceNumber) {
        super(sourceId);
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.DROP;
    }

    @Override
    public int getSourceId() { return sourceId; }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

}
