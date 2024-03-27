package Client.Tasks;
/*
    * This class is a part of the Client.Tasks package.
    * This class is an abstract class that extends the Thread class.
 */
import Client.VirtualSynchronyLibrary;

public abstract class RunningTask extends Thread{
    protected final VirtualSynchronyLibrary library;    // The library object to which the task belongs.

    /*
        * The constructor of the RunningTask class.
        * @param library The library object to which the task belongs.
     */
    public RunningTask(VirtualSynchronyLibrary library){
        this.library = library;
    }
}
