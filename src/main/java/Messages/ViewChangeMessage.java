package Messages;
/*
    * ViewChangeMessage is a message that is sent by a process to inform other processes about the new view.
    * It contains the new view and the id of the process to be removed from the view.
 */
import Client.Peer;
import java.util.ArrayList;

public class ViewChangeMessage extends Message {
    private final ArrayList<Peer> view;                                         // New view
    private final int toRemove;                                                 // id of the process to be removed from the view, -1 if no process is to be removed

    /*
        * Constructor for ViewChangeMessage
        * @param sourceId: id of the process sending the message
        * @param view: new view
        * @param toRemove: id of the process to be removed from the view, -1 if no process is to be removed
     */
    public ViewChangeMessage(int sourceId, ArrayList<Peer> view, int toRemove) {
        super(sourceId);
        this.view = view;
        this.toRemove = toRemove;
    }


    /*
        * Returns the new view
        * @return ArrayList<Peer>: new view
     */
    public ArrayList<Peer> getView() {
        return view;
    }

    /*
        * Returns the type of the message
        * @return MessageEnum: type of the message
     */
    @Override
    public MessageEnum getType() {
        return MessageEnum.VIEW_CHANGE;
    }

    /*
        * Returns the id of the process sending the message
        * @return int: id of the process sending the message
     */
    @Override
    public int getSourceId() { return sourceId; }

    /*
        * Returns the id of the process to be removed from the view
        * @return int: id of the process to be removed from the view
     */
    public int getToRemove() {
        return toRemove;
    }
}
