import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ListeningTask implements Runnable {

    private Node node;
    private int port;

    public ListeningTask(Node node, int port) {
        this.node = node;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Message message = Message.parseToMessage(new String(packet.getData(), 0, packet.getLength()));
                node.processMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}