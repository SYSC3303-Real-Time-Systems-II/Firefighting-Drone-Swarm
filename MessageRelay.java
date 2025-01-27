import java.util.Dictionary;
import java.util.Hashtable;

public class MessageRelay {

    private RelayPackage contents = null;
    private boolean empty = true;

    public synchronized void put(String name, RelayPackage relayPackage ) {
        while (!empty) {
            try {
                System.out.println("[" + name + "] -- Waiting since Counter is full");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        contents = relayPackage;
        empty = false;
        System.out.println("[" + name + "] -- Put out " + contents);
        notifyAll();
    }

    public synchronized RelayPackage get(String name, Systems systemType) {
        while (empty || contents == null || contents.receiverSystem != systemType) {
            try {
                System.out.println("[" + name + "] -- Waiting for a package for system ");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Retrieve the package
        RelayPackage receivePackage = contents;
        contents = null;
        empty = true;

        System.out.println("[" + name + "] -- Successfully obtained " + receivePackage);
        notifyAll(); // Notify producers and other waiting threads
        return receivePackage;
    }

}
