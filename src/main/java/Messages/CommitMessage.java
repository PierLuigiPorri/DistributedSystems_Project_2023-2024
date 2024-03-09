package Messages;

public class CommitMessage extends Message {

    public CommitMessage(int sourceId) {
        super(sourceId);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.COMMIT;
    }

    @Override
    public int getSourceId() { return sourceId; }

}
