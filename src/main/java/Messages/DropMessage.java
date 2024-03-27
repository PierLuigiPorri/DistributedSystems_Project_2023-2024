package Messages;
/*
    * DropMessage is a message that is sent by a node to inform other nodes that a message has been dropped.
    * It contains the sequence number of the message being dropped.
 */
public class DropMessage extends Message {
    private final int sequenceNumber;                           // Sequence number of the message being dropped

    /*
        * Constructor for DropMessage
        * @param sourceId: id of the process sending the message
        * @param sequenceNumber: sequence number of the message being dropped
     */
    public DropMessage(int sourceId, int sequenceNumber) {
        super(sourceId);
        this.sequenceNumber = sequenceNumber;
    }

    /*
        * Returns the type of the message
        * @return MessageEnum: type of the message
     */
    @Override
    public MessageEnum getType() {
        return MessageEnum.DROP;
    }

    /*
        * Returns the source id of the message
        * @return int: the source id of the message
     */
    @Override
    public int getSourceId() { return sourceId; }

    /*
        * Returns the sequence number of the message being dropped
        * @return int: sequence number of the message being dropped
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

}
