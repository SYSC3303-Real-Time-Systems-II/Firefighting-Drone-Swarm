public class DroneSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Coordinate current_coords;
    private EventBuffer eventBuffer;
    private DroneState droneState; // Takes care of the state of the drone


    private final double ACCELERATION_TIME = 0.051;
    private final double DECELERATION_TIME = 0.075;
    private final double TOP_SPEED = 20.8; // meters per second
    private final double DROP_WATER_TIME = 20.0;


    public DroneSubsystem(String name, EventBuffer eventBuffer) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.eventBuffer = eventBuffer;
        this.current_coords = new Coordinate(0, 0);
        this.droneState = DroneState.AVAILABLE; // The drone starts with an available state
    }

    // Returns the time taken to put out fire in minutes
    public double calculateTotalTravelTime(InputEvent event) {
        Coordinate fire_coords = event.getZone().getZoneCenter();
        double distance = Math.sqrt(Math.pow(fire_coords.getX() - current_coords.getX(), 2) + Math.pow(fire_coords.getY() - current_coords.getY(), 2));
        double travelTime = 2 * (distance / TOP_SPEED); // Both ways
        double totalSeconds = travelTime + ACCELERATION_TIME + DECELERATION_TIME + DROP_WATER_TIME;
        return totalSeconds / 60; // Convert to minutes
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
     * This method deals with the changing of the state for the drone. It handles when a drone goes from available to on route
     * to the fire zone, when it has arrived, when it's dropping the water and traveling back to the station.
     * @param event the event that is passed to the state machine.
     */
    public void handleDroneState(InputEvent event) {
        switch(droneState){
            case AVAILABLE: // When the drone is available
                System.out.println(name + ": Traveling to the to the fire zone."); // Prints out a message that the drone is on its way to the fire zone
                droneState = DroneState.ON_ROUTE; // The drone becomes on route to the fire zone
                break;
            case ON_ROUTE: // When the drone is on route
                System.out.println(name + ": Arrived at fire zone."); // Prints out a message that the drone has arrived at the fire zone
                droneState = DroneState.ARRIVED; // The drone has now arrived
                break;
            case ARRIVED: // When the drone has arrived
                System.out.println(name + ": Dropping water."); // Prints out a message that the drone is dropping water
                droneState = DroneState.DROPPING_WATER; // The drone is dropping water now
                break;
            case DROPPING_WATER: // When the drone is dropping water
                System.out.println(name + ": Water dropped, returning to base."); // Prints out a message saying that the watter was dropped and that it's returning to base
                droneState = DroneState.RETURNING_TO_BASE; // The drone is returning to base now
                break;
            case RETURNING_TO_BASE: // The last case where the drone is returning
                System.out.println(name + ": Arrived back at base and ready for next event."); // Prints out a message saying that the drone has arrived back and is now ready for the next event
                droneState = DroneState.AVAILABLE; // The drone becomes available again
                break;
        }
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 10) {
            // Step 1: Read from the Event Buffer
            InputEvent event = eventBuffer.getInputEvent(this.systemType);
            if (event != null && droneState == DroneState.AVAILABLE) { // The drone must be available

                // Step 1: Simulate handling the fire meaning that the drone will begin to handel the event
                System.out.println(this.name + ": HANDLING EVENT: " + event);
                handleDroneState(event); // Calls the state transition function with the event

                // Step 2: Comes back to the run function after it has transitioned through all state
                while(droneState != DroneState.AVAILABLE){
                    handleDroneState(event); // Calls the state transition function with the event
                }

                // Step 3: Update event status and time
                double travelTime = calculateTotalTravelTime(event);
                event.setStatus(Status.COMPLETE);
                event.setTime(event.getTime().plusMinutes((long) travelTime)); // Update time

                // Step 4: Send confirmation back to the Scheduler
                System.out.println(this.name + ": COMPLETED EVENT: " + event);
                System.out.println(this.name + ": SENDING --> " + event.toString() + " TO: " + Systems.Scheduler);
                eventBuffer.addInputEvent(event, Systems.Scheduler);

            } else {
                System.out.println("[" + systemType + " - " + name + "] No event to handle, retrying...");
                i--; // Retry the same iteration
            }
            i++;
        }
    }
}