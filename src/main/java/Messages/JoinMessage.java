package Messages;

import Client.Node;

import java.net.InetAddress;

public class JoinMessage extends Message {

    public JoinMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.JOIN;
    }

    @Override
    public int getSourceId() { return sourceId; }

}
