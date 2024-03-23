import Client.ReliableBroadcastLibrary;
import Client.State;
import Client.Tuple;
import Messages.ContentMessage;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.*;

public class ReliableBroadcastLibraryTest {

    //add unit test for the methods of the ReliableBroadcastLibrary class
    @Test
    public void testReliableBroadcastLibrary() throws IOException {
        ReliableBroadcastLibrary rbl = new ReliableBroadcastLibrary();
        assertNotNull(rbl);
        assertNotNull(rbl.getNode());
    }


    @Test
    public void instantiatePeers() throws IOException {
        ReliableBroadcastLibrary rbl = new ReliableBroadcastLibrary();
        ReliableBroadcastLibrary rbl2 = new ReliableBroadcastLibrary();
        ReliableBroadcastLibrary rbl3 = new ReliableBroadcastLibrary();
        rbl2.connect(InetAddress.getLocalHost(), 30);
        rbl3.connect(InetAddress.getLocalHost(), 30);
        rbl2.connect(InetAddress.getLocalHost(), 32);

        assertEquals(2, rbl.getNode().getView().size());
        assertEquals(2, rbl2.getNode().getView().size());
        assertEquals(2, rbl3.getNode().getView().size());
        System.out.println("1" + " " + rbl.getNode().getView().get(0).getId() + " " + rbl.getNode().getView().get(0).getPort());
        System.out.println("1" + " " + rbl.getNode().getView().get(1).getId() + " " + rbl.getNode().getView().get(1).getPort());
        System.out.println("2" + " " + rbl2.getNode().getView().get(0).getId() + " " + rbl2.getNode().getView().get(0).getPort());
        System.out.println("2" + " " + rbl2.getNode().getView().get(1).getId() + " " + rbl2.getNode().getView().get(1).getPort());
        System.out.println("3" + " " + rbl3.getNode().getView().get(0).getId() + " " + rbl3.getNode().getView().get(0).getPort());
        System.out.println("3" + " " + rbl3.getNode().getView().get(1).getId() + " " + rbl3.getNode().getView().get(1).getPort());
        //The weird port numbers you're seeing in the output are likely due to ephemeral port assignments by the operating system.
        // When you establish a connection to a server socket, the operating system assigns an available port number for the client socket. These port numbers are typically chosen from a range known as ephemeral ports, which are usually high-numbered ports.
        // In your test, you're creating multiple instances of ReliableBroadcastLibrary and connecting them to the same host address and port numbers. When you call connect method multiple times with the same port number, the operating system assigns different ephemeral port numbers for each client socket.
        //For example:
        //In the first instantiation rbl, the node's view ports are 64296 and 64297.
        //In the second instantiation rbl2, the node's view ports are 30 and 32.
        //In the third instantiation rbl3, the node's view ports are also 30 and 64298.
        //The port numbers you see in the output are the local port numbers assigned to the client sockets on the respective machines. They may seem random or unusual, but they are simply assigned by the operating system as part of the networking process.
    }

    @Test
    public void testReliableBroadcastLibraryProcessingMessagesIncomingMessage() throws IOException, InterruptedException {
        ReliableBroadcastLibrary rbl = new ReliableBroadcastLibrary();
        ReliableBroadcastLibrary rbl2 = new ReliableBroadcastLibrary();
        rbl2.connect(InetAddress.getLocalHost(), 30);
        rbl.getNode().setState(State.NORMAL);
        rbl.getNode().incrementAcks(new Tuple(1, 1));
        rbl.processMessage(new ContentMessage("test", 1, 1));
        rbl.getNode().setState(State.DISCONNECTED);

        assertNotNull(rbl.getNode().dequeueUnstableMessage());

        rbl.getNode().setState(State.NORMAL);
        rbl.getNode().incrementAcks(new Tuple(1, 1));
        rbl.processMessage(new ContentMessage("test", 1, 1));
        System.out.println(rbl.getNode().dequeueUnstableMessage().getPayload());
    }

    @Test
    public void sendMulticast() throws IOException, InterruptedException {

        ReliableBroadcastLibrary rbl = new ReliableBroadcastLibrary();
        ReliableBroadcastLibrary rbl2 = new ReliableBroadcastLibrary();
        ReliableBroadcastLibrary rbl3 = new ReliableBroadcastLibrary();
        rbl2.connect(InetAddress.getLocalHost(), 30);
        rbl3.connect(InetAddress.getLocalHost(), 30);
        rbl2.connect(InetAddress.getLocalHost(), 32);
        rbl.sendMulticast(new ContentMessage("test", rbl.getNode().getId(), 1));
        assertNotNull(rbl.getNode().dequeueOutgoingMessage());
        assertNotNull(rbl2.getNode().dequeueIncomingMessage());
        assertNotNull(rbl3.getNode().dequeueIncomingMessage());
        assertEquals(1, rbl.getNode().getAcks(new Tuple(1, rbl.getNode().getView().get(0).getId())));
        assertEquals(1, rbl.getNode().getAcks(new Tuple(1, rbl.getNode().getView().get(1).getId())));


    }
}
