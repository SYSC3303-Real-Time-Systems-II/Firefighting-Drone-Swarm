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

    private static int nextID = 1; // Will be used to uniquely increment the ID
    private LocalTime localTime; // Will have the local time start of the event

    private DroneStateMachine droneState; // This will be used for the drones state
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
        this.localTime = null;
        this.droneState = new AvailableState();
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
    public DroneStateMachine getDroneState() {
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
    public void sleepFor(double seconds) {
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

    public void setCompletedEvent(InputEvent event){
        completedEvent = event;
    }

    public void setCurrentEvent(InputEvent event){
        currentEvent = event;
    }

    public void setDroneState(DroneStateMachine droneState) {
        this.droneState = droneState;
    }

    public InputEvent getAssignedEvent() {
        return assignedEvent;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public InputEvent getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void run() {
        while (true) {
            droneState.handle(this);
        }
    }
}





