package Messages;

public class DropMessage extends Message {

    public DropMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.DROP;
    }

    @Override
    public int getSourceId() { return sourceId; }

}
