package Messages;

public class AckMessage extends Message {

    public AckMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.ACK;
    }

    @Override
    public int getSourceId() { return sourceId; }

}
