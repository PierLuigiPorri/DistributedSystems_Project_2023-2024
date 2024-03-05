import java.net.InetAddress;

public class StringMessage extends Message {
    private final String message;


    public StringMessage(InetAddress source, String message, int sequenceNumber, int sourceId, InetAddress destination) {
        super(source, sequenceNumber, sourceId, destination);
        this.message = message;
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
