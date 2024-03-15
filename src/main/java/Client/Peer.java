package Client;

import java.net.InetAddress;

public class Peer{
    private final int id;
    private final InetAddress address;
    private int sequenceNumber = 0;
    private final int port;

    public Peer(InetAddress address, int id, int port) {
        this.address = address;
        this.id = id;
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

    public void incrementSequenceNumber() {
        this.sequenceNumber++;
    }
}
