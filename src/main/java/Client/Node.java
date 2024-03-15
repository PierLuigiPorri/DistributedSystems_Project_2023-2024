package Client;

import Messages.ContentMessage;
import Messages.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Node {
    private ArrayList<Peer> view;
    private HashMap<Integer, Integer> viewTimers;
    private int id;      //TODO: set this when receiving a new view after a join
    private State state;
    private final Logger memory;
    private int sequenceNumber = 0;

    private final LinkedBlockingQueue<Message> incomingMessageQueue;
    private final LinkedBlockingQueue<ContentMessage> unstableMessageQueue;
    private final LinkedBlockingQueue<ContentMessage> outgoingMessageQueue;
    private final HashMap<Tuple, Integer> acks; //Message, number of acks

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
    }

    public void queueIncomingMessage(Message message) {
        this.incomingMessageQueue.add(message);
    }

    public Message dequeueIncomingMessage() throws InterruptedException {
        return this.incomingMessageQueue.take();
    }

    public void queueUnstableMessage(ContentMessage message) {
        this.unstableMessageQueue.add(message);
    }

    public ContentMessage dequeueUnstableMessage() throws InterruptedException {
        return this.unstableMessageQueue.take();
    }

    public void queueOutgoingMessage(ContentMessage message) {
        this.outgoingMessageQueue.add(message);
    }

    public ContentMessage dequeueOutgoingMessage() throws InterruptedException {
        return this.outgoingMessageQueue.take();
    }

    public ContentMessage peekOutgoingMessage() {
        return this.outgoingMessageQueue.peek();
    }

    public void writeOnDisk(ContentMessage message) {
        String address = "";
        for (Peer node : view) {
            if (node.getId() == message.getSourceId()) {
                address = node.getAddress().toString();
            }

            this.memory.info(message.toCommitString() + "from" + address);
        }
    }

    public void installNewView(ArrayList<Peer> newView) {
        this.view = newView;
    }

    public void initializeTimer() {
        this.viewTimers = new HashMap<>();
        for (Peer node : view) {
            this.viewTimers.put(node.getId(), 0);
        }
    }

    public void resetTimer(int id) {
        this.viewTimers.put(id, 0);
    }

    public void incrementTimers() {
        viewTimers.replaceAll((i, v) -> viewTimers.get(i) + 1);
    }


    public int checkIfSomeoneIsDead() {
        for (Peer node : view) {
            if (viewTimers.get(node.getId()) > 2) {
                return node.getId();
            }
        }
        return -1;
    }

    // Getters and setters

    public ArrayList<Peer> getView() {
        return view;
    }

    public int getId() {
        return this.id;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getAcks(Tuple message) {
        return this.acks.get(message);
    }

    public void incrementAcks(Tuple message) {
        this.acks.put(message, this.acks.get(message) + 1);
    }

    public void removeAcks(Tuple message) {
        this.acks.remove(message);
    }

    public void dropAcks(Tuple message) {
        this.acks.put(message, -1);
    }

    public LinkedBlockingQueue<Message> getIncomingMessageQueue() {
        return this.incomingMessageQueue;
    }

    public void incrementSequenceNumber() {
        this.sequenceNumber++;
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public ContentMessage peekUnstableMessage() {
        return this.unstableMessageQueue.peek();
    }

    public LinkedBlockingQueue<ContentMessage> getUnstableMessageQueue() {
        return this.unstableMessageQueue;
    }

}
