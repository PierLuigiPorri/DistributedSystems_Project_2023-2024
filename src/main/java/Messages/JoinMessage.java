package Messages;

public class JoinMessage extends Message {

    public JoinMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.JOIN;
    }

    @Override
    public int getSourceId() { return sourceId; }

}
