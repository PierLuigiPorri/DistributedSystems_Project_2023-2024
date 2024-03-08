package Messages;

import java.net.InetAddress;

public abstract class Message {
    protected int sourceId;

    public Message(int sourceId) {
        this.sourceId = sourceId;
    }

    public abstract MessageEnum getType();

    public abstract String getTransmissionString();

    public abstract int getSourceId();

}