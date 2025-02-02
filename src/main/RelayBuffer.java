import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RelayBuffer {
    private final Map<Systems, ArrayList<RelayPackage>> buffer = new HashMap<>();

    public RelayBuffer() {
        for (Systems system : Systems.values()) {
            buffer.put(system, new ArrayList<>());
        }
    }

    public Map<Systems, ArrayList<RelayPackage>> getBuffer() {
        return buffer;
    }

    // Add a RelayPackage to the buffer
    public synchronized void addReplayPackage(RelayPackage relayPackage) {
        Systems receiver = relayPackage.getReceiverSystem();
        buffer.get(receiver).add(relayPackage);
        notifyAll(); // Notify all waiting threads
    }

    // Get a RelayPackage from the buffer for a specific system
    public synchronized RelayPackage getRelayPackage(Systems system) {
        while (buffer.get(system).isEmpty()) {
            try {
                wait(); // Wait until a package is available
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted while waiting for a package");
                return null;
            }
        }
        return buffer.get(system).remove(0); // Remove and return the first package
    }
}