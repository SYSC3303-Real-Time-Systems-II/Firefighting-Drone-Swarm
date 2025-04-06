import java.io.*;
import java.net.DatagramSocket;
import java.time.*;

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
    public final double MAX_WATER_CAPACITY = 30.0;
    public final double MAX_BATTERY_CAPACITY = 100.0;
    public static final double BATTERY_DRAIN_RATE = 0.1; // battery % drained per second
    private String name; // This will be the name of teh drone based on its ID
    private int portID = 8000;

    private static int nextID = 1; // Will be used to uniquely increment the ID
    private LocalTime localTime; // Will have the local time start of the event
    private double waterLevel = MAX_WATER_CAPACITY;
    private double batteryLevel = MAX_BATTERY_CAPACITY;

    private DroneStateMachine droneState; // This will be used for the drones state
    private Coordinate currentCoordinates;
    private InputEvent assignedEvent;
    private DatagramSocket sendReceiveSocket; // A socket for the drone to send to receive

    /**
     * The constructor of the done system assigns a new ID and the state as available to start.
     */
    public Drone() {
        try {
            this.ID = nextID;
            this.portID += nextID;
            sendReceiveSocket = new DatagramSocket(portID); // Creates a new socket
            this.name = "Drone" + ID;
            this.localTime = null;
            this.droneState = new AvailableState();
            this.currentCoordinates = new Coordinate(0,0);
            this.assignedEvent = null;
            nextID++;
        } catch (IOException e){
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
     * A method use to simulate battery drain in the span of seconds given for the drone.
     * @param seconds the time in seconds.
     */
    public void drainBattery(double seconds) {
        double drainAmount = seconds * BATTERY_DRAIN_RATE;
        batteryLevel = Math.max(0, batteryLevel - drainAmount);
        System.out.println(getName() + ": Remaining battery " + String.format("%.2f", batteryLevel)+"%") ;
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

    /**
     * Sets the assigned event.
     * @param event
     */
    public void setAssignedEvent(InputEvent event) {
        this.assignedEvent = event;
    }


    /**
     * Update location of the drone from the given seconds past.
     * @param seconds the time in seconds.
     */
    public void updateLocation(double seconds){
        Coordinate fireCoordinates = assignedEvent.getZone().getZoneCenter();

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





