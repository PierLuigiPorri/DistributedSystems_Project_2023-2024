package Messages;


public class ContentMessage extends Message {
    private final String payload;
    private int sequenceNumber;

    public ContentMessage(String payload, int sourceId, int sequenceNumber) {
        super(sourceId);
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.CONTENT;
    }

    @Override
    public int getSourceId() {
        return sourceId;
    }

    public String getPayload() {
        return payload;
    }

    public String toCommitString() {
        return sourceId + "sent: " + this.payload;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
