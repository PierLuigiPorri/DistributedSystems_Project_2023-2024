package Client;
/*
    * Tuple class to store the sequence number and sender id of the message
    * Used for timers and acks.
 */
public class Tuple {
    private final int sequenceNumber;                                           // Sequence number of the message
    private final int senderId;                                                 // Sender id of the message

    /*
        * Constructor to initialize the sequence number and sender id of the message
        * @param sequenceNumber: Sequence number of the message
        * @param senderId: Sender id of the message
     */
    public Tuple(int sequenceNumber, int senderId) {
        this.sequenceNumber = sequenceNumber;
        this.senderId = senderId;
    }

}
