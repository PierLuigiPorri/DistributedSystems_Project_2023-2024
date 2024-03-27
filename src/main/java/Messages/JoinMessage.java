package Messages;
/*
    * JoinMessage is a message that is sent by a process to indicate that it wants to join the network.
    * It contains the id of the process that has to joined.
 */
public class JoinMessage extends Message{
    /*
        * Constructor for JoinMessage
        * @param id: id of the process that has to join the network
     */
    public JoinMessage(int id){  //id to be ignored
        super(id);
    }

    /*
        * Returns the type of the message
        * @return MessageEnum: type of the message
     */
    @Override
    public MessageEnum getType() {
        return MessageEnum.JOIN;
    }

    /*
        * Returns the id of the process sending the message
        * @return int: id of the process sending the message
     */
    @Override
    public int getSourceId() {
        return this.sourceId;
    }
}
