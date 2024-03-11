package Messages;

public abstract class Message implements java.io.Serializable {
    protected int sourceId;

    public Message(int sourceId) {
        this.sourceId = sourceId;
    }

    public abstract MessageEnum getType();


    public abstract int getSourceId();

}