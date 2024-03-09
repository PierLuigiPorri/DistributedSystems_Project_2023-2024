package Messages;

public class FlushMessage extends Message {

    public FlushMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.FLUSH;
    }

    @Override
    public int getSourceId() { return sourceId; }

}
