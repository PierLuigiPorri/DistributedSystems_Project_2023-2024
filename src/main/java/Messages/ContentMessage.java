package Messages;

/*
    * ContentMessage is a class that extends Message and represents a message that contains a payload.
 */
public class ContentMessage extends Message {
    private final String payload;                                                   // The payload of the message
    private int sequenceNumber;                                                     // The sequence number of the message

    /*
        * Constructor that initializes the sourceId, payload and sequenceNumber of the message.
        * @param payload: The payload of the message
        * @param sourceId: The sourceId of the message
        * @param sequenceNumber: The sequence number of the message
     */
    public ContentMessage(String payload, int sourceId, int sequenceNumber) {
        super(sourceId);
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
    }

    /*
     * Returns the type of the message
     * @return MessageEnum: type of the message
     */
    @Override
    public MessageEnum getType() {
        return MessageEnum.CONTENT;
    }

    /*
     * Returns the id of the process sending the message
     * @return int: id of the process sending the message
     */
    @Override
    public int getSourceId() {
        return sourceId;
    }

    /*
     * Returns the payload of the message
     * @return String: payload of the message
     */
    public String getPayload() {
        return payload;
    }

    /*
    * Returns the string to commit to the log
    * @return String: string to commit to the log
     */
    public String toCommitString() {
        return sourceId + "sent: " + this.payload;
    }

    /*
     * Returns the sequence number of the message
     * @return int: sequence number of the message
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /*
     * Sets the sequence number of the message
     * @param sequenceNumber: sequence number of the message
     */
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
