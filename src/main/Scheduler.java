import java.util.*;

/**
 * The Scheduler class is responsible for managing and prioritizing input events received from the FireIncidentSubsystem.
 * It sends the highest-priority events to the DroneSubsystem and handles confirmation messages back to the FireIncidentSubsystem.
 */
public class Scheduler implements Runnable {

    private String name;                                // Name of the scheduler
    private Systems systemType;                         // Type of the system (Scheduler)
    private PriorityQueue<InputEvent> inputEvents;      // Priority queue of input events, ordered by severity
    private Queue<RelayPackage> confirmationPackages;   // Queue of confirmation packages to send back to the FireIncidentSubsystem
    private Map<Integer, Zone> zones;                   // Map of zones, keyed by zone ID
    private RelayBuffer relayBuffer;                    // Buffer for communication with the FireIncidentSubsystem
    private EventBuffer eventBuffer;                    // Buffer for communication with the DroneSubsystem
    private SchedulerState schedulerState;              // The state of the scheduler for the state transition done by the state machine
    /**
     * Constructs a Scheduler object.
     *
     * @param name        The name of the scheduler.
     * @param relayBuffer The RelayBuffer used for communication with the FireIncidentSubsystem.
     * @param eventBuffer The EventBuffer used for communication with the DroneSubsystem.
     */
    public Scheduler(String name, RelayBuffer relayBuffer, EventBuffer eventBuffer) {
        // Comparator to prioritize events based on severity (High > Moderate > Low)
        Comparator<InputEvent> priorityComparator = Comparator.comparingInt(inputEvent -> {
            switch (inputEvent.getSeverity()) {
                case High: return 1; // Highest priority
                case Moderate: return 2;
                case Low: return 3; // Lowest priority
                default: throw new IllegalArgumentException("Unknown priority level");
            }
        });

        this.name = name;
        this.systemType = Systems.Scheduler;
        this.inputEvents = new PriorityQueue<>(priorityComparator);
        this.confirmationPackages = new LinkedList<>();
        this.zones = new HashMap<>();
        this.relayBuffer = relayBuffer;
        this.eventBuffer = eventBuffer;
        this.schedulerState = SchedulerState.WAITING;
    }

    /**
     * Adds zones to the scheduler's zone map.
     *
     * @param zonesList   The list of zones to add.
     * @param systemType  The system type sending the zones.
     * @param name        The name of the system sending the zones.
     */
    public synchronized void addZones(ArrayList<Zone> zonesList, Systems systemType, String name) {
        for (Zone zone : zonesList) {
            this.zones.put(zone.getZoneID(), zone);
        }
        System.out.println(this.name + ": Added zones: " + this.zones);
    }

    /**
     * Retrieves a map of all zones currently managed by the scheduler.
     *
     * @return A map of zone IDs to Zone objects.
     */
    public Map<Integer, Zone> getZones() {
        return zones;
    }

    /**
     * Deals with the transitioning of the states for the scheduler such as receiving and sending events to both the fire
     * incident subsystem and drone subsystem.
     * @param inputEvent the event that is both sent and received.
     * @param name the name of the input ID for the package.
     * @param relayPackage the name of the package being sent back to the FIS.
     */
    public void handleSchedulerState(InputEvent inputEvent, String name, RelayPackage relayPackage) {
        switch(schedulerState){
            case WAITING:
                System.out.println(this.name + ": RECEIVED AN EVENT <-- " + name + " FROM: " + Systems.FireIncidentSubsystem); // Prints out a message that the event was received
                // Process the event and add it to the inputEvents queue
                inputEvent.setZone(zones.get(inputEvent.getZoneId())); // Set the zone for the event
                this.inputEvents.add(inputEvent); // Adds the input events to the list of input events for the drone subsystem
                schedulerState = SchedulerState.RECEIVED_EVENT_FROM_FIS; // Makes the state as received an event from the FIS
                break;
            case RECEIVED_EVENT_FROM_FIS:
                System.out.println(this.name + ": SENDING THE EVENT --> " + inputEvent.toString() + " TO: " + Systems.DroneSubsystem);
                this.eventBuffer.addInputEvent(inputEvent, Systems.DroneSubsystem); // Sends the event to the drone sub-system
                schedulerState = SchedulerState.SENT_EVENT_TO_DRONE_SUBSYSTEM; // Makes the state as sent the event to teh drone subsystem
                break;
            case SENT_EVENT_TO_DRONE_SUBSYSTEM:
                System.out.println(this.name + ": RECEIVED EVENT <-- " + inputEvent.toString() + " FROM: " + Systems.DroneSubsystem);
                // Create a confirmation package and place in confirmationPackages queue
                RelayPackage sendingPackage = new RelayPackage("DRONE_CONFIRMATION", Systems.FireIncidentSubsystem, inputEvent, null);
                this.confirmationPackages.add(sendingPackage);
                schedulerState = SchedulerState.SEND_EVENT_TO_FIS; // Makes the state as getting ready to send the event back to the FIS
                break;
            case SEND_EVENT_TO_FIS:
                System.out.println(this.name + ": SENDING CONFIRMATION FOR --> " + relayPackage.getEvent().toString() + " TO: " + relayPackage.getReceiverSystem());
                relayBuffer.addReplayPackage(relayPackage);
                schedulerState = SchedulerState.WAITING; // Makes the state back to waiting
                break;
        }
    }

    /**
     * Gets the state of the scheduler.
     * @return the state of the scheduler.
     */
    public SchedulerState getSchedulerState() {
        return schedulerState;
    }

    /**
     * The run method is executed when the thread starts.
     * It reads events from the RelayBuffer, prioritizes them, and sends them to the DroneSubsystem.
     * It also handles confirmation messages from the DroneSubsystem and sends them back to the FireIncidentSubsystem.
     */
    @Override
    public void run() {
        int i = 0;

        // Loop to Read Input Events from FireIncidentSubsystem, sends Event to DroneSubsystem, and handles confirmation messages passing
        while (i < 10) {

            RelayPackage receivedPackage = relayBuffer.getRelayPackage(this.systemType);
            // Check for RelayPackage from FireIncidentSubsystem
            if (receivedPackage != null) {
                // Received Zone Package
                if (receivedPackage.getZone() != null) {
                    this.addZones(receivedPackage.getZone(), this.systemType, this.name);
                // Received Event Package
                } else {
                    handleSchedulerState(receivedPackage.getEvent(), receivedPackage.getRelayPackageID(), null); // Calls the function to handle the state event of receiving from the FIS
                }
            }

            //Send the highest-priority event to the Drone
            if (!this.inputEvents.isEmpty()) {

                //Send highest-priority event to the drone subsystem
                handleSchedulerState(this.inputEvents.poll(), null, null); // Calls the function to handle the state event of receiving from the FIS

                //Check if there is any event back from the Drone
                InputEvent receivedEvent = eventBuffer.getInputEvent(this.systemType);

                if (receivedEvent != null) {
                    handleSchedulerState(receivedEvent, null, null);
                }
            }

            //Send confirmation packages back to the FireIncidentSubsystem
            if (!this.confirmationPackages.isEmpty()) {
                handleSchedulerState(null, null, this.confirmationPackages.poll());
            }
            i++;
        }
    }


}