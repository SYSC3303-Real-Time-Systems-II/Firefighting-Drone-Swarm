import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The EventBuffer class is a thread-safe buffer used for storing and retrieving InputEvent objects.
 * EventBuffer is for communication between the Scheduler and DroneSubsystem.
 * It allows systems to communicate by adding and retrieving events in a synchronized manner.
 */
public class EventBuffer implements Serializable {

    // A map to store lists of InputEvents for each system
    private final Map<Systems, ArrayList<InputEvent>> buffer = new HashMap<>();

    /**
     * Constructs an EventBuffer object and initializes the buffer with an empty list for each system.
     */
    public EventBuffer() {
        for (Systems system : Systems.values()) {
            buffer.put(system, new ArrayList<>());
        }
    }

    /**
     * Get the hashmap buffer.
     * @return buffer.
     */
    public Map<Systems, ArrayList<InputEvent>> getBuffer() {
        return buffer;
    }

    /**
     * Adds an InputEvent to the buffer for a specific receiver system.
     * This method is synchronized to ensure thread safety.
     *
     * @param inputEvent     The InputEvent to be added to the buffer.
     * @param receiverSystem The system that will receive the event.
     */
    public synchronized void addInputEvent(InputEvent inputEvent, Systems receiverSystem) {
        buffer.get(receiverSystem).add(inputEvent);
        notifyAll(); // Notify all waiting threads
    }


    /**
     * Retrieves an InputEvent from the buffer for a specific system.
     * This method is synchronized to ensure thread safety.
     * If no events are available, the thread will wait until an event is added.
     *
     * @param system The system for which to retrieve the event.
     * @return The InputEvent retrieved from the buffer, or null if the thread is interrupted.
     */
    public synchronized InputEvent getInputEvent(Systems system) {
        while (buffer.get(system).isEmpty()) {
            try {
                wait(); // Wait until an event is available
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted while waiting for an event");
                return null;
            }
        }
        return buffer.get(system).remove(0); // Remove and return the first event
    }
}