public class DroneSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Coordinate current_coords;
    private EventBuffer eventBuffer;


    private final double ACCELERATION_TIME = 0.051;
    private final double DECELERATION_TIME = 0.075;
    private final double TOP_SPEED = 20.8; // meters per second
    private final double DROP_WATER_TIME = 20.0;


    public DroneSubsystem(String name, EventBuffer eventBuffer) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.eventBuffer = eventBuffer;
        this.current_coords = new Coordinate(0, 0);
    }

    // Returns the time taken to put out fire in minutes
    public double calculateTotalTravelTime(InputEvent event) {
        Coordinate fire_coords = event.getZone().getZoneCenter();
        double distance = Math.sqrt(Math.pow(fire_coords.getX() - current_coords.getX(), 2) + Math.pow(fire_coords.getY() - current_coords.getY(), 2));
        double travelTime = 2 * (distance / TOP_SPEED); // Both ways
        double totalSeconds = travelTime + ACCELERATION_TIME + DECELERATION_TIME + DROP_WATER_TIME;
        return totalSeconds / 60; // Convert to minutes
    }

    @Override
    public void run() {
        int i = 0;
        while (i < 10) {
            // Step 1: Read from the Event Buffer
            InputEvent event = eventBuffer.getInputEvent(this.systemType);
            if (event != null) {
                // Step 2: Calculate travel time
                double travelTime = calculateTotalTravelTime(event);

                // Step 3: Simulate handling the fire
                System.out.println(this.name + ": HANDLING EVENT: " + event);
                try {
                    Thread.sleep((int) (travelTime * 1000)); // Sleep for travel time

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Drone interrupted during travel");
                    return;
                }

                // Step 4: Update event status and time
                event.setStatus(Status.COMPLETE);
                event.setTime(event.getTime().plusMinutes((long) travelTime)); // Update time

                // Step 5: Send confirmation back to the Scheduler
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