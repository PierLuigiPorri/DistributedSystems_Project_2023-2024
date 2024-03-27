package Messages;
/*
    * DropMessage is a message that is sent by a process to inform other processes that a message has been dropped.
    * It contains the sequence number of the message being dropped.

 */
public class PingMessage extends Message {

    /*
        * Constructor for DropMessage
        * @param sourceId: id of the process sending the message
     */
    public PingMessage(int sourceId) {
        super(sourceId);
    }

    /*
        * Returns the type of the message
        * @return MessageEnum: type of the message
     */
    @Override
    public MessageEnum getType() {
        return MessageEnum.PING;
    }

    /*
        * Returns the id of the process sending the message
        * @return int: id of the process sending the message
     */
    @Override
    public int getSourceId() {
        return sourceId;
    }
}
