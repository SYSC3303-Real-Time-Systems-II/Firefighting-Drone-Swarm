import javax.sound.midi.Soundbank;
import java.util.*;

import static java.lang.Thread.sleep;

public class DroneSubsystem implements Runnable {

    private String name;
    private Systems systemType;
    private EventBuffer eventBuffer;
    private DroneSubsystemState droneSubsystemState; // Will have the state of the drone subsystem which is receiving and sending to the scheduler

    private List<Drone> workingDrones;
    private Queue<Drone> droneQueue;
    private List<Drone> drones;


    public DroneSubsystem(String name, EventBuffer eventBuffer) {
        this.name = name;
        this.systemType = Systems.DroneSubsystem;
        this.eventBuffer = eventBuffer;
        this.droneSubsystemState = DroneSubsystemState.WAITING;

        this.droneQueue = new LinkedList<>();

        this.workingDrones = new ArrayList<>();
        this.drones = new ArrayList<>();
        // Initialize multiple drones and start their threads
        for (int i = 0; i < 2; i++) {
            Drone drone = new Drone();
            droneQueue.add(drone);
            drones.add(drone);
            new Thread(drone).start();
        }

    }

    private Drone chooseDroneAlgorithm(InputEvent event) {
        Coordinate evnetCoords = event.getZone().getZoneCenter();
        Drone closestDrone = null;
        double minDistance = Double.MAX_VALUE;

        for (Drone drone : drones) {
            if (drone.getDroneState() == DroneState.AVAILABLE) {
                Coordinate droneCoords = drone.getCurrent_coords();
                double distance = Math.sqrt(Math.pow(evnetCoords.getX() - droneCoords.getX(), 2) + Math.pow(evnetCoords.getY() - droneCoords.getY(), 2));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestDrone = drone;
                }
            }
        }
        return closestDrone;
    }

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

                System.out.println("DRONE CHOSEN --- " + chooseDroneAlgorithm(event).getName());
                Drone handlingDrone = droneQueue.remove();
                handlingDrone.setAssignedEvent(event);
                System.out.println(handlingDrone.getName() + ": AVAILABLE TO HANDLE --> : " + event); // Prints that the drone that was found available to handle the event
                workingDrones.add(handlingDrone);
                droneSubsystemState = DroneSubsystemState.SENDING_EVENT_TO_SCHEDULER; // Makes the state as sending the event to the scheduler
                break;

            case SENDING_EVENT_TO_SCHEDULER:
                sleep(1400);
                //have it check if the var completedEvent is not null.
                for (int i = workingDrones.size() - 1; i >= 0; i--) {
                    Drone workingDrone = workingDrones.get(i);
                    InputEvent completedEvent = workingDrone.getCompletedEvent();
                    if (completedEvent != null){
                        System.out.println(workingDrone.getName() + ": COMPLETED EVENT (ARRIVED AT ZONE): " + completedEvent); // Prints out the time that the drone arrived at zone
                        System.out.println(name + ": SENDING EVENT TO SCHEDULER --> " + completedEvent.toString()); // Sends the message back to the Scheduler
                        droneQueue.add(workingDrone);
                        workingDrones.remove(workingDrone);
                        eventBuffer.addInputEvent(completedEvent, Systems.Scheduler); // Puts it the shared buffer with the scheduler
                    }
                }
                // Makes the state as waiting again for the next event
                droneSubsystemState = DroneSubsystemState.WAITING;
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