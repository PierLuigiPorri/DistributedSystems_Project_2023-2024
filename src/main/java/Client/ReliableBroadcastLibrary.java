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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import static java.lang.Thread.sleep;

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
                    int toRemove = viewChangeMessage.getToRemove();
                    if (toRemove != -1) {
                        closeConnection(toRemove);
                    }
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

    public void closeConnection(int toRemove) {
        this.node.getView().stream()
                .filter(peer -> peer.getId() == toRemove)
                .forEach(peer -> {
                    try {
                        sockets.get(peer).close();
                    } catch (IOException e) {
                        System.out.println("Error: failed to close connection with peer " + peer.getId() + ". " + e.getMessage());
                    }
                    sockets.remove(peer);
                });
        this.receiverThreads.get(toRemove).interrupt();
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
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(message);
                out.close();
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

    public int getViewSize() {
        return this.getNode().getView().size();
    }

    public void addPeer(Socket clientSocket) throws IOException, ClassNotFoundException, InterruptedException {
        this.node.setState(State.JOINING);
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        Message message = (Message) in.readObject();
        in.close();
        if (message.getType().equals(MessageEnum.JOIN)) {
            int nextId = 0;
            for (Peer peer : this.node.getView()) {
                if (peer.getId() == nextId) {
                    nextId++;
                } else break;
            }
            this.newView = this.node.getView();
            this.receiverThreads.put(nextId, new ReceiverTask(this, clientSocket));
            this.receiverThreads.get(nextId).start();
            Peer peer = new Peer(nextId, clientSocket.getInetAddress(), clientSocket.getPort());
            this.newView.add(peer);
            this.sockets.put(peer, clientSocket);
            sendUnicast(new ViewChangeMessage(this.node.getId(), this.newView, -1), clientSocket);
        } else {
            ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
            Peer newPeer = viewChangeMessage.getView().stream().filter(peer -> peer.getId() == viewChangeMessage.getSourceId()).findFirst().get();
            this.receiverThreads.put(newPeer.getId(), new ReceiverTask(this, clientSocket));
            this.receiverThreads.get(newPeer.getId()).start();
            this.sockets.put(newPeer, clientSocket);
            processMessage(viewChangeMessage);
        }
    }

    public void removePeer(int node) throws IOException {
        if (this.node.getState() != State.LEAVING){
            this.node.setState(State.VIEW_CHANGE);
        }
        this.newView = this.node.getView();
        if (node != this.node.getId()) {
            closeConnection(node);
        }
        this.newView.removeIf(peer -> peer.getId() == node);
        triggerViewChange(node);
    }

    public void triggerViewChange(int toRemove) throws IOException {
        this.node.setState(State.VIEW_CHANGE);
        this.newView.sort(Comparator.comparing(Peer::getId));
        sendMulticast(new ViewChangeMessage(this.node.getId(), this.newView, toRemove));
        for (ContentMessage messages : this.node.getUnstableMessageQueue()) {
            sendMulticast(messages);
        }
        sendMulticast(new FlushMessage(this.node.getId()));
    }

    public void leaveView() throws IOException {
        this.node.setState(State.LEAVING);
        removePeer(this.node.getId());
        handleDisconnection();
    }

    public void joinView(String address, int port){
        try {
            Socket firstSocket = new Socket(InetAddress.getByName(address), port);
            sleep(1000);
            sendUnicast(new JoinMessage(-1), firstSocket);
            Thread firstReceiver = new ReceiverTask(this, firstSocket);
            firstReceiver.start();
            ViewChangeMessage message = (ViewChangeMessage) this.node.dequeueIncomingMessage();
            this.newView = message.getView();
            Peer first = newView.stream().filter(peer -> peer.getId() == message.getSourceId()).findFirst().get();
            this.sockets.put(first, firstSocket);
            this.receiverThreads.put(message.getSourceId(), firstReceiver);
            int ownId = newView.stream().filter(peer -> {
                try {
                    return peer.getAddress().equals(InetAddress.getLocalHost());            //note: very weird method, might return wrong host address. Didn't find a better candidate.
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }).findFirst().get().getId();
            this.node.setId(ownId);
            for (Peer peer : this.newView.stream().filter(p -> p.getId() != this.node.getId() && p.getId() != message.getSourceId()).toList()) {
                Socket socket = new Socket(peer.getAddress(), peer.getPort());
                this.sockets.put(peer, socket);
                this.receiverThreads.put(peer.getId(), new ReceiverTask(this, socket));
                this.receiverThreads.get(peer.getId()).start();
            }
            deliverThread.start();
            sleep(1000);
            triggerViewChange(-1);
        } catch (Exception e) {
            System.err.println("Error: failed to join the view. " + e.getMessage());
        }
    }

    public ArrayList<Thread> getSendingThreads() {
        return this.sendingThreads;
    }

    public ArrayList<Thread> getProcessThreads() {
        return this.processThreads;
    }

    // TODO:testing purposes only
    public void connect(InetAddress address, int port) {
        try {
            Socket socket = new Socket(address, port);
            this.addPeer(socket);
        } catch (IOException e) {
            System.out.println("Error: failed to connect to peer at address " + address + " and port " + port + ". " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createMulticastGroup() throws UnknownHostException {
        this.node.getView().add(new Peer(0, InetAddress.getLocalHost(), this.connectionManager.getPort()));
        this.node.setId(0);
        deliverThread.start();
    }

    public void handleDisconnection(){
        for (Peer peer : this.node.getView()) {
            if (peer.getId() != this.node.getId()) {
                try {
                    sockets.get(peer).close();
                } catch (Exception e) {
                    System.out.println("Error: failed to close connection with peer " + peer.getId() + ". " + e.getMessage());
                }
            }
        }
        this.deliverThread.interrupt();
        this.connectionManager.interrupt();
        this.receiverThreads.forEach((id, thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
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

    public void reconnect(){
        handleDisconnection();
        this.node.setState(State.JOINING);
        //try to connect to the first peer in the view, if it fails, try the next one
        for (Peer peer : this.node.getView()) {
            try {
                joinView(peer.getAddress().getHostAddress(), peer.getPort());
                break;
            } catch (Exception e) {
                System.out.println("Failed to connect to peer " + peer.getId() + " at address " + peer.getAddress().getHostAddress() + " and port " + peer.getPort() + ". Trying next peer.");
            }
        }
    }
}