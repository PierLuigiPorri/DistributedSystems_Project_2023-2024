package Messages;

import Client.Peer;

import java.util.ArrayList;

public class ViewChangeMessage extends Message {
    private final ArrayList<Peer> view;
    private final int toRemove;

    public ViewChangeMessage(int sourceId, ArrayList<Peer> view, int toRemove) {
        super(sourceId);
        this.view = view;
        this.toRemove = toRemove;
    }


    public ArrayList<Peer> getView() {
        return view;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.VIEW_CHANGE;
    }

    @Override
    public int getSourceId() { return sourceId; }

    public int getToRemove() {
        return toRemove;
    }
}
