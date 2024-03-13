package Client;

import java.net.InetAddress;

public class Peer {
    private final int id;
    private final InetAddress address;
    private int sequenceNumber;
    private final int port;

    public Peer(InetAddress address, int id, int sequenceNumber, int port) {
        this.address = address;
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

}
