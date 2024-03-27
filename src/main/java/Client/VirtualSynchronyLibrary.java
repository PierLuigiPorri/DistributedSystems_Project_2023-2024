package Client;
/*
    * VirtualSynchronyLibrary.java
    * This class is the main class of the library.
    * It contains the main methods to send and receive messages, process messages, and handle view changes.
    * It also contains the main methods to handle connections and disconnections.
 */

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

public class VirtualSynchronyLibrary {

    private final Node node;                                    // node of the local machine
    private final ArrayList<Thread> sendingThreads;             // list of threads for sending messages
    private final ArrayList<Thread> processThreads;             // list of threads for processing messages
    private final HashMap<Integer, Thread> receiverThreads;     // list of threads for receiving messages
    private final Thread deliverThread;                         //thread for delivering incoming messages
    private final ConnectionManager connectionManager;          // class to handle incoming connections

    private int countFlush = 0;                                 // counter for the number of flush messages received
    private ArrayList<Peer> newView;                            // new view after a view change
    private final HashMap<Peer, Socket> sockets;                // map of sockets for each peer

    /*
    * Constructor for the VirtualSynchronyLibrary.
    * @param port The port number to listen for incoming connections.
    * @throws IOException
     */
    public VirtualSynchronyLibrary(int port) throws IOException {
        node = new Node();
        this.connectionManager = new ConnectionManager(this, port);
        connectionManager.start();
        this.deliverThread = new DeliverTask(this);
        sendingThreads = new ArrayList<>();
        processThreads = new ArrayList<>();
        receiverThreads = new HashMap<>();
        sockets = new HashMap<>();
    }

    /*
    * Method to process a message received by the node.
    * @param message The message to process.
    * @throws IOException
    * @throws InterruptedException
     */
    public void processMessage(Message message) throws IOException, InterruptedException {
        switch (message.getType()) {
            case CONTENT -> {
                ContentMessage contentMessage = (ContentMessage) message;
                boolean toDrop = (this.node.getUnstableMessageQueue().stream().noneMatch(m -> (m.getSourceId() == contentMessage.getSourceId() && m.getSequenceNumber() >= contentMessage.getSequenceNumber()) ||
                        (m.getSourceId() == contentMessage.getSourceId() && contentMessage.getSequenceNumber() - m.getSequenceNumber() > 1))) ||
                        this.node.getUnstableMessageQueue().isEmpty() && this.node.getView().stream().anyMatch(p -> p.getId() == contentMessage.getSourceId() && contentMessage.getSequenceNumber() - p.getSequenceNumber() > 1);
                if (!toDrop) {
                    this.node.queueUnstableMessage(contentMessage);
                    if (this.node.getState().equals(State.NORMAL)) {                // if the node is in the normal state, process the message. Else, we're in view change state, and we let it sit until the flush message is received.
                        ProcessTask processThread = new ProcessTask(this);
                        this.processThreads.add(processThread);
                        processThread.start();
                    }
                }
            }

            case VIEW_CHANGE -> {
                if (!this.node.getState().equals(State.VIEW_CHANGE)) {                  // ignore view change messages if we're already in view change state
                    this.node.setState(State.VIEW_CHANGE);
                    ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
                    newView = viewChangeMessage.getView();
                    int toRemove = viewChangeMessage.getToRemove();
                    if (toRemove != -1) {
                        closeConnection(toRemove);
                    }
                    for (ContentMessage messages : this.node.getUnstableMessageQueue()) {
                        sendMulticast(messages);
                    }
                    sendMulticast(new FlushMessage(this.node.getId()));
                }
            }
            case PING -> {
                PingMessage pingMessage = (PingMessage) message;
                this.getNode().resetTimer(pingMessage.getSourceId());
            }
            case ACK -> {
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
                    for (ContentMessage content : this.getNode().getUnstableMessageQueue()) {
                        this.node.writeOnDisk(content);
                        this.node.getView().stream().filter(peer -> peer.getId() == content.getSourceId()).forEach(Peer::incrementSequenceNumber);
                    }
                    this.getNode().getUnstableMessageQueue().clear();
                    this.node.installNewView(newView);
                    Thread pingThread = new Thread(new PingTask(this));
                    pingThread.start();
                    Thread timerThread = new TimerTask(this);
                    timerThread.start();
                    this.node.setState(State.NORMAL);
                }
            }
        }
    }

    /*
    * Method to close a connection with a peer.
    * @param toRemove The id of the peer to remove.
    * @throws IOException
     */
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

    /*
    * Method to send a message to the view. API call to send a message from an external software.
    * @param message The message to send.
    * @throws InterruptedException
     */
    public void sendMessage(ContentMessage message) throws InterruptedException {
        if (this.node.getState().equals(State.NORMAL)) {
            this.node.queueOutgoingMessage(message);
            SendingTask sendingThread = new SendingTask(this);
            this.sendingThreads.add(sendingThread);
            sendingThread.start();
        }
    }

    /*
    * Method to get the node of the library.
    * @return The node of the library.
     */
    public Node getNode() {
        return this.node;
    }

    /*
    * Method to send a multicast message to all nodes in the view.
    * @param message The message to send.
    * @throws IOException
     */
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

    /*
    * Method to send an unicast message to a specific peer.
    * @param message The message to send.
    * @param peerSocket The socket of the peer to send the message to.
    * @throws IOException
     */
    public void sendUnicast(Message message, Socket peerSocket) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(peerSocket.getOutputStream());
        out.writeObject(message);
        out.close();
    }

    /*
    * Method to get the socket of a peer from its id.
    * @param peerId The id of the peer.
    * @return The socket of the peer.
     */
    public Socket getSocketFromId(int peerId) throws IOException {
        return sockets.get(this.node.getView().stream().filter(peer -> peer.getId() == peerId).findFirst().orElseThrow(() -> new IOException("Error: failed to find socket.")));
    }

    /*
    * Method to get the size of the view.
    * @return The size of the view.
     */
    public int getViewSize() {
        return this.getNode().getView().size();
    }

    /*
    * Method to add a peer to the view.
    * @param clientSocket The socket of the peer to add.
    * @throws IOException
    * @throws ClassNotFoundException
    * @throws InterruptedException
     */
    public void addPeer(Socket clientSocket) throws IOException, ClassNotFoundException, InterruptedException {
        this.node.setState(State.JOINING);
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        Message message = (Message) in.readObject();                                                        //first message from the new peer
        in.close();
        if (message.getType().equals(MessageEnum.JOIN)) {                                                   //if the message is a join message, add the peer to the view
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
            sendUnicast(new ViewChangeMessage(this.node.getId(), this.newView, -1), clientSocket);  //send the view to the new peer to let it connect to the others
        } else {                                                                                              //if the message is not a join message, it's a view change message, so add new peer and then process it
            ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
            Peer newPeer = viewChangeMessage.getView().stream().filter(peer -> peer.getId() == viewChangeMessage.getSourceId()).findFirst().orElseThrow(() -> new IOException("Error: failed to find peer in the view."));
            this.receiverThreads.put(newPeer.getId(), new ReceiverTask(this, clientSocket));
            this.receiverThreads.get(newPeer.getId()).start();
            this.sockets.put(newPeer, clientSocket);
            processMessage(viewChangeMessage);
        }
    }

    /*
    * Method to remove a peer from the view.
    * @param node The id of the peer to remove.
    * @throws IOException
     */
    public void removePeer(int node) throws IOException {
        if (this.node.getState() != State.LEAVING){                             //if the state is LEAVING the node to remove is itself
            this.node.setState(State.VIEW_CHANGE);
        }
        this.newView = this.node.getView();
        if (node != this.node.getId()) {
            closeConnection(node);
        }
        this.newView.removeIf(peer -> peer.getId() == node);
        triggerViewChange(node);
    }

    /*
    * Method to trigger a view change.
    * @param toRemove The id of the peer to remove. If -1, no peer is removed.
    * @throws IOException
     */
    public void triggerViewChange(int toRemove) throws IOException {
        this.node.setState(State.VIEW_CHANGE);
        this.newView.sort(Comparator.comparing(Peer::getId));
        sendMulticast(new ViewChangeMessage(this.node.getId(), this.newView, toRemove));
        for (ContentMessage messages : this.node.getUnstableMessageQueue()) {
            sendMulticast(messages);
        }
        sendMulticast(new FlushMessage(this.node.getId()));
    }

    /*
    * Method to leave the view. API call to leave the view from an external software.
    * @throws IOException
     */
    public void leaveView() throws IOException {
        this.node.setState(State.LEAVING);
        removePeer(this.node.getId());
        handleDisconnection();
    }

    /*
    * Method to join a view.
    * @param address The address of the first peer to connect to.
    * @param port The port of the first peer to connect to.
     */
    public void joinView(String address, int port){
        try {
            Socket firstSocket = new Socket(InetAddress.getByName(address), port);
            sleep(1000);
            sendUnicast(new JoinMessage(-1), firstSocket);
            Thread firstReceiver = new ReceiverTask(this, firstSocket);
            firstReceiver.start();
            ViewChangeMessage message = (ViewChangeMessage) this.node.dequeueIncomingMessage();
            this.newView = message.getView();
            Peer first = newView.stream().filter(peer -> peer.getId() == message.getSourceId()).findFirst().orElseThrow(() -> new Exception("Error: failed to find the first peer in the view."));
            this.sockets.put(first, firstSocket);
            this.receiverThreads.put(message.getSourceId(), firstReceiver);
            int ownId = newView.stream().filter(peer -> {
                try {
                    return peer.getAddress().equals(InetAddress.getLocalHost());            //note: very weird method, might return wrong host address. Didn't find a better candidate. TODO: find a better method
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }).findFirst().orElseThrow(() -> new Exception("Error: failed to find peer in the view.")).getId();
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

    /*
    * Method to get the list of sending threads.
    * @return The list of sending threads.
     */
    public ArrayList<Thread> getSendingThreads() {
        return this.sendingThreads;
    }

    /*
    * Method to get the list of process threads.
    * @return The list of process threads.
     */
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

    /*
    * Method to create a multicast group. API call used by the first peer to create the group.
    * @throws UnknownHostException
     */
    public void createMulticastGroup() throws UnknownHostException {
        this.node.getView().add(new Peer(0, InetAddress.getLocalHost(), this.connectionManager.getPort()));
        this.node.setId(0);
        deliverThread.start();
    }

    /*
    * Method to complete a disconnection.
     */
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

    /*
    * Method to reconnect to the view after a disconnection.
     */
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