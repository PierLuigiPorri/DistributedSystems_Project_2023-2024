package Client;

import Client.Tasks.ReceiverTask;
import Messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
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
                if (message instanceof ContentMessage) {
                    ContentMessage contentMessage = (ContentMessage) message;
                    System.out.println(contentMessage.getMessage());
                }
                break;
            case JOIN:
                // add the new node to the view
                if (message instanceof JoinMessage) {
                    JoinMessage joinMessage = (JoinMessage) message;
                    // TRIGGERA VIEWCHANGE   view.add(joinMessage.getNode().getAddress());
                }
                break;
            case VIEW_CHANGE:
                // update the view
                if (message instanceof ViewChangeMessage) {
                    ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
                    // CREARE NUOVA VIEW    view = viewChangeMessage.getView();
                }
                break;
            case PING:
                // reset the timer for the sender
                if (message instanceof PingMessage) {
                    PingMessage pingMessage = (PingMessage) message;
                    //send messsage back to the sender of the ping
                    //TODO: send(new AckMessage(this.address, pingMessage.getSequenceNumber(), this.id, pingMessage.getSource()));
                }
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

    public void send(Message message, List<Peer> view) throws IOException {
        // send the ping to all nodes in the view
        for (Peer peer : view) {
            byte[] buf = message.getTransmissionString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, peer.getAddress(), peer.getPort());
            ioSocket.send(packet);
        }
    }
}
