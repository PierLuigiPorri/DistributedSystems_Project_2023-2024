package Messages;
/*
    * FlushMessage is a message that is sent by a process to flush the messages in the buffer of the receiver.
    * It contains the id of the sender of the message.
 */
public class FlushMessage extends Message {

    /*
        * Constructor for FlushMessage
        * @param sourceId: id of the process sending the message
     */
    public FlushMessage(int sourceId) {
        super(sourceId);
    }

    /*
        * Returns the type of the message
        * @return MessageEnum: type of the message
     */
    @Override
    public MessageEnum getType() {
        return MessageEnum.FLUSH;
    }

    /*
        * Returns the id of the process sending the message
        * @return int: id of the process sending the message
     */
    @Override
    public int getSourceId() { return sourceId; }

}
