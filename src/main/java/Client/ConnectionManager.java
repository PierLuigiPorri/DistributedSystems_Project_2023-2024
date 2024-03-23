package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager extends Thread {
    private final ReliableBroadcastLibrary library;
    private final int port;

    public ConnectionManager(ReliableBroadcastLibrary library, int port) {
        this.library = library;
        this.port = port;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        try {
            // Create a ServerSocket to listen for incoming connections
            try (ServerSocket serverSocket = new ServerSocket(port)) {

                // Listen for incoming connections
                while (true) {
                    // Accept incoming connection requests
                    Socket clientSocket = serverSocket.accept();
                    this.library.addPeer(clientSocket);
                }
            }
        } catch (IOException e) {
            System.out.println("Error: connection failed. " + e.getMessage());
        }
    }
}