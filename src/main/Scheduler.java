import java.util.*;

public class Scheduler implements Runnable {

    private String name;
    private Systems systemType;
    private PriorityQueue<InputEvent> inputEvents;
    private Queue<InputEvent> relayMessageEvents;

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

    }

    public synchronized void addInputEvent(InputEvent event, Systems systemType, String name){
        System.out.println("["+ systemType + " - " + name + "] Adding input event: " + event);
        this.inputEvents.add(event);
    }

    public synchronized InputEvent takeInputEvent(Systems systemType, String name){
        InputEvent event = this.inputEvents.poll();
        if (event != null) {
            System.out.println("["+ systemType + " - " + name + "] Received event: " + event);
        } else {
            System.out.println("["+ systemType + " - " + name + "] No available input events.");
        }
        return event;
    }

    public synchronized void addRelayMessageEvents(InputEvent event, Systems systemType, String name){
        System.out.println("["+ systemType + " - " + name + "] Adding relay message event: " + event);
        this.relayMessageEvents.add(event);
    }

    public synchronized InputEvent getRelayMessageEvent(Systems systemType, String name){
        InputEvent event = this.relayMessageEvents.poll();
        if (event != null) {
            System.out.println("["+ systemType + " - " + name + "] Received relayed event: " + event);
        } else {
            System.out.println("["+ systemType + " - " + name + "] No available relayed events.");
        }
        return event;
    }

    // Getter methods for testing
    public PriorityQueue<InputEvent> getInputEvents() {
        return inputEvents;
    }

    public Queue<InputEvent> getRelayMessageEvents() {
        return relayMessageEvents;
    }

    public void run() {

    }
}
