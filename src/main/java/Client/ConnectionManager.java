package Client;
/*
    * ConnectionManager.java
    * This class represents the thread that manages new connections to the node.
    * It listens for incoming connections and adds the new peer to the list of peers.
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager extends Thread {
    private final VirtualSynchronyLibrary library;                  // The library that manages the node
    private final int port;                                         // The port to listen for incoming connections

    /*
        * Constructor
        * @param library: The library that manages the node
        * @param port: The port to listen for incoming connections
     */
    public ConnectionManager(VirtualSynchronyLibrary library, int port) {
        this.library = library;
        this.port = port;
    }

    /*
        * The run method of the thread
        * It listens for incoming connections and adds the new peer to the list of peers
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        try {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    this.library.addPeer(clientSocket);
                }
            } catch (ClassNotFoundException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            System.out.println("Error: new connection failed. " + e.getMessage());
        }
    }

    /*
        * Get the port
        * @return: The port
     */
    public int getPort() {
        return port;
    }
}