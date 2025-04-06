import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DroneModel implements Runnable{
    private List<Drone> drones;
    private Map <String, Coordinate> coords = new HashMap<>();
    private Map <String, DroneStateMachine> states = new HashMap<>();
    private final ConcurrentLinkedQueue<Drone> availableDrones = new ConcurrentLinkedQueue<>();


    public DroneModel (List<Drone> drones){
        this.drones = drones;
    }
    @Override
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

    public Map <String, Coordinate> getCoordinates() {
        return coords;
    }
    public Map <String, DroneStateMachine> getStates() {
        return states;
    }
    public ConcurrentLinkedQueue<Drone> getAvailableDrones() {
        return availableDrones;
    }
}
