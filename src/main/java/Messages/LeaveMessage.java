package Messages;

public class LeaveMessage extends Message {

    public LeaveMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.LEAVE;
    }

    @Override
    public int getSourceId() {
        return sourceId;
    }

}
