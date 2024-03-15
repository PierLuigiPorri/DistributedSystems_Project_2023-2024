package Messages;

import Client.Peer;

import java.util.ArrayList;
import java.util.List;

public class ViewChangeMessage extends Message {
    private final ArrayList<Peer> view;

    public ViewChangeMessage(int sourceId, ArrayList<Peer> view) {
        super(sourceId);
        this.view = view;
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
}
