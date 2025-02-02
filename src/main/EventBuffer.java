import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventBuffer {
    private final Map<Systems, ArrayList<InputEvent>> buffer = new HashMap<>();

    public EventBuffer() {
        for (Systems system : Systems.values()) {
            buffer.put(system, new ArrayList<>());
        }
    }

    public Map<Systems, ArrayList<InputEvent>> getBuffer() {
        return buffer;
    }

    // Add an InputEvent to the buffer
    public synchronized void addInputEvent(InputEvent inputEvent, Systems receiverSystem) {
        buffer.get(receiverSystem).add(inputEvent);
        notifyAll(); // Notify all waiting threads
    }

    // Get an InputEvent from the buffer for a specific system
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