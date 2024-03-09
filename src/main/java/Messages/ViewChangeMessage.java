package Messages;

import Client.Peer;
import Messages.Message;

import java.net.InetAddress;
import java.util.List;

public class ViewChangeMessage extends Message {
    private final long timeCreated = System.currentTimeMillis();

    private List<Peer> view;
    private List<Integer> viewIds;

    public ViewChangeMessage(int sourceId,List<Peer> view, List<Integer> viewIds) {
        super(sourceId);
        this.view = view;
        this.viewIds = viewIds;
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
