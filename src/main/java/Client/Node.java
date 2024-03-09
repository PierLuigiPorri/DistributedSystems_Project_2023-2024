package Client;

import Messages.Message;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Node {
    private List<Peer> view;
    private HashMap<Integer, Integer> viewTimers;
    private int id;      //TODO: set this when receiving a new view after a join
    private State state;

    private final LinkedBlockingQueue<Message> incomingMessageQueue;
    private final LinkedBlockingQueue<Message> unstableMessageQueue;
    private final LinkedBlockingQueue<Message> outgoingMessageQueue;

    public Node() {
        this.incomingMessageQueue = new LinkedBlockingQueue<>();
        this.unstableMessageQueue = new LinkedBlockingQueue<>();
        this.outgoingMessageQueue = new LinkedBlockingQueue<>();
    }

    public void queueMessage(Message message){
        this.incomingMessageQueue.add(message);
    }

    public Message dequeueIncomingMessage() throws InterruptedException {
        return this.incomingMessageQueue.take();
    }

    public void queueUnstableMessage(Message message){
        this.unstableMessageQueue.add(message);
    }

    public void installNewView(List<Peer> newView){
        this.view = newView;
    }

    public void initializeTimer() {
        //TODO: for (Client.Node node : view)
        this.viewTimers=new HashMap<>();
        for (Peer node : view) {
            this.viewTimers.put(node.getId(), 0);
        }
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