import java.util.*;

public class DroneModel implements Runnable{
    private List<Drone> drones;
    private Map <String, Coordinate> coords = new HashMap<>();
    private Map <String, DroneStateMachine> states = new HashMap<>();

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
            }
                //for debugging -- will print out each drones curr location
//            for (Map.Entry<String, Coordinate> entry : hm.entrySet()) {
//                System.out.println("(********************"+entry.getKey() + " -> " + entry.getValue());
//            }
        }
    }

    public Map <String, Coordinate> getCoordinates() {
        return coords;
    }
    public Map <String, DroneStateMachine> getStates() {
        return states;
    }
}
