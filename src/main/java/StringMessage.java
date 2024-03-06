import java.net.InetAddress;

public class StringMessage extends Message {
    private final String message;
    private final int sequenceNumber;

    public StringMessage(InetAddress source, String message, int sourceId, InetAddress destination, int sequenceNumber) {
        super(source, sourceId, destination);
        this.message = message;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.STRING;
    }

    @Override
    public String getTransmissionString() {
        //TODO: Implement this method
        return null;
    }

    @Override
    public InetAddress getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }
}
