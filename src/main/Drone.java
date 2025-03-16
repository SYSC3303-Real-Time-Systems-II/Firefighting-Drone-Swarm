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


    private boolean changedEvent;
    private DroneStateMachine droneState; // This will be used for the drones state
    private Coordinate currentCoordinates;
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
        this.currentCoordinates = new Coordinate(0,0);
        this.assignedEvent = null;
        this.completedEvent = null;
        this.changedEvent = false;

    }

    // Synchronized getter for changedEvent
    public synchronized boolean isChangedEvent() {
        return changedEvent;
    }

    public Coordinate getCurrentCoordinates() {
        return currentCoordinates;
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

    // Synchronized setter for changedEvent
    public synchronized void setChangedEvent(boolean changedEvent) {
        this.changedEvent = changedEvent;
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
        Coordinate fireCoordinates = event.getZone().getZoneCenter();
        return Math.sqrt(Math.pow(fireCoordinates.getX() - currentCoordinates.getX(), 2) + Math.pow(fireCoordinates.getY() - currentCoordinates.getY(), 2)) / TOP_SPEED;
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

    public synchronized void setCurrentEvent(InputEvent event){
        currentEvent = event;
    }

    public void setDroneState(DroneStateMachine droneState) {
        this.droneState = droneState;
    }

    public synchronized InputEvent getAssignedEvent() {
        return assignedEvent;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public synchronized InputEvent getCurrentEvent() {
        return currentEvent;
    }


    public void checkIfTaskSwitch() {
        InputEvent assignedTask = getAssignedEvent();
        if (assignedTask != null) {
            InputEvent oldTask = getCurrentEvent();
            setAssignedEvent(null);
            setCurrentEvent(assignedTask);
//            System.out.println("[" + name + "] TASK SWITCHED FROM " + (oldTask != null ? oldTask.getZoneId() : "NONE") + " TO " + assignedTask.getZoneId());
            System.out.println("[" + name + "] TASK SWITCHED " +  oldTask.getEventID() + " --> " + assignedTask.getEventID());

            // Reset state to handle new task (e.g., recalculate path)
            if (droneState instanceof CruisingState) {
                setDroneState(new AscendingState()); // Restart ascent for new task
            }
        }
    }


    public void updateLocation(double seconds){
        Coordinate fireCoordinates = currentEvent.getZone().getZoneCenter();

        double distanceToTravel = Math.sqrt(Math.pow(fireCoordinates.getX() - currentCoordinates.getX(), 2) + Math.pow(fireCoordinates.getY() - currentCoordinates.getY(), 2));

        double directionX = ((fireCoordinates.getX() - currentCoordinates.getX()) / distanceToTravel);
        double directionY = ((fireCoordinates.getY() - currentCoordinates.getY()) / distanceToTravel);

        double updatedX = currentCoordinates.getX() + directionX * TOP_SPEED * seconds;
        double updatedY = currentCoordinates.getY() + directionY * TOP_SPEED * seconds;

        currentCoordinates = new Coordinate(updatedX, updatedY);
    }

    @Override
    public void run() {
        while (true) {
            droneState.handle(this);
        }
    }
}





