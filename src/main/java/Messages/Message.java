package Messages;
/*
    * Message is an abstract class that is extended by all the other message classes.
    * It contains the id of the process sending the message.
 */
public abstract class Message implements java.io.Serializable {
    protected final int sourceId;                       // id of the process sending the message

    /*
        * Constructor for Message
        * @param sourceId: id of the process sending the message
     */
    public Message(int sourceId) {
        this.sourceId = sourceId;
    }

    public abstract MessageEnum getType();


    public abstract int getSourceId();

}