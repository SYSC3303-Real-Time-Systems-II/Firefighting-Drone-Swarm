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
                Coordinate droneCoords = drone.getCurrentCoordinates();
                hm.put(drone.getName(), droneCoords);
            }

             //printing for debugging -- gives current location of the drones
//            for (Map.Entry<String, Coordinate> entry : hm.entrySet()) {
//                System.out.println("(********************"+entry.getKey() + " -> " + entry.getValue());
//            }
        }
    }
}
