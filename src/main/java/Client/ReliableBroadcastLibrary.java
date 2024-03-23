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

import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ReliableBroadcastLibrary {

    private final Node node;
    private final ArrayList<Thread> sendingThreads;
    private final ArrayList<Thread> processThreads;
    private final HashMap<Integer, Thread> receiverThreads;
    private final Thread deliverThread;
    private final ConnectionManager connectionManager;


    private int countFlush = 0;
    private ArrayList<Peer> newView;
    private final HashMap<Peer, Socket> sockets;

    public ReliableBroadcastLibrary(int port) throws IOException {
        node = new Node();
        this.connectionManager = new ConnectionManager(this, port);
        connectionManager.start();
        this.deliverThread = new DeliverTask(this);
        sendingThreads = new ArrayList<>();
        processThreads = new ArrayList<>();
        receiverThreads = new HashMap<>();
        sockets = new HashMap<>();
    }

    public void processMessage(Message message) throws IOException, InterruptedException {
        switch (message.getType()) {
            case CONTENT -> {
                // print the message
                ContentMessage contentMessage = (ContentMessage) message;
                boolean toDrop = false; //TODO: Reset this line, this is just for testing (this.node.getUnstableMessageQueue().stream().noneMatch(m -> (m.getSourceId() == contentMessage.getSourceId() && m.getSequenceNumber() >= contentMessage.getSequenceNumber()) || (m.getSourceId() == contentMessage.getSourceId() && contentMessage.getSequenceNumber() - m.getSequenceNumber() > 1))) || this.node.getUnstableMessageQueue().isEmpty() && this.node.getView().stream().anyMatch(p -> p.getId() == contentMessage.getSourceId() && contentMessage.getSequenceNumber() - p.getSequenceNumber() > 1);
                if (!toDrop) {
                    this.node.queueUnstableMessage(contentMessage);
                    //System.out.println(contentMessage.getPayload());
                    if (this.node.getState().equals(State.NORMAL)) {
                        ProcessTask processThread = new ProcessTask(this);
                        this.processThreads.add(processThread);
                        processThread.start();
                    }
                }
            }

            case VIEW_CHANGE -> {
                if (!this.node.getState().equals(State.VIEW_CHANGE)) {
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
                    Thread pingThread = new Thread(new PingTask(this));
                    pingThread.start();
                    Thread timerThread = new TimerTask(this);
                    timerThread.start();
                }
            }
            case LEAVE -> // remove the node from the view
                    removePeer(message.getSourceId());
        }
    }

    public void sendMessage(ContentMessage message) throws InterruptedException { //sends a message to the view
        if (this.node.getState().equals(State.NORMAL)) {
            this.node.queueOutgoingMessage(message);
            SendingTask sendingThread = new SendingTask(this);
            this.sendingThreads.add(sendingThread);
            sendingThread.start();
        }
    }

    public Node getNode() {
        return this.node;
    }

    public void sendMulticast(Message message) throws IOException {
        // send the ping to all nodes in the view using TCP
        ArrayList<Peer> view;
        if (this.node.getState().equals(State.VIEW_CHANGE)) {
            view = this.newView;
        } else { //view change case
            view = this.node.getView();
        }
        for (Peer peer : view) {
            if (peer.getId() != this.getNode().getId()) {
                Socket socket = this.getSocketFromId(peer.getId());
                if (this.hasConnection(peer.getId())) { // Ensure a valid connection exists
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(message);
                    out.close();
                } else {
                    System.err.println("No connection found for peer ID: " + peer.getId());
                }
            }
        }
    }

    public void sendUnicast(Message message, Socket peerSocket) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(peerSocket.getOutputStream());
        out.writeObject(message);
        out.close();
    }


    // Method to get the Socket associated with a peer
    public Socket getSocketFromId(int peerId) {
        return sockets.get(this.node.getView().stream().filter(peer -> peer.getId() == peerId).findFirst().get());
    }

    // Method to check if a connection exists with a peer
    public boolean hasConnection(int peerId) {
        return getSocketFromId(peerId) != null;
    }

    public int getViewSize() {
        return this.getNode().getView().size();
    }

    public void addPeer(Socket clientSocket) throws IOException {
        this.node.setState(State.VIEW_CHANGE);
        int nextId = 0;

        for (Peer peer : this.node.getView()) {
            if (peer.getId() == nextId) {
                nextId++;
            }
            else break;
        }
        this.newView = this.node.getView();
        this.receiverThreads.put(nextId, new ReceiverTask(this, clientSocket));
        this.receiverThreads.get(nextId).start();
        Peer peer = new Peer(nextId, clientSocket.getInetAddress(), clientSocket.getPort());
        this.newView.add(peer);
        this.sockets.put(peer, clientSocket);
        sendUnicast(new ViewChangeMessage(this.node.getId(), this.newView), clientSocket);
    }

    public void removePeer(int node) throws IOException {
        this.node.setState(State.VIEW_CHANGE);
        this.newView = this.node.getView();
        if (node != this.node.getId()) {
            this.node.getView().stream()
                    .filter(peer -> peer.getId() == node && hasConnection(node))
                    .forEach(peer -> {
                        try {
                            sockets.get(peer).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sockets.remove(peer);
                    });
        }
        this.newView.removeIf(peer -> peer.getId() == node);
        this.receiverThreads.get(node).interrupt();
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

    public void leaveView() throws IOException {
        this.node.setState(State.LEAVING);
        removePeer(this.node.getId());
        for (Peer peer : this.node.getView()) {
            if (peer.getId() != this.node.getId()) {
                try {
                    sockets.get(peer).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.deliverThread.interrupt();
        this.connectionManager.interrupt();
        this.receiverThreads.forEach((id, thread) -> thread.interrupt());
        this.sendingThreads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        this.processThreads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        this.node.setState(State.DISCONNECTED);
    }

    public void joinView(String address, int port) throws IOException, InterruptedException {
        this.node.setState(State.JOINING);
        Socket firstSocket = new Socket(InetAddress.getByName(address), port);
        Thread firstReceiver = new ReceiverTask(this, firstSocket);
        firstReceiver.start();
        ViewChangeMessage message = (ViewChangeMessage) this.node.dequeueIncomingMessage();
        this.newView = message.getView();
        Peer first = newView.stream().filter(peer -> peer.getId() == message.getSourceId()).findFirst().get();
        this.sockets.put(first, firstSocket);
        this.receiverThreads.put(message.getSourceId(), firstReceiver);
        //this.node.setId(newView.stream().filter(p-> p.getAddress().equals(TODO:put here the address of this machine).get(0).getId()));
        for (Peer peer : this.newView.stream().filter(p -> p.getId() != this.node.getId() && p.getId() != message.getSourceId()).toList()) {
            Socket socket = new Socket(peer.getAddress(), peer.getPort());\
            this.sockets.put(peer, socket);
            this.receiverThreads.put(peer.getId(), new ReceiverTask(this, socket));
            this.receiverThreads.get(peer.getId()).start();
        }
        deliverThread.start();
        triggerViewChange();
    }

    public ArrayList<Thread> getSendingThreads() {
        return this.sendingThreads;
    }

    public ArrayList<Thread> getProcessThreads() {
        return this.processThreads;
    }

    // Method used for testing.
    public void connect(InetAddress address, int port) {
        try {
            Socket socket = new Socket(address, port);
            this.addPeer(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}