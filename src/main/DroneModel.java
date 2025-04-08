import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The DroneModel class maintains a list of drones and tracks their current coordinates,
 * states, and available drones. It continuously updates this information when run in a separate thread.
 */
public class DroneModel implements Runnable{
    private List<Drone> drones;
    private Map <String, Coordinate> coords = new HashMap<>();
    private Map <String, DroneStateMachine> states = new HashMap<>();
    private final ConcurrentLinkedQueue<Drone> availableDrones = new ConcurrentLinkedQueue<>();

    /**
     * Constructs a DroneModel with the given list of drones.
     *
     * @param drones A list of {@link Drone} objects to be managed by this model.
     */
    public DroneModel (List<Drone> drones){
        this.drones = drones;
    }
    @Override

    /**
     * Continuously updates the coordinates and states of each drone.
     * Also maintains a queue of drones that are currently available (i.e., in AvailableState).
     * <p>
     * This method is intended to be executed in its own thread.
     * </p>
     */
    public void run() {
        while (true) {
            for (Drone drone : drones) {
                // Use synchronized access to coordinates
                synchronized (drone) {
                    coords.put(drone.getName(), drone.getCurrentCoordinates());
                    states.put(drone.getName(), drone.getDroneState());
                }

                // Update available drones based on current state
                if (drone.getDroneState() instanceof AvailableState) {
                    if (!availableDrones.contains(drone)) {
                        availableDrones.add(drone);
                    }
                } else {
                    availableDrones.remove(drone);
                }

            }
        }
    }

    /**
     * Returns a map of drone names to their current coordinates.
     *
     * @return A {@link Map} where the key is a drone name and the value is its {@link Coordinate}.
     */
    public Map <String, Coordinate> getCoordinates() {
        return coords;
    }

    /**
     * Returns a map of drone names to their current state machines.
     *
     * @return A {@link Map} where the key is a drone name and the value is its {@link DroneStateMachine}.
     */
    public Map <String, DroneStateMachine> getStates() {
        return states;
    }

    /**
     * Returns the queue of drones that are currently available.
     *
     * @return A {@link ConcurrentLinkedQueue} containing the available {@link Drone} objects.
     */
    public ConcurrentLinkedQueue<Drone> getAvailableDrones() {
        return availableDrones;
    }
}
