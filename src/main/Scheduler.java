import java.util.*;

public class Scheduler implements Runnable {

    private String name;
    private Systems systemType;
    private PriorityQueue<InputEvent> inputEvents;
    private Queue<InputEvent> relayMessageEvents;
    private Map<Integer, Zone> zones;

    public Scheduler(String name) {
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

    }

    public synchronized void addInputEvent(InputEvent event, Systems systemType, String name){
        System.out.println("[" +event.time +"]["+ systemType + " - " + name + "] Adding input event: " + event);
        event.setZone(zones.get(event.zone_id));
        this.inputEvents.add(event);
    }

    public synchronized InputEvent takeInputEvent(Systems systemType, String name){
        InputEvent event = this.inputEvents.poll();
        if (event != null) {
            System.out.println("[" +event.time +"]["+ systemType + " - " + name + "] Received event: " + event);
        } else {
            System.out.println("["+ systemType + " - " + name + "] No available input events.");
        }
        return event;
    }

    public synchronized void addRelayMessageEvents(InputEvent event, Systems systemType, String name){
        if (event != null){
            System.out.println("[" +event.time +"]["+ systemType + " - " + name + "] Adding relay message event: " + event);
            this.relayMessageEvents.add(event);
        } else {
            try {
                notifyAll();
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized InputEvent getRelayMessageEvent(Systems systemType, String name, boolean inputEventListEmpty){
        InputEvent event = this.relayMessageEvents.poll();
        if (event != null) {
            System.out.println("[" +event.time +"]["+ systemType + " - " + name + "] Received relayed event: " + event);
        } else {
            System.out.println("["+ systemType + " - " + name + "] No available relayed events.");
            if (inputEventListEmpty){
                try {
                    notifyAll();
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }
        return event;
    }

    public synchronized  void addZones(ArrayList<Zone> zonesList, Systems systemType, String name){
        for (Zone zone : zonesList) {
            this.zones.put(zone.getZoneID(), zone);
        }
        System.out.println("["+ systemType + " - " + name + "] Added zones: " + this.zones);
    }

    // Getter methods for testing
    public PriorityQueue<InputEvent> getInputEvents() {
        return inputEvents;
    }

    public Queue<InputEvent> getRelayMessageEvents() {
        return relayMessageEvents;
    }

    public Map<Integer, Zone> getZones() {
        return zones;
    }

    public void run() {

    }
}
