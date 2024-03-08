package Messages;

import java.net.InetAddress;

public class ContentMessage extends Message {
    private final String message;
    private final int sequenceNumber;

    public ContentMessage(String message, int sourceId, int sequenceNumber) {
        super(sourceId);
        this.message = message;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.CONTENT;
    }

    @Override
    public String getTransmissionString() {
        //TODO: Implement this method
        return null;
    }

    @Override
    public int getSourceId() { return sourceId; }

    public String getMessage() {
        return message;
    }
}
