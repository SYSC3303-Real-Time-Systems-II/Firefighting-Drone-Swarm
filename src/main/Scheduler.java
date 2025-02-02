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
                    System.out.println(this.name + ": Received <-- " + receivedPackage.getRelayPackageID() + " FROM: " + Systems.FireIncidentSubsystem);
                    // Process the event and add it to the inputEvents queue
                    InputEvent receivedEvent = receivedPackage.getEvent();
                    receivedEvent.setZone(zones.get(receivedEvent.getZoneId())); // Set the zone for the event
                    this.inputEvents.add(receivedEvent);

                }
            }


            //Send the highest-priority event to the Drone
            if (!this.inputEvents.isEmpty()) {

                //Send highest-priority event to the Drone
                InputEvent sendingEvent = this.inputEvents.poll();
                System.out.println(this.name + ": SENDING --> " + sendingEvent.toString() + " TO: " + Systems.DroneSubsystem);
                this.eventBuffer.addInputEvent(sendingEvent, Systems.DroneSubsystem);

                //Check if there is any event back from the Drone
                InputEvent receivedEvent = eventBuffer.getInputEvent(this.systemType);
                if (receivedEvent != null) {
                    System.out.println(this.name + ": Received <-- " + receivedEvent.toString() + " FROM: " + Systems.DroneSubsystem);
                    // Create a confirmation package and place in confirmationPackages queue
                    RelayPackage sendingPackage = new RelayPackage("DRONE_CONFIRMATION", Systems.FireIncidentSubsystem, receivedEvent, null);
                    this.confirmationPackages.add(sendingPackage);
                }
            }

            //Send confirmation packages back to the FireIncidentSubsystem
            if (!this.confirmationPackages.isEmpty()) {
                RelayPackage sendingPackage = this.confirmationPackages.poll();
                System.out.println(this.name + ": SENDING CONFIRMATION FOR --> " + sendingPackage.getRelayPackageID() + " TO: " + sendingPackage.getReceiverSystem());
                relayBuffer.addReplayPackage(sendingPackage);
            }

            i++;
        }
    }
}