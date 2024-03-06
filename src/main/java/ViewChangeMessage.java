import java.net.InetAddress;
import java.util.List;

public class ViewChangeMessage extends Message{
    private final long timeCreated = System.currentTimeMillis();

    private List<InetAddress> view;
    private List<Integer> viewIds;

    public ViewChangeMessage(InetAddress source, int sourceId, InetAddress destination, List<InetAddress> view, List<Integer> viewIds) {
        super(source, sourceId, destination);
        this.view = view;
        this.viewIds = viewIds;
    }


    public List<InetAddress> getView() {
        return view;
    }

    @Override
    public MessageEnum getType() {
        return MessageEnum.VIEW_CHANGE;
    }

    @Override
    public String getTransmissionString() {
        //TODO: implement method
        return null;
    }

    @Override
    public InetAddress getSource() { return source; }


}
