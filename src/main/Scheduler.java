import java.util.*;

public class Scheduler implements Runnable {

    private String name;
    private Systems systemType;
    private PriorityQueue<InputEvent> inputEvents;
    private Queue<RelayPackage> confirmationPackages;
    private Map<Integer, Zone> zones;
    private RelayBuffer relayBuffer;
    private EventBuffer eventBuffer;

    public Scheduler(String name, RelayBuffer relayBuffer, EventBuffer eventBuffer) {
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

    public synchronized void addZones(ArrayList<Zone> zonesList, Systems systemType, String name) {
        for (Zone zone : zonesList) {
            this.zones.put(zone.getZoneID(), zone);
        }
        System.out.println("[" + systemType + " - " + name + "] Added zones: " + this.zones);
    }

    @Override
    public void run() {
        int i = 0;

        while (i < 10) {
            // Step 1: Read from the Relay Buffer
            RelayPackage receivedPackage = relayBuffer.getRelayPackage(this.systemType);
            if (receivedPackage != null) {
                if (receivedPackage.getZone() != null) { // Zone Package
                    this.addZones(receivedPackage.getZone(), this.systemType, this.name);
                } else { // Event Package
                    System.out.println(this.name + ": Received <-- " + receivedPackage.getRelayPackageID() + " FROM: " + Systems.FireIncidentSubsystem);
                    // Process the event and add it to the inputEvents queue
                    InputEvent receivedEvent = receivedPackage.getEvent();
                    receivedEvent.setZone(zones.get(receivedEvent.getZoneId())); // Set the zone for the event
                    this.inputEvents.add(receivedEvent);

                }
            }


            // Step 2: Send the highest-priority event to the Drone
            if (!this.inputEvents.isEmpty()) {
                InputEvent sendingEvent = this.inputEvents.poll();
                System.out.println(this.name + ": SENDING --> " + sendingEvent.toString() + " TO: " + Systems.DroneSubsystem);
                this.eventBuffer.addInputEvent(sendingEvent, Systems.DroneSubsystem);

                // Step 3: Check if there is any event back from the Drone
                InputEvent receivedEvent = eventBuffer.getInputEvent(this.systemType);
                if (receivedEvent != null) {
                    System.out.println(this.name + ": Received <-- " + receivedEvent.toString() + " FROM: " + Systems.DroneSubsystem);
                    // Create a confirmation package to send back to the FireIncidentSubsystem
                    RelayPackage sendingPackage = new RelayPackage("DRONE_CONFIRMATION", Systems.FireIncidentSubsystem, receivedEvent, null);
                    this.confirmationPackages.add(sendingPackage);
                } else {
                    System.out.println(this.name + ": No event from Drone, retrying...");
                }
            }


            // Step 4: Send confirmation packages back to the FireIncidentSubsystem
            if (!this.confirmationPackages.isEmpty()) {
                RelayPackage sendingPackage = this.confirmationPackages.poll();
                System.out.println(this.name + ": SENDING CONFIRMATION FOR --> " + sendingPackage.getRelayPackageID() + " TO: " + sendingPackage.getReceiverSystem());
                relayBuffer.addReplayPackage(sendingPackage);
            }

            i++;
        }
    }
}