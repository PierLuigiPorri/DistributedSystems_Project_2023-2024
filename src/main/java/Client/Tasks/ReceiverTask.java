package Client.Tasks;

import Client.ReliableBroadcastLibrary;
import Messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ReceiverTask extends Thread {
    private final ReliableBroadcastLibrary library;
    private final Socket clientSocket;

    public ReceiverTask(ReliableBroadcastLibrary library, Socket clientSocket) {
        this.library = library;
        this.clientSocket = clientSocket;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
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
