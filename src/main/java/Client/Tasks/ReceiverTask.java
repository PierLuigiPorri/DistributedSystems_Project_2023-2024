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

    private boolean active = false;                     // A flag to indicate whether the thread is active or not. If it's not active, the thread is in the joining process and any incoming messages are stored in the setupMessage variable.

    private final ObjectOutputStream out;               // The output stream of the socket.

    private final ObjectInputStream in;                 // The input stream of the socket.

    private Message setupMessage;                       // The message that was received during the joining process.

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
                    if(active){
                        this.library.getNode().queueIncomingMessage(message);
                    }
                    else {
                        setupMessage = message;
                    }
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

    /*
        * This method sends a message to a specific node in the network.
        * @param message The message to be sent.

     */
    public void sendUnicast(Message message) throws IOException {
        out.writeObject(message);
    }

    /*
        * This method activates the thread for normal use.
     */
    public void activate() {
        active = true;
    }

    /*
        * This method returns the message during the joining process.
        * @return The message that was received.
     */
    public Message getSetupMessage() {
        return setupMessage;
    }
}
