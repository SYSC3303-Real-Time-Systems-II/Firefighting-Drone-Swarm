import java.util.*;

public class Scheduler implements Runnable {

    private String name;
    private Systems systemType;
    private PriorityQueue<InputEvent> inputEvents;
    private Queue<RelayPackage> relayMessageEvents;
    private Map<Integer, Zone> zones;
    private RelayBuffer relayBuffer;

    public Scheduler(String name, RelayBuffer relayBuffer) {
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
        this.relayMessageEvents = new LinkedList<>();
        this.zones = new HashMap<>();
        this.relayBuffer = relayBuffer;

    }

    public synchronized void addInputEvent(InputEvent event, Systems systemType, String name){
        System.out.println("[" +event.getTime() +"]["+ systemType + " - " + name + "] Adding input event: " + event);
        event.setZone(zones.get(event.getZoneId()));
        this.inputEvents.add(event);
    }

    public synchronized InputEvent takeInputEvent(Systems systemType, String name){
        InputEvent event = this.inputEvents.poll();
        if (event != null) {
            System.out.println("[" +event.getTime() +"]["+ systemType + " - " + name + "] Received event: " + event);
        } else {
            System.out.println("["+ systemType + " - " + name + "] No available input events.");
        }
        return event;
    }

//    public synchronized void addRelayMessageEvents(InputEvent event, Systems systemType, String name){
//        if (event != null){
//            System.out.println("[" +event.getTime() +"]["+ systemType + " - " + name + "] Adding relay message event: " + event);
//            this.relayMessageEvents.add(event);
//        } else {
//            try {
//                notifyAll();
//                wait();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

//    public synchronized InputEvent getRelayMessageEvent(Systems systemType, String name, boolean inputEventListEmpty){
//        InputEvent event = this.relayMessageEvents.poll();
//        if (event != null) {
//            System.out.println("[" +event.getTime() +"]["+ systemType + " - " + name + "] Received relayed event: " + event);
//        } else {
//            System.out.println("["+ systemType + " - " + name + "] No available relayed events.");
//            if (inputEventListEmpty){
//                try {
//                    notifyAll();
//                    wait();
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                return null;
//            }
//        }
//        return event;
//    }

    public synchronized  void addZones(ArrayList<Zone> zonesList, Systems systemType, String name){
        for (Zone zone : zonesList) {
            this.zones.put(zone.getZoneID(), zone);
        }
        System.out.println("["+ systemType + " - " + name + "] Added zones: " + this.zones);
    }

    @Override
    public void run() {
        int i = 0;

        while (i < 10) {
            RelayPackage receivedPackage = relayBuffer.getRelayPackage(this.systemType);
            if (receivedPackage != null) {
                if (receivedPackage.getZone() != null) { // Zone Package
                    this.addZones(receivedPackage.getZone(), this.systemType, this.name);
                } else { // Event Package
                    System.out.println(this.name + ": Received <-- " + receivedPackage.getRelayPackageID() + " FROM: " + Systems.FireIncidentSubsystem);

                    // Process the event and send highest prio event to Drone
                    InputEvent receivedEvent = receivedPackage.getEvent();
                    this.inputEvents.add(receivedEvent);


//                    // Simulate "DRONE STUFF" (non-blocking for FIS)
//                    System.out.println(this.name + " DOING DRONE STUFF");
//                    try {
//                        Thread.sleep(2000); // Simulate time-consuming task
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        System.err.println("Scheduler interrupted during DRONE STUFF");
//                    }


                    InputEvent sendEvent = new InputEvent(receivedEvent.getTime().toString(), receivedEvent.getZoneId(), receivedEvent.getEvent_type().toString(), receivedEvent.getSeverity().toString(), Status.COMPLETE);
                    // Create a new RelayPackage to send back
                    RelayPackage sendingPackage = new RelayPackage(receivedPackage.getRelayPackageID(), Systems.FireIncidentSubsystem, sendEvent, null);
                    System.out.println(this.name + ": SENDING CONFIRMATION FOR --> " + sendingPackage.getRelayPackageID() + " TO: " + sendingPackage.getReceiverSystem());
                    relayBuffer.addReplayPackage(sendingPackage);
                }
            } else {
                System.out.println(this.name + ": No item for Scheduler, retrying...");
                i--; // Retry the same iteration
            }
            i++;
        }

    }
}
