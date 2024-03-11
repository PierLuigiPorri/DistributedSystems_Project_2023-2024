package Messages;

import Client.Peer;
import java.util.List;

public class ViewChangeMessage extends Message {
    private final List<Peer> view;

    public ViewChangeMessage(int sourceId, List<Peer> view) {
        super(sourceId);
        this.view = view;
    }


    public List<Peer> getView() {
        return view;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.VIEW_CHANGE;
    }

    @Override
    public int getSourceId() { return sourceId; }
}
