package Messages;

public class PingMessage extends Message {

    public PingMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.PING;
    }

    @Override
    public int getSourceId() {
        return sourceId;
    }
}
