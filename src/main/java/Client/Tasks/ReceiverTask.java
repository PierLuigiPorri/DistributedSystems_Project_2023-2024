package Client.Tasks;

import Client.ReliableBroadcastLibrary;
import Messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverTask implements Runnable {
    private final ReliableBroadcastLibrary library;
    private final int port;

    public ReceiverTask(ReliableBroadcastLibrary library, int port) {
        this.library = library;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                Message message = (Message) in.readObject();
                if (message != null) {
                    this.library.getNode().queueIncomingMessage(message);
                } else {
                    System.err.println("Failed to deserialize received message.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
