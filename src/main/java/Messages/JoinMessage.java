package Messages;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class JoinMessage extends Message {

    private final InetAddress address;
    private final int port;

    public JoinMessage(int sourceId, int port) throws UnknownHostException {
        super(sourceId);
        this.address = InetAddress.getLocalHost();
        this.port = port;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.JOIN;
    }

    @Override
    public int getSourceId() { return sourceId; }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
