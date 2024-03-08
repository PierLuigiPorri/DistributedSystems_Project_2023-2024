package Messages;

import Messages.Message;

import java.net.InetAddress;

public class PingMessage extends Message {

    public PingMessage(int sourceId) {
        super(sourceId);
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
    public int getSourceId() {
        return sourceId;
    }
}
