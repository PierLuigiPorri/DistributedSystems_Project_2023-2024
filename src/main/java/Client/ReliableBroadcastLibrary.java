package Client;
//Reliable broadcast library
//Implement a library for reliable broadcast communication among a set of faulty processes, plus a simple application to test it (you are free to choose the application you prefer to highlight the characteristics of the library).
//The library must guarantee virtual synchrony, while ordering should be at least fifo.
//The project can be implemented as a real distributed application (for example, in Java) or it can be simulated using OmNet++.
//Assumptions:
//Assume (and leverage) a LAN scenario (i.e., link-layer broadcast is available).
//You may also assume no processes fail during the time required for previous failures to be recovered.

import Client.Tasks.*;
import Messages.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.List;

public class ReliableBroadcastLibrary {

    private final Node node;
    MulticastSocket ioSocket;

    private int countFlush = 0;
    private List<Peer> newView;

    public ReliableBroadcastLibrary(int port) throws IOException {
        ioSocket = new MulticastSocket(port);
        node = new Node();

        ReceiverTask receiverTask = new ReceiverTask(this, port);
        Thread receiverThread = new Thread(receiverTask);
        receiverThread.start();

        DeliverTask deliverTask = new DeliverTask(this);
        Thread deliverThread = new Thread(deliverTask);
        deliverThread.start();

        PingTask pingTask = new PingTask(this);
        Thread pingThread = new Thread(pingTask);
        pingThread.start();

        TimerTask timerTask = new TimerTask(this);
        Thread timerThread = new Thread(timerTask);
        timerThread.start();

        SendingTask sendingTask = new SendingTask(this);
        Thread sendingThread = new Thread(sendingTask);
        sendingThread.start();
    }

    public void processMessage(Message message) throws IOException {
        switch (message.getType()) {
            case CONTENT -> {
                // print the message
                ContentMessage contentMessage = (ContentMessage) message;
                this.node.queueUnstableMessage(contentMessage);
                System.out.println(contentMessage.getPayload());
                ProcessTask processTask = new ProcessTask(this);
                //create a new thread to process a message for each message we receive.
                Thread processThread = new Thread(processTask);
                //process the message, so to check if it is stable
                processThread.start();
            }
            case JOIN -> // add the new node to the view
                    triggerViewChange("add", message.getSourceId());

            case VIEW_CHANGE -> {
                this.node.setState(State.VIEW_CHANGE);
                // update the view
                for (ContentMessage messages : this.node.getUnstableMessageQueue()) {
                    send(messages);
                }
                ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
                newView = viewChangeMessage.getView();
                // CREATE NEW VIEW   view = viewChangeMessage.getView();
                send(new FlushMessage(this.node.getId()));
            }
            case PING -> {
                // reset the timer for the sender
                PingMessage pingMessage = (PingMessage) message;
                this.getNode().resetTimer(pingMessage.getSourceId());
            }
            case ACK -> {
                // add the ack to the acks list.
                AckMessage ackMessage = (AckMessage) message;
                Tuple tuple = new Tuple(ackMessage.getSequenceNumber(), ackMessage.getSenderId());
                this.getNode().incrementAcks(tuple);
            }
            case DROP -> {
                DropMessage dropMessage = (DropMessage) message;
                Tuple dropTuple = new Tuple(dropMessage.getSequenceNumber(), dropMessage.getSourceId());
                this.getNode().dropAcks(dropTuple);
            }
            case FLUSH -> {
                countFlush++;
                if (countFlush == this.getNode().getView().size() - 1) {

                    // flush all messages from the unstableQueueMessages
                    for (ContentMessage content : this.getNode().getUnstableMessageQueue()) {
                        this.getNode().writeOnDisk(content);
                    }
                    this.getNode().getUnstableMessageQueue().clear();
                    this.node.installNewView(newView);
                    this.node.setState(State.NORMAL);
                }
            }
            case LEAVE -> // remove the node from the view
                    triggerViewChange("remove", message.getSourceId());
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

    public int getViewSize() {
        return this.getNode().getView().size();
    }

    public void triggerViewChange(String type, int Node) {
        this.node.setState(State.VIEW_CHANGE);
    }
}