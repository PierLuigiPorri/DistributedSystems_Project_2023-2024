import java.net.InetAddress;
import java.util.List;

public class ReliableBroadcastLibrary {
    public void processMessage(Message message, List<InetAddress> view) {
        switch (message.getType()) {
            case STRING:
                // print the message
                if (message instanceof StringMessage) {
                    StringMessage stringMessage = (StringMessage) message;
                    System.out.println(stringMessage.getMessage());
                }
                break;
            case JOIN:
                // add the new node to the view
                if (message instanceof JoinMessage) {
                    JoinMessage joinMessage = (JoinMessage) message;
                    view.add(joinMessage.getNode().getAddress());
                }
                break;
            case VIEW_CHANGE:
                // update the view
                if (message instanceof ViewChangeMessage) {
                    ViewChangeMessage viewChangeMessage = (ViewChangeMessage) message;
                    view = viewChangeMessage.getView();
                }
                break;
            case PING:
                // reset the timer for the sender
                if (message instanceof PingMessage) {
                    PingMessage pingMessage = (PingMessage) message;
                    //send messsage back to the sender of the ping
                    //TODO: send(new AckMessage(this.address, pingMessage.getSequenceNumber(), this.id, pingMessage.getSource()));
                }
                break;
        }
    }
}
