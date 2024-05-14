package Client.Tasks;
/*
    * This class is responsible for receiving messages from other nodes in the network.
    * It listens for incoming messages and deserializes them.
 */
import Client.VirtualSynchronyLibrary;
import Messages.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ReceiverTask extends Thread {
    private final VirtualSynchronyLibrary library;      // The library that this receiver task is associated with.
    private final Socket clientSocket;                  // The socket that this receiver task is listening on.

    private final ObjectOutputStream out;

    private final ObjectInputStream in;

    /*
        * Constructor for the ReceiverTask class.
        * @param library The library that this receiver task is associated with.
        * @param clientSocket The socket that this receiver task is listening on.
     */
    public ReceiverTask(VirtualSynchronyLibrary library, Socket clientSocket) throws IOException{
        this.library = library;
        this.clientSocket = clientSocket;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    /*
        * This method listens for incoming messages and deserializes them.
        * It then adds the message to the incoming message queue of the library.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();
                if (message != null) {
                    System.out.println("Received message: " + message.toString());
                    this.library.getNode().queueIncomingMessage(message);
                } else {
                    System.err.println("Failed to deserialize received message.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in ReceiverTask: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUnicast(Message message) throws IOException {
        out.writeObject(message);
    }
}
