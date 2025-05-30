import java.io.*;
import java.net.DatagramSocket;
import java.time.*;
import java.util.Map;

/**
 * This class is the drone class which has a unique identifier, a name, and a state. The drone is coordinated by the drone subsystem
 * which when it receives an event from the drone subsystem it releases a drone to that specified zone.
 *
 * @author Rami Ayoub
 * @version 2.0
 */
public class Drone implements Runnable{
    private int ID; // This will be the ID of the drone
    public final double ACCELERATION_TIME = 0.051; // The acceleration time of the drone
    public final double DECELERATION_TIME = 0.075; // The deceleration time of the drone
    public final double TOP_SPEED = 20.8; // Top speed of the drone in meters per second
    public final double DROP_WATER_TIME = 20.0; // The time it takes for the drone to drop the water
    public static final double MAX_WATER_CAPACITY = 15.0;
    public final double MAX_BATTERY_CAPACITY = 100.0;
    public static final double BATTERY_DRAIN_RATE = 0.1; // battery % drained per second

    private String name; // This will be the name of teh drone based on its ID
    private int portID = 8000;

    private static int nextID = 1; // Will be used to uniquely increment the ID
    private LocalTime localTime; // Will have the local time start of the event
    private double waterLevel = MAX_WATER_CAPACITY;
    private double batteryLevel = MAX_BATTERY_CAPACITY;
    private boolean dropCompleted = false;

    private DroneStateMachine droneState; // This will be used for the drones state
    private Coordinate currentCoordinates;
    private InputEvent assignedEvent;
    //private InputEvent currentEvent;
    private DatagramSocket sendReceiveSocket; // A socket for the drone to send to receive

    /**
     * The constructor of the done system assigns a new ID and the state as available to start.
     */
    public Drone() {

        try {
            this.ID = nextID++;
            this.portID += nextID;
            sendReceiveSocket = new DatagramSocket(portID); // Creates a new socket
            this.name = "Drone" + ID;
            this.localTime = null;
            this.droneState = new AvailableState();
            this.currentCoordinates = new Coordinate(0, 0);
            this.assignedEvent = null;
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the current coordinates of the drone.
     * @return the current coordinates of the drone.
     */
    public Coordinate getCurrentCoordinates() {
        return currentCoordinates;
    }

    /**
     * Gets the portID number for the drone.
     * @return the portID of the drone.
     */
    public int getPortID(){
        return portID;
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
     * Gets the socket for the drone.
     * @return the socket for the drone.
     */
    public DatagramSocket getSendReceiveSocket() {
        return sendReceiveSocket;
    }

    /**
     * sets the barrerylevel of drone
     * @param batteryLevel sets the barrerylevel of drone
     */
    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    /**
     * serializes the event.
     * @param event to be serialized.
     * @return the serialized events.
     */
    public byte[] serializeEvent(InputEvent event) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(event);
        return bos.toByteArray();
    }

    /**
     * Deserialize the event that was received from the event subsystem.
     * @param data the event that was received
     * @return deserialized event.
     */
    public InputEvent deserializeEvent(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (InputEvent) ois.readObject();
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




//    /**
//     * Sets the current event of the drone.
//     * @param currentEvent the new current event of the drone.
//     */
//    public synchronized void setCurrentEvent(InputEvent currentEvent){
//        this.currentEvent = currentEvent;
//    }

    /**
     * Sets the drone state of the drone.
     * @param droneState the drone state.
     */
    public void setDroneState(DroneStateMachine droneState) {
        this.droneState = droneState;
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

//    /**
//     * Gets the current event of the drone.
//     * @return the current event of the drone.
//     */
//    public synchronized InputEvent getCurrentEvent() {
//        return currentEvent;
//    }

    public void setCurrentCoordinates(Coordinate coord) {
        this.currentCoordinates = coord;
    }
    /**
     * A method use to simulate battery drain in the span of seconds given for the drone.
     * @param seconds the time in seconds.
     */
    public void drainBattery(double seconds) {
        double drainAmount = seconds * BATTERY_DRAIN_RATE;
        batteryLevel = Math.max(0, batteryLevel - drainAmount);
        //System.out.println(getName() + ": Remaining battery " + String.format("%.2f", batteryLevel)+"%") ;
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

    public double calculateHomeZoneTime(Coordinate coordinate){
        return (Math.sqrt(Math.pow(currentCoordinates.getX() - coordinate.getX(), 2) + Math.pow(currentCoordinates.getY() - coordinate.getY(), 2)) / TOP_SPEED) * 2;
    }

    /**
     * Gets the ID of the drone.
     * @return drone ID.
     */
    public int getID() {
        return ID;
    }

    /**
     * Sets the assigned event.
     * @param event
     */
    public void setAssignedEvent(InputEvent event) {
        this.assignedEvent = event;
    }


//    /**
//     * Check if the task was switched and recalculates and moves to state.
//     */
//    public boolean checkIfTaskSwitch() {
//        InputEvent assignedTask = getAssignedEvent();
//        if (assignedTask != null) {
//            InputEvent oldTask = getCurrentEvent();
//            setAssignedEvent(null);
//            setCurrentEvent(assignedTask);
//            System.out.println("[" + name + "] TASK SWITCHED " +  oldTask.getEventID() + " --> " + assignedTask.getEventID());
//
//            // Reset state to handle new task (e.g., recalculate path)
//            if (droneState instanceof CruisingState) {
//                setDroneState(new AscendingState()); // Restart ascent for new task
//            }
//            return true;
//        }
//        return false;
//    }

    /**
     * Update location of the drone from the given seconds past.
     * @param seconds the time in seconds.
     */
    public void updateLocation(double seconds){
        Coordinate fireCoordinates = assignedEvent.getZone().getZoneCenter();
//        Coordinate fireCoordinates = currentEvent.getZone().getZoneCenter();

        double distanceToTravel = Math.sqrt(Math.pow(fireCoordinates.getX() - currentCoordinates.getX(), 2) + Math.pow(fireCoordinates.getY() - currentCoordinates.getY(), 2));

        double directionX = ((fireCoordinates.getX() - currentCoordinates.getX()) / distanceToTravel);
        double directionY = ((fireCoordinates.getY() - currentCoordinates.getY()) / distanceToTravel);

        double updatedX = currentCoordinates.getX() + directionX * TOP_SPEED * seconds;
        double updatedY = currentCoordinates.getY() + directionY * TOP_SPEED * seconds;

        currentCoordinates = new Coordinate(updatedX, updatedY);
    }


//    /**
//     * Wait for task to be given to the drone.
//     */
//    public void waitForTask(){
//        synchronized (this) {
//            while (this.assignedEvent == null) {
//                try {
//                    if (this.getDroneState() instanceof StuckState || this.getDroneState() instanceof JammedState) { // If the drone was stuck or the nozzle is broken makes the drone unavailable
//                        System.out.println("[" + this.name + "] NOW OFFLINE."); // Makes the drone offline
//                        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.OFFLINE, this.currentEvent, this.name);
//                    }
//                    else {
//                        System.out.println("[" + this.name + "] WAITING FOR EVENT."); // Else prints that the drone is waiting for an event
//                        MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.WAITING_FOR_TASK, this.currentEvent, this.name);
//                    }
//                    wait(); // Waits
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            setCurrentEvent(this.assignedEvent); // Move the assignedEvent to Current Event
//            setAssignedEvent(null); // Makes the assigned event null now
//            MetricAnalysisLogger.logEvent(MetricAnalysisLogger.EventStatus.ASSIGNED_EVENT, this.currentEvent, this.name);
//        }
//    }

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

    public boolean isDropCompleted() {
        return dropCompleted;
    }

    public void setDropCompleted(boolean dropCompleted) {
        this.dropCompleted = dropCompleted;
    }


    @Override
    public void run() {
        while (true) {
            droneState.handle(this);
        }
    }
}





