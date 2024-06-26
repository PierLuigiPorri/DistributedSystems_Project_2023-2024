import Client.VirtualSynchronyLibrary;
import Client.State;
import Client.Tuple;
import Messages.ContentMessage;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class VirtualSynchronyLibraryTest {

    String PIER = "127.0.0.1";//"192.168.1.158";
    String DAVIDE = "127.0.0.1";//"192.168.1.61";
    private final int port = 3000;
    //add unit test for the methods of the ReliableBroadcastLibrary class
    @Test
    public void testReliableBroadcastLibrary() throws IOException {
        VirtualSynchronyLibrary rbl = new VirtualSynchronyLibrary(PIER,port+30);
        assertNotNull(rbl);
        assertNotNull(rbl.getNode());
    }


    @Test
    public void instantiatePeers() throws IOException {
        VirtualSynchronyLibrary rbl = new VirtualSynchronyLibrary(PIER,port+30);
        VirtualSynchronyLibrary rbl2 = new VirtualSynchronyLibrary(PIER,port+31);
        VirtualSynchronyLibrary rbl3 = new VirtualSynchronyLibrary(PIER,port+32);
        rbl.createMulticastGroup();
        rbl2.joinView("localhost", port+30);
        rbl3.joinView("localhost", port+31);


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
    public void testReliableBroadcastLibraryProcessingMessagesIncomingMessage() throws Exception {
        VirtualSynchronyLibrary rbl = new VirtualSynchronyLibrary(PIER,port+30);
        VirtualSynchronyLibrary rbl2 = new VirtualSynchronyLibrary(PIER,port+31);
        rbl2.connect(InetAddress.getLocalHost(), port+30);
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

        VirtualSynchronyLibrary rbl = new VirtualSynchronyLibrary(PIER,port+30);
        VirtualSynchronyLibrary rbl2 = new VirtualSynchronyLibrary(PIER,port+31);
        VirtualSynchronyLibrary rbl3 = new VirtualSynchronyLibrary(PIER,port+32);
        rbl2.connect(InetAddress.getLocalHost(), port+30);
        rbl3.connect(InetAddress.getLocalHost(), port+30);
        rbl2.connect(InetAddress.getLocalHost(), port+32);
        rbl.sendMulticast(new ContentMessage("test", rbl.getNode().getId(), 1));
        assertNotNull(rbl.getNode().dequeueOutgoingMessage());
        assertNotNull(rbl2.getNode().dequeueIncomingMessage());
        assertNotNull(rbl3.getNode().dequeueIncomingMessage());
        assertEquals(1, rbl.getNode().getAcks(new Tuple(1, rbl.getNode().getView().get(0).getId())));
        assertEquals(1, rbl.getNode().getAcks(new Tuple(1, rbl.getNode().getView().get(1).getId())));

    }

    @Test
    public void test1() throws Exception {
        VirtualSynchronyLibrary rbl = testWithAnotherMachine(0, PIER, DAVIDE, port+30, port+31);
        sleep(45000);
        assertEquals(2, rbl.getNode().getView().size());
    }

    @Test
    public void test2() throws Exception {
        VirtualSynchronyLibrary rbl = testWithAnotherMachine(1, DAVIDE, PIER, port+31, port+30);
        sleep(45000);
        assertEquals(2, rbl.getNode().getView().size());

    }




    public VirtualSynchronyLibrary testWithAnotherMachine(int number, String ownAddress, String otherAddress, int ownPort, int otherPort) throws Exception {
        VirtualSynchronyLibrary rbl = new VirtualSynchronyLibrary(ownAddress,ownPort);
        if(number==0){
            rbl.createMulticastGroup();
        }
        else{
            rbl.joinView(otherAddress, otherPort);
        }
        return rbl;
    }
}
