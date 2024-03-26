package Messages;

public class JoinMessage extends Message{
    public JoinMessage(int id){  //id to be ignored
        super(id);
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.JOIN;
    }

    @Override
    public int getSourceId() {
        return this.sourceId;
    }
}
