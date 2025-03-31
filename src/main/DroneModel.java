import java.util.*;
public class DroneModel implements Runnable{
    private List<Drone> drones;
    private Map <String, Coordinate> hm = new HashMap<>();


    public DroneModel (List<Drone> drones){
        this.drones = drones;
    }

    @Override
    public void run() {
        while (true) {
            for (Drone drone : drones) {
                // Use synchronized access to coordinates
                synchronized (drone) {
                    hm.put(drone.getName(), drone.getCurrentCoordinates());
                }
            }
                //for debugging -- will print out each drones curr location
//            for (Map.Entry<String, Coordinate> entry : hm.entrySet()) {
//                System.out.println("(********************"+entry.getKey() + " -> " + entry.getValue());
//            }
        }
    }
}
