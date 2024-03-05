import java.net.InetAddress;

public class PingMessage extends Message {

    public PingMessage(InetAddress source, int sequenceNumber, int sourceId, InetAddress destination) {
        super(source, sequenceNumber, sourceId, destination);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.PING;
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
}
