package Client;

import java.net.InetAddress;
import java.net.Socket;

public class Peer{
    private final int id;
    private final InetAddress address;
    private int sequenceNumber = 0;
    private final int port;
    private Socket socket;

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

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
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
