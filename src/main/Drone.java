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
    public final double ACCELERATION_TIME = 0.051; // The acceleration time of the drone
    public final double DECELERATION_TIME = 0.075; // The deceleration time of the drone
    public final double TOP_SPEED = 20.8; // Top speed of the drone in meters per second
    public final double DROP_WATER_TIME = 20.0; // The time it takes for the drone to drop the water
    public final double MAX_WATER_CAPACITY = 30.0;
    public final double MAX_BATTERY_CAPACITY = 100.0;
    public static final double BATTERY_DRAIN_RATE = 0.1; // battery % drained per second
    private final String name; // This will be the name of teh drone based on its ID

    private static int nextID = 1; // Will be used to uniquely increment the ID
    private LocalTime localTime; // Will have the local time start of the event
    private double waterLevel = MAX_WATER_CAPACITY;
    private double batteryLevel = MAX_BATTERY_CAPACITY;


    private boolean changedEvent;
    private DroneStateMachine droneState; // This will be used for the drones state
    private Coordinate currentCoordinates;
    private InputEvent assignedEvent;
    private InputEvent currentEvent;
    private InputEvent handledEvent; // Completed or failed events

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
        this.handledEvent = null;
        this.changedEvent = false;
    }

    /**
     * Gets the current coordinates of the drone.
     * @return the current coordinates of the drone.
     */
    public Coordinate getCurrentCoordinates() {
        return currentCoordinates;
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
     * Gets the water capacity of the drone.
     * @return the water capacity of the drone.
     */
    public double getWaterLevel() {
        return waterLevel;
    }

    /**
     * Sets the water capacity of the drone.
     * @param waterLevel the new water level of the drone.
     */
    public void setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
    }

    /**
     * Refills the water in drone to the max capacity.
     */
    public void refillWater() {
        this.waterLevel = MAX_WATER_CAPACITY;  // Use the constant
    }

    /**
     * Gets the current battery level of the drone.
     * @return the battery level of the drone.
     */
    public double getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Charge the battery level of the drone to max capacity.
     */
    public void chargeBattery() {
        this.batteryLevel = MAX_BATTERY_CAPACITY;
    }

    /**
     * Gets the completed event of the drone.
     * @return the completed event of the drone.
     */
    public InputEvent getHandledEvent() {
        synchronized (this) {
            return handledEvent;
        }
    }

    /**
     * Sets the completed of the drone.
     * @param handledEvent the completed event of the drone.
     */
    public void setHandledEvent(InputEvent handledEvent){
        this.handledEvent = handledEvent;
    }

    /**
     * Sets the current event of the drone.
     * @param currentEvent the new current event of the drone.
     */
    public synchronized void setCurrentEvent(InputEvent currentEvent){
        this.currentEvent = currentEvent;
    }

    /**
     * Sets the drone state of the drone.
     * @param droneState the drone state.
     */
    public void setDroneState(DroneStateMachine droneState) {
        this.droneState = droneState;
    }

    /**
     * Gets the drone state of the drone.
     * @return the drone state.
     */
    public DroneStateMachine getDroneState() {
        return droneState;
    }


    /**
     * Gets the current assigned event of the drone.
     * @return the current assigned event of the drone.
     */
    public synchronized InputEvent getAssignedEvent() {
        return assignedEvent;
    }

    /**
     * Gets the local time of the drone.
     * @return the local time of the drone.
     */
    public LocalTime getLocalTime() {
        return localTime;
    }

    /**
     * Gets the current event of the drone.
     * @return the current event of the drone.
     */
    public synchronized InputEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * A method use to simulate battery drain in the span of seconds given for the drone.
     * @param seconds the time in seconds.
     */
    public void drainBattery(double seconds) {
        double drainAmount = seconds * BATTERY_DRAIN_RATE;
        batteryLevel = Math.max(0, batteryLevel - drainAmount);
        System.out.println(getName() + ": Remaining battery " + String.format("%.2f", batteryLevel) + "%");
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

    /**
     * Calculate the travel time from current position to the event given.
     * @param event the inputEvent.
     * @return time in seconds.
     */
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


    /**
     * Check if the task was switched and recalculates and moves to state.
     */
    public void checkIfTaskSwitch() {
        InputEvent assignedTask = getAssignedEvent();
        if (assignedTask != null) {
            InputEvent oldTask = getCurrentEvent();
            setAssignedEvent(null);
            setCurrentEvent(assignedTask);
            // System.out.println("[" + name + "] TASK SWITCHED FROM " + (oldTask != null ? oldTask.getZoneId() : "NONE") + " TO " + assignedTask.getZoneId());
            System.out.println("[" + name + "] TASK SWITCHED " +  oldTask.getEventID() + " --> " + assignedTask.getEventID());

            // Reset state to handle new task (e.g., recalculate path)
            if (droneState instanceof CruisingState) {
                setDroneState(new AscendingState()); // Restart ascent for new task
            }
        }
    }

    /**
     * Update location of the drone from the given seconds past.
     * @param seconds the time in seconds.
     */
    public void updateLocation(double seconds){
        Coordinate fireCoordinates = currentEvent.getZone().getZoneCenter();

        double distanceToTravel = Math.sqrt(Math.pow(fireCoordinates.getX() - currentCoordinates.getX(), 2) + Math.pow(fireCoordinates.getY() - currentCoordinates.getY(), 2));

        double directionX = ((fireCoordinates.getX() - currentCoordinates.getX()) / distanceToTravel);
        double directionY = ((fireCoordinates.getY() - currentCoordinates.getY()) / distanceToTravel);

        double updatedX = currentCoordinates.getX() + directionX * TOP_SPEED * seconds;
        double updatedY = currentCoordinates.getY() + directionY * TOP_SPEED * seconds;

        currentCoordinates = new Coordinate(updatedX, updatedY);
    }


    /**
     * Wait for task to be given to the drone.
     */
    public void waitForTask(){
        synchronized (this) {
            while (this.assignedEvent == null) {
                try {
                    if (this.getDroneState() instanceof StuckState || this.getDroneState() instanceof JammedState) { // If the drone was stuck or the nozzle is broken makes the drone unavailable
                        System.out.println("[" + this.name + "] NOW OFFLINE."); // Makes the drone offline
                    }
                    else {
                        System.out.println("[" + this.name + "] WAITING FOR EVENT."); // Else prints that the drone is waiting for an event
                    }
                    wait(); // Waits
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            setCurrentEvent(this.assignedEvent); // Move the assignedEvent to Current Event
            setAssignedEvent(null); // Makes the assigned event null now
        }
    }

    public double calculateReturnTravelTime() {
        // Calculate distance to base (0,0)
        double distance = Math.sqrt(Math.pow(currentCoordinates.getX(), 2) + Math.pow(currentCoordinates.getY(), 2));
        return distance / TOP_SPEED;
    }

    public void updateReturnLocation(double seconds) {
        Coordinate base = new Coordinate(0, 0);
        double distanceToBase = Math.sqrt(Math.pow(currentCoordinates.getX() - base.getX(), 2) + Math.pow(currentCoordinates.getY() - base.getY(), 2));

        if (distanceToBase == 0) return;

        double directionX = (base.getX() - currentCoordinates.getX()) / distanceToBase;
        double directionY = (base.getY() - currentCoordinates.getY()) / distanceToBase;

        double newX = currentCoordinates.getX() + directionX * TOP_SPEED * seconds;
        double newY = currentCoordinates.getY() + directionY * TOP_SPEED * seconds;

        // Snap to base if very close
        if (Math.abs(newX) < 0.1) newX = 0;
        if (Math.abs(newY) < 0.1) newY = 0;

        currentCoordinates = new Coordinate(newX, newY);
    }

    // Add this setter for coordinates
    public synchronized void setCurrentCoordinates(Coordinate coords) {
        this.currentCoordinates = coords;
    }


    @Override
    public void run() {
        while (true) {
            droneState.handle(this);
        }
    }
}





