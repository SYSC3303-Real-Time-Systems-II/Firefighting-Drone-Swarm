import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The RelayBuffer class is a thread-safe buffer used for storing and retrieving RelayPackage objects.
 * EventBuffer is for communication between the Scheduler and FireIncidentSubsystem.
 * It allows systems to communicate by adding and retrieving packages in a synchronized manner.
 */
public class RelayBuffer {

    // A map to store lists of RelayPackages for each system
    private final Map<Systems, ArrayList<RelayPackage>> buffer = new HashMap<>();

    /**
     * Constructs a RelayBuffer object and initializes the buffer with an empty list for each system.
     */
    public RelayBuffer() {
        for (Systems system : Systems.values()) {
            buffer.put(system, new ArrayList<>());
        }
    }

    /**
     * Adds a RelayPackage to the buffer for the specified receiver system.
     * This method is synchronized to ensure thread safety.
     *
     * @param relayPackage The RelayPackage to be added to the buffer.
     */
    public synchronized void addReplayPackage(RelayPackage relayPackage) {
        Systems receiver = relayPackage.getReceiverSystem();
        buffer.get(receiver).add(relayPackage);
        notifyAll(); // Notify all waiting threads
    }

    /**
     * Retrieves a RelayPackage from the buffer for a specific system.
     * This method is synchronized to ensure thread safety.
     * If no packages are available, the thread will wait until a package is added.
     *
     * @param system The system for which to retrieve the package.
     * @return The RelayPackage retrieved from the buffer, or null if the thread is interrupted.
     */
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