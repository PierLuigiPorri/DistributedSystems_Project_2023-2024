package Messages;
/*
    * AckMessage is a message that is sent by a process to acknowledge the receipt of a message.
    * It contains the sequence number of the message being acknowledged and the id of the sender of the message.
 */
public class AckMessage extends Message {
    private final int sequenceNumber;                                       // Sequence number of the message being acknowledged
    private final int senderId;                                             // id of the sender of the message

    /*
        * Constructor for AckMessage
        * @param sourceId: id of the process sending the message
        * @param sequenceNumber: sequence number of the message being acknowledged
        * @param senderId: id of the sender of the message
     */
    public AckMessage(int sourceId, int sequenceNumber, int senderId) {
        super(sourceId);
        this.sequenceNumber = sequenceNumber;
        this.senderId = senderId;
    }

    /*
        * Returns the type of the message
        * @return MessageEnum: type of the message
     */
    @Override
    public MessageEnum getType() {
        return MessageEnum.ACK;
    }

    /*
        * Returns the id of the process sending the message
        * @return int: id of the process sending the message
     */
    @Override
    public int getSourceId() { return sourceId; }

    /*
        * Returns the sequence number of the message being acknowledged
        * @return int: sequence number of the message being acknowledged
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /*
        * Returns the id of the sender of the message
        * @return int: id of the sender of the message
     */
    public int getSenderId() {
        return senderId;
    }
}
