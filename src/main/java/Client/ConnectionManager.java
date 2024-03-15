package Client;

import Client.Tasks.ReceiverTask;

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
            ServerSocket serverSocket = new ServerSocket(port);

            // Listen for incoming connections
            while (true) {
                // Accept incoming connection requests
                Socket clientSocket = serverSocket.accept();

                // Once a connection is accepted, create a ReceiverTask to handle it
                ReceiverTask receiverTask = new ReceiverTask(library, clientSocket);
                Thread receiverThread = new Thread(receiverTask);
                receiverThread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}