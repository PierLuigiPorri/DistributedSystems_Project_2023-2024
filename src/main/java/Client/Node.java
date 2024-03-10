package Client;

import Messages.ContentMessage;
import Messages.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Node {
    private List<Peer> view;
    private HashMap<Integer, Integer> viewTimers;
    private int id;      //TODO: set this when receiving a new view after a join
    private State state;
    private final Logger memory;

    private final LinkedBlockingQueue<Message> incomingMessageQueue;
    private final LinkedBlockingQueue<Message> unstableMessageQueue;
    private final LinkedBlockingQueue<Message> outgoingMessageQueue;

    public Node() throws IOException {
        this.memory = Logger.getLogger("memory");
        FileHandler fh = new FileHandler("memory.log");
        this.memory.addHandler(fh);
        fh.setFormatter(new SimpleFormatter());
        this.incomingMessageQueue = new LinkedBlockingQueue<>();
        this.unstableMessageQueue = new LinkedBlockingQueue<>();
        this.outgoingMessageQueue = new LinkedBlockingQueue<>();
    }

    public void queueIncomingMessage(Message message){
        this.incomingMessageQueue.add(message);
    }

    public Message dequeueIncomingMessage() throws InterruptedException {
        return this.incomingMessageQueue.take();
    }

    public void queueUnstableMessage(Message message){
        this.unstableMessageQueue.add(message);
    }
    public void dequeueUnstableMessage() throws InterruptedException {
        this.unstableMessageQueue.take();
    }

    public void queueOutgoingMessage(Message message){
        this.outgoingMessageQueue.add(message);
    }

    public Message dequeueOutgoingMessage() throws InterruptedException {
        return this.outgoingMessageQueue.take();
    }

    public void installNewView(List<Peer> newView){
        this.view = newView;
    }

    public void initializeTimer() {
        this.viewTimers=new HashMap<>();
        for (Peer node : view) {
            this.viewTimers.put(node.getId(), 0);
        }
    }

    public void resetTimer(int id){
        this.viewTimers.put(id, 0);
    }

    public void incrementTimers(){
        viewTimers.replaceAll((i, v) -> viewTimers.get(i) + 1);
    }


    public int checkIfSomeoneIsDead() {
        for (Peer node : view) {
            if (viewTimers.get(node.getId()) > 3) {
                return node.getId();
            }
        }
        return -1;
    }

    public void commit(ContentMessage message) {
        memory.info(message.toCommitString());
    }

    // Getters and setters

    public List<Peer> getView() {
        return view;
    }

    public HashMap<Integer, Integer> getViewTimers() {
        return viewTimers;
    }

    public void setView(List<Peer> view) {
        this.view = view;
    }

    public void setViewTimers(HashMap<Integer, Integer> viewTimers) {
        this.viewTimers = viewTimers;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

}