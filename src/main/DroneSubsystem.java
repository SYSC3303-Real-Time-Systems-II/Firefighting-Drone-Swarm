import java.util.*;

import static java.lang.Thread.sleep;

public class DroneSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private Coordinate current_coords;
    private EventBuffer eventBuffer;
    private DroneSubsystemState droneSubsystemState; // Will have the state of the drone subsystem which is receiving and sending to the scheduler


    private Queue<Drone> workingDrones;
    private Queue<Drone> droneQueue;

    private Map<InputEvent, Drone> completeEvents;
//    private Map<Drone, InputEvent> droneInputEventMap;

    public DroneSubsystem(String name, EventBuffer eventBuffer) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.eventBuffer = eventBuffer;
        this.current_coords = new Coordinate(0, 0);
        this.droneSubsystemState = DroneSubsystemState.WAITING;

        this.droneQueue = new LinkedList<>();
        this.workingDrones = new LinkedList<>();
        // Initialize multiple drones and start their threads
        for (int i = 0; i < 1; i++) {
            Drone drone = new Drone();
            droneQueue.add(drone);
            //droneCoordinateMap.put(drone, drone.getCurrent_coords());
            new Thread(drone).start();
        }

    }



    /**
     * Handles the state machine for the drone subsystem class which is dealing with the state of sending and receiving an input event
     * from the scheduler. The drone subsystem alerts the scheduler once a drone has arrived at a zone.
     * @param event the input event that was received or will be sent to scheduler.
     */
//    public void handleDroneSubsystemState(InputEvent event) {
//        switch (droneSubsystemState) {
//            case WAITING:
//                System.out.println(name + ": RECEIVED EVENT FROM SCHEDULER --> " + event.toString()); // Prints a message saying that the drone subsystem has received an event from the scheduler
//                System.out.println(name + ": HANDLING EVENT: " + event); // Prints a message saying that the drone subsystem will handel the event
//                droneSubsystemState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER; // Makes the state as received the event from the scheduler
//                break;
//            case RECEIVED_EVENT_FROM_SCHEDULER:
//                System.out.println(drone1.getName() + ": AVAILABLE TO HANDLE --> : " + event); // Prints that the drone that was found available to handle the event
//                drone1.setLocalTime(event.getTime()); // Sets the event as the local time for the drone
//                drone1.handleDroneState(calculateZoneTravelTime(event), event.getZoneId()); // Calls the state transition function of the drone to be set as on route to the zone
//                droneSubsystemState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER; // Makes the state as sending the event to the scheduler
//                break;
//            case SENDING_EVENT_TO_SCHEDULER:
//                event.setStatus(Status.COMPLETE); // Makes the status complete
//                event.setTime(event.getTime().plusSeconds((long) calculateArrivalZoneTime(event))); // Update time
//                System.out.println(drone1.getName() + ": COMPLETED EVENT (ARRIVED AT ZONE): " + event); // Prints out the time that the drone arrived at zone
//                System.out.println(name + ": SENDING EVENT TO SCHEDULER --> " + event.toString()); // Sends the message back to the Scheduler
//                drone1.handleDroneState(calculateZoneTravelTime(event), event.getZoneId()); // Calls the state transition function of the drone to be set as arrived
//                eventBuffer.addInputEvent(event, Systems.Scheduler); // Puts it the shared buffer with the scheduler
//                droneSubsystemState = DroneSubsystemState.WAITING; // Makes the state as waiting again for the next event
//                break;
//        }
//    }

//    private Drone chooseDroneAlgorithm(InputEvent event) {
//        Coordinate coords = event.getZone().getZoneCenter();
//        Drone closestDrone = null;
//        double minDistance = Double.MAX_VALUE;
//
//        for (Drone drone : drones) {
//            if (drone.getDroneState() == DroneState.AVAILABLE) {
//                Coordinate droneCoords = drone.getCurrent_coords();
//                double distance = Math.sqrt(Math.pow(coords.getX() - droneCoords.getX(), 2) + Math.pow(coords.getY() - droneCoords.getY(), 2));
//                if (distance < minDistance) {
//                    minDistance = distance;
//                    closestDrone = drone;
//                }
//            }
//        }
//        return closestDrone;
//    }

    /**
//     * Gets the state of the drone subsystem.
//     * @return the state of drone subsystem.
//     */
//    public DroneSubsystemState getDroneSubsystemState() {
//        return droneSubsystemState;
//    }

//    /**
//     * A method used to calculate the travel time to a zone and can also be used to calculate the travel time back from the zone.
//     * @param event The event that is sent to the zone.
//     * @return the travel time of a zone.
//     */
//    public double calculateZoneTravelTime(InputEvent event){
//        Coordinate fire_coords = event.getZone().getZoneCenter();
//        return Math.sqrt(Math.pow(fire_coords.getX() - current_coords.getX(), 2) + Math.pow(fire_coords.getY() - current_coords.getY(), 2)) / drone1.getTOP_SPEED();
//    }
//
//    /**
//     * Returns the arrival time of drone to arrive at a zone,
//     * @param event The event sent to the drone subsystem.
//     * @return the arrival time.
//     */
//    public double calculateArrivalZoneTime(InputEvent event) {
//        return calculateZoneTravelTime(event) + drone1.getACCELERATION_TIME(); // Convert to minutes
//    }


    /**
     * Handles the state machine for the drone subsystem class which is dealing with the state of sending and receiving an input event
     * from the scheduler. The drone subsystem alerts the scheduler once a drone has arrived at a zone.
     * @param event the input event that was received or will be sent to scheduler.
     */
    public void handleDroneSubsystemState(InputEvent event) throws InterruptedException {
        switch (droneSubsystemState){
            case WAITING:
                System.out.println(name + ": RECEIVED EVENT FROM SCHEDULER --> " + event.toString()); // Prints a message saying that the drone subsystem has received an event from the scheduler
                System.out.println(name + ": HANDLING EVENT: " + event); // Prints a message saying that the drone subsystem will handel the event
                droneSubsystemState = DroneSubsystemState.RECEIVED_EVENT_FROM_SCHEDULER; // Makes the state as received the event from the scheduler
                break;

            case RECEIVED_EVENT_FROM_SCHEDULER:
                Drone handlingDrone = droneQueue.remove();
                handlingDrone.setAssignedEvent(event);
                System.out.println(handlingDrone.getName() + ": AVAILABLE TO HANDLE --> : " + event); // Prints that the drone that was found available to handle the event
                workingDrones.add(handlingDrone);
                droneSubsystemState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER; // Makes the state as sending the event to the scheduler
                break;

            case SENDING_EVENT_TO_SCHEDULER:

                sleep(1000);
                Drone drone = workingDrones.remove();
                InputEvent completedEvent = drone.getCompletedEvent();
                System.out.println(drone.getName() + ": COMPLETED EVENT (ARRIVED AT ZONE): " + completedEvent); // Prints out the time that the drone arrived at zone
                System.out.println(name + ": SENDING EVENT TO SCHEDULER --> " + completedEvent.toString()); // Sends the message back to the Scheduler
                droneQueue.add(drone);
                eventBuffer.addInputEvent(completedEvent, Systems.Scheduler); // Puts it the shared buffer with the scheduler
                droneSubsystemState = DroneSubsystemState.WAITING; // Makes the state as waiting again for the next event
                break;

        }
    }


    @Override
    public void run() {
        int i = 0;
        while (i < 10) {

            // Step 1: Read from the Event Buffer sent from scheduler
            InputEvent event = eventBuffer.getInputEvent(this.systemType);
            if (event != null){

                //Change drone subsystem state (WAITING -> RECEIVED_EVENT_FROM_SCHEDULER)
                try {
                    handleDroneSubsystemState(event); // Calls the function to handle the state change.
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                //Change drone subsystem state (RECEIVED_EVENT_FROM_SCHEDULER -> SENDING_EVENT_TO_SCHEDULER)
                try {
                    handleDroneSubsystemState(event);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                //Change drone subsystem state (RECEIVED_EVENT_FROM_SCHEDULER -> WAITING)
                try {
                    handleDroneSubsystemState(event);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            } else {
                System.out.println("[" + systemType + " - " + name + "] No event to handle, retrying...");
                i--; // Retry the same iteration
            }

            i++;
        }
    }
}