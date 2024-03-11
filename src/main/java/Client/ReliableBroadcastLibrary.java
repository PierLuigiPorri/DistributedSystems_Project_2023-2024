package Client;
//Reliable broadcast library
//Implement a library for reliable broadcast communication among a set of faulty processes, plus a simple application to test it (you are free to choose the application you prefer to highlight the characteristics of the library).
//The library must guarantee virtual synchrony, while ordering should be at least fifo.
//The project can be implemented as a real distributed application (for example, in Java) or it can be simulated using OmNet++.
//Assumptions:
//Assume (and leverage) a LAN scenario (i.e., link-layer broadcast is available).
//You may also assume no processes fail during the time required for previous failures to be recovered.

import Client.Tasks.ReceiverTask;
import Messages.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.List;

public class ReliableBroadcastLibrary {

    private final Node node;
    MulticastSocket ioSocket;


    public ReliableBroadcastLibrary(int port) throws IOException {
        ioSocket = new MulticastSocket(port);
        node = new Node();
        ReceiverTask receiverTask = new ReceiverTask(this, port);
        Thread receiverThread = new Thread(receiverTask);
        receiverThread.start();
    }

    public void processMessage(Message message, List<Peer> view) {
        switch (message.getType()) {
            case CONTENT:
                // print the message
                ContentMessage contentMessage = (ContentMessage) message;
                node.queueUnstableMessage(message);
                //TODO:actually process the message
                System.out.println(contentMessage.getMessage());

                break;
            case JOIN:
                // add the new node to the view
                    JoinMessage joinMessage = (JoinMessage) message;
                    // TRIGGERA VIEWCHANGE   view.add(joinMessage.getNode().getAddress());

                break;
            case VIEW_CHANGE:
                // update the view
                    ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
                    // CREARE NUOVA VIEW    view = viewChangeMessage.getView();

                break;
            case PING:
                // reset the timer for the sender
                    PingMessage pingMessage = (PingMessage) message;
                    //send messsage back to the sender of the ping
                    //TODO: send(new AckMessage(this.address, pingMessage.getSequenceNumber(), this.id, pingMessage.getSource()));

                break;
        }
    }

    public Message parseToMessage(String message) {
        //TODO: Implement this method
        return null;
    }

    public Node getNode() {
        return this.node;
    }

    public void send(Message message) throws IOException {
        // send the ping to all nodes in the view using TCP
        for (Peer peer : this.node.getView()) {
            Socket socket = new Socket(peer.getAddress(), peer.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
            out.close();
            socket.close();
        }
    }

    public void triggerViewChange(String type, int Node) {
        this.node.setState(State.VIEW_CHANGE);
    }
}
