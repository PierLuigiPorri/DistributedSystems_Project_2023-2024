package Client;

import java.net.InetAddress;
import java.net.Socket;

public class Peer{
    private final int id;
    private int sequenceNumber = 0;
    private Socket socket;

    public Peer(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public int getId() {
        return id;
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
