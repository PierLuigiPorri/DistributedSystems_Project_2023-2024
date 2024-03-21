package Client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

public class Peer implements Serializable {
    private final int id;
    private int sequenceNumber = 0;

    private final InetAddress address;
    private final int port;
    public Peer(int id, InetAddress address, int port) {
        this.id = id;
        this.address = address;
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
