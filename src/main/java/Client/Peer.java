package Client;
/*
    * Peer class represents a peer in the network.
    * It has an id, address, port and sequence number.
 */
import java.io.Serializable;
import java.net.InetAddress;

public class Peer implements Serializable {
    private final int id;                                   // Peer id
    private int sequenceNumber = 0;                         // Sequence number of the peer

    private final InetAddress address;                      // Peer address
    private final int port;                                 // Peer port

    /*
        * Constructor to initialize the peer with id, address and port.
        * @param id: Peer id
        * @param address: Peer address
        * @param port: Peer port
     */
    public Peer(int id, InetAddress address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    /*
        * Get the id of the peer.
        * @return id: Peer id
     */
    public int getId() {
        return id;
    }

    /*
        * Get the address of the peer.
        * @return address: Peer address
     */
    public InetAddress getAddress() {
        return address;
    }

    /*
        * Get the port of the peer.
        * @return port: Peer port
     */
    public int getPort() {
        return port;
    }

    /*
        * Get the sequence number of the peer.
        * @return sequenceNumber: Sequence number of the peer
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /*
        * Increment the sequence number of the peer.
     */
    public void incrementSequenceNumber() {
        this.sequenceNumber++;
    }
}
