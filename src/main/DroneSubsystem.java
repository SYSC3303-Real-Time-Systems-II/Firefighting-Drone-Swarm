import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

public class DroneSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private EventBuffer eventBuffer;
    private DroneSubsystemState droneSubsystemState; // Will have the state of the drone subsystem which is receiving and sending to the scheduler
    private Drone drone; // The drone that will be used to send out to the zones for fire
    private Map<Drone, Coordinate> droneCoordinateMap;


    public DroneSubsystem(String name, EventBuffer eventBuffer) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.eventBuffer = eventBuffer;
        this.droneSubsystemState = DroneSubsystemState.WAITING;
        this.drone = new Drone();
        this.droneCoordinateMap = new HashMap<>();
        this.droneCoordinateMap.put(drone,drone.getCurrent_coords());
    }


    public Drone chooseDroneAlgorithm(InputEvent event){
        Coordinate coords = event.getZone().getZoneCenter();
        Drone closestDrone = null;
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<Drone, Coordinate> entry : droneCoordinateMap.entrySet()){
            Coordinate entryDrone_coords = entry.getKey().getCurrent_coords();
            double distance = Math.sqrt(Math.pow(coords.getX() - entryDrone_coords.getX(), 2) + Math.pow(coords.getY() - entryDrone_coords.getY(), 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestDrone = entry.getKey();
            }
        }
        return closestDrone;
    }


    /**
     * Handles the state machine for the drone subsystem class which is dealing with the state of sending and receiving an input event
     * from the scheduler. The drone subsystem alerts the scheduler once a drone has arrived at a zone.
     * @param event the input event that was received or will be sent to scheduler.
     */
    public void handleDroneSubsystemState(InputEvent event) {
        switch (droneSubsystemState) {
            case WAITING:
                System.out.println(name + ": RECEIVED EVENT FROM SCHEDULER --> " + event.toString()); // Prints a message saying that the drone subsystem has received an event from the scheduler
                System.out.println(name + ": HANDLING EVENT: " + event); // Prints a message saying that the drone subsystem will handel the event
                droneSubsystemState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER; // Makes the state as received the event from the scheduler
                break;
            case RECEIVED_EVENT_FROM_SCHEDULER:
                Drone droneChosen = chooseDroneAlgorithm(event);

                System.out.println(droneChosen.getName() + ": AVAILABLE TO HANDLE --> : " + event); // Prints that the drone that was found available to handle the event
                droneChosen.setLocalTime(event.getTime()); // Sets the event as the local time for the drone
                // Calls the state transition function of the drone to be set as on route to the zone
                droneChosen.handleDroneState(event);
                droneSubsystemState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER; // Makes the state as sending the event to the scheduler
                break;

            case SENDING_EVENT_TO_SCHEDULER:
                event.setStatus(Status.COMPLETE); // Makes the status complete
                event.setTime(event.getTime().plusSeconds((long) drone.calculateArrivalZoneTime(event))); // Update time
                System.out.println(drone.getName() + ": COMPLETED EVENT (ARRIVED AT ZONE): " + event); // Prints out the time that the drone arrived at zone
                System.out.println(name + ": SENDING EVENT TO SCHEDULER --> " + event.toString()); // Sends the message back to the Scheduler

                // Calls the state transition function of the drone to be set as arrived
                drone.handleDroneState(event);

                eventBuffer.addInputEvent(event, Systems.Scheduler); // Puts it the shared buffer with the scheduler
                droneSubsystemState = DroneSubsystemState.WAITING; // Makes the state as waiting again for the next event
                break;
        }
    }

    /**
     * Gets the state of the drone subsystem.
     * @return the state of drone subsystem.
     */
    public DroneSubsystemState getDroneSubsystemState() {
        return droneSubsystemState;
    }


    @Override
    public void run() {
        int i = 0;
        while (i < 10) {
            // Step 1: Read from the Event Buffer sent from scheduler
            InputEvent event = eventBuffer.getInputEvent(this.systemType);
            if (event != null && drone.getDroneState() == DroneState.AVAILABLE) { // Checks if the event is not null and that the drone is available

                // Step 2: There was an event now the drone subsystem switches state
                handleDroneSubsystemState(event); // Calls the function to handle the state change.

                // Step 3: Simulate handling the fire meaning that the drone will begin to handle the events, checks for its state first
                if(drone.getDroneState() == DroneState.AVAILABLE) { // If the drone is available
                    handleDroneSubsystemState(event); // Calls the function to handle the state change.
                    drone.handleDroneState(event);
                }

                // Step 4: Check if the drone has arrived at the zone to message back to the scheduler
                if(drone.getDroneState() == DroneState.ARRIVED) {
                    handleDroneSubsystemState(event); // Calls the function to handle the state change.
                }

                // Step 5: Heads back to home base
                drone.handleDroneState(event);

                // Step 6: Arrives back at the home base and now ready to be sent to the next zone
                drone.handleDroneState(event);

            } else {
                System.out.println("[" + systemType + " - " + name + "] No event to handle, retrying...");
                i--; // Retry the same iteration
            }
            i++;
        }
    }
}