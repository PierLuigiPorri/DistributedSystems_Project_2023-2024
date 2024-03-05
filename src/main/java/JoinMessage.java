import java.net.InetAddress;

public class JoinMessage extends Message{
    private final long timeCreated = System.currentTimeMillis();

    private InetAddress nodeAddress;
    private int nodePort;
    private String nodeState= "Joining";

    public JoinMessage(InetAddress source, int sequenceNumber, InetAddress nodeAddress, int nodePort, int sourceId, InetAddress destination) {
        super(source, sequenceNumber, sourceId, destination );
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
    }

    public Node getNode() {
        return new Node(nodeAddress, nodePort, nodeState);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.JOIN;
    }

    @Override
    public String getTransmissionString() {
        //TODO: Implement this method
        return null;
    }

    @Override
    public InetAddress getSource() { return source; }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
