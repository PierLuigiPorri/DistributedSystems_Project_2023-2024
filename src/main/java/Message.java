import java.net.InetAddress;

public abstract class Message {
    protected int sourceId;
    protected InetAddress destination;
    protected int sequenceNumber;
    protected InetAddress source;
    protected final long timeCreated = System.currentTimeMillis();

    public Message(InetAddress source, int sequenceNumber, int sourceId, InetAddress destination) {
        this.source = source;
        this.sequenceNumber = sequenceNumber;
        this.sourceId = sourceId;
        this.destination = destination;
    }

    static Message parseToMessage(String message) {
        //TODO: Implement this method
        return null;
    }

    abstract MessageEnum getType();

    abstract String getTransmissionString();

    abstract InetAddress getSource();

    protected int getMillisSinceCreated() {
        return (int) (System.currentTimeMillis() - timeCreated);
    }

    public enum MessageEnum {
        JOIN, BEGIN, STRING, PING, LEAVE, NACK, ACK, VIEW, VIEW_CHANGE
    }
}