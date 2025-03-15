import java.time.*;

/**
 * This class is the drone class which has a unique identifier, a name, and a state. The drone is coordinated by the drone subsystem
 * which when it receives an event from the drone subsystem it releases a drone to that specified zone.
 *
 * @author Rami Ayoub
 * @version 2.0
 */
public class Drone implements Runnable{
    private final int ID; // This will be the ID of the drone
    private final double ACCELERATION_TIME = 0.051; // The acceleration time of the drone
    private final double DECELERATION_TIME = 0.075; // The deceleration time of the drone
    private final double TOP_SPEED = 20.8; // Top speed of the drone in meters per second
    private final double DROP_WATER_TIME = 20.0; // The time it takes for the drone to drop the water
    private final String name; // This will be the name of teh drone based on its ID
    private DroneState droneState; // This will be used for the drones state
    private static int nextID = 1; // Will be used to uniquely increment the ID
    private static LocalTime localTime; // Will have the local time start of the event

    private Coordinate currentCoords;
    private InputEvent assignedEvent;
    private InputEvent currentEvent;
    private InputEvent completedEvent;

    /**
     * The constructor of the done system assigns a new ID and the state as available to start.
     */
    public Drone() {
        this.ID = nextID++;
        this.name = "Drone" + ID;
        this.droneState = DroneState.AVAILABLE;
        this.localTime = null;
        this.currentCoords = new Coordinate(0,0);
        this.assignedEvent = null;
        this.completedEvent = null;

    }

    public Coordinate getCurrent_coords() {
        return currentCoords;
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

    public double calculateZoneTravelTime(InputEvent event){
        Coordinate fire_coords = event.getZone().getZoneCenter();
        return Math.sqrt(Math.pow(fire_coords.getX() - currentCoords.getX(), 2) + Math.pow(fire_coords.getY() - currentCoords.getY(), 2)) / TOP_SPEED;
    }

    /**
     * Gets the ID of the drone.
     * @return drone ID.
     */
    public int getID() {
        return ID;
    }

    public void setAssignedEvent(InputEvent event) {
        synchronized (this) {
            this.assignedEvent = event;
            notify();  // Notify the waiting thread that an event is available
        }
    }

    public InputEvent getCompletedEvent() {
        synchronized (this) {
            return completedEvent;
        }
    }

    /**
     * This method deals with the changing of the state for the drone. It handles when a drone goes from available to on route
     * to the fire zone, when it has arrived, when it's dropping the water and traveling back to the station.
     */
    public void handleDroneState() {
        switch(droneState) {
            case AVAILABLE:
                //lock and wait until a task is assigned
                synchronized (this) {
                    while (assignedEvent == null) {
                        try {
                            System.out.println("["+this.name + "] WAITING FOR EVENT");
                            wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    currentEvent = assignedEvent;
                }
                System.out.println("["+this.name + "] GOT EVENT" + currentEvent);
                setLocalTime(currentEvent.getTime());
                System.out.println("["+this.name + "] TRAVELING TO ZONE: " + currentEvent.getZoneId() + " : AT TIME: " + localTime); // Prints out a message that the drone is on its way to the zone and the time it traveled
                localTime = localTime.plusSeconds((long) ACCELERATION_TIME); // Adds the local time
                sleepFor(ACCELERATION_TIME); // Simulates the acceleration time
                droneState = DroneState.ASCENDING; // The drone becomes on route to the fire zone
                break;

            case ASCENDING:
                double travelZoneTime = calculateZoneTravelTime(currentEvent);
                localTime = localTime.plusSeconds((long) travelZoneTime); // Adds the local time
                sleepFor(ACCELERATION_TIME);
                System.out.println("["+this.name + "] ASCENDING AT TIME: " + localTime);
                droneState = DroneState.CRUISING;
                break;

            case CRUISING:
                double travelZoneTime3 = calculateZoneTravelTime(currentEvent);
                sleepFor(travelZoneTime3);
                System.out.println("["+this.name + "] CRUISING TO ZONE: " + currentEvent.getZoneId() +  " : AT TIME: " + localTime);
                droneState = DroneState.ARRIVED;

            case ARRIVED: // When the drone has arrived
                System.out.println("["+this.name + "] ARRIVED AT ZONE: " + currentEvent.getZoneId() +  " : AT TIME: " + localTime); // Prints out a message that the drone has arrived at the fire zone
                System.out.println("["+this.name + "] DROPPING WATER: AT TIME: " + localTime); // Prints out a message that the drone is dropping water
                localTime = localTime.plusSeconds((long) DROP_WATER_TIME); // Adds the local time
                sleepFor(DROP_WATER_TIME); // Simulates the water drop time
                droneState = DroneState.DROPPING_WATER; // The drone is dropping water now
                break;

            case DROPPING_WATER:// When the drone is dropping water
                System.out.println("["+this.name + "] WATER DROPPED, RETURNING TO BASE: AT TIME: " + localTime); // Prints out a message saying that the watter was dropped and that it's returning to base
                localTime = localTime.plusSeconds((long) DECELERATION_TIME); // Adds the local time
                sleepFor(DECELERATION_TIME); // Simulates the deceleration time
                droneState = DroneState.RETURNING_TO_BASE; // The drone is returning to base now
                break;

            case RETURNING_TO_BASE: // The last case where the drone is returning
                double travelZoneTime2 = calculateZoneTravelTime(currentEvent);
                localTime = localTime.plusSeconds((long) travelZoneTime2 - 4); // Adds the local time
                sleepFor(travelZoneTime2 - 4); // Simulates the travel zone time
                System.out.println("["+this.name + "] ARRIVED BACK AT BASE AND READY FOR NEXT EVENT: AT TIME: " + localTime); // Prints out a message saying that the drone has arrived back and is now ready for the next event
                completedEvent = currentEvent;
                currentEvent = null;
                assignedEvent = null;
                droneState = DroneState.AVAILABLE; // The drone becomes available again
                break;
        }
    }

    @Override
    public void run() {
        while (true) {
            handleDroneState();
        }
    }
}
