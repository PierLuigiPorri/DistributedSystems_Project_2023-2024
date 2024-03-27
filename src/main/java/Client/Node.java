package Client;
/*
    Node class is the main class of the client side. It contains all the information about the client, such as the view, the state, the id, the sequence number, the incoming and outgoing message queues, the acks, and the memory logger.
 */
import Messages.ContentMessage;
import Messages.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Node {
    private ArrayList<Peer> view;                                                       // List of peers in the view
    private HashMap<Integer, Integer> viewTimers;                                       // Timers for each peer in the view
    private int id;                                                                     // id of the client
    private State state;                                                                // State of the client
    private final Logger memory;                                                        // Logger for the memory
    private int sequenceNumber = 0;                                                     // Sequence number of the client

    private final LinkedBlockingQueue<Message> incomingMessageQueue;                    // Queue for incoming messages
    private final LinkedBlockingQueue<ContentMessage> unstableMessageQueue;             // Queue for unstable messages
    private final LinkedBlockingQueue<ContentMessage> outgoingMessageQueue;             // Queue for outgoing messages
    private final HashMap<Tuple, Integer> acks;                                         //Message, number of acks

    /*
        Constructor of the Node class. It initializes the memory logger, the incoming, unstable and outgoing message queues, the acks, the state, and the view.
     */
    public Node() throws IOException {
        this.memory = Logger.getLogger("memory");
        FileHandler fh = new FileHandler("memory.log");
        this.memory.addHandler(fh);
        fh.setFormatter(new SimpleFormatter());
        this.incomingMessageQueue = new LinkedBlockingQueue<>();
        this.unstableMessageQueue = new LinkedBlockingQueue<>();
        this.outgoingMessageQueue = new LinkedBlockingQueue<>();
        this.acks = new HashMap<>();
        this.state = State.JOINING;
        this.view = new ArrayList<>();
    }

    /*
    * Method to queue incoming messages.
    * @param message: Message to be queued.
    * @throws InterruptedException
     */
    public void queueIncomingMessage(Message message) throws InterruptedException {
        this.incomingMessageQueue.put(message);
    }

    /*
    * Method to dequeue incoming messages.
    * @return Message: Message dequeued.
    * @throws InterruptedException
     */
    public Message dequeueIncomingMessage() throws InterruptedException {
        return this.incomingMessageQueue.take();
    }

    /*
    * Method to queue unstable messages.
    * @param message: Message to be queued.
    * @throws InterruptedException
     */
    public void queueUnstableMessage(ContentMessage message) throws InterruptedException {
        this.unstableMessageQueue.put(message);
    }

    /*
    * Method to dequeue unstable messages.
    * @return ContentMessage: Message dequeued.
    * @throws InterruptedException
     */
    public ContentMessage dequeueUnstableMessage() throws InterruptedException {
        return this.unstableMessageQueue.take();
    }

    /*
    * Method to queue outgoing messages.
    * @param message: Message to be queued.
    * @throws InterruptedException
     */
    public void queueOutgoingMessage(ContentMessage message) throws InterruptedException {
        this.outgoingMessageQueue.put(message);
    }

    /*
    * Method to dequeue outgoing messages.
    * @return ContentMessage: Message dequeued.
    * @throws InterruptedException
     */
    public ContentMessage dequeueOutgoingMessage() throws InterruptedException {
        return this.outgoingMessageQueue.take();
    }

    /*
    * Method to write on the disk the content of a message.
    * @param message: Message to be written on the disk.
     */
    public void writeOnDisk(ContentMessage message) {
        String address = "";
        for (Peer node : view) {
            if (node.getId() == message.getSourceId()) {
                address = node.getAddress().toString();
            }
            this.memory.info(message.toCommitString() + "from" + address);
        }
    }

    /*
    * Method to install a new view.
    * @param newView: New view to be installed.
     */
    public void installNewView(ArrayList<Peer> newView) {
        this.view = newView;
    }

    /*
    * Method to initialize the timers for each peer in the view.
     */
    public void initializeTimer() {
        this.viewTimers = new HashMap<>();
        for (Peer node : view) {
            this.viewTimers.put(node.getId(), 0);
        }
    }

    /*
    * Method to reset the timer of a peer in the view.
    * @param id: id of the peer whose timer is to be reset.
     */
    public void resetTimer(int id) {
        this.viewTimers.put(id, 0);
    }

    /*
    * Method to increment the timers of each peer in the view.
    * If a peer's timer is greater than 2, it is considered dead.
     */
    public void incrementTimers() {
        viewTimers.replaceAll((i, v) -> viewTimers.get(i) + 1);
    }

    /*
    * Method to check if someone is dead.
    * @return int: id of the dead peer.
    * If no one is dead, it returns -1.
    * If more than one peer is dead, it returns -2.
    * If one peer is dead, it returns its id.
     */
    public int checkIfSomeoneIsDead() {
        int deadNode = -1;
        for (Peer node : view) {
            if (viewTimers.get(node.getId()) > 2) {
                if (deadNode == -1) {
                    deadNode = node.getId();
                } else {
                    return -2;
                }
            }
        }
        return deadNode;
    }

    /*
    * Method to get the view of the client.
    * @return ArrayList<Peer>: List of peers in the view.
     */
    public ArrayList<Peer> getView() {
        return view;
    }

    /*
    * Method to get the id of the client.
    * @return int: id of the client.
     */
    public int getId() {
        return this.id;
    }

    /*
    * Method to set the id of the client.
    * @param id: id to be set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /*
    * Method to get the state of the client.
    * @return State: state of the client.
     */
    public State getState() {
        return this.state;
    }

    /*
    * Method to set the state of the client.
    * @param state: state to be set.
     */
    public void setState(State state) {
        this.state = state;
    }

    /*
    * Method to get the number of acks of a message.
    * @param message: Message whose acks are to be returned.
    * @return int: number of acks of the message.
     */
    public int getAcks(Tuple message) {
        return this.acks.get(message);
    }

    /*
    * Method to increment the acks of a message.
    * @param message: Message whose acks are to be incremented.
     */
    public void incrementAcks(Tuple message) {
        if (this.acks.containsKey(message)) {
            this.acks.put(message, this.acks.get(message) + 1);
        } else {
            this.acks.put(message, 1);
        }
    }

    /*
    * Method to remove the acks of a message.
    * @param message: Message whose acks are to be removed.
     */
    public void removeAcks(Tuple message) {
        this.acks.remove(message);
    }

    /*
    * Method to drop the acks of a message.
    * @param message: Message whose acks are to be dropped.
     */
    public void dropAcks(Tuple message) {
        this.acks.put(message, -1);
    }

    /*
    * Method to increment the sequence number of the client.
     */
    public void incrementSequenceNumber() {
        this.sequenceNumber++;
    }

    /*
    * Method to get the sequence number of the client.
    * @return int: sequence number of the client.
     */
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    /*
    * Method to peek the first message in the unstable message queue.
    * @return ContentMessage: Message peeked.
     */
    public ContentMessage peekUnstableMessage() {
        return this.unstableMessageQueue.peek();
    }

    /*
    * Method to get the unstable message queue.
    * @return LinkedBlockingQueue<ContentMessage>: Unstable message queue.
     */
    public LinkedBlockingQueue<ContentMessage> getUnstableMessageQueue() {
        return this.unstableMessageQueue;
    }

    /*
    * Method to peek the outgoing message queue.
    * @return ContentMessage: Message peeked.
     */
    public ContentMessage peekOutgoingMessage() {
        return this.outgoingMessageQueue.peek();
    }
}