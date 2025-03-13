import java.time.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class is the drone class which has a unique identifier, a name, and a state. The drone is coordinated by the drone subsystem
 * which when it receives an event from the drone subsystem it releases a drone to that specified zone.
 *
 * @author Rami Ayoub
 * @version 2.0
 */


public class Drone {
    private final int ID; // This will be the ID of the drone
    private final double ACCELERATION_TIME = 0.051; // The acceleration time of the drone
    private final double DECELERATION_TIME = 0.075; // The deceleration time of the drone
    private final double TOP_SPEED = 20.8; // Top speed of the drone in meters per second
    private final double DROP_WATER_TIME = 20.0; // The time it takes for the drone to drop the water
    private final String name; // This will be the name of teh drone based on its ID
    private DroneState droneState; // This will be used for the drones state
    private static int nextID = 1; // Will be used to uniquely increment the ID
    private static LocalTime localTime; // Will have the local time start of the event
    private Coordinate current_coords;


    /**
     * The constructor of the done system assigns a new ID and the state as available to start.
     */
    public Drone() {
        this.ID = nextID++;
        this.name = "Drone" + ID;
        this.droneState = DroneState.AVAILABLE;
        this.localTime = null;
        this.current_coords = new Coordinate(0, 0);
    }

    /**
     * Gets the current_coords of the drone.
     * @return the current_coords of drone.
     */
    public Coordinate getCurrent_coords() {
        return current_coords;
    }

    /**
     * Gets the state of the drone.
     * @return the state of drone.
     */
    public DroneState getDroneState() {
        return droneState;
    }

    /**
     * Gets the name of the drone.
     * @return the name of the drone.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the local time.
     * @param localTime the local time.
     */
    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    /**
     * Sets the setCurrent_coords.
     * @param new_coords new coords.
     */
    public void setCurrent_coords(Coordinate new_coords) {
        this.current_coords = new_coords;
    }

    /**
     * Gets the acceleration time of the drone
     * @return drones acceleration time.
     */
    public double getACCELERATION_TIME() {
        return ACCELERATION_TIME;
    }

    /**
     * Gets the deceleration time of the drone
     * @return drones deceleration time.
     */
    public double getDECELERATION_TIME() {
        return DECELERATION_TIME;
    }

    /**
     * Gets the top speed of the drone in meters per second
     * @return top speed
     */
    public double getTOP_SPEED() {
        return TOP_SPEED;
    }

    /**
     * Gets the time it takes the drone to drop water
     * @return drones water drop time.
     */
    public double getDROP_WATER_TIME() {
        return DROP_WATER_TIME;
    }

    /**
     * A method used to simulate a delay of time for when the drone has travelled to the zone and back.
     * @param seconds the time in seconds.
     */
    private void sleepFor(double seconds) {
        try {
            Thread.sleep((int) (seconds * 1000)); // Convert to milliseconds
        } catch (InterruptedException e) { // If something when wrong
            Thread.currentThread().interrupt(); // Gets the interrupt
            System.err.println("Drone interrupted during sleep."); // Prints the error
        }
    }

    /**
     * A method used to calculate the travel time to a zone and can also be used to calculate the travel time back from the zone.
     * @param event The event that is sent to the zone.
     * @return the travel time of a zone.
     */
    public double calculateZoneTravelTime(InputEvent event){
        Coordinate fire_coords = event.getZone().getZoneCenter();
        return Math.sqrt(Math.pow(fire_coords.getX() - current_coords.getX(), 2) + Math.pow(fire_coords.getY() - current_coords.getY(), 2)) / getTOP_SPEED();
    }

    /**
     * Returns the arrival time of drone to arrive at a zone,
     * @param event The event sent to the drone subsystem.
     * @return the arrival time.
     */
    public double calculateArrivalZoneTime(InputEvent event) {
        return calculateZoneTravelTime(event) + getACCELERATION_TIME(); // Convert to minutes
    }

    /**
     * This method deals with the changing of the state for the drone. It handles when a drone goes from available to on route
     * to the fire zone, when it has arrived, when it's dropping the water and traveling back to the station.
     */
    public void handleDroneState(InputEvent event) {

        double travelZoneTime = calculateZoneTravelTime(event);
        int zoneID = event.getZoneId();

        switch(droneState){
            case AVAILABLE: // When the drone is available
                System.out.println(name + ": TRAVELING TO ZONE: " + zoneID +  " : AT TIME: " + localTime); // Prints out a message that the drone is on its way to the zone and the time it traveled
                System.out.println(name + "AVAILABLE ***************************** CURRENT LOCATION: " + getCurrent_coords());
                localTime = localTime.plusSeconds((long) ACCELERATION_TIME); // Adds the local time
                //sleepFor(ACCELERATION_TIME); // Simulates the acceleration time
                droneState = DroneState.ASCENDING; // The drone becomes on route to the fire zone
                break;
//            case ON_ROUTE: // When the drone is on route
//                localTime = localTime.plusSeconds((long) travelZoneTime); // Adds the local time
//                //sleepFor(travelZoneTime); // Simulates the travel zone time
//                System.out.println(name + ": ARRIVED AT ZONE: " + zoneID +  " : AT TIME: " + localTime); // Prints out a message that the drone has arrived at the fire zone
//                droneState = DroneState.ARRIVED; // The drone has now arrived
//                break;
            case ASCENDING:
                sleepFor(ACCELERATION_TIME);
                System.out.println(name + ": ASCENDING AT TIME: " + localTime);
                droneState = DroneState.CRUISING;
                break;
            case CRUISING:
                sleepFor(travelZoneTime);
                System.out.println(name + ": CRUISING TO ZONE: " + zoneID +  " : AT TIME: " + localTime);
                droneState = DroneState.ARRIVED;
            case ARRIVED: // When the drone has arrived
                setCurrent_coords(event.getZone().getZoneCenter());
                System.out.println(name + "ARRIVED ***************************** CURRENT LOCATION: " + getCurrent_coords());
                System.out.println(name + ": DROPPING WATER: AT TIME: " + localTime); // Prints out a message that the drone is dropping water
                localTime = localTime.plusSeconds((long) DROP_WATER_TIME); // Adds the local time
                //sleepFor(DROP_WATER_TIME); // Simulates the water drop time
                droneState = DroneState.DROPPING_WATER; // The drone is dropping water now
                break;
            case DROPPING_WATER: // When the drone is dropping water
                System.out.println(name + ": WATER DROPPED, RETURNING TO BASE: AT TIME: " + localTime); // Prints out a message saying that the watter was dropped and that it's returning to base
                localTime = localTime.plusSeconds((long) DECELERATION_TIME); // Adds the local time
                //sleepFor(DECELERATION_TIME); // Simulates the deceleration time
                droneState = DroneState.RETURNING_TO_BASE; // The drone is returning to base now
                break;
            case RETURNING_TO_BASE: // The last case where the drone is returning
                localTime = localTime.plusSeconds((long) travelZoneTime - 4); // Adds the local time
                //sleepFor(travelZoneTime - 4); // Simulates the travel zone time
                System.out.println(name + ": ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: " + localTime); // Prints out a message saying that the drone has arrived back and is now ready for the next event
                setCurrent_coords(new Coordinate(0,0));
                System.out.println(name + "HOME ***************************** CURRENT LOCATION: " + getCurrent_coords());

                droneState = DroneState.AVAILABLE; // The drone becomes available again
                break;
        }
    }

    /**
     * Gets the ID of the drone.
     * @return drone ID.
     */
    public int getID() {
        return ID;
    }

}
