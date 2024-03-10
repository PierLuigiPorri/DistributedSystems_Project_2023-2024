package Client.Tasks;

import Client.ReliableBroadcastLibrary;
import Messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiverTask implements Runnable {
    private final ReliableBroadcastLibrary library;
    private final int port;

    public ReceiverTask(ReliableBroadcastLibrary library, int port) {
        this.library = library;
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
                String receivedData = new String(packet.getData(), 0, packet.getLength());
                Message message = Message.deserialize(receivedData);
                if (message != null) {
                    this.library.getNode().queueIncomingMessage(message);
                } else {
                    System.err.println("Failed to deserialize received message.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
