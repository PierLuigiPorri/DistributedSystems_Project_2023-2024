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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReliableBroadcastLibrary {

    private final Node node;
    MulticastSocket ioSocket;

    private Thread receiverThread;
    private Thread deliverThread;
    private Thread pingThread;
    private Thread timerThread;
    private ArrayList<Thread> sendingThreads;

    private ArrayList<Thread> processThreads;

    private int countFlush = 0;
    private ArrayList<Peer> newView;

    public ReliableBroadcastLibrary(int port) throws IOException {
        ioSocket = new MulticastSocket(port);
        node = new Node();

        receiverThread = new ReceiverTask(this, port);
        receiverThread.start();
        deliverThread = new DeliverTask(this);
        deliverThread.start();
        sendingThreads = new ArrayList<Thread>();
        processThreads = new ArrayList<Thread>();
    }

    public void processMessage(Message message) throws IOException {
        switch (message.getType()) {
            case CONTENT -> {
                // print the message
                ContentMessage contentMessage = (ContentMessage) message;
                boolean toDrop = (this.node.getUnstableMessageQueue().stream().noneMatch(m -> (m.getSourceId() == contentMessage.getSourceId() && m.getSequenceNumber() >= contentMessage.getSequenceNumber()) || (m.getSourceId() == contentMessage.getSourceId() && contentMessage.getSequenceNumber() - m.getSequenceNumber() > 1))) || this.node.getUnstableMessageQueue().isEmpty() && this.node.getView().stream().anyMatch(p->p.getId()==contentMessage.getSourceId() && contentMessage.getSequenceNumber()-p.getSequenceNumber()>1);
                if(!toDrop) {
                    this.node.queueUnstableMessage(contentMessage);
                    //System.out.println(contentMessage.getPayload());
                    if (this.node.getState().equals(State.NORMAL)) {
                        ProcessTask processThread = new ProcessTask(this);
                        this.processThreads.add(processThread);
                        processThread.start();
                    }
                }
            }
            case JOIN -> {// add the new node to the view
                JoinMessage joinMessage = (JoinMessage) message;
                addPeer(joinMessage.getAddress(), joinMessage.getPort());
            }

            case VIEW_CHANGE -> {
                if(!this.node.getState().equals(State.VIEW_CHANGE)) {
                    this.node.setState(State.VIEW_CHANGE);
                    // update the view
                    ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
                    newView = viewChangeMessage.getView();
                    for (ContentMessage messages : this.node.getUnstableMessageQueue()) {
                        sendMulticast(messages);
                    }
                    // CREATE NEW VIEW   view = viewChangeMessage.getView();
                    sendMulticast(new FlushMessage(this.node.getId()));
                }
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
                        this.node.writeOnDisk(content);
                        this.node.getView().stream().filter(peer -> peer.getId() == content.getSourceId()).forEach(Peer::incrementSequenceNumber);
                    }
                    this.getNode().getUnstableMessageQueue().clear();
                    this.node.installNewView(newView);
                    this.node.setState(State.NORMAL);
                    pingThread = new Thread(new PingTask(this));
                    pingThread.start();
                    timerThread = new TimerTask(this);
                    timerThread.start();
                }
            }
            case LEAVE -> // remove the node from the view
                    removePeer(message.getSourceId());
        }
    }

    public void sendMessage(ContentMessage message){ //sends a message to the view
        if(this.node.getState().equals(State.NORMAL)){
            this.node.queueOutgoingMessage(message);
            SendingTask sendingThread = new SendingTask(this);
            this.sendingThreads.add(sendingThread);
            sendingThread.start();
        }
    }

    public Message parseToMessage(String message) {
        //TODO: Implement this method
        return null;
    }

    public Node getNode() {
        return this.node;
    }

    public void sendMulticast(Message message) throws IOException { //TODO: CHANGE THIS
        // send the ping to all nodes in the view using TCP
        ArrayList<Peer> view;
        if(this.node.getState().equals(State.NORMAL)) {
            view = this.node.getView();
        }
        else { //view change case
            view = this.newView;
        }
        for (Peer peer : view) {
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

    public void addPeer(InetAddress address, int port) throws IOException {
        this.node.setState(State.VIEW_CHANGE);
        int nextId = 0;
        for(Peer peer : this.node.getView()){
            if(peer.getId() == nextId){
                nextId++;
            }
            else break;
        }
        this.newView = this.node.getView();
        this.newView.add(new Peer(address, nextId, port));
        triggerViewChange();
    }

    public void removePeer(int node) throws IOException {
        this.node.setState(State.VIEW_CHANGE);
        this.newView = this.node.getView();
        this.newView.removeIf(peer -> peer.getId() == node);
        triggerViewChange();
    }

    public void triggerViewChange() throws IOException {
        this.newView.sort(Comparator.comparing(Peer::getId));
        sendMulticast(new ViewChangeMessage(this.node.getId(), this.newView));
        for (ContentMessage messages : this.node.getUnstableMessageQueue()) {
            sendMulticast(messages);
        }
        sendMulticast(new FlushMessage(this.node.getId()));
    }

    public void leaveView() {
        this.node.setState(State.LEAVING);
    }

    public ArrayList<Thread> getSendingThreads() {
        return this.sendingThreads;
    }

    public ArrayList<Thread> getProcessThreads() {
        return this.processThreads;
    }
}